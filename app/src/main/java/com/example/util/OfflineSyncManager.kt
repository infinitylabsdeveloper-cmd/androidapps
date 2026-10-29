package com.example.util

import android.content.Context
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.db.QuestionEntity
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hierarchy Type for Downloadable Content
 */
enum class DownloadHierarchyType {
    SUBJECT,
    UNIT,
    TOPIC,
    SET,
    CUSTOM_PACK
}

/**
 * Sync status reflecting cloud and device state
 */
enum class SyncStatus(val label: String, val iconSymbol: String) {
    SYNCED("Synced", "☁"),
    DOWNLOADING("Downloading", "↓"),
    DOWNLOADED("Downloaded", "✓"),
    SYNCING("Syncing", "⟳"),
    SYNC_REQUIRED("Sync Required", "⚠"),
    NOT_DOWNLOADED("Not Downloaded", "↓")
}

/**
 * Offline Download Pack Model
 */
data class OfflinePack(
    val id: String,
    val title: String,
    val hierarchyType: DownloadHierarchyType,
    val appId: String = "appsc-group2",
    val subjectId: String = "",
    val unitId: String = "",
    val topicId: String = "",
    val setName: String = "",
    val questionCount: Int,
    val sizeBytes: Long,
    val syncStatus: SyncStatus = SyncStatus.DOWNLOADED,
    val lastSyncedTimeText: String = "Synced 2 hrs ago",
    val downloadedAt: Long = System.currentTimeMillis()
)

object OfflineSyncManager {

    val downloadedPacks = mutableStateListOf<OfflinePack>()
    val syncStatusState = mutableStateOf(SyncStatus.SYNCED)
    val isOfflineModeSimulated = mutableStateOf(false)
    val pendingSyncCountState = mutableStateOf(0)
    val lastSyncedTimestamp = mutableLongStateOf(System.currentTimeMillis())

    // Real-time downloading progress map: packId -> progress [0.0f..1.0f]
    val downloadingProgressMap = mutableStateMapOf<String, Float>()
    // Entity specific status map: entityId -> SyncStatus
    val entitySyncStatusMap = mutableStateMapOf<String, SyncStatus>()

    // In-memory cache of decrypted questions for fast access
    private val offlineQuestionsMemoryCache = mutableMapOf<String, List<Question>>()

    init {
        initializeDefaultOfflineData()
    }

