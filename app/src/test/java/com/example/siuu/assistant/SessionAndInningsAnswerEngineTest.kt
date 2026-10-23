package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated test suite for Task 7 (Session Intelligence Queries) and Task 8 (Innings Intelligence Queries).
 */
class SessionAndInningsAnswerEngineTest {

    private val sampleFacts = AssistantWarehouseFacts(
        matchDetails = mapOf(
            "winner" to "Australia",
            "margin" to "251 runs",
            "venue" to "Edgbaston, Birmingham",
            "player_of_match" to "Steve Smith"
        ),
        storyDetails = mapOf(
            "highest_pressure_session" to "Innings 4 Session 2",
            "highest_pressure_value" to "96.6",
            "highest_momentum_session" to "Day 4 Session 2",
            "highest_momentum_value" to "45.0"
        ),
        tablesQueried = listOf("fact_match_sessions.csv", "fact_session_commentary.csv", "fact_innings_commentary.csv")
    )

    // ==================== TASK 7: SESSION INTELLIGENCE ====================

    @Test
    fun testSession_whatHappenedInDay3Session2() {
        val question = "What happened in Day 3 Session 2?"
        val entities = AssistantEntities(dayNumber = 3, sessionNumber = 2)
        val answer = SessionAnswerEngine.answerSessionQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Day 3 Session 2"))
        assertTrue(answer.reasoning!!.evidence.isNotEmpty())
        assertTrue(answer.answerText.contains("FACT"))
        assertTrue(answer.answerText.contains("EVIDENCE"))
        assertTrue(answer.answerText.contains("CONCLUSION"))
    }

    @Test
    fun testSession_whichSessionChangedTheMatch() {
        val question = "Which session changed the match?"
        val entities = AssistantEntities()
        val answer = SessionAnswerEngine.answerSessionQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Day 4 Session 2"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Steve Smith") || it.contains("Matthew Wade") })
        assertTrue(answer.reasoning!!.conclusion.contains("declare on their own terms"))
    }

    @Test
    fun testSession_mostDominantSession() {
        val question = "Most dominant session?"
        val entities = AssistantEntities()
        val answer = SessionAnswerEngine.answerSessionQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Day 4 Session 2"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("114 runs") && it.contains("0 wickets") })
    }

    @Test
    fun testSession_whenWasPressureHighest() {
        val question = "When was pressure highest?"
        val entities = AssistantEntities()
        val answer = SessionAnswerEngine.answerSessionQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Innings 4 Session 2"))
        assertTrue(answer.reasoning!!.fact.contains("96.6"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Nathan Lyon") })
    }

    @Test
    fun testSession_whoControlledSession1() {
        val question = "Who controlled Session 1?"
        val entities = AssistantEntities(dayNumber = 1, sessionNumber = 1)
        val answer = SessionAnswerEngine.answerSessionQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("England controlled Day 1 Session 1"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Stuart Broad") })
    }

    // ==================== TASK 8: INNINGS INTELLIGENCE ====================

    @Test
    fun testInnings_howDidAustraliaRecover() {
        val question = "How did Australia recover?"
        val entities = AssistantEntities(teamName = "Australia")
        val answer = InningsAnswerEngine.answerInningsQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("122/8 to reach 284"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("144") && it.contains("Steve Smith") })
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Peter Siddle") })
    }

    @Test
    fun testInnings_whatHappenedInThe3rdInnings() {
        val question = "What happened in the 3rd innings?"
        val entities = AssistantEntities(inningsNumber = 3)
        val answer = InningsAnswerEngine.answerInningsQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("487/7 declared"))
        assertTrue(answer.reasoning!!.fact.contains("target of 398"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Matthew Wade") })
    }

    @Test
    fun testInnings_whyWasTheDeclarationImportant() {
        val question = "Why was the declaration important?"
        val entities = AssistantEntities()
        val answer = InningsAnswerEngine.answerInningsQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Tim Paine declared at 487/7"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("398 runs") })
        assertTrue(answer.reasoning!!.conclusion.contains("maximized bowling overs"))
    }

    @Test
    fun testInnings_howWasTheTargetCreated() {
        val question = "How was the target created?"
        val entities = AssistantEntities()
        val answer = InningsAnswerEngine.answerInningsQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("398-run target was constructed"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Steve Smith (142)") })
        assertTrue(answer.reasoning!!.evidence.any { it.contains("Matthew Wade (110)") })
    }

    @Test
    fun testInnings_bestInningsOfTheMatch() {
        val question = "Best innings of the match?"
        val entities = AssistantEntities()
        val answer = InningsAnswerEngine.answerInningsQuery(entities, sampleFacts, question)

        assertNotNull(answer.reasoning)
        assertTrue(answer.reasoning!!.fact.contains("Steve Smith's 144"))
        assertTrue(answer.reasoning!!.evidence.any { it.contains("88.4% control") || it.contains("50.7%") })
        assertTrue(answer.reasoning!!.conclusion.contains("highest leverage"))
    }
}
