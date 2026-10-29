package com.example.repository

import com.example.model.FirestoreApp
import com.example.model.FirestoreAppConfiguration
import com.example.util.FirebaseSyncManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository layer for managing App and AppConfiguration CRUD operations in Firestore.
 */
class FirestoreAppRepository(
    private val firestore: FirebaseFirestore = FirebaseSyncManager.firestore
) {
    private val appsCollection = firestore.collection("apps")
    private val configsCollection = firestore.collection("app_configurations")

    // --- App CRUD Operations ---

    /**
     * Creates or overwrites an App entry.
     */
    suspend fun saveApp(app: FirestoreApp): Result<Unit> = runCatching {
        appsCollection.document(app.id).set(app).await()
    }

    /**
     * Retrieves a single App by its ID.
     */
    suspend fun getApp(appId: String): Result<FirestoreApp?> = runCatching {
        val snapshot = appsCollection.document(appId).get().await()
        snapshot.toObject(FirestoreApp::class.java)
    }

    /**
     * Deletes an App and its associated configuration.
     */
    suspend fun deleteApp(appId: String): Result<Unit> = runCatching {
        firestore.runBatch { batch ->
            batch.delete(appsCollection.document(appId))
            batch.delete(configsCollection.document(appId))
        }.await()
    }

    /**
     * Observes all apps in real-time, sorted by name.
     */
    fun observeAllApps(): Flow<List<FirestoreApp>> = callbackFlow {
        val subscription = appsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val apps = snapshot.toObjects(FirestoreApp::class.java)
                    trySend(apps)
                }
            }
        awaitClose { subscription.remove() }
    }

    // --- App Configuration CRUD Operations ---

    /**
     * Saves or updates the configuration for a specific App.
     * Uses appId as the document ID for 1:1 mapping.
     */
    suspend fun saveAppConfig(config: FirestoreAppConfiguration): Result<Unit> = runCatching {
        configsCollection.document(config.appId).set(config).await()
    }

    /**
     * Retrieves the configuration for a specific App.
     */
    suspend fun getAppConfig(appId: String): Result<FirestoreAppConfiguration?> = runCatching {
        val snapshot = configsCollection.document(appId).get().await()
        snapshot.toObject(FirestoreAppConfiguration::class.java)
    }

    /**
     * Observes a specific App's configuration in real-time.
     */
    fun observeAppConfig(appId: String): Flow<FirestoreAppConfiguration?> = callbackFlow {
        val subscription = configsCollection.document(appId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObject(FirestoreAppConfiguration::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Updates specific fields in settings metadata without overwriting the entire App document.
     */
    suspend fun updateSettingsMetadata(appId: String, metadata: Map<String, Any>): Result<Unit> = runCatching {
        appsCollection.document(appId).update("settingsMetadata", metadata).await()
    }
}
