package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.model.PracticeSetModel
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel and Singleton state manager for active practice sessions,
 * syllabus hierarchies, question indices, and test evaluations.
 */
class PracticeStateViewModel : ViewModel() {

    // Current Selection Context
    var currentSubject by mutableStateOf<Subject?>(null)
        private set
    var currentUnit by mutableStateOf<UnitModel?>(null)
        private set
    var currentTopic by mutableStateOf<TopicModel?>(null)
        private set
    var currentSubtopic by mutableStateOf<String?>(null)
        private set
    var currentSet by mutableStateOf<PracticeSetModel?>(null)
        private set
    var currentContextSets by mutableStateOf<List<PracticeSetModel>>(emptyList())
        private set
    var currentSetIndex by mutableIntStateOf(0)
        private set
    var currentQuestionIndex by mutableIntStateOf(0)
        private set

    // Active Practice Arena Context
    var activePracticeId by mutableStateOf("practice-session-default")
        private set
    var activePracticeTitle by mutableStateOf("Practice Arena")
        private set
    var activePracticeQuestions by mutableStateOf<List<Question>>(emptyList())
        private set
    var activePracticeShowCelebration by mutableStateOf(false)
        private set
    var selectedMockTestId by mutableStateOf<String?>(null)
        private set

    // Test Results State
    var testScore by mutableDoubleStateOf(0.0)
        private set
    var testCorrect by mutableIntStateOf(0)
        private set
    var testWrong by mutableIntStateOf(0)
        private set
    var testUserAnswers by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    // Reactive StateFlow for state observers
    private val _practiceState = MutableStateFlow(PracticeState())
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    data class PracticeState(
        val currentSubject: Subject? = null,
        val currentUnit: UnitModel? = null,
        val currentTopic: TopicModel? = null,
        val currentSubtopic: String? = null,
        val currentSet: PracticeSetModel? = null,
        val currentContextSets: List<PracticeSetModel> = emptyList(),
        val currentSetIndex: Int = 0,
        val currentQuestionIndex: Int = 0,
        val activePracticeId: String = "practice-session-default",
        val activePracticeTitle: String = "Practice Arena",
        val activePracticeQuestions: List<Question> = emptyList(),
        val activePracticeShowCelebration: Boolean = false,
        val selectedMockTestId: String? = null
    )

    private fun updateStateFlow() {
        _practiceState.value = PracticeState(
            currentSubject = currentSubject,
            currentUnit = currentUnit,
            currentTopic = currentTopic,
            currentSubtopic = currentSubtopic,
            currentSet = currentSet,
            currentContextSets = currentContextSets,
            currentSetIndex = currentSetIndex,
            currentQuestionIndex = currentQuestionIndex,
            activePracticeId = activePracticeId,
            activePracticeTitle = activePracticeTitle,
            activePracticeQuestions = activePracticeQuestions,
            activePracticeShowCelebration = activePracticeShowCelebration,
            selectedMockTestId = selectedMockTestId
        )
    }

    /**
     * Resets all practice and syllabus context.
     * Clears currentSet, currentSubtopic, currentQuestionIndex, and all transient practice data.
     */
    fun resetPracticeContext() {
        currentSubject = null
        currentUnit = null
        currentTopic = null
        currentSubtopic = null
        currentSet = null
        currentContextSets = emptyList()
        currentSetIndex = 0
        currentQuestionIndex = 0
        activePracticeQuestions = emptyList()
        activePracticeTitle = "Practice Arena"
        activePracticeShowCelebration = false
        activePracticeId = "practice-session-default"
        selectedMockTestId = null
        testScore = 0.0
        testCorrect = 0
        testWrong = 0
        testUserAnswers = emptyMap()
        updateStateFlow()
    }

    fun setSubject(subject: Subject?) {
        currentSubject = subject
        currentUnit = null
        currentTopic = null
        currentSubtopic = null
        updateStateFlow()
    }

    fun setUnit(unit: UnitModel?) {
        currentUnit = unit
        currentTopic = null
        currentSubtopic = null
        updateStateFlow()
    }

    fun setTopic(topic: TopicModel?) {
        currentTopic = topic
        currentSubtopic = topic?.subtopics?.firstOrNull()
        updateStateFlow()
    }

    fun setSubtopic(subtopic: String?) {
        currentSubtopic = subtopic
        updateStateFlow()
    }

    fun setHierarchy(
        subject: Subject?,
        unit: UnitModel? = null,
        topic: TopicModel? = null,
        subtopic: String? = null
    ) {
        currentSubject = subject
        currentUnit = unit
        currentTopic = topic
        currentSubtopic = subtopic ?: topic?.subtopics?.firstOrNull()
        updateStateFlow()
    }

    fun setPracticeSet(
        set: PracticeSetModel?,
        allSets: List<PracticeSetModel> = emptyList(),
        index: Int = 0
    ) {
        currentSet = set
        currentContextSets = allSets
        currentSetIndex = if (set != null && allSets.isNotEmpty()) {
            allSets.indexOfFirst { it.id == set.id }.coerceAtLeast(0)
        } else index
        updateStateFlow()
    }

    fun setQuestionIndex(index: Int) {
        currentQuestionIndex = index.coerceAtLeast(0)
        updateStateFlow()
    }

    fun startPracticeSession(
        id: String,
        title: String,
        questions: List<Question>,
        showCelebration: Boolean = false,
        initialIndex: Int = 0
    ) {
        activePracticeId = id
        activePracticeTitle = title
        activePracticeQuestions = questions
        activePracticeShowCelebration = showCelebration
        currentQuestionIndex = initialIndex
        updateStateFlow()
    }

    fun setMockTest(mockTestId: String?) {
        selectedMockTestId = mockTestId
        updateStateFlow()
    }

    fun setTestEvaluation(
        score: Double,
        correct: Int,
        wrong: Int,
        userAnswers: Map<String, String>
    ) {
        testScore = score
        testCorrect = correct
        testWrong = wrong
        testUserAnswers = userAnswers
    }

    companion object {
        @Volatile
        private var instance: PracticeStateViewModel? = null

        fun getInstance(): PracticeStateViewModel {
            return instance ?: synchronized(this) {
                instance ?: PracticeStateViewModel().also { instance = it }
            }
        }
    }
}
