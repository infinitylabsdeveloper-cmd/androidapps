package com.example.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import com.example.db.AppDatabase
import com.example.db.BookmarkEntity
import com.example.db.OfflineContentPackEntity
import com.example.db.TestResultEntity
import com.example.db.UserProgressEntity
import com.example.util.AppConfigProvider
import com.example.util.FirebaseSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Enterprise WorkManager task responsible for:
 * 1. Periodic File Scanning: Auditing app-private storage, encrypted question vaults, cache buffers, and Room database health.
 * 2. Automated Multi-Tenant Local Backup: Exporting and rotating secure snapshots of user progress, bookmarks, and test history.
 * 3. Cloud Data Synchronization: Syncing backup metadata to Firebase Firestore when connectivity is verified.
 */
class DataSyncAndBackupWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DataSyncBackupWorker"
        const val UNIQUE_WORK_NAME = "periodic_file_scanning_and_backup_task"
        const val PREFS_NAME = "backup_sync_preferences"
        const val KEY_LAST_BACKUP_TIMESTAMP = "key_last_backup_timestamp"
        const val KEY_LAST_BACKUP_SIZE_BYTES = "key_last_backup_size_bytes"
        const val KEY_LAST_SCAN_FILE_COUNT = "key_last_scan_file_count"
        const val KEY_LAST_SCAN_STORAGE_BYTES = "key_last_scan_storage_bytes"
        const val KEY_LAST_STATUS = "key_last_status"
        const val KEY_LAST_FILE_NAME = "key_last_file_name"
        private const val MAX_BACKUP_FILES_TO_KEEP = 5

        data class BackupStatusInfo(
            val lastBackupTimestamp: Long,
            val lastBackupSizeBytes: Long,
            val scannedFileCount: Int,
            val scannedStorageBytes: Long,
            val lastStatus: String,
            val lastBackupFileName: String
        ) {
            val formattedDate: String
                get() = if (lastBackupTimestamp > 0) {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastBackupTimestamp))
                } else "Never"

            val formattedSize: String
                get() = when {
                    lastBackupSizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", lastBackupSizeBytes / (1024.0 * 1024.0))
                    lastBackupSizeBytes >= 1024 -> String.format(Locale.US, "%.1f KB", lastBackupSizeBytes / 1024.0)
                    else -> "$lastBackupSizeBytes B"
                }
        }

        /**
         * Reads the latest backup and file scan metrics from preferences.
         */
        fun getLastBackupStatus(context: Context): BackupStatusInfo {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return BackupStatusInfo(
                lastBackupTimestamp = prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L),
                lastBackupSizeBytes = prefs.getLong(KEY_LAST_BACKUP_SIZE_BYTES, 0L),
                scannedFileCount = prefs.getInt(KEY_LAST_SCAN_FILE_COUNT, 0),
                scannedStorageBytes = prefs.getLong(KEY_LAST_SCAN_STORAGE_BYTES, 0L),
                lastStatus = prefs.getString(KEY_LAST_STATUS, "IDLE") ?: "IDLE",
                lastBackupFileName = prefs.getString(KEY_LAST_FILE_NAME, "") ?: ""
            )
        }

        /**
         * Schedules the periodic file scanning and automated backup task (runs every 24 hours).
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DataSyncAndBackupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 2,
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .addTag("tag_periodic_file_scan_backup")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Periodic file scanning and backup task scheduled successfully (every 24h)")
        }

        /**
         * Triggers an immediate one-time file scan and backup execution.
         */
        fun runImmediate(context: Context): UUID {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<DataSyncAndBackupWorker>()
                .setConstraints(constraints)
                .addTag("tag_immediate_scan_backup")
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Immediate file scanning and backup task enqueued: ${oneTimeRequest.id}")
            return oneTimeRequest.id
        }

        /**
         * Cancels any scheduled background scanning and backup tasks.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Periodic file scanning and backup work cancelled")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Log.i(TAG, "Starting periodic file scanning and background backup task...")

        try {
            // =========================================================================
            // 1. FILE SCANNING & STORAGE AUDIT
            // =========================================================================
            var totalFilesCount = 0
            var totalStorageBytes = 0L

            fun scanDir(dir: File) {
                if (!dir.exists() || !dir.isDirectory) return
                val entries = dir.listFiles() ?: return
                for (file in entries) {
                    if (file.isDirectory) {
                        scanDir(file)
                    } else if (file.isFile) {
                        totalFilesCount++
                        totalStorageBytes += file.length()
                    }
                }
            }

            // Scan internal files dir (including .secure_mcq_vault and backups)
            scanDir(appContext.filesDir)

            // Scan Room database file
            val dbFile = appContext.getDatabasePath("app_database")
            val dbSizeBytes = if (dbFile.exists()) dbFile.length() else 0L

            // Scan image cache
            val imageCacheDir = File(appContext.cacheDir, "image_cache")
            val imageCacheSizeBytes = if (imageCacheDir.exists()) {
                imageCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } else 0L

            Log.d(TAG, "File Scan complete: $totalFilesCount files found, storage: $totalStorageBytes bytes, DB size: $dbSizeBytes bytes")

            // =========================================================================
            // 2. TENANT DATABASE SNAPSHOT & LOCAL BACKUP CREATION
            // =========================================================================
            val database = AppDatabase.getDatabase(appContext)
            val currentAppId = AppConfigProvider.appId

            // Collect local entities for active tenant
            val userProgress = database.userProgressDao().getUserProgressDirect(currentAppId)
            val bookmarksList = database.bookmarkDao().getAllBookmarks(currentAppId).firstOrNull() ?: emptyList()
            val testResultsList = database.testResultDao().getAllResultsDirect(currentAppId)
            val offlinePacksList = database.offlineContentPackDao().getAllPacksDirect(currentAppId)

            val backupRootJson = JSONObject().apply {
                put("formatVersion", 1)
                put("backupTimestamp", System.currentTimeMillis())
                put("appId", currentAppId)
                put("deviceModel", Build.MODEL)
                put("androidVersion", Build.VERSION.RELEASE)

                // Storage Scan Summary
                put("scanSummary", JSONObject().apply {
                    put("scannedFilesCount", totalFilesCount)
                    put("scannedStorageBytes", totalStorageBytes)
                    put("databaseSizeBytes", dbSizeBytes)
                    put("imageCacheSizeBytes", imageCacheSizeBytes)
                })

                // User Progress
                put("userProgress", JSONObject().apply {
                    put("streakDays", userProgress?.streakDays ?: 0)
                    put("totalXp", userProgress?.totalXp ?: 0)
                    put("dailyTargetQuestions", userProgress?.dailyTargetQuestions ?: 30)
                    put("completedQuestionsCount", userProgress?.completedQuestionsCount ?: 0)
                    put("testsCompletedCount", userProgress?.testsCompletedCount ?: 0)
                    put("lastActiveDate", userProgress?.lastActiveDate ?: "")
                })

                // Bookmarks
                val bookmarksArray = JSONArray()
                bookmarksList.take(500).forEach { bm ->
                    bookmarksArray.put(JSONObject().apply {
                        put("id", bm.id)
                        put("subjectId", bm.subjectId)
                        put("unitId", bm.unitId)
                        put("topicId", bm.topicId)
                        put("questionText", bm.questionText)
                        put("bookmarkedAt", bm.bookmarkedAt)
                    })
                }
                put("bookmarksCount", bookmarksList.size)
                put("bookmarks", bookmarksArray)

                // Test Results History
                val testsArray = JSONArray()
                testResultsList.take(100).forEach { tr ->
                    testsArray.put(JSONObject().apply {
                        put("id", tr.id)
                        put("testTitle", tr.testTitle)
                        put("testType", tr.testType)
                        put("scorePercentage", tr.scorePercentage)
                        put("totalQuestions", tr.totalQuestions)
                        put("correctAnswers", tr.correctAnswers)
                        put("wrongAnswers", tr.wrongAnswers)
                        put("timeTakenSeconds", tr.timeTakenSeconds)
                        put("timestamp", tr.timestamp)
                    })
                }
                put("testResultsCount", testResultsList.size)
                put("testResults", testsArray)

                // Offline Packs Summary
                val packsArray = JSONArray()
                offlinePacksList.forEach { pack ->
                    packsArray.put(JSONObject().apply {
                        put("id", pack.id)
                        put("title", pack.title)
                        put("questionCount", pack.questionCount)
                        put("sizeBytes", pack.sizeBytes)
                        put("syncStatus", pack.syncStatus)
                        put("downloadedAt", pack.downloadedAt)
                    })
                }
                put("offlinePacksCount", offlinePacksList.size)
                put("offlinePacks", packsArray)
            }

            // Write backup to dedicated local backups directory
            val backupsDir = File(appContext.filesDir, "backups").apply {
                if (!exists()) mkdirs()
            }

            val backupFileName = "backup_${currentAppId}_${System.currentTimeMillis()}.json"
            val targetBackupFile = File(backupsDir, backupFileName)
            FileOutputStream(targetBackupFile).use { fos ->
                fos.write(backupRootJson.toString(2).toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            val writtenBytes = targetBackupFile.length()
            Log.i(TAG, "Local backup snapshot written successfully: $backupFileName ($writtenBytes bytes)")

            // =========================================================================
            // 3. BACKUP ROTATION & CLEANUP (Keep latest MAX_BACKUP_FILES_TO_KEEP)
            // =========================================================================
            val existingBackups = backupsDir.listFiles { f -> f.isFile && f.name.startsWith("backup_") && f.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()

            if (existingBackups.size > MAX_BACKUP_FILES_TO_KEEP) {
                val toDelete = existingBackups.drop(MAX_BACKUP_FILES_TO_KEEP)
                for (oldFile in toDelete) {
                    val deleted = oldFile.delete()
                    Log.d(TAG, "Pruned older backup snapshot: ${oldFile.name} (success=$deleted)")
                }
            }

            // =========================================================================
            // 4. CLOUD SYNCHRONIZATION WITH FIRESTORE (If user authenticated)
            // =========================================================================
            var cloudSyncStatus = "LOCAL_ONLY"
            try {
                val currentUser = FirebaseSyncManager.auth.currentUser
                if (currentUser != null && !currentUser.isAnonymous) {
                    val userEmail = currentUser.email ?: currentUser.uid
                    val syncDocRef = FirebaseSyncManager.firestore
                        .collection("users")
                        .document(userEmail)
                        .collection("tenant_backups")
                        .document(currentAppId)

                    val syncPayload = hashMapOf<String, Any>(
                        "appId" to currentAppId,
                        "backupTimestamp" to System.currentTimeMillis(),
                        "backupSizeBytes" to writtenBytes,
                        "bookmarksCount" to bookmarksList.size,
                        "testsCompletedCount" to testResultsList.size,
                        "streakDays" to (userProgress?.streakDays ?: 0),
                        "totalXp" to (userProgress?.totalXp ?: 0),
                        "deviceModel" to Build.MODEL,
                        "lastSyncStatus" to "SUCCESS",
                        "lastUpdated" to System.currentTimeMillis()
                    )

                    syncDocRef.set(syncPayload)
                    cloudSyncStatus = "SYNCED_TO_CLOUD"
                    Log.d(TAG, "Backup sync document updated in Firestore for $userEmail / $currentAppId")
                }
            } catch (cloudEx: Exception) {
                Log.w(TAG, "Cloud sync notice (offline or skipped): ${cloudEx.message}")
            }

            // =========================================================================
            // 5. UPDATE PERSISTENT PREFERENCES & METRICS
            // =========================================================================
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putLong(KEY_LAST_BACKUP_TIMESTAMP, System.currentTimeMillis())
                putLong(KEY_LAST_BACKUP_SIZE_BYTES, writtenBytes)
                putInt(KEY_LAST_SCAN_FILE_COUNT, totalFilesCount)
                putLong(KEY_LAST_SCAN_STORAGE_BYTES, totalStorageBytes)
                putString(KEY_LAST_STATUS, "SUCCESS ($cloudSyncStatus)")
                putString(KEY_LAST_FILE_NAME, backupFileName)
                apply()
            }

            val elapsedMs = System.currentTimeMillis() - startTime
            Log.i(TAG, "DataSyncAndBackupWorker finished successfully in ${elapsedMs}ms")

            val outputData = workDataOf(
                "backup_file" to backupFileName,
                "backup_size" to writtenBytes,
                "file_count" to totalFilesCount,
                "cloud_sync" to cloudSyncStatus
            )
            Result.success(outputData)
        } catch (t: Throwable) {
            Log.e(TAG, "Error executing DataSyncAndBackupWorker: ${t.message}", t)
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putString(KEY_LAST_STATUS, "FAILED: ${t.message}")
                apply()
            }
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(workDataOf("error" to (t.message ?: "Unknown error")))
            }
        }
    }
}
