package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated test suite for Task 3 (Reasoning Style Answers) and Task 4 (Answer Transparency / Tracing).
 */
class AssistantReasoningTraceTest {

    @Test
    fun testMatchSummary_ReasoningAndTrace() {
        val question = "Why did Australia win?"
        val intent = AssistantIntentClassifier.classify(question)
        val entities = AssistantEntities(teamName = "Australia", matchId = 1144993L)
        val facts = AssistantWarehouseFacts(
            matchDetails = mapOf(
                "winner" to "Australia",
                "margin" to "251 runs",
                "venue" to "Edgbaston, Birmingham",
                "player_of_match" to "Steve Smith"
            ),
            tablesQueried = listOf("dim_matches.json", "fact_match_story.csv"),
            rawFactStrings = listOf("Australia won by 251 runs at Edgbaston")
        )

        val answer = AssistantReasoningEngine.synthesizeAnswer(intent, entities, facts, question)

        // 1. Verify AssistantEvidence structure (FACT + EVIDENCE + CONCLUSION)
        val reasoning = answer.reasoning
        assertNotNull("Reasoning must not be null", reasoning)
        assertTrue("Fact must state victory", reasoning!!.fact.contains("Australia won by 251 runs"))
        assertTrue("Evidence must have multiple points", reasoning.evidence.size >= 3)
        assertTrue("Evidence must mention Steve Smith", reasoning.evidence.any { it.contains("Steve Smith") })
        assertTrue("Evidence must mention Nathan Lyon", reasoning.evidence.any { it.contains("Nathan Lyon") })
        assertTrue("Conclusion must state batting/bowling reasons", reasoning.conclusion.contains("primary reasons for victory"))

        // 2. Verify Rendered Text Format
        assertTrue("Rendered text must contain FACT header", answer.answerText.contains("FACT"))
        assertTrue("Rendered text must contain EVIDENCE header", answer.answerText.contains("EVIDENCE"))
        assertTrue("Rendered text must contain CONCLUSION header", answer.answerText.contains("CONCLUSION"))

        // 3. Verify Internal Traceability (Task 4)
        val trace = answer.trace
        assertNotNull("Answer trace must be captured", trace)
        assertEquals(intent, trace!!.intent)
        assertEquals(question, trace.question)
        assertTrue("Trace must log tables used", trace.tablesUsed.contains("dim_matches.json"))
        assertTrue("Trace must log evidence items", trace.evidenceUsed.isNotEmpty())
        assertTrue("Trace execution time must be >= 0", trace.executionTimeMs >= 0)
    }

    @Test
    fun testPlayerRating_ReasoningAndTrace() {
        val question = "Why is Smith rated 9.7?"
        val intent = AssistantIntentClassifier.classify(question)
        val entities = AssistantEntities(playerName = "Steve Smith", playerId = 267L, targetRating = 9.7)
        val facts = AssistantWarehouseFacts(
            playerRating = mapOf("batting_match_rating" to "9.3", "bowling_match_rating" to "6.4"),
            playerImpact = mapOf("match_mvp_score" to "100.0", "impact_rank" to "1"),
            tablesQueried = listOf("fact_player_match_rating.csv", "fact_player_match_impact.csv")
        )

        val answer = AssistantReasoningEngine.synthesizeAnswer(intent, entities, facts, question)

        // 1. Verify AssistantEvidence
        val reasoning = answer.reasoning
        assertNotNull(reasoning)
        assertTrue(reasoning!!.fact.contains("highest match rating"))
        assertTrue(reasoning.evidence.any { it.contains("286 total runs") || it.contains("centuries") })
        assertTrue(reasoning.conclusion.contains("highest overall contribution"))

        // 2. Verify Internal Trace
        val trace = answer.trace
        assertNotNull(trace)
        assertEquals(intent, trace!!.intent)
        assertEquals(entities.playerName, trace.entities.playerName)
    }

    @Test
    fun testSeriesSummary_ReasoningAndTrace() {
        val question = "Who dominated the Ashes?"
        val intent = AssistantIntentClassifier.classify(question)
        val entities = AssistantEntities(seriesName = "Ashes 2019", seriesId = "37")
        val facts = AssistantWarehouseFacts(
            seriesBattingStats = listOf(
                mapOf("player_name" to "Steve Smith", "stat_value" to "774", "country" to "Australia")
            ),
            tablesQueried = listOf("series_batting_rankings.csv", "dim_series.csv")
        )

        val answer = AssistantReasoningEngine.synthesizeAnswer(intent, entities, facts, question)

        val reasoning = answer.reasoning
        assertNotNull(reasoning)
        assertTrue(reasoning!!.fact.contains("Steve Smith"))
        assertTrue(reasoning.evidence.any { it.contains("774 runs") })
        assertTrue(reasoning.conclusion.contains("Smith was the most influential batter"))

        val trace = answer.trace
        assertNotNull(trace)
        assertEquals(intent, trace!!.intent)
    }

    @Test
    fun testStatQuery_ReasoningAndTrace() {
        val question = "Highest control %?"
        val intent = AssistantIntentClassifier.classify(question)
        val entities = AssistantEntities(metricName = "control")
        val facts = AssistantWarehouseFacts(
            tablesQueried = listOf("fact_player_match_impact.csv")
        )

        val answer = AssistantReasoningEngine.synthesizeAnswer(intent, entities, facts, question)

        val reasoning = answer.reasoning
        assertNotNull(reasoning)
        assertTrue(reasoning!!.fact.contains("88.4%"))
        assertTrue(reasoning.evidence.any { it.contains("366 balls faced") })
        assertTrue(reasoning.conclusion.contains("Superior shot control"))

        val trace = answer.trace
        assertNotNull(trace)
        assertEquals(AssistantQuestionType.STAT_QUERY, trace!!.intent)
    }
}
