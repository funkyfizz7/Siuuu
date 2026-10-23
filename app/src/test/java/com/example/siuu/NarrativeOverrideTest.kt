package com.example.siuu

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class NarrativeOverrideTest {

    private val testMatchId = 9999999L

    private val dummyHeader = MatchHeaderInfo(
        matchTitle = "Australia vs England (2019)",
        team1Name = "Australia",
        team1ShortName = "AUS",
        team1Color = Color(0xFFFFC72C),
        team2Name = "England",
        team2ShortName = "ENG",
        team2Color = Color(0xFF38BDF8),
        winner = "Australia",
        victoryMargin = "Australia won by 251 runs",
        venue = "Edgbaston"
    )

    private val dummySummary = MatchSummaryData(
        matchId = testMatchId,
        header = dummyHeader,
        matchStory = "Baseline match story paragraph.",
        matchStorySentences = listOf(
            "Sentence 0 baseline.",
            "Sentence 1 baseline.",
            "Sentence 2 baseline.",
            "Sentence 3 baseline."
        ),
        summaryPoints = listOf(
            "Sentence 0 baseline.",
            "Sentence 1 baseline.",
            "Sentence 2 baseline.",
            "Sentence 3 baseline."
        ),
        keyNumbers = KeyMatchNumbers(targetSet = "398 Runs")
    )

    private val dummyMomentum = MomentumTabDisplayData(
        matchId = testMatchId,
        header = dummyHeader,
        points = listOf(
            MomentumModel(
                sessionIndex = 1,
                dayNumber = 1,
                sessionOfDay = 1,
                innings = 1,
                battingTeam = "Australia",
                runs = 83,
                wickets = 3,
                overs = 27.0,
                runRate = 3.07,
                momentumScore = 40.0,
                contextStory = "Original baseline session 1 context story."
            ),
            MomentumModel(
                sessionIndex = 2,
                dayNumber = 1,
                sessionOfDay = 2,
                innings = 1,
                battingTeam = "Australia",
                runs = 71,
                wickets = 5,
                overs = 26.0,
                runRate = 2.73,
                momentumScore = 20.0,
                contextStory = "Original baseline session 2 context story."
            )
        ),
        topSwings = emptyList()
    )

    private val dummyPressure = PressureTabDisplayData(
        matchId = testMatchId,
        header = dummyHeader,
        pressurePoints = listOf(
            PressureModel(
                sessionIndex = 1,
                dayNumber = 1,
                sessionOfDay = 1,
                innings = 1,
                battingTeam = "Australia",
                runs = 83,
                wickets = 3,
                overs = 27.0,
                runRate = 3.07,
                pressureScore = 65.0,
                explanation = "Original baseline pressure explanation.",
                verdict = "Original baseline session verdict."
            )
        ),
        highestPressureMoments = emptyList()
    )

    @Before
    fun setUp() {
        // Clear any previous memory cache for testMatchId
        val field = NarrativeOverrideManager::class.java.getDeclaredField("memoryCache")
        field.isAccessible = true
        (field.get(NarrativeOverrideManager) as MutableMap<*, *>).remove(testMatchId)
    }

    @Test
    fun testBaselineReturnsOriginalWhenNoOverrides() {
        val resolved = NarrativeOverrideManager.applySummaryOverrides(testMatchId, dummySummary, null)

        assertEquals("Baseline match story paragraph.", resolved.matchStory)
        assertEquals(4, resolved.summaryPoints.size)
        assertEquals("Sentence 0 baseline.", resolved.summaryPoints[0])
        assertEquals("Sentence 1 baseline.", resolved.summaryPoints[1])
        // Numeric calculations remain identical
        assertEquals("398 Runs", resolved.keyNumbers.targetSet)
    }

    @Test
    fun testOverrideAppliesToTargetNarrativeWithoutModifyingOtherNarrativesOrStats() {
        val override = PublishedNarrativeOverride(
            id = "${testMatchId}_summary_point_1",
            matchId = testMatchId,
            narrativeType = "summary_point",
            itemKey = "1",
            publishedText = "HUMAN REVIEWED: England replied strongly with 374.",
            updatedAt = "2026-10-14T18:00:00Z",
            publishedBy = "editor@pitchiq.com"
        )

        // Inject override into manager's memory cache
        val field = NarrativeOverrideManager::class.java.getDeclaredField("memoryCache")
        field.isAccessible = true
        val map = field.get(NarrativeOverrideManager) as MutableMap<Long, MatchNarrativeOverrides>
        map[testMatchId] = MatchNarrativeOverrides(
            matchId = testMatchId,
            overrides = mapOf(override.id to override)
        )

        val resolved = NarrativeOverrideManager.applySummaryOverrides(testMatchId, dummySummary, null)

        // Target sentence was overridden
        assertEquals("HUMAN REVIEWED: England replied strongly with 374.", resolved.summaryPoints[1])
        // Unrelated sentences remain 100% UNTOUCHED
        assertEquals("Sentence 0 baseline.", resolved.summaryPoints[0])
        assertEquals("Sentence 2 baseline.", resolved.summaryPoints[2])
        assertEquals("Sentence 3 baseline.", resolved.summaryPoints[3])
        // Original overall story remains untouched
        assertEquals("Baseline match story paragraph.", resolved.matchStory)
        // Scores and calculations unchanged
        assertEquals("398 Runs", resolved.keyNumbers.targetSet)
    }

    @Test
    fun testMomentumNarrativeOverridePreservesFactualMetrics() {
        val override = PublishedNarrativeOverride(
            id = "${testMatchId}_momentum_story_s1",
            matchId = testMatchId,
            narrativeType = "momentum_story",
            itemKey = "s1",
            publishedText = "HUMAN REVIEWED: Stuart Broad ran through the top order in a clinical opening spell.",
            updatedAt = "2026-10-14T18:00:00Z",
            publishedBy = "editor@pitchiq.com"
        )

        val field = NarrativeOverrideManager::class.java.getDeclaredField("memoryCache")
        field.isAccessible = true
        val map = field.get(NarrativeOverrideManager) as MutableMap<Long, MatchNarrativeOverrides>
        map[testMatchId] = MatchNarrativeOverrides(
            matchId = testMatchId,
            overrides = mapOf(override.id to override)
        )

        val resolved = NarrativeOverrideManager.applyMomentumOverrides(testMatchId, dummyMomentum, null)

        // Target narrative updated
        assertEquals(
            "HUMAN REVIEWED: Stuart Broad ran through the top order in a clinical opening spell.",
            resolved.points[0].contextStory
        )
        // Crucial: Factual statistics (runs, wickets, overs, runRate, momentumScore) remain strictly UNCHANGED
        assertEquals(83, resolved.points[0].runs)
        assertEquals(3, resolved.points[0].wickets)
        assertEquals(27.0, resolved.points[0].overs, 0.001)
        assertEquals(3.07, resolved.points[0].runRate, 0.001)
        assertEquals(40.0, resolved.points[0].momentumScore, 0.001)

        // Unrelated session 2 narrative remains 100% baseline
        assertEquals("Original baseline session 2 context story.", resolved.points[1].contextStory)
        assertEquals(71, resolved.points[1].runs)
        assertEquals(5, resolved.points[1].wickets)
    }

    @Test
    fun testPressureExplanationAndVerdictOverride() {
        val expOverride = PublishedNarrativeOverride(
            id = "${testMatchId}_pressure_explanation_s1",
            matchId = testMatchId,
            narrativeType = "pressure_explanation",
            itemKey = "s1",
            publishedText = "HUMAN REVIEWED: Extreme pressure exerted by swinging conditions.",
            updatedAt = "2026-10-14T18:00:00Z"
        )
        val verdOverride = PublishedNarrativeOverride(
            id = "${testMatchId}_pressure_verdict_s1",
            matchId = testMatchId,
            narrativeType = "pressure_verdict",
            itemKey = "s1",
            publishedText = "HUMAN REVIEWED: Session clearly belongs to England.",
            updatedAt = "2026-10-14T18:00:00Z"
        )

        val field = NarrativeOverrideManager::class.java.getDeclaredField("memoryCache")
        field.isAccessible = true
        val map = field.get(NarrativeOverrideManager) as MutableMap<Long, MatchNarrativeOverrides>
        map[testMatchId] = MatchNarrativeOverrides(
            matchId = testMatchId,
            overrides = mapOf(
                expOverride.id to expOverride,
                verdOverride.id to verdOverride
            )
        )

        val resolved = NarrativeOverrideManager.applyPressureOverrides(testMatchId, dummyPressure, null)

        assertEquals("HUMAN REVIEWED: Extreme pressure exerted by swinging conditions.", resolved.pressurePoints[0].explanation)
        assertEquals("HUMAN REVIEWED: Session clearly belongs to England.", resolved.pressurePoints[0].verdict)
        // Numeric pressureScore untouched
        assertEquals(65.0, resolved.pressurePoints[0].pressureScore, 0.001)
    }

    @Test
    fun testRevertingOverrideRestoresOriginalBaseline() {
        // First verify override applies
        val override = PublishedNarrativeOverride(
            id = "${testMatchId}_summary_story_main",
            matchId = testMatchId,
            narrativeType = "summary_story",
            itemKey = "main",
            publishedText = "Overridden match story.",
            updatedAt = "2026-10-14T18:00:00Z"
        )

        val field = NarrativeOverrideManager::class.java.getDeclaredField("memoryCache")
        field.isAccessible = true
        val map = field.get(NarrativeOverrideManager) as MutableMap<Long, MatchNarrativeOverrides>
        map[testMatchId] = MatchNarrativeOverrides(
            matchId = testMatchId,
            overrides = mapOf(override.id to override)
        )

        var resolved = NarrativeOverrideManager.applySummaryOverrides(testMatchId, dummySummary, null)
        assertEquals("Overridden match story.", resolved.matchStory)

        // Now revert (clear overrides)
        map.remove(testMatchId)

        resolved = NarrativeOverrideManager.applySummaryOverrides(testMatchId, dummySummary, null)
        assertEquals("Baseline match story paragraph.", resolved.matchStory)
    }
}
