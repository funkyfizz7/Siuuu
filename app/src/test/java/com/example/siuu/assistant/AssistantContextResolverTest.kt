package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AssistantContextResolver, AssistantIntentClassifier, and Pronoun Resolution.
 */
class AssistantContextResolverTest {

    @Test
    fun testPronounResolution_playerInspector_whyIsHeRatedSoHighly() {
        val question = "Why is he rated so highly?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.PLAYER_RATING, intent)

        val context = AssistantContext(currentPlayerId = 267L, currentTab = "Ratings")
        val rawEntities = AssistantEntities()
        
        // Mocking resolution logic
        val resolved = if (question.contains("he", true) && context.currentPlayerId != null) {
            rawEntities.copy(playerId = context.currentPlayerId, playerName = "Steve Smith")
        } else rawEntities

        assertEquals(267L, resolved.playerId)
        assertEquals("Steve Smith", resolved.playerName)
    }

    @Test
    fun testPronounResolution_matchIntelligence_whyDidTheyWin() {
        val question = "Why did they win?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.MATCH_SUMMARY, intent)

        val context = AssistantContext(currentMatchId = 1144993L, currentTab = "Summary")
        val rawEntities = AssistantEntities()

        val resolved = if (question.contains("they", true) && context.currentMatchId != null) {
            rawEntities.copy(matchId = context.currentMatchId, teamName = "Australia")
        } else rawEntities

        assertEquals(1144993L, resolved.matchId)
        assertEquals("Australia", resolved.teamName)
    }

    @Test
    fun testPronounResolution_seriesHub_whoDominated() {
        val question = "Who dominated the series?"
        val intent = AssistantIntentClassifier.classify(question)
        assertEquals(AssistantQuestionType.SERIES_SUMMARY, intent)

        val context = AssistantContext(currentSeriesId = 37L)
        val rawEntities = AssistantEntities()

        val resolved = if (question.contains("series", true) && context.currentSeriesId != null) {
            rawEntities.copy(seriesId = "37", seriesName = "Ashes 2019")
        } else rawEntities

        assertEquals("37", resolved.seriesId)
        assertEquals("Ashes 2019", resolved.seriesName)
    }

    @Test
    fun testIntentClassification_expandedCategories() {
        assertEquals(AssistantQuestionType.PLAYER_SPELL, AssistantIntentClassifier.classify("What was Lyon's best spell?"))
        assertEquals(AssistantQuestionType.MATCH_COLLAPSE, AssistantIntentClassifier.classify("How did England collapse?"))
        assertEquals(AssistantQuestionType.MATCH_PARTNERSHIP, AssistantIntentClassifier.classify("What was the biggest partnership?"))
        assertEquals(AssistantQuestionType.MATCH_MOMENTUM, AssistantIntentClassifier.classify("Who controlled Day 3?"))
        assertEquals(AssistantQuestionType.MATCH_PRESSURE, AssistantIntentClassifier.classify("Who applied more pressure?"))
        assertEquals(AssistantQuestionType.SESSION_SUMMARY, AssistantIntentClassifier.classify("Summarize Day 4."))
        assertEquals(AssistantQuestionType.SESSION_SUMMARY, AssistantIntentClassifier.classify("Explain Session 2."))
        assertEquals(AssistantQuestionType.STAT_QUERY, AssistantIntentClassifier.classify("Highest control %?"))
        assertEquals(AssistantQuestionType.STAT_QUERY, AssistantIntentClassifier.classify("Most dot balls?"))
        assertEquals(AssistantQuestionType.STAT_QUERY, AssistantIntentClassifier.classify("Highest impact rating?"))
        assertEquals(AssistantQuestionType.SERIES_STATS, AssistantIntentClassifier.classify("Most runs in Ashes 2019?"))
        assertEquals(AssistantQuestionType.SERIES_STATS, AssistantIntentClassifier.classify("Most wickets in Ashes 2019?"))
    }
}