    private fun initializeDefaultOfflineData() {
        val historySubject = SampleData.subjectsState.firstOrNull { it.id == "subj-history" }
            ?: SampleData.subjectsState.firstOrNull()

        if (historySubject != null) {
            val firstUnit = historySubject.units.firstOrNull()
            val firstTopic = firstUnit?.topics?.firstOrNull()

            // Seed Subject Pack
            val subPackId = "subject_${historySubject.id}"
            downloadedPacks.add(
                OfflinePack(
                    id = subPackId,
                    title = historySubject.name,
                    hierarchyType = DownloadHierarchyType.SUBJECT,
                    subjectId = historySubject.id,
                    questionCount = 320,
                    sizeBytes = 450L * 1024 * 1024,
                    syncStatus = SyncStatus.DOWNLOADED,
                    lastSyncedTimeText = "Synced 2 hrs ago"
                )
            )
            entitySyncStatusMap[subPackId] = SyncStatus.DOWNLOADED

            // Seed Unit Pack
            if (firstUnit != null) {
                val unitPackId = "unit_${historySubject.id}_${firstUnit.id}"
                downloadedPacks.add(
                    OfflinePack(
                        id = unitPackId,
                        title = firstUnit.name,
                        hierarchyType = DownloadHierarchyType.UNIT,
                        subjectId = historySubject.id,
                        unitId = firstUnit.id,
                        questionCount = 120,
                        sizeBytes = 180L * 1024 * 1024,
                        syncStatus = SyncStatus.DOWNLOADED,
                        lastSyncedTimeText = "Synced 2 hrs ago"
                    )
                )
                entitySyncStatusMap[unitPackId] = SyncStatus.DOWNLOADED

                // Seed Topic Pack (Indus Valley)
                if (firstTopic != null) {
                    val topicPackId = "topic_${historySubject.id}_${firstUnit.id}_${firstTopic.id}"
                    downloadedPacks.add(
                        OfflinePack(
                            id = topicPackId,
                            title = firstTopic.name,
                            hierarchyType = DownloadHierarchyType.TOPIC,
                            subjectId = historySubject.id,
                            unitId = firstUnit.id,
                            topicId = firstTopic.id,
                            questionCount = 50,
                            sizeBytes = 75L * 1024 * 1024,
                            syncStatus = SyncStatus.DOWNLOADED,
                            lastSyncedTimeText = "Synced 2 hrs ago"
                        )
                    )
                    entitySyncStatusMap[topicPackId] = SyncStatus.DOWNLOADED
                }

                // Seed Sets (Set 1, Set 2, Set 3, Set 4)
                for (i in 1..4) {
                    val setPackId = "set_${historySubject.id}_${firstUnit.id}_top-hist-1_Set_$i"
                    downloadedPacks.add(
                        OfflinePack(
                            id = setPackId,
                            title = "Set $i",
                            hierarchyType = DownloadHierarchyType.SET,
                            subjectId = historySubject.id,
                            unitId = firstUnit.id,
                            topicId = "top-hist-1",
                            setName = "Set $i",
                            questionCount = 25,
                            sizeBytes = 10L * 1024 * 1024,
                            syncStatus = SyncStatus.DOWNLOADED,
                            lastSyncedTimeText = "Synced 2 hrs ago"
                        )
                    )
                    entitySyncStatusMap[setPackId] = SyncStatus.DOWNLOADED
                }
            }
        }

        // Seed Indian Geography as downloaded
        val geoSub = SampleData.subjectsState.firstOrNull { it.id == "subj-geography" }
        if (geoSub != null) {
            val geoPackId = "subject_${geoSub.id}"
            downloadedPacks.add(
                OfflinePack(
                    id = geoPackId,
                    title = geoSub.name,
                    hierarchyType = DownloadHierarchyType.SUBJECT,
                    subjectId = geoSub.id,
                    questionCount = 280,
                    sizeBytes = 380L * 1024 * 1024,
                    syncStatus = SyncStatus.DOWNLOADED,
                    lastSyncedTimeText = "Synced 5 hrs ago"
                )
            )
            entitySyncStatusMap[geoPackId] = SyncStatus.DOWNLOADED
        }

        // Seed Indian Constitution as downloaded
        val constSub = SampleData.subjectsState.firstOrNull { it.id == "subj-constitution" }
        if (constSub != null) {
            val constPackId = "subject_${constSub.id}"
            downloadedPacks.add(
                OfflinePack(
                    id = constPackId,
                    title = constSub.name,
                    hierarchyType = DownloadHierarchyType.SUBJECT,
                    subjectId = constSub.id,
                    questionCount = 340,
                    sizeBytes = 520L * 1024 * 1024,
                    syncStatus = SyncStatus.DOWNLOADED,
                    lastSyncedTimeText = "Synced 1 day ago"
                )
            )
            entitySyncStatusMap[constPackId] = SyncStatus.DOWNLOADED
        }

        // Seed Indian Society as Syncing
        val socSub = SampleData.subjectsState.firstOrNull { it.id == "subj-society" }
        if (socSub != null) {
            val socPackId = "subject_${socSub.id}"
            entitySyncStatusMap[socPackId] = SyncStatus.SYNCING
            downloadingProgressMap[socPackId] = 0.20f
        }
    }

    // -------------------------------------------------------------
    // Status Query Helpers
    // -------------------------------------------------------------

    fun isPackDownloaded(packId: String): Boolean {
        return downloadedPacks.any { it.id == packId }
    }

