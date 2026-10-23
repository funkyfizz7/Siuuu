package com.example.siuu.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 100-Question automated test suite verifying Intent Classification across all 5 mandatory domains:
 * - Match Questions (30)
 * - Player Questions (25)
 * - Series Questions (20)
 * - Stat Questions (15)
 * - Commentary Questions (10)
 */
class Assistant100QuestionSuiteTest {

    @Test
    fun testMatchQuestions_30() {
        val testCases = listOf(
            "Why did Australia win?" to AssistantQuestionType.MATCH_SUMMARY,
            "Why did they win?" to AssistantQuestionType.MATCH_SUMMARY,
            "What changed the match?" to AssistantQuestionType.MATCH_TURNING_POINT,
            "What was the turning point?" to AssistantQuestionType.MATCH_TURNING_POINT,
            "What was the biggest turning point?" to AssistantQuestionType.MATCH_TURNING_POINT,
            "Who controlled Day 3?" to AssistantQuestionType.MATCH_MOMENTUM,
            "Who controlled Day 4?" to AssistantQuestionType.MATCH_MOMENTUM,
            "Who won momentum?" to AssistantQuestionType.MATCH_MOMENTUM,
            "How did the momentum shift?" to AssistantQuestionType.MATCH_MOMENTUM,
            "Who applied more pressure?" to AssistantQuestionType.MATCH_PRESSURE,
            "When was pressure highest?" to AssistantQuestionType.MATCH_PRESSURE,
            "When was the pressure highest?" to AssistantQuestionType.MATCH_PRESSURE,
            "What was the biggest partnership?" to AssistantQuestionType.MATCH_PARTNERSHIP,
            "What was the highest partnership in the match?" to AssistantQuestionType.MATCH_PARTNERSHIP,
            "How did England collapse?" to AssistantQuestionType.MATCH_COLLAPSE,
            "What was the biggest collapse?" to AssistantQuestionType.MATCH_COLLAPSE,
            "How did Australia recover?" to AssistantQuestionType.MATCH_RECOVERY,
            "What was the best recovery?" to AssistantQuestionType.MATCH_RECOVERY,
            "Who was Player of the Match?" to AssistantQuestionType.MATCH_SUMMARY,
            "What was the victory margin?" to AssistantQuestionType.MATCH_SUMMARY,
            "Where was the 1st Test played?" to AssistantQuestionType.MATCH_SUMMARY,
            "How did England lose the match?" to AssistantQuestionType.MATCH_SUMMARY,
            "How did Australia beat England?" to AssistantQuestionType.MATCH_SUMMARY,
            "What happened in the 4th innings?" to AssistantQuestionType.MATCH_SUMMARY,
            "Who won the first Test?" to AssistantQuestionType.MATCH_SUMMARY,
            "Why did Australia dominate?" to AssistantQuestionType.MATCH_SUMMARY,
            "What was the match story?" to AssistantQuestionType.MATCH_SUMMARY,
            "Who dominated Day 1?" to AssistantQuestionType.MATCH_MOMENTUM,
            "Who dominated Day 2?" to AssistantQuestionType.MATCH_MOMENTUM,
            "What changed in the second innings?" to AssistantQuestionType.MATCH_TURNING_POINT
        )

        for ((q, expected) in testCases) {
            val actual = AssistantIntentClassifier.classify(q)
            assertEquals("Failed for query: '$q'", expected, actual)
        }
    }

    @Test
    fun testPlayerQuestions_25() {
        val testCases = listOf(
            "Why is Smith rated 9.7?" to AssistantQuestionType.PLAYER_RATING,
            "Why is Steve Smith rated 9.7?" to AssistantQuestionType.PLAYER_RATING,
            "Why is he rated so highly?" to AssistantQuestionType.PLAYER_RATING,
            "Why is Cummins rated so high?" to AssistantQuestionType.PLAYER_RATING,
            "Why is Nathan Lyon rated so high?" to AssistantQuestionType.PLAYER_RATING,
            "Why is Ben Stokes rated so high?" to AssistantQuestionType.PLAYER_RATING,
            "Why is Joe Root rated so high?" to AssistantQuestionType.PLAYER_RATING,
            "How did Root score his century?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Steve Smith score his twin centuries?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Warner perform?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did David Warner perform?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Usman Khawaja play?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Matthew Wade bat?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Travis Head perform?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Stuart Broad bowl?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did James Anderson bowl?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "How did Chris Woakes perform?" to AssistantQuestionType.PLAYER_PERFORMANCE,
            "What was Lyon's best spell?" to AssistantQuestionType.PLAYER_SPELL,
            "What was Cummins' best spell?" to AssistantQuestionType.PLAYER_SPELL,
            "What was Broad's best spell?" to AssistantQuestionType.PLAYER_SPELL,
            "What was Pattinson's best spell?" to AssistantQuestionType.PLAYER_SPELL,
            "Who had highest impact?" to AssistantQuestionType.STAT_QUERY,
            "Who was the MVP?" to AssistantQuestionType.STAT_QUERY,
            "Why is Peter Siddle rated so high?" to AssistantQuestionType.PLAYER_RATING,
            "How did Tim Paine perform?" to AssistantQuestionType.PLAYER_PERFORMANCE
        )

        for ((q, expected) in testCases) {
            val actual = AssistantIntentClassifier.classify(q)
            assertEquals("Failed for query: '$q'", expected, actual)
        }
    }

