package com.example.repository

import android.content.Context
import android.util.Log
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.db.AppDatabase
import com.example.db.CulturalCarouselEntity
import com.example.model.CulturalCarouselItem
import com.example.util.FirebaseSyncManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Production-ready Repository managing the Andhra Pradesh Cultural Image Carousel.
 * Provides instant offline Room caching and real-time backend synchronization with Cloud Firestore.
 */
class CulturalCarouselRepository private constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val carouselDao = database.culturalCarouselDao()
    private val firestore by lazy { FirebaseSyncManager.firestore }

    private var firestoreListener: ListenerRegistration? = null

    private val _allItems = MutableStateFlow<List<CulturalCarouselItem>>(emptyList())
    val allItems: StateFlow<List<CulturalCarouselItem>> = _allItems.asStateFlow()

    private val _enabledItems = MutableStateFlow<List<CulturalCarouselItem>>(emptyList())
    val enabledItems: StateFlow<List<CulturalCarouselItem>> = _enabledItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // 1. Immediately load local items from Room database or seed defaults
        scope.launch {
            loadLocalItems()
        }

        // 2. Start real-time Firestore synchronization
        startFirestoreSync()
    }

    private suspend fun loadLocalItems() {
        val cached = carouselDao.getAllItemsDirect()
        if (cached.isNotEmpty()) {
            val domainList = cached.map { it.toDomainModel() }
            updateStateWithItems(domainList)
            preloadCarouselImages(domainList)
        } else {
            // Seed defaults
            val defaults = CulturalCarouselItem.getDefaultCulturalItems()
            val entities = defaults.map { CulturalCarouselEntity.fromDomainModel(it) }
            carouselDao.insertItems(entities)
            updateStateWithItems(defaults)
            preloadCarouselImages(defaults)
            // Attempt to seed Firestore if admin/authenticated
            seedFirestoreIfNeeded(defaults)
        }
    }

    private fun updateStateWithItems(items: List<CulturalCarouselItem>) {
        val sorted = items.sortedBy { it.orderIndex }
        _allItems.value = sorted
        val enabledOnly = sorted.filter { it.isEnabled }
        _enabledItems.value = if (enabledOnly.isNotEmpty()) enabledOnly else sorted
    }

    /**
     * Real-time sync with Cloud Firestore collection `cultural_carousel`
     */
    fun startFirestoreSync() {
        _isLoading.value = true
        firestoreListener?.remove()
        try {
            firestoreListener = firestore.collection(FIRESTORE_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.w(TAG, "Cultural carousel Firestore sync note: ${error.message}. Serving local verified Room cache.")
                        scope.launch {
                            val local = carouselDao.getAllItemsDirect()
                            if (local.isNotEmpty()) {
                                updateStateWithItems(local.map { it.toDomainModel() })
                            }
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val remoteItems = snapshot.documents.mapNotNull { doc ->
                            try {
                                val item = doc.toObject(CulturalCarouselItem::class.java)
                                item?.copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (remoteItems.isNotEmpty()) {
                            scope.launch {
                                val entities = remoteItems.map { CulturalCarouselEntity.fromDomainModel(it) }
                                carouselDao.clearAll()
                                carouselDao.insertItems(entities)
                                updateStateWithItems(remoteItems)
                                preloadCarouselImages(remoteItems)
                            }
                        }
                    } else if (snapshot != null && snapshot.isEmpty) {
                        // Remote is empty, seed with defaults if local is ready
                        scope.launch {
                            val local = carouselDao.getAllItemsDirect()
                            if (local.isEmpty()) {
                                val defaults = CulturalCarouselItem.getDefaultCulturalItems()
                                seedFirestoreIfNeeded(defaults)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.w(TAG, "Firestore listener setup soft error: ${e.message}")
        }
    }

    private fun seedFirestoreIfNeeded(defaults: List<CulturalCarouselItem>) {
        try {
            for (item in defaults) {
                firestore.collection(FIRESTORE_COLLECTION)
                    .document(item.id)
                    .set(item, SetOptions.merge())
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Seed firestore item deferred: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Seed firestore soft error: ${e.message}")
        }
    }

    /**
     * Admin: Add new cultural carousel item
     */
    suspend fun addItem(item: CulturalCarouselItem) = withContext(ioDispatcher) {
        val nextOrder = (_allItems.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
        val newItem = item.copy(orderIndex = nextOrder, updatedAt = System.currentTimeMillis())
        carouselDao.insertItem(CulturalCarouselEntity.fromDomainModel(newItem))
        val updated = _allItems.value + newItem
        updateStateWithItems(updated)
        syncItemToFirestore(newItem)
        preloadCarouselImages(listOf(newItem))
    }

    /**
     * Admin: Update existing cultural carousel item
     */
    suspend fun updateItem(item: CulturalCarouselItem) = withContext(ioDispatcher) {
        val updated = item.copy(updatedAt = System.currentTimeMillis())
        carouselDao.insertItem(CulturalCarouselEntity.fromDomainModel(updated))
        val currentList = _allItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        updateStateWithItems(currentList)
        syncItemToFirestore(updated)
        preloadCarouselImages(listOf(updated))
    }

    /**
     * Admin: Toggle enable/disable status
     */
    suspend fun toggleItemEnabled(id: String, isEnabled: Boolean) = withContext(ioDispatcher) {
        val item = _allItems.value.find { it.id == id } ?: return@withContext
        val updated = item.copy(isEnabled = isEnabled, updatedAt = System.currentTimeMillis())
        updateItem(updated)
    }

    /**
     * Admin: Delete carousel item
     */
    suspend fun deleteItem(id: String) = withContext(ioDispatcher) {
        carouselDao.deleteById(id)
        val filtered = _allItems.value.filter { it.id != id }
        updateStateWithItems(filtered)
        try {
            firestore.collection(FIRESTORE_COLLECTION).document(id).delete()
                .addOnFailureListener { e ->
                    Log.w(TAG, "Delete from firestore deferred: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore delete error: ${e.message}")
        }
    }

    /**
     * Admin: Reorder items (e.g. Move Up or Move Down)
     */
    suspend fun reorderItems(orderedList: List<CulturalCarouselItem>) = withContext(ioDispatcher) {
        val reindexed = orderedList.mapIndexed { idx, it ->
            it.copy(orderIndex = idx + 1, updatedAt = System.currentTimeMillis())
        }
        val entities = reindexed.map { CulturalCarouselEntity.fromDomainModel(it) }
        carouselDao.insertItems(entities)
        updateStateWithItems(reindexed)

        // Push new order indices to Firestore
        try {
            val batch = firestore.batch()
            for (item in reindexed) {
                val ref = firestore.collection(FIRESTORE_COLLECTION).document(item.id)
                batch.update(ref, "orderIndex", item.orderIndex)
                batch.update(ref, "updatedAt", item.updatedAt)
            }
            batch.commit().addOnFailureListener { e ->
                Log.w(TAG, "Batch reorder sync deferred: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Batch reorder exception: ${e.message}")
        }
    }

    /**
     * Admin: Reset to default cultural items
     */
    suspend fun resetToDefaults() = withContext(ioDispatcher) {
        val defaults = CulturalCarouselItem.getDefaultCulturalItems()
        carouselDao.clearAll()
        carouselDao.insertItems(defaults.map { CulturalCarouselEntity.fromDomainModel(it) })
        updateStateWithItems(defaults)
        preloadCarouselImages(defaults)
        seedFirestoreIfNeeded(defaults)
    }

    /**
     * Asynchronously preloads carousel image assets into Coil memory & disk cache
     */
    fun preloadCarouselImages(items: List<CulturalCarouselItem>) {
        try {
            val loader = Coil.imageLoader(context)
            items.forEach { item ->
                if (item.imageUrl.isNotBlank()) {
                    val request = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .allowHardware(true)
                        .crossfade(true)
                        .build()
                    loader.enqueue(request)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Preload carousel images soft error: ${e.message}")
        }
    }

    private fun syncItemToFirestore(item: CulturalCarouselItem) {
        try {
            firestore.collection(FIRESTORE_COLLECTION)
                .document(item.id)
                .set(item, SetOptions.merge())
                .addOnFailureListener { e ->
                    Log.w(TAG, "Item sync to Firestore deferred: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "syncItemToFirestore error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "CulturalCarouselRepo"
        const val FIRESTORE_COLLECTION = "cultural_carousel"

        @Volatile
        private var INSTANCE: CulturalCarouselRepository? = null

        fun getInstance(context: Context): CulturalCarouselRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = CulturalCarouselRepository(context.applicationContext, db)
                INSTANCE = instance
                instance
            }
        }
    }
}
