package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import coil.Coil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enterprise WorkManager task that periodically purges temporary cache files,
 * stale HTTP response caches, orphaned download files, and old image buffers
 * to keep the application memory footprint minimal and improve background performance.
 */
class CacheCleanupWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CacheCleanupWorker"
        const val UNIQUE_WORK_NAME = "periodic_cache_and_buffer_cleanup"
        private const val MAX_TEMP_FILE_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours

        /**
         * Enqueues or updates the periodic background cache cleanup work request.
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 2,
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .addTag("tag_cache_cleanup")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Periodic cache cleanup task scheduled successfully (every 24 hours)")
        }

        /**
         * Triggers an immediate one-time cache and buffer cleanup.
         */
        fun runImmediate(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<CacheCleanupWorker>()
                .addTag("tag_immediate_cache_cleanup")
                .build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Immediate cache cleanup task enqueued")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting periodic cache and image buffer cleanup...")

            var bytesFreed = 0L
            var filesDeleted = 0

            // 1. Clear Coil in-memory cache to release stale image buffers and bitmaps
            try {
                val imageLoader = Coil.imageLoader(appContext)
                imageLoader.memoryCache?.clear()
                Log.d(TAG, "Coil memory cache cleared successfully.")
            } catch (t: Throwable) {
                Log.w(TAG, "Error clearing Coil memory cache: ${t.message}")
            }

            // 2. Prune old temporary files in appContext.cacheDir
            try {
                val cacheDir = appContext.cacheDir
                if (cacheDir != null && cacheDir.exists()) {
                    val now = System.currentTimeMillis()
                    cacheDir.listFiles()?.forEach { file ->
                        // Prune temporary files, log files, or old files outside the active image cache
                        if (file.isDirectory && file.name != "image_cache") {
                            val (deleted, freed) = deleteOldFilesRecursively(file, now)
                            filesDeleted += deleted
                            bytesFreed += freed
                        } else if (file.isFile) {
                            if (file.name.endsWith(".tmp") ||
                                file.name.startsWith("temp_") ||
                                file.name.startsWith("profile_pic_") ||
                                (now - file.lastModified() > MAX_TEMP_FILE_AGE_MS)
                            ) {
                                val size = file.length()
                                if (file.delete()) {
                                    bytesFreed += size
                                    filesDeleted++
                                }
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Error cleaning cache directory: ${t.message}")
            }

            // 3. Prune old/stale entries in image_cache if total size exceeds limit (e.g. 80MB)
            try {
                val imageCacheDir = appContext.cacheDir?.resolve("image_cache")
                if (imageCacheDir != null && imageCacheDir.exists()) {
                    val maxAllowedSizeBytes = 80L * 1024 * 1024 // 80 MB threshold for routine background prune
                    val files = imageCacheDir.listFiles()?.sortedBy { it.lastModified() }
                    var totalSize = files?.sumOf { it.length() } ?: 0L

                    if (totalSize > maxAllowedSizeBytes && files != null) {
                        for (file in files) {
                            if (totalSize <= maxAllowedSizeBytes) break
                            val length = file.length()
                            if (file.delete()) {
                                totalSize -= length
                                bytesFreed += length
                                filesDeleted++
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Error pruning disk image cache: ${t.message}")
            }

            // 4. Clean external cache directory if accessible
            try {
                val externalCache = appContext.externalCacheDir
                if (externalCache != null && externalCache.exists()) {
                    val now = System.currentTimeMillis()
                    val (deleted, freed) = deleteOldFilesRecursively(externalCache, now)
                    filesDeleted += deleted
                    bytesFreed += freed
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Error cleaning external cache: ${t.message}")
            }

            // 5. Suggest GC execution to reclaim native buffer references when backgrounded
            System.gc()

            Log.i(TAG, "Cache cleanup completed. Freed ${bytesFreed / 1024} KB across $filesDeleted files.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cache cleanup failed: ${e.message}", e)
            Result.retry()
        }
    }

    private fun deleteOldFilesRecursively(dir: File, now: Long): Pair<Int, Long> {
        var count = 0
        var freed = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val (nestedCount, nestedFreed) = deleteOldFilesRecursively(file, now)
                count += nestedCount
                freed += nestedFreed
                // Delete directory if now empty
                if (file.listFiles()?.isEmpty() == true) {
                    file.delete()
                }
            } else {
                if (file.name.endsWith(".tmp") ||
                    file.name.startsWith("temp_") ||
                    file.name.startsWith("profile_pic_") ||
                    (now - file.lastModified() > MAX_TEMP_FILE_AGE_MS)
                ) {
                    val size = file.length()
                    if (file.delete()) {
                        count++
                        freed += size
                    }
                }
            }
        }
        return Pair(count, freed)
    }
}
