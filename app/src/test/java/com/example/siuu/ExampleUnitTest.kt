package com.example.siuu

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFormatCricketScore_allOutScores() {
        // Place 1: Header scores
        assertEquals("179", formatCricketScore("179/10"))
        assertEquals("246", formatCricketScore("246/10"))
        assertEquals("179 & 246", formatCricketScore("179/10 & 246/10"))
        assertEquals("67 & 362/9", formatCricketScore("67/10 & 362/9"))

        // Place 2: Innings card score with overs
        assertEquals("179 (52.1 ov)", formatCricketScore("179/10 (52.1 ov)"))
        assertEquals("362/9 (125.4 ov)", formatCricketScore("362/9 (125.4 ov)"))
        assertEquals("487/7 dec (149.0 ov)", formatCricketScore("487/7 dec (149.0 ov)"))

        // Rounding of .6 overs
        assertEquals("180 (53 ov)", formatCricketScore("180/10 (52.6 ov)"))
    }

    @Test
    fun testHelpDialogConstants() {
        assertEquals("Match Momentum Flow", MATCH_MOMENTUM_FLOW_HELP_TITLE)
        assertEquals(3, MATCH_MOMENTUM_FLOW_HELP_PARAGRAPHS.size)
        assertTrue(MATCH_MOMENTUM_FLOW_HELP_PARAGRAPHS[0].startsWith("Match Momentum Flow shows how control"))

        assertEquals("Pressure Flow Graph", PRESSURE_FLOW_GRAPH_HELP_TITLE)
        assertEquals(4, PRESSURE_FLOW_GRAPH_HELP_PARAGRAPHS.size)
        assertTrue(PRESSURE_FLOW_GRAPH_HELP_PARAGRAPHS[0].startsWith("The Pressure Flow Graph illustrates"))
        assertTrue(PRESSURE_FLOW_GRAPH_HELP_PARAGRAPHS[3].contains("corresponds to the team that is batting"))
    }
}