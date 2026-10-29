package com.example

import com.example.data.SampleData
import com.example.ui.screens.generateSetsForQuestionsList
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying the 5-level full-screen learning hierarchy routes and data resolution:
 * 1. Subjects (Home) -> Units (SyllabusExplorer)
 * 2. Units (SyllabusExplorer) -> Topics (UnitTopics)
 * 3. Topics (UnitTopics) -> Sets (TopicSets)
 * 4. Sets (TopicSets) -> Subtopics (SetSubtopics)
 * 5. Subtopics (SetSubtopics) -> Content/Detail (PracticeArena)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hierarchyNavigationRoutes_haveDistinctStableRouteKeys() {
        val subject = SampleData.subjectsState.first()
        val unit = subject.units.first()
        val topic = unit.topics.first()
        val questions = SampleData.getQuestionsForPractice(subject.id, unit.id, topic.id, "")
        val sets = generateSetsForQuestionsList(
            questions = questions.ifEmpty { SampleData.questionsState.take(10) },
            baseId = "set-${subject.id}-${unit.id}-${topic.id}-all"
        )
        val firstSet = sets.first()
        val firstSubtopic = topic.subtopics.firstOrNull() ?: "Core Concepts"

        val homeScreen = AppScreen.Home
        val unitsScreen = AppScreen.SyllabusExplorer(subject.id, subject)
        val topicsScreen = AppScreen.UnitTopics(subject.id, unit.id, subject, unit)
        val setsScreen = AppScreen.TopicSets(subject.id, unit.id, topic.id, subject, unit, topic)
        val subtopicsScreen = AppScreen.SetSubtopics(
            subjectId = subject.id,
            unitId = unit.id,
            topicId = topic.id,
            setId = firstSet.id,
            setNumber = firstSet.setNumber,
            setTitle = firstSet.title,
            fallbackSubject = subject,
            fallbackUnit = unit,
            fallbackTopic = topic
        )
        val practiceDetailScreen = AppScreen.PracticeArena(
            subjectId = subject.id,
            unitId = unit.id,
            topicId = topic.id,
            setName = firstSubtopic,
            setId = firstSet.id,
            subtopicId = "subtopic-${firstSet.id}-1",
            subtopicTitle = firstSubtopic,
            preloadedQuestions = firstSet.questions
        )

        val routeKeys = listOf(
            homeScreen.routeKey,
            unitsScreen.routeKey,
            topicsScreen.routeKey,
            setsScreen.routeKey,
            subtopicsScreen.routeKey,
            practiceDetailScreen.routeKey
        )

        // All 6 hierarchy destinations must have unique, non-blank route keys
        assertEquals(6, routeKeys.distinct().size)
        assertTrue(routeKeys.all { it.isNotBlank() })
        assertEquals("units_${subject.id}", unitsScreen.routeKey)
        assertEquals("topics_${subject.id}_${unit.id}", topicsScreen.routeKey)
        assertEquals("sets_${subject.id}_${unit.id}_${topic.id}", setsScreen.routeKey)
        assertEquals("subtopics_${subject.id}_${unit.id}_${topic.id}_${firstSet.id}", subtopicsScreen.routeKey)
    }
}
