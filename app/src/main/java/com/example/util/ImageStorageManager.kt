package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.Coil
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

/**
 * ImageStorageManager handles persistent storage and caching for user-picked and downloaded images
 * for Subjects, Units, Topics, and Posters.
 *
 * Ensures that temporary content:// URIs from Android's Photo Picker are permanently copied into
 * the app's sandboxed storage so they never expire or lose read permissions across app restarts,
 * and primes Coil's memory and disk cache for instant UI rendering.
 */
object ImageStorageManager {

    private const val TAG = "ImageStorageManager"
    private const val MEDIA_SUBDIR = "exam_media"

    /**
     * Persists a picked image Uri into the app's internal files storage.
     * Returns a stable file:// URI string that Coil can cache and render indefinitely.
     */
    suspend fun persistPickedImage(
        context: Context,
        sourceUri: Uri,
        entityType: String = "item"
    ): String = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, MEDIA_SUBDIR).apply {
                if (!exists()) mkdirs()
            }

            val sanitizedType = entityType.lowercase().replace("\\s+".toRegex(), "_")
            val targetFile = File(mediaDir, "${sanitizedType}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val fileUri = Uri.fromFile(targetFile).toString()
            Log.d(TAG, "Successfully persisted image to $fileUri (${targetFile.length()} bytes)")

            // Prime Coil cache immediately
            preloadIntoCoil(context, fileUri)

            fileUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist image URI: $sourceUri", e)
            sourceUri.toString()
        }
    }

    /**
     * Preloads an image into Coil's memory and disk cache.
     */
    suspend fun preloadIntoCoil(context: Context, imageUrl: String) = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) return@withContext
        try {
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
            imageLoader.enqueue(request)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preload image: $imageUrl", e)
        }
    }

    /**
     * Returns total disk usage of cached images and saved media in human-readable format.
     */
    fun getStorageStats(context: Context): StorageStats {
        val coilCacheDir = File(context.cacheDir, "image_cache")
        val mediaDir = File(context.filesDir, MEDIA_SUBDIR)

        val coilSize = getFolderSize(coilCacheDir)
        val mediaSize = getFolderSize(mediaDir)
        val fileCount = (mediaDir.listFiles()?.size ?: 0)

        return StorageStats(
            coilCacheFormatted = formatBytes(coilSize),
            savedMediaFormatted = formatBytes(mediaSize),
            totalFormatted = formatBytes(coilSize + mediaSize),
            customImagesCount = fileCount
        )
    }

    /**
     * Clears Coil's disk cache.
     */
    suspend fun clearCoilCache(context: Context) = withContext(Dispatchers.IO) {
        try {
            val coilCacheDir = File(context.cacheDir, "image_cache")
            if (coilCacheDir.exists()) {
                coilCacheDir.deleteRecursively()
                coilCacheDir.mkdirs()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing Coil cache", e)
        }
    }

    private fun getFolderSize(folder: File): Long {
        if (!folder.exists()) return 0L
        var length: Long = 0
        folder.listFiles()?.forEach { file ->
            length += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return length
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[index]
    }

    data class StorageStats(
        val coilCacheFormatted: String,
        val savedMediaFormatted: String,
        val totalFormatted: String,
        val customImagesCount: Int
    )
}
