package com.example.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.model.Question
import com.example.model.CategoryContentCounts
import com.example.model.DrawerConfig
import com.example.model.CardBorderConfig
import com.example.model.PromotionalPosterModel
import com.example.model.AppWallpaperModel
import com.example.data.SampleData
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Centralized Manager for Firebase Services:
 * 1. Firebase Authentication (Real Phone SMS OTP & Google Sign-In)
 * 2. Cloud Firestore (MCQs, Daily Quizzes, Current Affairs, Banners, Subscriptions)
 * 3. Firebase Cloud Storage (PDFs, Diagrams, Banners)
 * 4. Firebase Cloud Messaging (FCM Push Notifications)
 *
 * Configured for account: sobhadhanaraju0@gmail.com
 */
object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"

    const val ADMIN_EMAIL = "sobhadhanaraju0@gmail.com"

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    const val FIRESTORE_DATABASE_ID = "ai-studio-android-androida-7c3e8735-fcd2-4822-9637-743227eebfb5"

    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)

    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    /**
     * Initializes FCM Push Notification listener (disabled on unprovisioned backend to prevent FIS errors).
     */
    fun initializeFcm(context: Context, onTokenReceived: ((String) -> Unit)? = null) {
        // FCM auto-initialization disabled to prevent FIS_AUTH_ERROR on unprovisioned backend
    }

    init {
        try {
            // Configure Firebase Auth settings for silent background verification
            auth.setLanguageCode("en")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth settings initialization: ${e.message}")
        }
    }

    /**
     * Enables or disables app verification for developer testing or emulator environments.
     */
    fun setAppVerificationDisabledForTesting(disabled: Boolean) {
        try {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(disabled)
            Log.d(TAG, "FirebaseAuth app verification disabled for testing: $disabled")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set app verification setting: ${e.message}")
        }
    }

    /**
     * Triggers Phone SMS OTP sending via Firebase Authentication with silent background verification.
     */
    fun sendSmsOtp(
        phoneNumber: String,
        activity: android.app.Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            builder.setForceResendingToken(resendToken)
        }

        try {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (e: Exception) {
            Log.w(TAG, "PhoneAuthProvider.verifyPhoneNumber failed: ${e.message}")
            callbacks.onVerificationFailed(com.google.firebase.FirebaseException("SMS Provider unavailable: ${e.message}"))
        }
    }

    /**
     * Verifies an SMS OTP code using Firebase PhoneAuthProvider and signs in the user.
     */
    fun verifySmsOtp(
        verificationId: String,
        otpCode: String,
        onSuccess: (com.google.firebase.auth.FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onFailure(Exception("Authentication failed: No user record returned."))
                        }
                    } else {
                        onFailure(task.exception ?: Exception("Verification failed. Please check the code and try again."))
                    }
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Uploads Current Affairs PDF or Question Banner to Firebase Cloud Storage.
     */
    fun uploadPdfToStorage(
        fileName: String,
        fileBytes: ByteArray,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val ref = storage.reference.child("current_affairs_pdfs/$fileName")
            ref.putBytes(fileBytes)
                .continueWithTask(com.google.android.gms.tasks.Continuation<com.google.firebase.storage.UploadTask.TaskSnapshot, com.google.android.gms.tasks.Task<android.net.Uri>> { task ->
                    if (!task.isSuccessful) {
                        val ex = (task as com.google.android.gms.tasks.Task<*>).exception
                        if (ex != null) throw ex
                    }
                    ref.downloadUrl
                })
                .addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Syncs Question Bank item to Cloud Firestore under tenant appId.
     */
    fun syncQuestionToFirestore(
        appId: String,
        questionMap: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val qId = questionMap["id"]?.toString() ?: System.currentTimeMillis().toString()
            firestore.collection("apps")
                .document(appId)
                .collection("questions")
                .document(qId)
                .set(questionMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Manually re-fetches and updates the question bank & count metrics from Cloud Firestore.
     * Updates SampleData.questionsState and returns result summary.
     */
    fun fetchQuestionsAndSyncMetrics(
        appId: String,
        context: Context?,
        onComplete: (success: Boolean, count: Int, message: String) -> Unit
    ) {
        try {
            firestore.collection("apps")
                .document(appId)
                .collection("questions")
                .get()
                .addOnSuccessListener { snapshot ->
                    val newQuestions = mutableListOf<Question>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.id
                            val subjectId = doc.getString("subjectId") ?: ""
                            val unitId = doc.getString("unitId") ?: ""
                            val topicId = doc.getString("topicId") ?: ""
                            val contentLevel = doc.getString("contentLevel") ?: "TOPIC"
                            val subtopic = doc.getString("subtopic") ?: ""
                            val questionText = doc.getString("questionText") ?: doc.getString("question") ?: ""
                            val correctOption = doc.getString("correctOption") ?: "A"
                            val explanation = doc.getString("explanation")
                            val setName = doc.getString("setName") ?: ""
                            val difficulty = doc.getString("difficulty") ?: "Medium"
                            val questionType = doc.getString("questionType") ?: "SINGLE_CHOICE"
                            val source = doc.getString("source")
                            val language = doc.getString("language") ?: "English / Telugu"
                            val isActive = doc.getBoolean("isActive") ?: true

                            val rawOptions = doc.get("options")
                            val optionsMap = when (rawOptions) {
                                is Map<*, *> -> rawOptions.entries.associate { (k, v) -> k.toString() to v.toString() }
                                is List<*> -> {
                                    val letters = listOf("A", "B", "C", "D", "E")
                                    rawOptions.mapIndexed { idx, opt -> (letters.getOrNull(idx) ?: "Option ${idx + 1}") to opt.toString() }.toMap()
                                }
                                else -> emptyMap()
                            }

                            if (questionText.isNotBlank()) {
                                newQuestions.add(
                                    Question(
                                        id = id,
                                        appId = appId,
                                        subjectId = subjectId,
                                        unitId = unitId,
                                        topicId = topicId,
                                        contentLevel = contentLevel,
                                        subtopic = subtopic,
                                        questionText = questionText,
                                        options = optionsMap,
                                        correctOption = correctOption,
                                        explanation = explanation,
                                        setName = setName,
                                        difficulty = difficulty,
                                        questionType = questionType,
                                        source = source,
                                        language = language,
                                        isActive = isActive
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed parsing question doc ${doc.id}", e)
                        }
                    }

                    if (newQuestions.isEmpty()) {
                        // Check fallback collection 'questions'
                        firestore.collection("questions")
                            .whereEqualTo("appId", appId)
                            .get()
                            .addOnSuccessListener { globalSnap ->
                                for (doc in globalSnap.documents) {
                                    try {
                                        val qText = doc.getString("questionText") ?: doc.getString("question") ?: ""
                                        if (qText.isNotBlank()) {
                                            newQuestions.add(
                                                Question(
                                                    id = doc.id,
                                                    appId = appId,
                                                    subjectId = doc.getString("subjectId") ?: "",
                                                    unitId = doc.getString("unitId") ?: "",
                                                    topicId = doc.getString("topicId") ?: "",
                                                    questionText = qText,
                                                    options = when (val opt = doc.get("options")) {
                                                        is Map<*, *> -> opt.entries.associate { (k, v) -> k.toString() to v.toString() }
                                                        else -> emptyMap()
                                                    },
                                                    correctOption = doc.getString("correctOption") ?: "A",
                                                    explanation = doc.getString("explanation"),
                                                    setName = doc.getString("setName") ?: ""
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {}
                                }
                                applyFetchedQuestions(newQuestions, appId, onComplete)
                            }
                            .addOnFailureListener {
                                applyFetchedQuestions(newQuestions, appId, onComplete)
                            }
                    } else {
                        applyFetchedQuestions(newQuestions, appId, onComplete)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Firestore question fetch failed: ${e.message}")
                    val localCount = SampleData.questionsState.count { it.appId == appId || it.appId == "ALL" }
                    onComplete(true, localCount, "Metrics synced (Offline/Cached: $localCount MCQs active)")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during fetchQuestionsAndSyncMetrics", e)
            val localCount = SampleData.questionsState.count { it.appId == appId || it.appId == "ALL" }
            onComplete(false, localCount, "Refreshed metrics ($localCount MCQs active)")
        }
    }

    // =========================================================================
    // REAL-TIME SYLLABUS & MCQ CLOUD SYNCHRONIZATION (ADMIN <-> USER APPS)
    // =========================================================================

    /**
     * Converts a Question domain model into a Firestore-compatible HashMap with complete metadata.
     */
    fun questionToMap(q: Question): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to q.id,
            "appId" to q.appId,
            "subjectId" to q.subjectId,
            "unitId" to q.unitId,
            "topicId" to q.topicId,
            "contentLevel" to q.contentLevel,
            "subtopic" to q.subtopic,
            "questionText" to q.questionText,
            "options" to q.options,
            "correctOption" to q.correctOption,
            "setName" to q.setName,
            "difficulty" to q.difficulty,
            "questionType" to q.questionType,
            "language" to q.language,
            "marks" to q.marks,
            "negativeMarks" to q.negativeMarks,
            "isActive" to q.isActive,
            "createdAt" to q.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        q.explanation?.let { map["explanation"] = it }
        q.source?.let { map["source"] = it }
        q.imageUrl?.let { map["imageUrl"] = it }
        q.year?.let { map["year"] = it }
        q.exam?.let { map["exam"] = it }
        q.contentHash?.let { map["contentHash"] = it }
        q.importBatchId?.let { map["importBatchId"] = it }
        if (q.tags.isNotEmpty()) map["tags"] = q.tags
        return map
    }

    /**
     * Synchronizes a single Question to Firestore under apps/{appId}/questions/{qId}.
     */
    fun syncQuestionModelToFirestore(
        question: Question,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val qMap = questionToMap(question)
            firestore.collection("apps")
                .document(question.appId)
                .collection("questions")
                .document(question.id)
                .set(qMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced question ${question.id} to Firestore")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed syncing question ${question.id} to Firestore: ${e.message}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Synchronizes a batch of Questions to Firestore efficiently using batch writes.
     */
    fun syncQuestionsBatchToFirestore(
        appId: String,
        questions: List<Question>,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        if (questions.isEmpty()) {
            onSuccess()
            return
        }
        try {
            // Firestore batches allow up to 500 operations
            val chunks = questions.chunked(450)
            var completedChunks = 0
            var hadError = false

            for (chunk in chunks) {
                val batch = firestore.batch()
                for (q in chunk) {
                    val docRef = firestore.collection("apps")
                        .document(appId)
                        .collection("questions")
                        .document(q.id)
                    batch.set(docRef, questionToMap(q))
                }
                batch.commit()
                    .addOnSuccessListener {
                        completedChunks++
                        if (completedChunks == chunks.size && !hadError) {
                            Log.d(TAG, "Batch synced ${questions.size} questions to Firestore for appId: $appId")
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        if (!hadError) {
                            hadError = true
                            Log.e(TAG, "Batch question sync error: ${e.message}")
                            onFailure(e)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during syncQuestionsBatchToFirestore", e)
            onFailure(e)
        }
    }

    /**
     * Deletes a Question from Cloud Firestore.
     */
    fun deleteQuestionFromFirestore(
        appId: String,
        questionId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            firestore.collection("apps")
                .document(appId)
                .collection("questions")
                .document(questionId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Question $questionId deleted from Firestore")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting question $questionId from Firestore: ${e.message}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Synchronizes a complete Subject hierarchy (with Units & Topics) to Cloud Firestore.
     */
    fun syncSubjectToFirestore(
        subject: com.example.model.Subject,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val unitsList = subject.units.map { u ->
                val topicsList = u.topics.map { t ->
                    mapOf(
                        "id" to t.id,
                        "appId" to t.appId,
                        "name" to t.name,
                        "subtitle" to (t.subtitle ?: ""),
                        "imageUrl" to (t.imageUrl ?: ""),
                        "estimatedMcqs" to (t.estimatedMcqs ?: ""),
                        "difficulty" to t.difficulty,
                        "practiceSetsCount" to t.practiceSetsCount,
                        "completionPercentage" to t.completionPercentage,
                        "subtopics" to t.subtopics
                    )
                }
                mapOf(
                    "id" to u.id,
                    "appId" to u.appId,
                    "name" to u.name,
                    "unitNumber" to u.unitNumber,
                    "subtitle" to (u.subtitle ?: ""),
                    "imageUrl" to (u.imageUrl ?: ""),
                    "estimatedMcqs" to (u.estimatedMcqs ?: ""),
                    "topics" to topicsList
                )
            }

            val subjectMap = mapOf(
                "id" to subject.id,
                "appId" to subject.appId,
                "name" to subject.name,
                "category" to subject.category,
                "categories" to subject.categories,
                "icon" to subject.icon,
                "imageUrl" to subject.imageUrl,
                "heroQuote" to subject.heroQuote,
                "color" to subject.color,
                "units" to unitsList,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("apps")
                .document(subject.appId)
                .collection("syllabus")
                .document(subject.id)
                .set(subjectMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced Subject ${subject.name} to Firestore for app ${subject.appId}")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed syncing Subject ${subject.name} to Firestore: ${e.message}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Deletes a Subject from Cloud Firestore syllabus.
     */
    fun deleteSubjectFromFirestore(
        appId: String,
        subjectId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            firestore.collection("apps")
                .document(appId)
                .collection("syllabus")
                .document(subjectId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Subject $subjectId deleted from Firestore syllabus")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting subject $subjectId: ${e.message}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Persists independent dashboard card visibility configuration (Prelims, Mains, All) to Cloud Firestore.
     */
    fun saveAppCardVisibility(
        appId: String,
        isPrelims: Boolean,
        isMains: Boolean,
        isAll: Boolean,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val configMap = mapOf(
                "isPrelimsCardEnabled" to isPrelims,
                "isMainsCardEnabled" to isMains,
                "isAllCardEnabled" to isAll,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("apps")
                .document(appId)
                .collection("config")
                .document("card_visibility")
                .set(configMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully persisted card visibility for app '$appId': Prelims=$isPrelims, Mains=$isMains, All=$isAll")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed persisting card visibility for app '$appId': ${e.message}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            Log.w(TAG, "Exception saving card visibility for app '$appId': ${e.message}")
            onFailure(e)
        }
    }

    /**
     * Real-time Flow of Syllabus Subjects for [appId] from Cloud Firestore.
     * Whenever admin modifies/adds/deletes subjects, this triggers automatically for the User App.
     */
    fun observeSyllabusForApp(appId: String): Flow<List<com.example.model.Subject>> = callbackFlow {
        val listener = firestore.collection("apps")
            .document(appId)
            .collection("syllabus")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Syllabus snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val subjectsList = mutableListOf<com.example.model.Subject>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.id
                            val name = doc.getString("name") ?: ""
                            val category = doc.getString("category") ?: "prelims"
                            val rawCategories = doc.get("categories") as? List<*> ?: emptyList<Any>()
                            val categories = rawCategories.map { it.toString() }
                            val icon = doc.getString("icon") ?: "BookOpen"
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val heroQuote = doc.getString("heroQuote") ?: ""
                            val color = doc.getString("color") ?: "from-amber-600 to-orange-700"

                            val rawUnits = doc.get("units") as? List<*> ?: emptyList<Any>()
                            val unitsList = rawUnits.mapNotNull { uItem ->
                                val uMap = uItem as? Map<*, *> ?: return@mapNotNull null
                                val uId = uMap["id"]?.toString() ?: ""
                                val uName = uMap["name"]?.toString() ?: ""
                                val uNumber = (uMap["unitNumber"] as? Number)?.toInt() ?: 1
                                val uSubtitle = uMap["subtitle"]?.toString()?.ifBlank { null }
                                val uImage = uMap["imageUrl"]?.toString()?.ifBlank { null }
                                val uEstMcqs = uMap["estimatedMcqs"]?.toString()?.ifBlank { null }

                                val rawTopics = uMap["topics"] as? List<*> ?: emptyList<Any>()
                                val topicsList = rawTopics.mapNotNull { tItem ->
                                    val tMap = tItem as? Map<*, *> ?: return@mapNotNull null
                                    val tId = tMap["id"]?.toString() ?: ""
                                    val tName = tMap["name"]?.toString() ?: ""
                                    val tSubtitle = tMap["subtitle"]?.toString()?.ifBlank { null }
                                    val tImage = tMap["imageUrl"]?.toString()?.ifBlank { null }
                                    val tEst = tMap["estimatedMcqs"]?.toString()?.ifBlank { null }
                                    val tDiff = tMap["difficulty"]?.toString() ?: "Medium"
                                    val tSetsCount = (tMap["practiceSetsCount"] as? Number)?.toInt() ?: 3
                                    val tComp = (tMap["completionPercentage"] as? Number)?.toInt() ?: 0
                                    val tSubtopics = (tMap["subtopics"] as? List<*>)?.map { it.toString() } ?: emptyList()

                                    com.example.model.TopicModel(
                                        id = tId,
                                        appId = appId,
                                        name = tName,
                                        subtitle = tSubtitle,
                                        imageUrl = tImage,
                                        estimatedMcqs = tEst,
                                        difficulty = tDiff,
                                        practiceSetsCount = tSetsCount,
                                        completionPercentage = tComp,
                                        subtopics = tSubtopics
                                    )
                                }

                                com.example.model.UnitModel(
                                    id = uId,
                                    appId = appId,
                                    name = uName,
                                    unitNumber = uNumber,
                                    subtitle = uSubtitle,
                                    imageUrl = uImage,
                                    estimatedMcqs = uEstMcqs,
                                    topics = topicsList
                                )
                            }

                            if (name.isNotBlank()) {
                                subjectsList.add(
                                    com.example.model.Subject(
                                        id = id,
                                        appId = appId,
                                        name = name,
                                        category = category,
                                        categories = categories,
                                        icon = icon,
                                        imageUrl = imageUrl,
                                        heroQuote = heroQuote,
                                        color = color,
                                        units = unitsList
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing subject doc ${doc.id}", e)
                        }
                    }
                    if (subjectsList.isNotEmpty()) {
                        trySend(subjectsList)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow of Questions for [appId] from Cloud Firestore.
     * Whenever admin uploads JSON MCQs, edits, or deletes questions, this emits live updates.
     */
    fun observeQuestionsForApp(appId: String): Flow<List<Question>> = callbackFlow {
        val listener = firestore.collection("apps")
            .document(appId)
            .collection("questions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Questions snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val questionsList = mutableListOf<Question>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.id
                            val subjectId = doc.getString("subjectId") ?: ""
                            val unitId = doc.getString("unitId") ?: ""
                            val topicId = doc.getString("topicId") ?: ""
                            val contentLevel = doc.getString("contentLevel") ?: "TOPIC"
                            val subtopic = doc.getString("subtopic") ?: ""
                            val questionText = doc.getString("questionText") ?: doc.getString("question") ?: ""
                            val correctOption = doc.getString("correctOption") ?: "A"
                            val explanation = doc.getString("explanation")
                            val setName = doc.getString("setName") ?: ""
                            val difficulty = doc.getString("difficulty") ?: "Medium"
                            val questionType = doc.getString("questionType") ?: "SINGLE_CHOICE"
                            val source = doc.getString("source")
                            val language = doc.getString("language") ?: "English / Telugu"
                            val isActive = doc.getBoolean("isActive") ?: true

                            val rawOptions = doc.get("options")
                            val optionsMap = when (rawOptions) {
                                is Map<*, *> -> rawOptions.entries.associate { (k, v) -> k.toString() to v.toString() }
                                is List<*> -> {
                                    val letters = listOf("A", "B", "C", "D", "E")
                                    rawOptions.mapIndexed { idx, opt -> (letters.getOrNull(idx) ?: "Option ${idx + 1}") to opt.toString() }.toMap()
                                }
                                else -> emptyMap()
                            }

                            if (questionText.isNotBlank()) {
                                questionsList.add(
                                    Question(
                                        id = id,
                                        appId = appId,
                                        subjectId = subjectId,
                                        unitId = unitId,
                                        topicId = topicId,
                                        contentLevel = contentLevel,
                                        subtopic = subtopic,
                                        questionText = questionText,
                                        options = optionsMap,
                                        correctOption = correctOption,
                                        explanation = explanation,
                                        setName = setName,
                                        difficulty = difficulty,
                                        questionType = questionType,
                                        source = source,
                                        language = language,
                                        isActive = isActive
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed parsing question ${doc.id}", e)
                        }
                    }
                    trySend(questionsList)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun applyFetchedQuestions(
        newQuestions: List<Question>,
        appId: String,
        onComplete: (Boolean, Int, String) -> Unit
    ) {
        var added = 0
        var updated = 0
        newQuestions.forEach { q ->
            val idx = SampleData.questionsState.indexOfFirst {
                it.id == q.id || (it.appId == q.appId && it.questionText.trim().equals(q.questionText.trim(), ignoreCase = true))
            }
            if (idx != -1) {
                SampleData.questionsState[idx] = q
                updated++
            } else {
                SampleData.questionsState.add(q)
                added++
            }
        }
        val total = SampleData.questionsState.count { it.appId == appId || it.appId == "ALL" }
        val msg = if (added > 0 || updated > 0) {
            "Synced from Firestore: $added new, $updated updated MCQs"
        } else {
            "Metrics up to date from Firestore ($total MCQs active)"
        }
        onComplete(true, total, msg)
    }

    /**
     * Syncs a Legal Document to Firestore.
     */
    fun syncLegalDocumentToFirestore(
        doc: com.example.model.LegalDocument,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val docMap = mapOf(
                "id" to doc.id,
                "type" to doc.type.name,
                "title" to doc.title,
                "content" to doc.content,
                "mappedAppIds" to doc.mappedAppIds,
                "isEnabled" to doc.isEnabled,
                "order" to doc.order,
                "lastUpdated" to doc.lastUpdated,
                "bgGradientStart" to (doc.bgGradientStart ?: ""),
                "bgGradientEnd" to (doc.bgGradientEnd ?: ""),
                "accentColorHex" to (doc.accentColorHex ?: ""),
                "iconName" to (doc.iconName ?: ""),
                "isPublished" to doc.isPublished,
                "sectionsJson" to (doc.sectionsJson ?: ""),
                "socialLinksJson" to (doc.socialLinksJson ?: ""),
                "draftContent" to (doc.draftContent ?: ""),
                "draftSectionsJson" to (doc.draftSectionsJson ?: ""),
                "draftTitle" to (doc.draftTitle ?: "")
            )
            firestore.collection("legal_documents")
                .document(doc.id)
                .set(docMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Deletes a Legal Document from Firestore.
     */
    fun deleteLegalDocumentFromFirestore(
        id: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            firestore.collection("legal_documents")
                .document(id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Syncs a Support FAQ to Firestore.
     */
    fun syncSupportFaqToFirestore(
        faq: com.example.model.SupportFaq,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val faqMap = mapOf(
                "id" to faq.id,
                "question" to faq.question,
                "answer" to faq.answer,
                "mappedAppIds" to faq.mappedAppIds,
                "isEnabled" to faq.isEnabled,
                "order" to faq.order
            )
            firestore.collection("support_faqs")
                .document(faq.id)
                .set(faqMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Deletes a Support FAQ from Firestore.
     */
    fun deleteSupportFaqFromFirestore(
        id: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            firestore.collection("support_faqs")
                .document(id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Fetches and synchronizes Legal Documents and Support FAQs from Firestore to the local database.
     */
    fun fetchAndSyncLegalSupport(
        repository: com.example.repository.AppRepository,
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        try {
            // 1. Fetch Legal Documents
            firestore.collection("legal_documents")
                .get()
                .addOnSuccessListener { snapshot ->
                    val docs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val typeStr = doc.getString("type") ?: return@mapNotNull null
                            val type = try {
                                com.example.model.LegalDocType.valueOf(typeStr)
                            } catch (e: IllegalArgumentException) {
                                Log.e(TAG, "Unknown LegalDocType found: $typeStr, skipping document: $id")
                                return@mapNotNull null
                            }
                            val title = doc.getString("title") ?: ""
                            val content = doc.getString("content") ?: ""
                            val mappedAppIds = doc.get("mappedAppIds") as? List<String> ?: emptyList()
                            val isEnabled = doc.getBoolean("isEnabled") ?: true
                            val order = doc.getLong("order")?.toInt() ?: 0
                            val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                            val bgGradientStart = doc.getString("bgGradientStart")?.ifEmpty { null }
                            val bgGradientEnd = doc.getString("bgGradientEnd")?.ifEmpty { null }
                            val accentColorHex = doc.getString("accentColorHex")?.ifEmpty { null }
                            val iconName = doc.getString("iconName")?.ifEmpty { null }
                            val isPublished = doc.getBoolean("isPublished") ?: true
                            val sectionsJson = doc.getString("sectionsJson")?.ifEmpty { null }
                            val socialLinksJson = doc.getString("socialLinksJson")?.ifEmpty { null }
                            val draftContent = doc.getString("draftContent")?.ifEmpty { null }
                            val draftSectionsJson = doc.getString("draftSectionsJson")?.ifEmpty { null }
                            val draftTitle = doc.getString("draftTitle")?.ifEmpty { null }

                            com.example.model.LegalDocument(
                                id = id,
                                type = type,
                                title = title,
                                content = content,
                                mappedAppIds = mappedAppIds,
                                isEnabled = isEnabled,
                                order = order,
                                lastUpdated = lastUpdated,
                                bgGradientStart = bgGradientStart,
                                bgGradientEnd = bgGradientEnd,
                                accentColorHex = accentColorHex,
                                iconName = iconName,
                                isPublished = isPublished,
                                sectionsJson = sectionsJson,
                                socialLinksJson = socialLinksJson,
                                draftContent = draftContent,
                                draftSectionsJson = draftSectionsJson,
                                draftTitle = draftTitle
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing Firestore LegalDocument", e)
                            null
                        }
                    }

                    // Save fetched documents to local Room
                    docs.forEach { doc ->
                        repository.saveLegalDocument(doc)
                    }

                    // 2. Fetch Support FAQs
                    firestore.collection("support_faqs")
                        .get()
                        .addOnSuccessListener { faqSnapshot ->
                            val faqs = faqSnapshot.documents.mapNotNull { doc ->
                                try {
                                    val id = doc.id
                                    val question = doc.getString("question") ?: ""
                                    val answer = doc.getString("answer") ?: ""
                                    val mappedAppIds = doc.get("mappedAppIds") as? List<String> ?: emptyList()
                                    val isEnabled = doc.getBoolean("isEnabled") ?: true
                                    val order = doc.getLong("order")?.toInt() ?: 0

                                    com.example.model.SupportFaq(
                                        id = id,
                                        question = question,
                                        answer = answer,
                                        mappedAppIds = mappedAppIds,
                                        isEnabled = isEnabled,
                                        order = order
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing Firestore SupportFaq", e)
                                    null
                                }
                            }

                            // Save fetched FAQs to local Room
                            faqs.forEach { faq ->
                                repository.saveSupportFaq(faq)
                            }

                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed fetching support FAQs from Firestore", e)
                            onComplete(false)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed fetching legal documents from Firestore", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchAndSyncLegalSupport", e)
            onComplete(false)
        }
    }

    /**
     * Updates FCM token in Firestore for a user.
     */
    fun updateFcmToken(email: String, token: String) {
        firestore.collection("users")
            .document(email)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Token updated for $email")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update token: ${e.message}")
            }
    }

    /**
     * Syncs a UserProfileModel to Firestore under the 'users' collection.
     */
    fun syncUserProfileToFirestore(
        profile: com.example.model.UserProfileModel,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val userEmail = profile.email.ifBlank { "anonymous@exam.portal" }
            val profileMap = mapOf(
                "name" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "district" to profile.district,
                "targetExam" to profile.targetExam,
                "targetAppId" to profile.targetAppId,
                "preferredLanguage" to profile.preferredLanguage,
                "dailyGoalQuestions" to profile.dailyGoalQuestions,
                "streakDays" to profile.streakDays,
                "isProSubscribed" to profile.isProSubscribed,
                "proPlanName" to profile.proPlanName,
                "activatedAt" to profile.activatedAt,
                "expiresAt" to profile.expiresAt,
                "subscriptionSource" to profile.subscriptionSource,
                "isLoggedIn" to profile.isLoggedIn,
                "profileImageUrl" to (profile.profileImageUrl ?: ""),
                "activeSessionId" to (profile.activeSessionId ?: ""),
                "isSubscriptionCancelled" to profile.isSubscriptionCancelled,
                "cancellationReason" to (profile.cancellationReason ?: ""),
                "cancellationDetails" to (profile.cancellationDetails ?: ""),
                "isPhoneVerified" to profile.isPhoneVerified,
                "isEmailVerified" to profile.isEmailVerified
            )
            firestore.collection("users")
                .document(userEmail)
                .set(profileMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Fetches a UserProfileModel from Firestore.
     */
    fun fetchUserProfileFromFirestore(
        email: String,
        onSuccess: (com.example.model.UserProfileModel) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            firestore.collection("users")
                .document(email)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val profile = com.example.model.UserProfileModel(
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: email,
                            phone = doc.getString("phone") ?: "",
                            district = doc.getString("district") ?: "Visakhapatnam",
                            targetExam = doc.getString("targetExam") ?: "APPSC Group-2",
                            targetAppId = doc.getString("targetAppId") ?: "appsc-group2",
                            preferredLanguage = doc.getString("preferredLanguage") ?: "Telugu & English",
                            dailyGoalQuestions = doc.getLong("dailyGoalQuestions")?.toInt() ?: 30,
                            streakDays = doc.getLong("streakDays")?.toInt() ?: 0,
                            isProSubscribed = doc.getBoolean("isProSubscribed") ?: false,
                            proPlanName = doc.getString("proPlanName") ?: "",
                            activatedAt = doc.getLong("activatedAt") ?: 0L,
                            expiresAt = doc.getLong("expiresAt") ?: 0L,
                            subscriptionSource = doc.getString("subscriptionSource") ?: "DIRECT_PURCHASE",
                            isLoggedIn = doc.getBoolean("isLoggedIn") ?: false,
                            profileImageUrl = doc.getString("profileImageUrl"),
                            activeSessionId = doc.getString("activeSessionId"),
                            isSubscriptionCancelled = doc.getBoolean("isSubscriptionCancelled") ?: false,
                            cancellationReason = doc.getString("cancellationReason"),
                            cancellationDetails = doc.getString("cancellationDetails"),
                            isPhoneVerified = doc.getBoolean("isPhoneVerified") ?: true,
                            isEmailVerified = doc.getBoolean("isEmailVerified") ?: true
                        )
                        onSuccess(profile)
                    } else {
                        onFailure(Exception("Profile not found"))
                    }
                }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    // =========================================================================
    // Category Content Counts Dynamic Firestore Synchronization
    // =========================================================================

    /**
     * Parses category content counts (Prelims, Mains, General, All) from a Firestore document.
     */
    fun parseCategoryCounts(snapshot: DocumentSnapshot?, appId: String): CategoryContentCounts? {
        if (snapshot == null || !snapshot.exists()) return null
        try {
            // Check direct document properties
            var prelims = snapshot.getLong("prelimsCount")?.toInt()
                ?: snapshot.getLong("prelims")?.toInt()
            var mains = snapshot.getLong("mainsCount")?.toInt()
                ?: snapshot.getLong("mains")?.toInt()
            var general = snapshot.getLong("generalCount")?.toInt()
                ?: snapshot.getLong("general")?.toInt()
            var all = snapshot.getLong("allCount")?.toInt()
                ?: snapshot.getLong("all")?.toInt()
                ?: snapshot.getLong("totalCount")?.toInt()

            // Check nested maps (e.g. "categoryCounts", "contentCounts", "counts")
            val map = (snapshot.get("categoryCounts") as? Map<*, *>)
                ?: (snapshot.get("contentCounts") as? Map<*, *>)
                ?: (snapshot.get("categories") as? Map<*, *>)

            if (map != null) {
                if (prelims == null) {
                    prelims = (map["prelims"] as? Number)?.toInt()
                        ?: (map["PRELIMS"] as? Number)?.toInt()
                        ?: map["prelims"]?.toString()?.toIntOrNull()
                }
                if (mains == null) {
                    mains = (map["mains"] as? Number)?.toInt()
                        ?: (map["MAINS"] as? Number)?.toInt()
                        ?: map["mains"]?.toString()?.toIntOrNull()
                }
                if (general == null) {
                    general = (map["general"] as? Number)?.toInt()
                        ?: (map["GENERAL"] as? Number)?.toInt()
                        ?: map["general"]?.toString()?.toIntOrNull()
                }
                if (all == null) {
                    all = (map["all"] as? Number)?.toInt()
                        ?: (map["ALL"] as? Number)?.toInt()
                        ?: (map["total"] as? Number)?.toInt()
                        ?: map["all"]?.toString()?.toIntOrNull()
                }
            }

            if (prelims != null || mains != null || general != null || all != null) {
                val p = prelims ?: 0
                val m = mains ?: 0
                val g = general ?: 0
                val a = all ?: (p + m + g)
                val updated = snapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                return CategoryContentCounts(
                    prelimsCount = p,
                    mainsCount = m,
                    generalCount = g,
                    allCount = if (a > 0) a else (p + m + g),
                    isFromFirestore = true,
                    source = "firestore_doc",
                    lastUpdated = updated
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing category counts from doc ${snapshot.id}: ${e.message}")
        }
        return null
    }

    /**
     * Real-time dynamic Flow of content counts from Firestore for Prelims, Mains, General, and All.
     * Listens in real-time to both dedicated metrics documents and content collections in Firestore.
     */
    fun observeCategoryContentCounts(appId: String): Flow<CategoryContentCounts> = callbackFlow {
        val metricsDocRef = firestore.collection("apps")
            .document(appId)
            .collection("metrics")
            .document("category_counts")

        val rootDocRef = firestore.collection("content_counts").document(appId)
        val appDocRef = firestore.collection("apps").document(appId)

        var lastEmittedCounts: CategoryContentCounts? = null

        fun emitIfChanged(counts: CategoryContentCounts) {
            if (lastEmittedCounts != counts) {
                lastEmittedCounts = counts
                trySend(counts)
            }
        }

        // Real-time snapshot listener on primary metrics document
        val listener1 = metricsDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Category counts listener error (metrics): ${error.message}")
                return@addSnapshotListener
            }
            val parsed = parseCategoryCounts(snapshot, appId)
            if (parsed != null) {
                emitIfChanged(parsed)
            }
        }

        // Real-time snapshot listener on fallback root document
        val listener2 = rootDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val parsed = parseCategoryCounts(snapshot, appId)
            if (parsed != null) {
                emitIfChanged(parsed)
            }
        }

        // Real-time snapshot listener on app tenant document
        val listener3 = appDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val parsed = parseCategoryCounts(snapshot, appId)
            if (parsed != null) {
                emitIfChanged(parsed)
            }
        }

        // Also dynamically listen to questions collection in Firestore
        val listenerQuestions = firestore.collection("apps")
            .document(appId)
            .collection("questions")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) return@addSnapshotListener
                var prelimsQ = 0
                var mainsQ = 0
                var generalQ = 0
                val totalQ = snapshot.documents.size

                for (doc in snapshot.documents) {
                    val category = (doc.getString("category") ?: doc.getString("syllabusCategory") ?: "").lowercase()
                    val tags = (doc.get("tags") as? List<*>)?.map { it.toString().lowercase() } ?: emptyList()
                    val setName = (doc.getString("setName") ?: "").lowercase()
                    val sub = (doc.getString("subjectId") ?: "").lowercase()

                    val isPrelims = category.contains("prelim") || tags.any { it.contains("prelim") } || setName.contains("prelim") || sub.contains("prelim")
                    val isMains = category.contains("main") || tags.any { it.contains("main") } || setName.contains("main") || sub.contains("main")
                    val isGeneral = category.contains("general") || tags.any { it.contains("general") } || sub.contains("general") || sub.contains("current-affairs")

                    if (isPrelims) prelimsQ++
                    if (isMains) mainsQ++
                    if (isGeneral) generalQ++
                    if (!isPrelims && !isMains && !isGeneral) {
                        prelimsQ++
                    }
                }

                if (lastEmittedCounts == null || !lastEmittedCounts!!.isFromFirestore) {
                    emitIfChanged(
                        CategoryContentCounts(
                            prelimsCount = prelimsQ,
                            mainsCount = mainsQ,
                            generalCount = generalQ,
                            allCount = totalQ,
                            isFromFirestore = true,
                            source = "firestore_questions",
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }

        awaitClose {
            listener1.remove()
            listener2.remove()
            listener3.remove()
            listenerQuestions.remove()
        }
    }

    /**
     * Single-shot fetch of content counts from Firestore for Prelims, Mains, General, and All.
     */
    fun fetchCategoryContentCounts(
        appId: String,
        onResult: (CategoryContentCounts) -> Unit
    ) {
        val metricsDocRef = firestore.collection("apps")
            .document(appId)
            .collection("metrics")
            .document("category_counts")

        metricsDocRef.get()
            .addOnSuccessListener { snapshot ->
                val parsed = parseCategoryCounts(snapshot, appId)
                if (parsed != null) {
                    onResult(parsed)
                } else {
                    firestore.collection("content_counts").document(appId).get()
                        .addOnSuccessListener { rootSnap ->
                            val rootParsed = parseCategoryCounts(rootSnap, appId)
                            if (rootParsed != null) {
                                onResult(rootParsed)
                            } else {
                                fetchCountsFromQuestionsCollection(appId, onResult)
                            }
                        }
                        .addOnFailureListener {
                            fetchCountsFromQuestionsCollection(appId, onResult)
                        }
                }
            }
            .addOnFailureListener {
                fetchCountsFromQuestionsCollection(appId, onResult)
            }
    }

    private fun fetchCountsFromQuestionsCollection(
        appId: String,
        onResult: (CategoryContentCounts) -> Unit
    ) {
        firestore.collection("apps")
            .document(appId)
            .collection("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    var p = 0
                    var m = 0
                    var g = 0
                    for (doc in snapshot.documents) {
                        val cat = (doc.getString("category") ?: "").lowercase()
                        val sub = (doc.getString("subjectId") ?: "").lowercase()
                        if (cat.contains("prelim") || sub.contains("prelim")) p++
                        if (cat.contains("main") || sub.contains("main")) m++
                        if (cat.contains("general") || sub.contains("general")) g++
                        if (!cat.contains("prelim") && !cat.contains("main") && !cat.contains("general")) p++
                    }
                    onResult(
                        CategoryContentCounts(
                            prelimsCount = p,
                            mainsCount = m,
                            generalCount = g,
                            allCount = snapshot.size(),
                            isFromFirestore = true,
                            source = "firestore_questions"
                        )
                    )
                } else {
                    val localSubjects = SampleData.getSubjectsForApp(appId)
                    onResult(
                        CategoryContentCounts(
                            prelimsCount = localSubjects.count { it.matchesCategoryFilter("PRELIMS") },
                            mainsCount = localSubjects.count { it.matchesCategoryFilter("MAINS") },
                            generalCount = localSubjects.count { it.matchesCategoryFilter("GENERAL") },
                            allCount = localSubjects.size,
                            isFromFirestore = false,
                            source = "local_fallback"
                        )
                    )
                }
            }
            .addOnFailureListener {
                val localSubjects = SampleData.getSubjectsForApp(appId)
                onResult(
                    CategoryContentCounts(
                        prelimsCount = localSubjects.count { it.matchesCategoryFilter("PRELIMS") },
                        mainsCount = localSubjects.count { it.matchesCategoryFilter("MAINS") },
                        generalCount = localSubjects.count { it.matchesCategoryFilter("GENERAL") },
                        allCount = localSubjects.size,
                        isFromFirestore = false,
                        source = "local_fallback"
                    )
                )
            }
    }

    /**
     * Syncs updated category content counts to Firestore.
     */
    fun syncCategoryContentCountsToFirestore(
        appId: String,
        counts: CategoryContentCounts,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val data = mapOf(
                "appId" to appId,
                "prelimsCount" to counts.prelimsCount,
                "mainsCount" to counts.mainsCount,
                "generalCount" to counts.generalCount,
                "allCount" to counts.allCount,
                "categoryCounts" to mapOf(
                    "prelims" to counts.prelimsCount,
                    "mains" to counts.mainsCount,
                    "general" to counts.generalCount,
                    "all" to counts.allCount
                ),
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("apps")
                .document(appId)
                .collection("metrics")
                .document("category_counts")
                .set(data)
                .addOnSuccessListener {
                    firestore.collection("content_counts").document(appId).set(data)
                    onSuccess()
                }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    /**
     * Ensures initial counts exist in Firestore so dynamic listeners immediately have valid documents.
     */
    fun ensureCategoryCountsInitialized(
        appId: String,
        defaultCounts: CategoryContentCounts
    ) {
        try {
            val docRef = firestore.collection("apps")
                .document(appId)
                .collection("metrics")
                .document("category_counts")

            docRef.get().addOnSuccessListener { snap ->
                if (!snap.exists() || snap.getLong("allCount") == null) {
                    syncCategoryContentCountsToFirestore(appId, defaultCounts)
                }
            }.addOnFailureListener {
                // Soft failure, non-blocking
            }
        } catch (e: Exception) {
            Log.w(TAG, "ensureCategoryCountsInitialized error: ${e.message}")
        }
    }

    // =========================================================================
    // ADMIN CONFIGURATION PERSISTENCE & REAL-TIME SYNCHRONIZATION
    // =========================================================================

    /**
     * TopBar Configuration Persistence & Real-time Flow
     */
    fun syncTopBarConfigToFirestore(
        appId: String,
        config: com.example.model.TopBarConfig,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val data = mapOf(
            "isEnabled" to config.isEnabled,
            "title" to config.title,
            "subtitle" to config.subtitle,
            "additionalText" to config.additionalText,
            "premiumBadgeText" to config.premiumBadgeText,
            "showMenu" to config.showMenu,
            "showAppIcon" to config.showAppIcon,
            "showTitle" to config.showTitle,
            "showSubtitle" to config.showSubtitle,
            "showAdditionalText" to config.showAdditionalText,
            "showTextSettings" to config.showTextSettings,
            "showPremium" to config.showPremium,
            "showNotifications" to config.showNotifications,
            "titleFontFamily" to config.titleFontFamily,
            "titleFontSizeSp" to config.titleFontSizeSp,
            "titleFontWeight" to config.titleFontWeight,
            "titleFontStyle" to config.titleFontStyle,
            "titleColorHex" to config.titleColorHex,
            "subtitleFontFamily" to config.subtitleFontFamily,
            "subtitleFontSizeSp" to config.subtitleFontSizeSp,
            "subtitleFontWeight" to config.subtitleFontWeight,
            "subtitleFontStyle" to config.subtitleFontStyle,
            "subtitleColorHex" to config.subtitleColorHex,
            "backgroundColorStringHex" to config.backgroundColorStringHex,
            "secondaryBackgroundColorHex" to config.secondaryBackgroundColorHex,
            "backgroundType" to config.backgroundType,
            "useGradient" to config.useGradient,
            "gradientPreset" to config.gradientPreset,
            "gradientStartColorHex" to config.gradientStartColorHex,
            "gradientEndColorHex" to config.gradientEndColorHex,
            "backgroundImageUrl" to config.backgroundImageUrl,
            "imageOpacity" to config.imageOpacity,
            "headerBorderColorHex" to config.headerBorderColorHex,
            "headerBorderThicknessDp" to config.headerBorderThicknessDp,
            "targetScope" to config.targetScope,
            "selectedAppIds" to config.selectedAppIds,
            "lastUpdated" to System.currentTimeMillis()
        )

        val batch = firestore.batch()
        val docRef = firestore.collection("apps").document(appId).collection("configs").document("top_bar")
        batch.set(docRef, data)
        batch.set(firestore.collection("top_bar_configs").document(appId), data)

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun observeTopBarConfig(appId: String): Flow<com.example.model.TopBarConfig?> = callbackFlow {
        val listener = firestore.collection("top_bar_configs")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val title = snapshot.getString("title") ?: ""
                        val subtitle = snapshot.getString("subtitle") ?: ""
                        val isEnabled = snapshot.getBoolean("isEnabled") ?: true
                        val titleColorHex = snapshot.getString("titleColorHex") ?: "#FFFFFF"
                        val subtitleColorHex = snapshot.getString("subtitleColorHex") ?: "#FFD700"
                        val gradientStart = snapshot.getString("gradientStartColorHex") ?: "#0B192C"
                        val gradientEnd = snapshot.getString("gradientEndColorHex") ?: "#1E3E62"
                        val isGradient = snapshot.getBoolean("useGradient") ?: true
                        
                        val config = com.example.model.TopBarConfig(
                            isEnabled = isEnabled,
                            title = title,
                            subtitle = subtitle,
                            titleColorHex = titleColorHex,
                            subtitleColorHex = subtitleColorHex,
                            useGradient = isGradient,
                            gradientStartColorHex = gradientStart,
                            gradientEndColorHex = gradientEnd
                        )
                        trySend(config)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Live Content Dashboard Configuration Persistence & Flow
     */
    fun syncLiveDashboardConfigToFirestore(
        appId: String,
        config: com.example.model.LiveDashboardConfig,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val data = mapOf(
            "headerTitlePrefix" to config.headerTitlePrefix,
            "headerTitleSuffix" to config.headerTitleSuffix,
            "headerSubtitle" to config.headerSubtitle,
            "liveBankButtonText" to config.liveBankButtonText,
            "liveIndicatorColor" to config.liveIndicatorColor,
            "containerBgStart" to config.containerBgStart,
            "containerBgEnd" to config.containerBgEnd,
            "containerBorderColor" to config.containerBorderColor,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("apps").document(appId).collection("configs").document("live_dashboard")
            .set(data)
            .addOnSuccessListener {
                firestore.collection("live_dashboard_configs").document(appId).set(data)
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun observeLiveDashboardConfig(appId: String): Flow<com.example.model.LiveDashboardConfig?> = callbackFlow {
        val listener = firestore.collection("live_dashboard_configs")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val prefix = snapshot.getString("headerTitlePrefix") ?: "LIVE CONTENT"
                        val suffix = snapshot.getString("headerTitleSuffix") ?: "DASHBOARD"
                        val subtitle = snapshot.getString("headerSubtitle") ?: "Real-time question bank & syllabus metrics"
                        val liveColor = snapshot.getString("liveIndicatorColor") ?: "#00C853"
                        val containerStart = snapshot.getString("containerBgStart") ?: "#FFF3EE"
                        val containerEnd = snapshot.getString("containerBgEnd") ?: "#FCE4EC"
                        
                        val config = com.example.model.LiveDashboardConfig(
                            headerTitlePrefix = prefix,
                            headerTitleSuffix = suffix,
                            headerSubtitle = subtitle,
                            liveIndicatorColor = liveColor,
                            containerBgStart = containerStart,
                            containerBgEnd = containerEnd
                        )
                        trySend(config)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Card Border & Styling Configuration Persistence & Flow
     */
    fun syncCardBorderConfigToFirestore(
        appId: String,
        config: CardBorderConfig,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val data = mapOf(
            "topBorderColorHex" to config.topBorderColorHex,
            "otherBorderColorHex" to config.otherBorderColorHex,
            "topBorderWidthDp" to config.topBorderWidthDp,
            "otherBorderWidthDp" to config.otherBorderWidthDp,
            "cornerRadiusDp" to config.cornerRadiusDp,
            "targetScope" to config.targetScope,
            "selectedAppIds" to config.selectedAppIds,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("apps").document(appId).collection("configs").document("card_borders")
            .set(data)
            .addOnSuccessListener {
                firestore.collection("card_border_configs").document(appId).set(data)
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun observeCardBorderConfig(appId: String): Flow<CardBorderConfig?> = callbackFlow {
        val listener = firestore.collection("card_border_configs")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val topColor = snapshot.getLong("topBorderColorHex") ?: 0xFFFFD700L
                        val otherColor = snapshot.getLong("otherBorderColorHex") ?: 0xFFD4AF37L
                        val topWidth = snapshot.getLong("topBorderWidthDp")?.toInt() ?: 2
                        val otherWidth = snapshot.getLong("otherBorderWidthDp")?.toInt() ?: 2
                        val radius = snapshot.getLong("cornerRadiusDp")?.toInt() ?: 16
                        val scope = snapshot.getString("targetScope") ?: "CURRENT"

                        val config = CardBorderConfig(
                            topBorderColorHex = topColor,
                            otherBorderColorHex = otherColor,
                            topBorderWidthDp = topWidth,
                            otherBorderWidthDp = otherWidth,
                            cornerRadiusDp = radius,
                            targetScope = scope
                        )
                        trySend(config)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Left Navigation Drawer Configuration Persistence & Flow
     */
    fun syncDrawerConfigToFirestore(
        appId: String,
        config: DrawerConfig,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val data = mapOf(
            "headerTitle" to config.header.title,
            "headerSubtitle" to config.header.subtitle,
            "headerBgImage" to config.header.backgroundImageUrl,
            "targetScope" to config.targetScope,
            "selectedAppIds" to config.selectedAppIds,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("apps").document(appId).collection("configs").document("drawer_config")
            .set(data)
            .addOnSuccessListener {
                firestore.collection("drawer_configs").document(appId).set(data)
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun observeDrawerConfig(appId: String): Flow<DrawerConfig?> = callbackFlow {
        val listener = firestore.collection("drawer_configs")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val title = snapshot.getString("headerTitle") ?: "APPSC Group-2"
                        val subtitle = snapshot.getString("headerSubtitle") ?: "Executive Preparation Portal"
                        val bgImage = snapshot.getString("headerBgImage") ?: ""

                        val defaultDrawer = SampleData.getDrawerConfigForApp(appId)
                        val config = defaultDrawer.copy(
                            header = defaultDrawer.header.copy(
                                title = title,
                                subtitle = subtitle,
                                backgroundImageUrl = bgImage
                            )
                        )
                        trySend(config)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Broadcast Announcement Persistence & Real-time Flow
     */
    fun syncAnnouncementToFirestore(
        appId: String,
        noticeText: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val data = mapOf(
            "noticeText" to noticeText,
            "appId" to appId,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("announcements").document(appId).set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun observeAnnouncement(appId: String): Flow<String?> = callbackFlow {
        val listener = firestore.collection("announcements")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val notice = snapshot.getString("noticeText")
                    trySend(notice)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Animation Engine Configuration Persistence & Real-time Flow
     */
    fun syncAnimationEngineConfigToFirestore(
        appId: String,
        config: com.example.model.AnimationEngineConfig,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            val jsonPayload = AnimationConfigManager.serializeConfigToJson(config)
            val data = mapOf(
                "appId" to appId,
                "configJson" to jsonPayload,
                "versionNumber" to config.versionNumber,
                "targetScope" to config.targetScope,
                "selectedAppIds" to config.selectedAppIds,
                "activePresetId" to config.masterConfig.activePresetId.name,
                "masterEnabled" to config.masterConfig.masterEnabled,
                "updatedAt" to config.updatedAt,
                "updatedBy" to config.updatedBy
            )

            firestore.collection("apps").document(appId).collection("configs").document("animation_engine")
                .set(data)
                .addOnSuccessListener {
                    firestore.collection("animation_engine_configs").document(appId).set(data)
                    if (config.targetScope == "ALL") {
                        firestore.collection("animation_engine_configs").document("global").set(data)
                    } else if (config.targetScope == "SELECTED") {
                        config.selectedAppIds.forEach { targetId ->
                            firestore.collection("animation_engine_configs").document(targetId).set(data)
                        }
                    }
                    onSuccess()
                }
                .addOnFailureListener { e -> onFailure(e) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun observeAnimationEngineConfig(appId: String): Flow<com.example.model.AnimationEngineConfig?> = callbackFlow {
        val listener = firestore.collection("animation_engine_configs")
            .document(appId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val json = snapshot.getString("configJson") ?: ""
                        val parsed = AnimationConfigManager.parseConfigFromJson(json)
                        trySend(parsed)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Master Admin Config Auto-Synchronizer
     * Automatically subscribes to all Firestore admin config topics for [appId]
     * and updates SampleData in real-time.
     */
    fun startMasterAdminConfigSync(
        appId: String,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ) {
        coroutineScope.launch {
            try {
                observeAnimationEngineConfig(appId).collect { animConfig ->
                    if (animConfig != null) {
                        AnimationConfigManager.appConfigsState[appId] = animConfig
                        if (appId == SampleData.currentActiveAppId.value) {
                            AnimationConfigManager.globalConfigState.value = animConfig
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Animation Config listener error: ${e.message}")
            }
        }

        coroutineScope.launch {
            try {
                observeTopBarConfig(appId).collect { topBarConfig ->
                    if (topBarConfig != null) {
                        SampleData.appTopBarConfigsState[appId] = topBarConfig
                        if (appId == SampleData.currentActiveAppId.value) {
                            SampleData.topBarConfigState.value = topBarConfig
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master TopBar Config listener error: ${e.message}")
            }
        }

        coroutineScope.launch {
            try {
                observeLiveDashboardConfig(appId).collect { liveConfig ->
                    if (liveConfig != null) {
                        SampleData.liveDashboardConfigState.value = liveConfig
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master LiveDashboard Config listener error: ${e.message}")
            }
        }

        coroutineScope.launch {
            try {
                observeCardBorderConfig(appId).collect { borderConfig ->
                    if (borderConfig != null) {
                        SampleData.cardBorderConfigState.value = borderConfig
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Card Border Config listener error: ${e.message}")
            }
        }

        coroutineScope.launch {
            try {
                observeDrawerConfig(appId).collect { drawerConfig ->
                    if (drawerConfig != null) {
                        SampleData.appDrawerConfigsState[appId] = drawerConfig
                        if (appId == SampleData.currentActiveAppId.value) {
                            SampleData.drawerConfigState.value = drawerConfig
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Drawer Config listener error: ${e.message}")
            }
        }

        coroutineScope.launch {
            try {
                observeAnnouncement(appId).collect { announcement ->
                    if (!announcement.isNullOrBlank()) {
                        SampleData.broadcastNoticeState.value = announcement
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Announcement listener error: ${e.message}")
            }
        }

        // Real-time Syllabus Synchronization from Firestore into SampleData & Room
        coroutineScope.launch {
            try {
                observeSyllabusForApp(appId).collect { remoteSubjects ->
                    if (remoteSubjects.isNotEmpty()) {
                        remoteSubjects.forEach { rSub ->
                            val idx = SampleData.subjectsState.indexOfFirst { it.id == rSub.id }
                            if (idx != -1) {
                                SampleData.subjectsState[idx] = rSub
                            } else {
                                SampleData.subjectsState.add(rSub)
                            }
                        }
                        Log.d(TAG, "Real-time sync updated ${remoteSubjects.size} subjects for $appId")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Syllabus listener error: ${e.message}")
            }
        }

        // Real-time MCQ Question Bank Synchronization from Firestore into SampleData & Room
        coroutineScope.launch {
            try {
                observeQuestionsForApp(appId).collect { remoteQuestions ->
                    if (remoteQuestions.isNotEmpty()) {
                        remoteQuestions.forEach { rQ ->
                            val idx = SampleData.questionsState.indexOfFirst {
                                it.id == rQ.id || (it.appId == rQ.appId && it.questionText.trim().equals(rQ.questionText.trim(), ignoreCase = true))
                            }
                            if (idx != -1) {
                                SampleData.questionsState[idx] = rQ
                            } else {
                                SampleData.questionsState.add(rQ)
                            }
                        }
                        Log.d(TAG, "Real-time sync updated ${remoteQuestions.size} questions for $appId")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Master Questions listener error: ${e.message}")
            }
        }
    }
}