    fun isSubjectDownloaded(subjectId: String): Boolean {
        return downloadedPacks.any { it.hierarchyType == DownloadHierarchyType.SUBJECT && it.subjectId == subjectId }
    }

    fun isUnitDownloaded(subjectId: String, unitId: String): Boolean {
        return isSubjectDownloaded(subjectId) || downloadedPacks.any {
            (it.hierarchyType == DownloadHierarchyType.UNIT || it.hierarchyType == DownloadHierarchyType.SUBJECT) &&
                    it.subjectId == subjectId && it.unitId == unitId
        }
    }

    fun isTopicDownloaded(subjectId: String, unitId: String, topicId: String): Boolean {
        return isSubjectDownloaded(subjectId) || isUnitDownloaded(subjectId, unitId) || downloadedPacks.any {
            it.subjectId == subjectId && (unitId.isBlank() || it.unitId == unitId) && it.topicId == topicId
        }
    }

    fun isSetDownloaded(subjectId: String, setName: String): Boolean {
        return isSubjectDownloaded(subjectId) || downloadedPacks.any {
            it.subjectId == subjectId && it.setName == setName
        }
    }

    fun isSetDownloaded(subjectId: String, unitId: String, topicId: String, setName: String): Boolean {
        val packId = "set_${subjectId}_${unitId}_${topicId}_${setName.replace(" ", "_")}"
        return isSubjectDownloaded(subjectId) || isUnitDownloaded(subjectId, unitId) || isTopicDownloaded(subjectId, unitId, topicId) ||
                downloadedPacks.any { it.id == packId }
    }

    fun getSubjectSyncStatus(subjectId: String): SyncStatus {
        val packId = "subject_$subjectId"
        return entitySyncStatusMap[packId] ?: if (isSubjectDownloaded(subjectId)) SyncStatus.DOWNLOADED else SyncStatus.NOT_DOWNLOADED
    }

    fun getUnitSyncStatus(subjectId: String, unitId: String): SyncStatus {
        val packId = "unit_${subjectId}_${unitId}"
        return entitySyncStatusMap[packId] ?: if (isUnitDownloaded(subjectId, unitId)) SyncStatus.DOWNLOADED else SyncStatus.NOT_DOWNLOADED
    }

    fun getTopicSyncStatus(subjectId: String, unitId: String, topicId: String): SyncStatus {
        val packId = "topic_${subjectId}_${unitId}_${topicId}"
        return entitySyncStatusMap[packId] ?: if (isTopicDownloaded(subjectId, unitId, topicId)) SyncStatus.DOWNLOADED else SyncStatus.NOT_DOWNLOADED
    }

    fun getSetSyncStatus(subjectId: String, unitId: String, topicId: String, setName: String): SyncStatus {
        val packId = "set_${subjectId}_${unitId}_${topicId}_${setName.replace(" ", "_")}"
        return entitySyncStatusMap[packId] ?: if (isSetDownloaded(subjectId, unitId, topicId, setName)) SyncStatus.DOWNLOADED else SyncStatus.NOT_DOWNLOADED
    }

    // -------------------------------------------------------------
    // Download Operations (Subject, Unit, Topic, Set)
    // -------------------------------------------------------------

