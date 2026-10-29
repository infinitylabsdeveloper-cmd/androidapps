package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Enterprise WorkManager task responsible for:
 * 1. Periodically monitoring the app's source directory (internal files, offline question vaults, study content).
 * 2. Fingerprinting existing files by relative path, size, and last modified timestamp.
 * 3. Detecting newly added or modified source files.
 * 4. Automatically triggering the secure multi-tenant backup routine (DataSyncAndBackupWorker)
 *    whenever changes or new files are detected.
 */
class FileChangeWatcherWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "FileChangeWatcher"
        const val UNIQUE_WORK_NAME = "periodic_file_change_watcher_task"
        const val PREFS_NAME = "file_watcher_preferences"

        const val KEY_LAST_CHECK_TIMESTAMP = "key_last_check_timestamp"
        const val KEY_LAST_TRIGGER_TIMESTAMP = "key_last_trigger_timestamp"
        const val KEY_TOTAL_WATCHED_FILES = "key_total_watched_files"
        const val KEY_NEW_FILES_COUNT = "key_new_files_count"
        const val KEY_MODIFIED_FILES_COUNT = "key_modified_files_count"
        const val KEY_LAST_STATUS = "key_last_status"
        private const val KEY_STORED_FILE_MANIFEST = "key_stored_file_manifest"

        data class WatcherStatusInfo(
            val lastCheckTimestamp: Long,
            val lastTriggerTimestamp: Long,
            val totalWatchedFiles: Int,
            val lastNewFilesCount: Int,
            val lastModifiedFilesCount: Int,
            val lastStatus: String
        ) {
            val formattedCheckDate: String
                get() = if (lastCheckTimestamp > 0) {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastCheckTimestamp))
                } else "Never"

            val formattedTriggerDate: String
                get() = if (lastTriggerTimestamp > 0) {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastTriggerTimestamp))
                } else "None (Awaiting new files)"
        }

        fun getWatcherStatus(context: Context): WatcherStatusInfo {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return WatcherStatusInfo(
                lastCheckTimestamp = prefs.getLong(KEY_LAST_CHECK_TIMESTAMP, 0L),
                lastTriggerTimestamp = prefs.getLong(KEY_LAST_TRIGGER_TIMESTAMP, 0L),
                totalWatchedFiles = prefs.getInt(KEY_TOTAL_WATCHED_FILES, 0),
                lastNewFilesCount = prefs.getInt(KEY_NEW_FILES_COUNT, 0),
                lastModifiedFilesCount = prefs.getInt(KEY_MODIFIED_FILES_COUNT, 0),
                lastStatus = prefs.getString(KEY_LAST_STATUS, "IDLE") ?: "IDLE"
            )
        }

        /**
         * Schedules periodic monitoring of source directory for file additions or changes.
         * Runs every 6 hours with storage and battery constraints.
         */
        fun schedulePeriodic(context: Context, intervalHours: Long = 6) {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<FileChangeWatcherWorker>(
                repeatInterval = intervalHours,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 30,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .addTag("tag_file_change_watcher")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Periodic source directory file change watcher scheduled (every ${intervalHours}h)")
        }

        /**
         * Triggers an immediate one-time scan of the source directory.
         */
        fun runImmediate(context: Context): UUID {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<FileChangeWatcherWorker>()
                .setConstraints(constraints)
                .addTag("tag_immediate_file_watcher")
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Immediate source directory file watcher task enqueued: ${oneTimeRequest.id}")
            return oneTimeRequest.id
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "File change watcher cancelled")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val checkTime = System.currentTimeMillis()
        Log.i(TAG, "Running periodic source directory file change check...")

        try {
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val storedManifestJson = prefs.getString(KEY_STORED_FILE_MANIFEST, null)
            val previousManifest = mutableMapOf<String, String>()

            if (!storedManifestJson.isNullOrBlank()) {
                try {
                    val jsonObj = JSONObject(storedManifestJson)
                    val keys = jsonObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        previousManifest[key] = jsonObj.optString(key)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing previous file manifest: ${e.message}")
                }
            }

            // 1. Scan the source directories
            // We monitor appContext.filesDir (including .secure_mcq_vault, downloads, study content)
            // but explicitly ignore the "backups" directory to avoid trigger feedback loops!
            val currentManifest = mutableMapOf<String, String>()
            val baseDir = appContext.filesDir

            fun scanDirectory(dir: File, relativePath: String = "") {
                if (!dir.exists() || !dir.isDirectory) return
                val entries = dir.listFiles() ?: return
                for (file in entries) {
                    val itemRelativePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                    // Ignore backups folder to prevent self-triggering
                    if (file.isDirectory) {
                        if (file.name.equals("backups", ignoreCase = true)) {
                            continue
                        }
                        scanDirectory(file, itemRelativePath)
                    } else if (file.isFile) {
                        // Fingerprint: size in bytes + last modified timestamp
                        val fingerprint = "${file.length()}_${file.lastModified()}"
                        currentManifest[itemRelativePath] = fingerprint
                    }
                }
            }

            // Scan internal filesDir
            scanDirectory(baseDir)

            // Also check Room database file for data changes
            val dbFile = appContext.getDatabasePath("app_database")
            if (dbFile.exists() && dbFile.isFile) {
                currentManifest["database/app_database"] = "${dbFile.length()}_${dbFile.lastModified()}"
            }

            // 2. Diff detection against previous manifest
            val newFiles = mutableListOf<String>()
            val modifiedFiles = mutableListOf<String>()

            for ((path, fingerprint) in currentManifest) {
                if (!previousManifest.containsKey(path)) {
                    newFiles.add(path)
                } else if (previousManifest[path] != fingerprint) {
                    modifiedFiles.add(path)
                }
            }

            val hasChanges = newFiles.isNotEmpty() || modifiedFiles.isNotEmpty()
            val isFirstBaseline = previousManifest.isEmpty()

            Log.i(TAG, "Source scan complete. Watched files: ${currentManifest.size}. New: ${newFiles.size}, Modified: ${modifiedFiles.size}. FirstBaseline=$isFirstBaseline")

            // 3. Serialize and save current manifest
            val updatedManifestObj = JSONObject()
            for ((k, v) in currentManifest) {
                updatedManifestObj.put(k, v)
            }

            var backupTriggered = false
            val statusMessage: String

            if (hasChanges && !isFirstBaseline) {
                // New or modified source files detected! Trigger the backup routine
                statusMessage = "CHANGES: +${newFiles.size} new, ~${modifiedFiles.size} mod. Backup triggered."
                Log.i(TAG, statusMessage)

                DataSyncAndBackupWorker.runImmediate(appContext)
                backupTriggered = true

                prefs.edit().apply {
                    putLong(KEY_LAST_CHECK_TIMESTAMP, checkTime)
                    putLong(KEY_LAST_TRIGGER_TIMESTAMP, checkTime)
                    putInt(KEY_TOTAL_WATCHED_FILES, currentManifest.size)
                    putInt(KEY_NEW_FILES_COUNT, newFiles.size)
                    putInt(KEY_MODIFIED_FILES_COUNT, modifiedFiles.size)
                    putString(KEY_LAST_STATUS, statusMessage)
                    putString(KEY_STORED_FILE_MANIFEST, updatedManifestObj.toString())
                    apply()
                }
            } else {
                statusMessage = if (isFirstBaseline) {
                    "BASELINE INDEXED (${currentManifest.size} files)"
                } else {
                    "NO CHANGES (${currentManifest.size} files identical)"
                }

                prefs.edit().apply {
                    putLong(KEY_LAST_CHECK_TIMESTAMP, checkTime)
                    putInt(KEY_TOTAL_WATCHED_FILES, currentManifest.size)
                    putInt(KEY_NEW_FILES_COUNT, 0)
                    putInt(KEY_MODIFIED_FILES_COUNT, 0)
                    putString(KEY_LAST_STATUS, statusMessage)
                    putString(KEY_STORED_FILE_MANIFEST, updatedManifestObj.toString())
                    apply()
                }
            }

            val outputData = workDataOf(
                "watched_files" to currentManifest.size,
                "new_files" to newFiles.size,
                "modified_files" to modifiedFiles.size,
                "backup_triggered" to backupTriggered
            )
            Result.success(outputData)
        } catch (t: Throwable) {
            Log.e(TAG, "Error in FileChangeWatcherWorker: ${t.message}", t)
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putString(KEY_LAST_STATUS, "ERROR: ${t.message}")
                apply()
            }
            if (runAttemptCount < 3) Result.retry() else Result.failure(workDataOf("error" to (t.message ?: "Unknown error")))
        }
    }
}
