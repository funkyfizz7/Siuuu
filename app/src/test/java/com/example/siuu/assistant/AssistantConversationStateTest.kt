package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Phase 2: Task 6 — Conversational Follow-Up Engine.
 * Tests multi-turn conversation memory, state updates, and anaphoric pronoun/demonstrative resolution.
 */
class AssistantConversationStateTest {

    @Test
    fun testMultiTurnConversationFlow() {
        var state = AssistantConversationState()

        // Turn 1: "Why did Australia win?"
        val q1 = "Why did Australia win?"
        val intent1 = AssistantIntentClassifier.classify(q1, state)
        assertEquals(AssistantQuestionType.MATCH_SUMMARY, intent1)

        val entities1 = AssistantEntities(teamName = "Australia", matchId = 1144993L)
        val facts1 = AssistantWarehouseFacts(
            matchDetails = mapOf("winner" to "Australia", "margin" to "251 runs", "venue" to "Edgbaston, Birmingham", "player_of_match" to "Steve Smith"),
            tablesQueried = listOf("dim_matches.json", "fact_match_story.csv")
        )
        val answer1 = AssistantReasoningEngine.synthesizeAnswer(intent1, entities1, facts1, q1)
        state = state.updateWithTurn(q1, intent1, entities1, answer1)

        assertEquals("Australia", state.lastMentionedTeam)
        assertEquals(1144993L, state.lastMatchId)
        assertEquals(1, state.history.size)

        // Turn 2: "What was the turning point?"
        val q2 = "What was the turning point?"
        val intent2 = AssistantIntentClassifier.classify(q2, state)
        assertEquals(AssistantQuestionType.MATCH_TURNING_POINT, intent2)

        val entities2 = AssistantEntities(matchId = state.lastMatchId, playerName = "Steve Smith", playerId = 267L)
        val facts2 = AssistantWarehouseFacts(
            turningPoints = listOf(
                mapOf("rank" to "1", "title" to "Steve Smith 144 (1st Innings) & Nathan Lyon 6/49 (4th Innings)", "importance_score" to "94.8", "tier" to "Match Defining Turning Point", "player" to "Steve Smith")
            ),
            tablesQueried = listOf("fact_match_turning_points.csv")
        )
        val answer2 = AssistantReasoningEngine.synthesizeAnswer(intent2, entities2, facts2, q2)
        state = state.updateWithTurn(q2, intent2, entities2, answer2)

        assertNotNull(state.lastTurningPointTitle)
        assertTrue(state.lastTurningPointTitle!!.contains("Steve Smith 144"))
        assertEquals("Steve Smith", state.lastPlayerName)
        assertEquals(267L, state.lastPlayerId)
        assertEquals(2, state.history.size)

        // Turn 3: "And who caused it?"
        val q3 = "And who caused it?"
        val intent3 = AssistantIntentClassifier.classify(q3, state)
        assertEquals(AssistantQuestionType.MATCH_TURNING_POINT, intent3)

        // Verify that "it" resolves to turning point in context
        val entities3 = AssistantEntities(playerId = state.lastPlayerId, playerName = state.lastPlayerName, matchId = state.lastMatchId)
        val answer3 = AssistantReasoningEngine.synthesizeAnswer(intent3, entities3, facts2, q3)
        state = state.updateWithTurn(q3, intent3, entities3, answer3)

        assertNotNull(answer3.reasoning)
        assertTrue("Fact must identify Steve Smith and Nathan Lyon", answer3.reasoning!!.fact.contains("Steve Smith") && answer3.reasoning!!.fact.contains("Nathan Lyon"))
        assertEquals(3, state.history.size)

        // Turn 4: "Why is he rated 9.7?"
        val q4 = "Why is he rated 9.7?"
        val intent4 = AssistantIntentClassifier.classify(q4, state)
        assertEquals(AssistantQuestionType.PLAYER_RATING, intent4)

        val entities4 = AssistantEntities(playerId = state.lastPlayerId, playerName = state.lastPlayerName, targetRating = 9.7)
        val facts4 = AssistantWarehouseFacts(
            playerRating = mapOf("batting_match_rating" to "9.3", "bowling_match_rating" to "6.4"),
            playerImpact = mapOf("match_mvp_score" to "100.0", "impact_rank" to "1")
        )
        val answer4 = AssistantReasoningEngine.synthesizeAnswer(intent4, entities4, facts4, q4)
        state = state.updateWithTurn(q4, intent4, entities4, answer4)

        assertTrue(answer4.reasoning!!.fact.contains("Steve Smith"))
        assertTrue(answer4.reasoning!!.fact.contains("9.7 / 10"))
        assertEquals(4, state.history.size)

        // Turn 5: "What happened after that?"
        val q5 = "What happened after that?"
        val intent5 = AssistantIntentClassifier.classify(q5, state)
        assertEquals(AssistantQuestionType.MATCH_SUMMARY, intent5)

        val entities5 = AssistantEntities(matchId = state.lastMatchId)
        val answer5 = AssistantReasoningEngine.synthesizeAnswer(intent5, entities5, facts1, q5)
        state = state.updateWithTurn(q5, intent5, entities5, answer5)

        assertTrue("Answer must describe sequence following turning point", answer5.reasoning!!.fact.contains("Following Steve Smith's 1st-innings rescue"))
        assertEquals(5, state.history.size)
    }
}
