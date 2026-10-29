package com.example

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.util.ConnectivityObserver
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class that sets up a centralized, high-performance Coil ImageLoader
 * with dedicated in-memory and on-disk caching. Non-critical dependencies and background
 * asset preloading/decoding are handled lazily and asynchronously via Coroutines to keep
 * the main thread completely free during the splash screen transition.
 */
class AppApplication : Application(), ImageLoaderFactory {

    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Lazily load ImageLoader to avoid any overhead on the main thread during initial app creation
    private val lazyImageLoader: ImageLoader by lazy {
        buildImageLoader()
    }

    override fun onCreate() {
        super.onCreate()

        // Global crash barrier to catch and log any unhandled exceptions instead of abruptly closing the app
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AppApplication", "FATAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
        }

        // Multiprocess WebView data directory safety initialization (Android 9+)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val processName = getProcessName()
                if (packageName != processName) {
                    android.webkit.WebView.setDataDirectorySuffix(processName)
                }
            }
        } catch (e: Exception) {
            Log.e("AppApplication", "WebView setDataDirectorySuffix note: ${e.message}")
        }

        // 0. Ensure FirebaseApp is initialized synchronously before any component requests Firebase services
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("AppApplication", "FirebaseApp initialized synchronously on startup")
            }
        } catch (e: Exception) {
            Log.e("AppApplication", "Synchronous FirebaseApp initialization note: ${e.message}")
        }

        // Move non-critical dependency initialization and background asset decoding
        // completely off the main thread using Coroutines on Dispatchers.IO
        applicationScope.launch {
            // 1. Asynchronously verify Firebase Auth session in background thread
            try {
                if (FirebaseApp.getApps(this@AppApplication).isEmpty()) {
                    FirebaseApp.initializeApp(this@AppApplication)
                }
                val auth = com.example.util.FirebaseSyncManager.auth
                Log.d("AppApplication", "Firebase initialized. Current user: ${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("AppApplication", "FirebaseApp initialization exception: ${e.message}")
            }

            // 2. Lazily start connectivity observer on background thread
            try {
                ConnectivityObserver.startMonitoring(this@AppApplication)
                Log.d("AppApplication", "ConnectivityObserver started asynchronously on background thread")
            } catch (e: Exception) {
                Log.e("AppApplication", "ConnectivityObserver initialization error: ${e.message}")
            }

            // 3. Register singleton ImageLoader
            try {
                val loader = lazyImageLoader
                Coil.setImageLoader(loader)
                Log.d("AppApplication", "Coil ImageLoader initialized")
            } catch (e: Exception) {
                Log.e("AppApplication", "ImageLoader initialization error: ${e.message}")
            }
        }
    }

    override fun newImageLoader(): ImageLoader = lazyImageLoader

    private fun buildImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Perform all network fetching, decoding, and caching on background IO threads
            .dispatcher(Dispatchers.IO)
            .fetcherDispatcher(Dispatchers.IO)
            .decoderDispatcher(Dispatchers.IO)
            .transformationDispatcher(Dispatchers.Default)
            // Memory cache: custom memory cache size (25% of available heap) with strong references
            .memoryCache {
                val availableHeap = Runtime.getRuntime().maxMemory()
                val memoryCacheBytes = (availableHeap * 0.25).toInt()
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .maxSizeBytes(memoryCacheBytes)
                    .strongReferencesEnabled(true)
                    .build()
            }
            // Disk cache: up to 100 MB stored in internal app cache directory
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB disk cache limit
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Cache images aggressively even if server headers are absent
            .crossfade(true)
            .allowHardware(true)
            .build()
    }
}
