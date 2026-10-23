package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for PitchIQ AI Assistant Foundation.
 * Verifies intent classification and query taxonomy across mandatory test queries:
 * 1. "Why did Australia win?"
 * 2. "What was the turning point?"
 * 3. "Why is Steve Smith rated 9.7?"
 * 4. "Most runs in Ashes 2019?"
 */
class AssistantRepositoryTest {

    @Test
    fun testIntent_whyDidAustraliaWin() {
        val question = "Why did Australia win?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_SUMMARY, intent)
    }

    @Test
    fun testIntent_whatWasTheTurningPoint() {
        val question = "What was the turning point?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_TURNING_POINT, intent)
    }

    @Test
    fun testIntent_whyIsSteveSmithRated9Point7() {
        val question = "Why is Steve Smith rated 9.7?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.PLAYER_RATING, intent)
    }

    @Test
    fun testIntent_mostRunsInAshes2019() {
        val question = "Most runs in Ashes 2019?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.SERIES_STATS, intent)
    }

    @Test
    fun testIntent_momentumQuery() {
        val question = "How did the momentum shift in 2nd innings?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_MOMENTUM, intent)
    }

    @Test
    fun testIntent_pressureQuery() {
        val question = "When was the pressure highest?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_PRESSURE, intent)
    }

    @Test
    fun testIntent_whoWonTheAshes() {
        val question = "Who won the Ashes 2019?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.SERIES_SUMMARY, intent)
    }

    @Test
    fun testIntent_playerPerformance() {
        val question = "How did David Warner perform?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.PLAYER_PERFORMANCE, intent)
    }

    @Test
    fun testIntent_statQuery() {
        val question = "Highest partnership in the match"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_PARTNERSHIP, intent)

        val statQ = "Highest control %?"
        assertEquals(AssistantQuestionType.STAT_QUERY, AssistantIntentClassifier.classify(statQ))
    }

    @Test
    fun testAnswerModels_structure() {
        val answer = AssistantAnswer(
            questionType = AssistantQuestionType.MATCH_SUMMARY,
            headline = "Australia won by 251 runs",
            answerText = "Twin centuries from Steve Smith secured victory.",
            keyFacts = listOf("Smith 144 & 142", "Cummins 4 wickets"),
            dataPoints = mapOf("winner" to "Australia", "margin" to "251 runs"),
            confidence = 0.99,
            sources = listOf("dim_matches.json")
        )

        assertNotNull(answer)
        assertEquals("Australia won by 251 runs", answer.headline)
        assertEquals(2, answer.keyFacts.size)
        assertEquals("Australia", answer.dataPoints["winner"])
        assertTrue(answer.confidence > 0.9)
    }
}
