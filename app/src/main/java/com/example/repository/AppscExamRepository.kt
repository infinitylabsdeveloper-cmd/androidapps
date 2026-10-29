package com.example.repository

import com.example.db.AppDatabase
import com.example.db.BookmarkEntity
import com.example.db.GeneratedMockTestEntity
import com.example.db.QuestionEntity
import com.example.db.TestResultEntity
import com.example.db.toDomain
import com.example.db.toEntity
import com.example.model.CurrentAffair
import com.example.model.GeneratedMockTest
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TestResult
import com.example.model.UserProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Primary Repository for Exam Content, User Progress, and Mock Test Persistence.
 * Built with Room Database, Coroutines, and Flow, following the AppConfiguration architecture.
 */
class AppRepository(
    private val database: AppDatabase,
    val appId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val testResultDao = database.testResultDao()
    private val bookmarkDao = database.bookmarkDao()
    private val mockTestDao = database.generatedMockTestDao()
    private val questionDao = database.questionDao()
    private val userProgressDao = database.userProgressDao()
    private val legalSupportDao = database.legalSupportDao()
    private val subjectDao = database.subjectDao()
    private val currentAffairsDao = database.currentAffairsDao()

    val userProgressRepository = UserProgressRepository(database, ioDispatcher)

    /**
     * Real-time Room Flow of all cached Subjects for this app.
     */
    val cachedSubjectsFlow: StateFlow<List<Subject>> = subjectDao.getSubjectsForApp(appId)
        .map { list -> list.map { it.toDomain() } }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of all cached Current Affairs for this app.
     */
    val cachedCurrentAffairsFlow: StateFlow<List<CurrentAffair>> = currentAffairsDao.getAllCurrentAffairs(appId)
        .map { list -> list.map { it.toDomain() } }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Caches a list of Subjects into Room Database to avoid redundant API fetches.
     */
    suspend fun cacheSubjects(subjects: List<Subject>) = withContext(ioDispatcher) {
        val entities = subjects.map { it.toEntity() }
        subjectDao.insertSubjects(entities)
    }

    /**
     * Caches a single Subject into Room.
     */
    suspend fun cacheSubject(subject: Subject) = withContext(ioDispatcher) {
        subjectDao.insertSubject(subject.toEntity())
    }

    /**
     * Deletes a cached Subject from Room.
     */
    suspend fun deleteCachedSubject(subjectId: String) = withContext(ioDispatcher) {
        subjectDao.deleteSubjectById(subjectId)
    }

    /**
     * Caches a list of Current Affairs into Room Database.
     */
    suspend fun cacheCurrentAffairs(items: List<CurrentAffair>) = withContext(ioDispatcher) {
        val entities = items.map { it.toEntity() }
        currentAffairsDao.insertCurrentAffairs(entities)
    }

    /**
     * Caches a single Current Affair into Room.
     */
    suspend fun cacheCurrentAffair(item: CurrentAffair) = withContext(ioDispatcher) {
        currentAffairsDao.insertCurrentAffair(item.toEntity())
    }

    /**
     * Deletes a cached Current Affair from Room.
     */
    suspend fun deleteCachedCurrentAffair(id: String) = withContext(ioDispatcher) {
        currentAffairsDao.deleteById(id)
    }

    /**
     * Synchronizes initial cache if database is empty, loading from bundled/sample data
     * so that immediate and offline access has full data without requiring API calls.
     */
    suspend fun syncInitialCacheIfEmpty(
        subjects: List<Subject>,
        currentAffairs: List<CurrentAffair>
    ) = withContext(ioDispatcher) {
        val hasSubjects = subjectDao.getSubjectsForApp(appId).firstOrNull()?.isNotEmpty() == true
        if (!hasSubjects && subjects.isNotEmpty()) {
            val entities = subjects.map { it.toEntity() }
            subjectDao.insertSubjects(entities)
        }
        val hasCurrentAffairs = currentAffairsDao.getAllCurrentAffairs(appId).firstOrNull()?.isNotEmpty() == true
        if (!hasCurrentAffairs && currentAffairs.isNotEmpty()) {
            val entities = currentAffairs.map { it.toEntity() }
            currentAffairsDao.insertCurrentAffairs(entities)
        }
    }

    /**
     * Real-time Room Flow of all Legal Documents.
     */
    val allLegalDocuments: StateFlow<List<com.example.model.LegalDocument>> = legalSupportDao.getAllDocumentsFlow()
        .map { list ->
            list.map { it.toDomain() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of all Support FAQs.
     */
    val allFaqs: StateFlow<List<com.example.model.SupportFaq>> = legalSupportDao.getAllFaqsFlow()
        .map { list ->
            list.map { it.toDomain() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Saves or updates a Legal Document.
     */
    fun saveLegalDocument(doc: com.example.model.LegalDocument) {
        repositoryScope.launch(ioDispatcher) {
            legalSupportDao.upsertDocument(doc.toEntity())
        }
    }

    /**
     * Deletes a Legal Document.
     */
    fun deleteLegalDocument(id: String) {
        repositoryScope.launch(ioDispatcher) {
            legalSupportDao.deleteDocumentById(id)
        }
    }

    /**
     * Saves or updates a Support FAQ.
     */
    fun saveSupportFaq(faq: com.example.model.SupportFaq) {
        repositoryScope.launch(ioDispatcher) {
            legalSupportDao.upsertFaq(faq.toEntity())
        }
    }

    /**
     * Deletes a Support FAQ.
     */
    fun deleteSupportFaq(id: String) {
        repositoryScope.launch(ioDispatcher) {
            legalSupportDao.deleteFaqById(id)
        }
    }

    /**
     * Real-time Room Flow of all recorded test results for this app/exam.
     */
    val allTestResults: StateFlow<List<TestResult>> = testResultDao.getAllResults(appId)
        .map { list ->
            list.map { it.toDomainTestResult() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of all bookmarked questions for this app/exam.
     */
    val allBookmarks: StateFlow<List<Question>> = bookmarkDao.getAllBookmarks(appId)
        .map { list ->
            list.map { it.toDomainQuestion() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of user-generated mock tests.
     */
    val allMockTests: StateFlow<List<GeneratedMockTest>> = mockTestDao.getAllMockTests(appId)
        .map { list ->
            list.map { it.toDomainModel() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of all questions in the local bank.
     */
    val allQuestions: StateFlow<List<Question>> = questionDao.getAllQuestions(appId)
        .map { list ->
            list.map { it.toDomainQuestion() }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Real-time Room Flow of User Progress & Streaks.
     */
    val userProgress: StateFlow<UserProgress> = userProgressRepository.getUserProgress(appId)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = UserProgress(appId = appId)
        )

    /**
     * Saves questions into the Room question bank.
     */
    fun saveQuestions(questions: List<Question>) {
        repositoryScope.launch(ioDispatcher) {
            val entities = questions.map { it.toEntity(appId) }
            questionDao.insertQuestions(entities)
        }
    }

    /**
     * Retrieves current count of bookmarks.
     */
    fun getBookmarkCount(): Int {
        return allBookmarks.value.size
    }

    /**
     * Adds a question to bookmarks in Room database.
     */
    fun addBookmark(question: Question) {
        repositoryScope.launch(ioDispatcher) {
            bookmarkDao.insertBookmark(question.toBookmarkEntity(appId))
        }
    }

    /**
     * Removes a bookmark by question ID from Room database.
     */
    fun removeBookmark(questionId: String) {
        repositoryScope.launch(ioDispatcher) {
            bookmarkDao.deleteBookmarkById(questionId, appId)
        }
    }

    /**
     * Toggles bookmark state for a question in Room database.
     */
    fun toggleBookmark(question: Question) {
        repositoryScope.launch(ioDispatcher) {
            if (bookmarkDao.isBookmarked(question.id, appId)) {
                bookmarkDao.deleteBookmarkById(question.id, appId)
            } else {
                bookmarkDao.insertBookmark(question.toBookmarkEntity(appId))
            }
        }
    }

    /**
     * Synchronous check against in-memory state or suspend check against Room.
     */
    fun isBookmarked(questionId: String): Boolean {
        return allBookmarks.value.any { it.id == questionId }
    }

    suspend fun isBookmarkedSuspend(questionId: String): Boolean = withContext(ioDispatcher) {
        bookmarkDao.isBookmarked(questionId, appId)
    }

    /**
     * Persists Mock Test Result into Room Database and updates User Progress.
     */
    fun saveTestResult(
        testTitle: String,
        totalQuestions: Int,
        correctCount: Int,
        wrongCount: Int,
        unansweredCount: Int,
        percentage: Float,
        accuracyStr: String,
        timeSeconds: Int,
        userAnswers: Map<String, String> = emptyMap(),
        testType: String = "MOCK_TEST"
    ) {
        repositoryScope.launch(ioDispatcher) {
            val entity = TestResultEntity(
                appId = appId,
                testTitle = testTitle,
                testType = testType,
                totalQuestions = totalQuestions,
                correctAnswers = correctCount,
                wrongAnswers = wrongCount,
                unanswered = unansweredCount,
                score = correctCount.toDouble(),
                scorePercentage = percentage,
                accuracy = accuracyStr,
                timeTakenSeconds = timeSeconds,
                timestamp = System.currentTimeMillis(),
                userAnswers = userAnswers
            )
            testResultDao.insertResult(entity)

            // Update user progress metrics
            userProgressRepository.recordMockTestCompletion(
                appId = appId,
                scorePercentage = percentage,
                questionsCount = totalQuestions,
                correctCount = correctCount,
                timeSpentSeconds = timeSeconds
            )
        }
    }

    /**
     * Clears all test results history for this app.
     */
    fun clearTestHistory() {
        repositoryScope.launch(ioDispatcher) {
            testResultDao.clearHistory(appId)
        }
    }

    /**
     * Deletes a generated mock test from Room.
     */
    fun deleteMockTest(testId: String) {
        repositoryScope.launch(ioDispatcher) {
            mockTestDao.deleteMockTestById(testId)
        }
    }

    /**
     * Saves a generated mock test into Room.
     */
    fun saveMockTest(test: GeneratedMockTest) {
        repositoryScope.launch(ioDispatcher) {
            mockTestDao.insertMockTest(test.toEntity())
        }
    }

    /**
     * Records practice question answer to update streaks and XP.
     */
    fun recordQuestionAttempt(isCorrect: Boolean, timeSpentSeconds: Int = 20) {
        repositoryScope.launch(ioDispatcher) {
            userProgressRepository.recordQuestionAnswered(
                appId = appId,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpentSeconds
            )
        }
    }
}

// Full backward compatibility type alias
typealias AppscExamRepository = AppRepository

// Model to Entity Mappers
fun TestResultEntity.toDomainTestResult(): TestResult {
    val dateStr = try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Recent"
    }

    return TestResult(
        id = id.toString(),
        testName = testTitle,
        totalQuestions = totalQuestions,
        score = correctAnswers,
        percentage = scorePercentage.toInt(),
        accuracy = accuracy,
        date = dateStr
    )
}

fun BookmarkEntity.toDomainQuestion(): Question {
    val optionsMap = mutableMapOf<String, String>()
    if (optionA.isNotBlank()) optionsMap["A"] = optionA
    if (optionB.isNotBlank()) optionsMap["B"] = optionB
    if (optionC.isNotBlank()) optionsMap["C"] = optionC
    if (optionD.isNotBlank()) optionsMap["D"] = optionD

    return Question(
        id = id,
        appId = appId,
        subjectId = subjectId,
        unitId = unitId,
        topicId = topicId,
        subtopic = subtopic,
        questionText = questionText,
        options = optionsMap,
        correctOption = correctOption,
        explanation = explanation
    )
}

fun Question.toBookmarkEntity(appId: String): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        appId = appId,
        subjectId = subjectId,
        unitId = unitId,
        topicId = topicId,
        subtopic = subtopic,
        questionText = questionText,
        optionA = options["A"] ?: "",
        optionB = options["B"] ?: "",
        optionC = options["C"] ?: "",
        optionD = options["D"] ?: "",
        correctOption = correctOption,
        explanation = explanation ?: "",
        bookmarkedAt = System.currentTimeMillis()
    )
}

fun Question.toEntity(appId: String): QuestionEntity {
    return QuestionEntity(
        id = id,
        appId = appId,
        subjectId = subjectId,
        unitId = unitId,
        topicId = topicId,
        subtopic = subtopic,
        questionText = questionText,
        options = options,
        correctOption = correctOption,
        explanation = explanation,
        setName = setName,
        difficulty = difficulty,
        language = language,
        imageUrl = imageUrl,
        year = year,
        exam = exam,
        marks = marks,
        negativeMarks = negativeMarks
    )
}

fun QuestionEntity.toDomainQuestion(): Question {
    return Question(
        id = id,
        appId = appId,
        subjectId = subjectId,
        unitId = unitId,
        topicId = topicId,
        subtopic = subtopic,
        questionText = questionText,
        options = options,
        correctOption = correctOption,
        explanation = explanation,
        setName = setName,
        difficulty = difficulty,
        language = language,
        imageUrl = imageUrl,
        year = year,
        exam = exam,
        marks = marks,
        negativeMarks = negativeMarks
    )
}

fun GeneratedMockTest.toEntity(): GeneratedMockTestEntity {
    return GeneratedMockTestEntity(
        id = id,
        appId = appId,
        title = title,
        createdAt = createdAt,
        subjectConfigs = subjectConfigs,
        totalQuestions = totalQuestions,
        positiveMarks = positiveMarks,
        negativeMarks = negativeMarks,
        durationMinutes = durationMinutes,
        questions = questions,
        examType = examType,
        difficulty = difficulty,
        selectionMode = selectionMode,
        totalMarks = totalMarks,
        status = status,
        score = score,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        unanswered = unanswered,
        timeTakenSeconds = timeTakenSeconds,
        percentage = percentage,
        userAnswers = userAnswers,
        testMode = testMode.name,
        markedForReview = markedForReview,
        skippedQuestions = skippedQuestions,
        currentIndex = currentIndex,
        remainingSeconds = remainingSeconds,
        attemptsHistory = attemptsHistory
    )
}

fun GeneratedMockTestEntity.toDomainModel(): GeneratedMockTest {
    return GeneratedMockTest(
        id = id,
        appId = appId,
        title = title,
        createdAt = createdAt,
        subjectConfigs = subjectConfigs,
        totalQuestions = totalQuestions,
        positiveMarks = positiveMarks,
        negativeMarks = negativeMarks,
        durationMinutes = durationMinutes,
        questions = questions,
        examType = examType,
        difficulty = difficulty,
        selectionMode = selectionMode,
        totalMarks = if (totalMarks > 0) totalMarks else totalQuestions * positiveMarks,
        status = status,
        score = score,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        unanswered = unanswered,
        timeTakenSeconds = timeTakenSeconds,
        percentage = percentage,
        userAnswers = userAnswers,
        testMode = com.example.model.MockTestMode.valueOf(testMode),
        markedForReview = markedForReview,
        skippedQuestions = skippedQuestions,
        currentIndex = currentIndex,
        remainingSeconds = remainingSeconds,
        attemptsHistory = attemptsHistory
    )
}

// Legal & Support Mappers
fun com.example.model.LegalDocument.toEntity(): com.example.db.LegalDocumentEntity {
    return com.example.db.LegalDocumentEntity(
        id = id,
        type = type.name,
        title = title,
        content = content,
        mappedAppIds = mappedAppIds,
        isEnabled = isEnabled,
        displayOrder = order,
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
}

fun com.example.db.LegalDocumentEntity.toDomain(): com.example.model.LegalDocument {
    return com.example.model.LegalDocument(
        id = id,
        type = com.example.model.LegalDocType.values().find { it.name == type } ?: com.example.model.LegalDocType.PRIVACY_POLICY,
        title = title,
        content = content,
        mappedAppIds = mappedAppIds,
        isEnabled = isEnabled,
        order = displayOrder,
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
}

fun com.example.model.SupportFaq.toEntity(): com.example.db.SupportFaqEntity {
    return com.example.db.SupportFaqEntity(
        id = id,
        question = question,
        answer = answer,
        mappedAppIds = mappedAppIds,
        isEnabled = isEnabled,
        displayOrder = order
    )
}

fun com.example.db.SupportFaqEntity.toDomain(): com.example.model.SupportFaq {
    return com.example.model.SupportFaq(
        id = id,
        question = question,
        answer = answer,
        mappedAppIds = mappedAppIds,
        isEnabled = isEnabled,
        order = displayOrder
    )
}