    @Test
    fun testSeriesQuestions_20() {
        val testCases = listOf(
            "Who dominated the Ashes?" to AssistantQuestionType.SERIES_SUMMARY,
            "Who won the Ashes 2019?" to AssistantQuestionType.SERIES_SUMMARY,
            "Who won the series?" to AssistantQuestionType.SERIES_SUMMARY,
            "Summarize Ashes 2019" to AssistantQuestionType.SERIES_SUMMARY,
            "Summarize the 2019 Ashes series" to AssistantQuestionType.SERIES_SUMMARY,
            "Most runs in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "Most runs in the series?" to AssistantQuestionType.SERIES_STATS,
            "Most runs?" to AssistantQuestionType.SERIES_STATS,
            "Most wickets in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "Most wickets in the series?" to AssistantQuestionType.SERIES_STATS,
            "Most wickets?" to AssistantQuestionType.SERIES_STATS,
            "Best batter in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "Best batter?" to AssistantQuestionType.SERIES_STATS,
            "Best bowler in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "Best bowler?" to AssistantQuestionType.SERIES_STATS,
            "Who scored the most centuries in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "Who had the highest batting average in Ashes 2019?" to AssistantQuestionType.SERIES_STATS,
            "What was the final scoreline of Ashes 2019?" to AssistantQuestionType.SERIES_SUMMARY,
            "Did Australia retain the Ashes in 2019?" to AssistantQuestionType.SERIES_SUMMARY,
            "Which Tests did Australia win in Ashes 2019?" to AssistantQuestionType.SERIES_SUMMARY
        )

        for ((q, expected) in testCases) {
            val actual = AssistantIntentClassifier.classify(q)
            assertEquals("Failed for query: '$q'", expected, actual)
        }
    }

    @Test
    fun testStatQuestions_15() {
        val testCases = listOf(
            "Highest control %?" to AssistantQuestionType.STAT_QUERY,
            "Who had the highest control percentage?" to AssistantQuestionType.STAT_QUERY,
            "Best bowling rating?" to AssistantQuestionType.STAT_QUERY,
            "Who had the best bowling rating in the match?" to AssistantQuestionType.STAT_QUERY,
            "Most dot balls?" to AssistantQuestionType.STAT_QUERY,
            "Who bowled the most dot balls in the match?" to AssistantQuestionType.STAT_QUERY,
            "Highest impact rating?" to AssistantQuestionType.STAT_QUERY,
            "Who had the highest impact rating?" to AssistantQuestionType.STAT_QUERY,
            "Best batting rating?" to AssistantQuestionType.STAT_QUERY,
            "Who had the best batting rating?" to AssistantQuestionType.STAT_QUERY,
            "Best bowling figures in the match?" to AssistantQuestionType.STAT_QUERY,
            "Highest individual score in the match?" to AssistantQuestionType.STAT_QUERY,
            "Who had the lowest economy rate?" to AssistantQuestionType.STAT_QUERY,
            "Highest team total in the match?" to AssistantQuestionType.STAT_QUERY,
            "Most wickets in a single innings?" to AssistantQuestionType.STAT_QUERY
        )

        for ((q, expected) in testCases) {
            val actual = AssistantIntentClassifier.classify(q)
            assertEquals("Failed for query: '$q'", expected, actual)
        }
    }

    @Test
    fun testCommentaryQuestions_10() {
        val testCases = listOf(
            "Summarize Day 4." to AssistantQuestionType.SESSION_SUMMARY,
            "Summarize Day 1." to AssistantQuestionType.SESSION_SUMMARY,
            "Summarize Day 2." to AssistantQuestionType.SESSION_SUMMARY,
            "Summarize Day 3." to AssistantQuestionType.SESSION_SUMMARY,
            "Summarize Day 5." to AssistantQuestionType.SESSION_SUMMARY,
            "Explain Session 2." to AssistantQuestionType.SESSION_SUMMARY,
            "Explain Session 1." to AssistantQuestionType.SESSION_SUMMARY,
            "Explain Session 3." to AssistantQuestionType.SESSION_SUMMARY,
            "How did England recover?" to AssistantQuestionType.MATCH_RECOVERY,
            "What happened in Day 4 Session 2?" to AssistantQuestionType.SESSION_SUMMARY
        )

        for ((q, expected) in testCases) {
            val actual = AssistantIntentClassifier.classify(q)
            assertEquals("Failed for query: '$q'", expected, actual)
        }
    }
}