    /**
     * Download an Entire Subject (All units, topics, and sets)
     */
    fun downloadSubject(
        context: Context,
        subject: Subject,
        onProgress: (Float) -> Unit = {},
        onComplete: (Boolean) -> Unit = {}
    ) {
        val packId = "subject_${subject.id}"
        if (isSubjectDownloaded(subject.id)) {
            onComplete(true)
            return
        }

        entitySyncStatusMap[packId] = SyncStatus.DOWNLOADING
        downloadingProgressMap[packId] = 0.05f

        CoroutineScope(Dispatchers.IO).launch {
            for (p in 10..100 step 15) {
                delay(100)
                val progress = p / 100f
                withContext(Dispatchers.Main) {
                    downloadingProgressMap[packId] = progress
                    onProgress(progress)
                }
            }

            val allQuestions = SampleData.getQuestionsForPractice(subject.id, "", "", "")

            // 1. Save into AES-256 Encrypted Private Vault
            SecureMcqVault.saveEncryptedVaultFile(context, packId, allQuestions, "appsc-group2")

            // 2. Persist in Room DB question_bank table
            try {
                val db = AppDatabase.getDatabase(context)
                val entities = allQuestions.map { q ->
                    QuestionEntity(
                        id = q.id,
                        appId = q.appId,
                        subjectId = q.subjectId,
                        unitId = q.unitId,
                        topicId = q.topicId,
                        subtopic = q.subtopic,
                        questionText = q.questionText,
                        options = q.options,
                        correctOption = q.correctOption,
                        explanation = q.explanation,
                        setName = q.setName,
                        difficulty = q.difficulty,
                        language = q.language,
                        imageUrl = q.imageUrl,
                        year = q.year,
                        exam = q.exam,
                        marks = q.marks,
                        negativeMarks = q.negativeMarks
                    )
                }
                db.questionDao().insertQuestions(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val pack = OfflinePack(
                id = packId,
                title = subject.name,
                hierarchyType = DownloadHierarchyType.SUBJECT,
                subjectId = subject.id,
                questionCount = if (allQuestions.isNotEmpty()) allQuestions.size else 320,
                sizeBytes = 450L * 1024 * 1024,
                syncStatus = SyncStatus.DOWNLOADED,
                lastSyncedTimeText = "Synced Just now"
            )

            withContext(Dispatchers.Main) {
                downloadedPacks.removeAll { it.id == packId }
                downloadedPacks.add(pack)
                entitySyncStatusMap[packId] = SyncStatus.DOWNLOADED
                downloadingProgressMap.remove(packId)
                offlineQuestionsMemoryCache[packId] = allQuestions
                onComplete(true)
            }
        }
    }

    /**
     * Download a specific Unit
     */
    fun downloadUnit(
        context: Context,
        subject: Subject,
        unit: UnitModel,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val packId = "unit_${subject.id}_${unit.id}"
        if (isUnitDownloaded(subject.id, unit.id)) {
            onComplete(true)
            return
        }

        entitySyncStatusMap[packId] = SyncStatus.DOWNLOADING
        downloadingProgressMap[packId] = 0.1f

        CoroutineScope(Dispatchers.IO).launch {
            for (p in 20..100 step 25) {
                delay(80)
                withContext(Dispatchers.Main) {
                    downloadingProgressMap[packId] = p / 100f
                }
            }

            val questions = SampleData.getQuestionsForPractice(subject.id, unit.id, "", "")

            SecureMcqVault.saveEncryptedVaultFile(context, packId, questions, "appsc-group2")

            try {
                val db = AppDatabase.getDatabase(context)
                val entities = questions.map { q ->
                    QuestionEntity(
                        id = q.id,
                        appId = q.appId,
                        subjectId = q.subjectId,
                        unitId = q.unitId,
                        topicId = q.topicId,
                        subtopic = q.subtopic,
                        questionText = q.questionText,
                        options = q.options,
                        correctOption = q.correctOption,
                        explanation = q.explanation,
                        setName = q.setName,
                        difficulty = q.difficulty,
                        language = q.language,
                        imageUrl = q.imageUrl,
                        year = q.year,
                        exam = q.exam,
                        marks = q.marks,
                        negativeMarks = q.negativeMarks
                    )
                }
                db.questionDao().insertQuestions(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val pack = OfflinePack(
                id = packId,
                title = unit.name,
                hierarchyType = DownloadHierarchyType.UNIT,
                subjectId = subject.id,
                unitId = unit.id,
                questionCount = if (questions.isNotEmpty()) questions.size else 120,
                sizeBytes = 180L * 1024 * 1024,
                syncStatus = SyncStatus.DOWNLOADED,
                lastSyncedTimeText = "Synced Just now"
            )

            withContext(Dispatchers.Main) {
                downloadedPacks.removeAll { it.id == packId }
                downloadedPacks.add(pack)
                entitySyncStatusMap[packId] = SyncStatus.DOWNLOADED
                downloadingProgressMap.remove(packId)
                offlineQuestionsMemoryCache[packId] = questions
                onComplete(true)
            }
        }
    }

    /**
     * Download a specific Topic
     */
    fun downloadTopic(
        context: Context,
        subject: Subject,
        unit: UnitModel,
        topic: TopicModel,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val packId = "topic_${subject.id}_${unit.id}_${topic.id}"
        if (isTopicDownloaded(subject.id, unit.id, topic.id)) {
            onComplete(true)
            return
        }

        entitySyncStatusMap[packId] = SyncStatus.DOWNLOADING
        downloadingProgressMap[packId] = 0.15f

        CoroutineScope(Dispatchers.IO).launch {
            for (p in 30..100 step 35) {
                delay(70)
                withContext(Dispatchers.Main) {
                    downloadingProgressMap[packId] = p / 100f
                }
            }

            val questions = SampleData.getQuestionsForPractice(subject.id, unit.id, topic.id, "")

            SecureMcqVault.saveEncryptedVaultFile(context, packId, questions, "appsc-group2")

            val pack = OfflinePack(
                id = packId,
                title = topic.name,
                hierarchyType = DownloadHierarchyType.TOPIC,
                subjectId = subject.id,
                unitId = unit.id,
                topicId = topic.id,
                questionCount = if (questions.isNotEmpty()) questions.size else 50,
                sizeBytes = 60L * 1024 * 1024,
                syncStatus = SyncStatus.DOWNLOADED,
                lastSyncedTimeText = "Synced Just now"
            )

            withContext(Dispatchers.Main) {
                downloadedPacks.removeAll { it.id == packId }
                downloadedPacks.add(pack)
                entitySyncStatusMap[packId] = SyncStatus.DOWNLOADED
                downloadingProgressMap.remove(packId)
                offlineQuestionsMemoryCache[packId] = questions
                onComplete(true)
            }
        }
    }

    /**
     * Download a specific Practice Set
     */
    fun downloadPracticeSet(
        context: Context,
        subjectId: String,
        unitId: String,
        topicId: String,
        setName: String,
        questions: List<Question> = emptyList(),
        onComplete: (Boolean) -> Unit = {}
    ) {
        val packId = "set_${subjectId}_${unitId}_${topicId}_${setName.replace(" ", "_")}"
        if (isPackDownloaded(packId)) {
            onComplete(true)
            return
        }

        entitySyncStatusMap[packId] = SyncStatus.DOWNLOADING
        downloadingProgressMap[packId] = 0.2f

        CoroutineScope(Dispatchers.IO).launch {
            delay(250)
            val resolvedQuestions = questions.ifEmpty {
                SampleData.getQuestionsForPractice(subjectId, unitId, topicId, "").take(25)
            }

            SecureMcqVault.saveEncryptedVaultFile(context, packId, resolvedQuestions, "appsc-group2")

            val pack = OfflinePack(
                id = packId,
                title = setName,
                hierarchyType = DownloadHierarchyType.SET,
                subjectId = subjectId,
                unitId = unitId,
                topicId = topicId,
                setName = setName,
                questionCount = if (resolvedQuestions.isNotEmpty()) resolvedQuestions.size else 25,
                sizeBytes = 10L * 1024 * 1024,
                syncStatus = SyncStatus.DOWNLOADED,
                lastSyncedTimeText = "Synced Just now"
            )

            withContext(Dispatchers.Main) {
                downloadedPacks.removeAll { it.id == packId }
                downloadedPacks.add(pack)
                entitySyncStatusMap[packId] = SyncStatus.DOWNLOADED
                downloadingProgressMap.remove(packId)
                offlineQuestionsMemoryCache[packId] = resolvedQuestions
                onComplete(true)
            }
        }
    }

    /**
     * Remove / Delete offline pack from secure encrypted storage and active list
     */
    fun removePack(context: Context, packId: String) {
        downloadedPacks.removeAll { it.id == packId }
        entitySyncStatusMap[packId] = SyncStatus.NOT_DOWNLOADED
        downloadingProgressMap.remove(packId)
        offlineQuestionsMemoryCache.remove(packId)
        SecureMcqVault.deleteEncryptedVaultFile(context, packId)
    }

    /**
     * Remove entire Subject and all nested child packs
     */
    fun removeSubject(context: Context, subjectId: String) {
        val toRemove = downloadedPacks.filter { it.subjectId == subjectId }
        toRemove.forEach { pack ->
            removePack(context, pack.id)
        }
        val subPackId = "subject_$subjectId"
        entitySyncStatusMap[subPackId] = SyncStatus.NOT_DOWNLOADED
    }

    // -------------------------------------------------------------
    // Questions Retrieval for Offline Practice
    // -------------------------------------------------------------

    fun getQuestionsForPractice(
        context: Context,
        subjectId: String,
        unitId: String = "",
        topicId: String = "",
        setName: String = ""
    ): List<Question> {
        val specificPackId = when {
            setName.isNotBlank() -> "set_${subjectId}_${unitId}_${topicId}_${setName.replace(" ", "_")}"
            topicId.isNotBlank() -> "topic_${subjectId}_${unitId}_${topicId}"
            unitId.isNotBlank() -> "unit_${subjectId}_${unitId}"
            else -> "subject_$subjectId"
        }

        offlineQuestionsMemoryCache[specificPackId]?.let { return it }

        val decrypted = SecureMcqVault.readEncryptedVaultFile(context, specificPackId, "appsc-group2")
        if (decrypted.isNotEmpty()) {
            offlineQuestionsMemoryCache[specificPackId] = decrypted
            return decrypted
        }

        val fallback = SampleData.getQuestionsForPractice(subjectId, unitId, topicId, setName)
        return fallback.ifEmpty { SampleData.questionsState.take(25) }
    }

    // -------------------------------------------------------------
    // Storage & Metrics Formatters
    // -------------------------------------------------------------

    fun getTotalStorageUsedBytes(context: Context? = null): Long {
        val sum = downloadedPacks.sumOf { it.sizeBytes }
        return if (sum > 0) sum else (1.3 * 1024 * 1024 * 1024).toLong()
    }

    fun getTotalOfflineQuestionsCount(): Int {
        val count = downloadedPacks.sumOf { it.questionCount }
        return if (count > 0) count else 320
    }

    fun formatStorageSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f GB", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }

    // -------------------------------------------------------------
    // Offline Simulation & Cloud Sync
    // -------------------------------------------------------------

    fun toggleOfflineSimulation(enable: Boolean) {
        isOfflineModeSimulated.value = enable
        syncStatusState.value = if (enable) SyncStatus.SYNC_REQUIRED else SyncStatus.SYNCED
    }

    fun triggerSync(onResult: (Boolean, String) -> Unit) {
        if (isOfflineModeSimulated.value) {
            onResult(false, "Device is in Simulated Offline Mode. Disable offline mode to sync.")
            return
        }

        syncStatusState.value = SyncStatus.SYNCING
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                syncStatusState.value = SyncStatus.SYNCED
                pendingSyncCountState.value = 0
                lastSyncedTimestamp.longValue = System.currentTimeMillis()
                onResult(true, "All MCQs, answers & bookmarks synchronized with cloud!")
            }
        }
    }

    fun recordOfflineAttempt() {
        pendingSyncCountState.value += 1
        if (syncStatusState.value != SyncStatus.SYNC_REQUIRED) {
            syncStatusState.value = SyncStatus.SYNC_REQUIRED
        }
    }

    fun clearAllStorage(context: Context) {
        downloadedPacks.clear()
        entitySyncStatusMap.clear()
        downloadingProgressMap.clear()
        offlineQuestionsMemoryCache.clear()
        SecureMcqVault.clearAllVault(context)
    }
}
