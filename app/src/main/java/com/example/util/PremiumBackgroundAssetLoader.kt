package com.example.util

import android.content.Context
import android.util.Log
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles asynchronous preloading and caching of premium background assets,
 * ensuring all heavy network fetching and bitmap decoding occur entirely off
 * the UI thread to guarantee 60/120 FPS buttery smooth launch performance.
 */
object PremiumBackgroundAssetLoader {

    private const val TAG = "PremiumAssetLoader"

    // Default premium background assets across Login, OTP, and App Themes
    val DEFAULT_PREMIUM_BACKGROUND_URLS = listOf(
        // Andhra Pradesh landmarks & heritage for Login/OTP Carousel
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200&auto=format&fit=crop", // Prakasam Barrage
        "https://images.unsplash.com/photo-1627483262769-04d0a1401487?q=80&w=1200&auto=format&fit=crop", // Araku Valley
        "https://images.unsplash.com/photo-1599707367072-cd6ada2bc375?q=80&w=1200&auto=format&fit=crop", // Lepakshi Temple
        "https://images.unsplash.com/photo-1610123598147-f632aa18b875?q=80&w=1200&auto=format&fit=crop", // Tirumala Hills
        // Administrative Hub Banner
        "https://images.unsplash.com/photo-1541872703-74c5e44368f9?q=80&w=1200&auto=format&fit=crop",
        // Fallback Wallpapers & High-res Backgrounds
        "https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?w=800",
        "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800",
        "https://images.unsplash.com/photo-1627483262769-04d0a1401487?q=80&w=1400&auto=format&fit=crop"
    )

    private val preloaderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Preloads and decodes premium background assets asynchronously on Dispatchers.IO.
     * Moving bitmap decoding to background coroutines ensures the main thread remains
     * completely free during the splash screen transition.
     */
    fun preloadAssetsAsync(context: Context, additionalUrls: List<String> = emptyList()) {
        preloaderScope.launch {
            try {
                val loader = Coil.imageLoader(context)
                val allUrls = (DEFAULT_PREMIUM_BACKGROUND_URLS + additionalUrls).distinct()
                allUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .dispatcher(Dispatchers.IO)
                            .fetcherDispatcher(Dispatchers.IO)
                            .decoderDispatcher(Dispatchers.IO)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .allowHardware(true)
                            .crossfade(true)
                            .build()
                        // execute() decodes the bitmap directly on the background IO thread
                        loader.execute(request)
                    }
                }
                Log.d(TAG, "Asynchronously preloaded and decoded ${allUrls.size} premium background assets.")
            } catch (t: Throwable) {
                Log.w(TAG, "Async preload non-fatal warning: ${t.message}")
            }
        }
    }
}
