package com.example.siuu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionWinnerEvaluatorTest {

    @Test
    fun testCatastrophicCollapse_Adelaide2020_AustraliaWinsSession() {
        // Adelaide 2020 Day 3 Session 1: India bowled out for 36 (9 wickets lost in session)
        val model = MomentumModel(
            sessionIndex = 7,
            dayNumber = 3,
            sessionOfDay = 1,
            innings = 3,
            battingTeam = "IND",
            runs = 42,
            wickets = 9,
            overs = 21.2,
            runRate = 1.96,
            momentumScore = 44.0,
            momentumSwing = 44.0,
            sessionWinner = "neutral", // original flawed value in json
            commentaryText = "The session concluded with honors shared between India and Australia."
        )

        val playerTeamMap = mapOf(
            "Josh Hazlewood" to "AUS",
            "Pat Cummins" to "AUS"
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "IND",
            playerTeamMap = playerTeamMap,
            target = 90
        )

        assertEquals("team1", result.sessionWinner)
        assertEquals("dominant", result.sessionWinType)
        assertTrue(result.commentaryText.contains("AUS taking decisive control") || result.commentaryText.contains("AUS clearly on top"))
        assertTrue(result.evaluatedReason.contains("9 wkts"))
    }

    @Test
    fun testFourthInningsTargetPressure_Chasing500_BowlingWinsSession() {
        // Target is 500, Batting team scores 100 but loses 2 wickets
        val model = MomentumModel(
            sessionIndex = 10,
            dayNumber = 4,
            sessionOfDay = 1,
            innings = 4,
            battingTeam = "IND",
            runs = 100,
            wickets = 2,
            overs = 28.0,
            runRate = 3.57,
            momentumScore = 5.0,
            momentumSwing = 0.0,
            sessionWinner = "neutral"
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "IND",
            target = 500
        )

        // Bowling team is AUS (team1), batting is IND (team2)
        assertEquals("team1", result.sessionWinner)
        assertTrue(result.evaluatedReason.contains("Scoreboard pressure chasing 500"))
    }

    @Test
    fun testFourthInningsLowTarget_Chasing90_BattingWinsSession() {
        // Target is 90, Batting team scores 78/2 to virtually seal the win
        val model = MomentumModel(
            sessionIndex = 8,
            dayNumber = 3,
            sessionOfDay = 2,
            innings = 4,
            battingTeam = "AUS",
            runs = 78,
            wickets = 2,
            overs = 21.0,
            runRate = 3.71,
            momentumScore = 20.0,
            momentumSwing = 10.0,
            sessionWinner = "team1"
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "IND",
            target = 90
        )

        // AUS is batting (team1)
        assertEquals("team1", result.sessionWinner)
        assertTrue(result.evaluatedReason.contains("target of 90"))
    }

    @Test
    fun testBalancedSession_HonorsShared() {
        // Normal innings, 75 runs for 2 wickets in 28 overs
        val model = MomentumModel(
            sessionIndex = 2,
            dayNumber = 1,
            sessionOfDay = 2,
            innings = 1,
            battingTeam = "AUS",
            runs = 75,
            wickets = 2,
            overs = 28.0,
            runRate = 2.68,
            momentumScore = 0.0,
            momentumSwing = 0.0,
            sessionWinner = "neutral"
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "IND",
            target = 0
        )

        assertEquals("neutral", result.sessionWinner)
    }

    @Test
    fun testWicketlessBattingDominance() {
        // 110 runs for 0 wickets in 30 overs
        val model = MomentumModel(
            sessionIndex = 4,
            dayNumber = 2,
            sessionOfDay = 1,
            innings = 1,
            battingTeam = "AUS",
            runs = 110,
            wickets = 0,
            overs = 30.0,
            runRate = 3.67,
            momentumScore = 18.0,
            momentumSwing = 8.0,
            sessionWinner = "neutral"
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "IND",
            target = 0
        )

        assertEquals("team1", result.sessionWinner)
        assertEquals("dominant", result.sessionWinType)
    }

    @Test
    fun testEnglandVsSouthAfrica_Day1Session3_EnglandWinsDominant() {
        // Lord's 2017 Day 1 Session 3: England scored 175 runs for 1 wicket in 35.3 overs against South Africa
        val model = MomentumModel(
            sessionIndex = 3,
            dayNumber = 1,
            sessionOfDay = 3,
            innings = 1,
            battingTeam = "England",
            runs = 175,
            wickets = 1,
            overs = 35.3,
            runRate = 4.93,
            momentumScore = 26.4,
            momentumSwing = 5.2,
            sessionWinner = "team1",
            commentaryText = "England dictated terms with aggressive strokeplay in Day 1 Session 3, racking up 175 runs for 1 wickets in 35.3 overs."
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "ENG",
            team2ShortName = "SOU",
            team1FullName = "England",
            team2FullName = "South Africa",
            target = 0
        )

        assertEquals("team1", result.sessionWinner)
        assertEquals("dominant", result.sessionWinType)
        assertTrue(result.evaluatedReason.contains("Batting dominance"))
    }

    @Test
    fun testHeadingley2019_Day4Session2_EnglandWinsMatchAndSession() {
        // Headingley 2019 Day 4 Session 2: England scored 124 runs for 5 wickets (362/9)
        // chasing target 359 to win the Test match by 1 wicket (Ben Stokes 135*, Leach 1*)
        val model = MomentumModel(
            sessionIndex = 11,
            dayNumber = 4,
            sessionOfDay = 2,
            innings = 4,
            battingTeam = "England",
            runs = 124,
            wickets = 5,
            overs = 29.5,
            runRate = 4.16,
            momentumScore = -95.0,
            momentumSwing = -70.2,
            sessionWinner = "team2",
            sessionEndScore = "ENG 362/9",
            contextStory = "Pursuing a fourth-innings target of 359 runs, Ben Stokes's heroic 103* carried England past the target to seal a thrilling victory at 362/9, shifting match momentum emphatically toward England.",
            commentaryText = "Australia's bowling attack ran riot in Day 4 Session 2, capturing 5 crucial wickets as England managed 124 runs in 29.5 overs. England comprehensively dominated the session, establishing clear ascendancy and significantly improving their match-winning trajectory."
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "ENG",
            team1FullName = "Australia",
            team2FullName = "England",
            target = 359,
            matchWinner = "England",
            isFinalSession = true
        )

        // England is team2. The session MUST belong to England (blue color, team2).
        assertEquals("team2", result.sessionWinner)
        assertEquals("dominant", result.sessionWinType)
        assertTrue(result.evaluatedReason.contains("Test victory"))
    }

    @Test
    fun testMatchConcludingSession_BowlingTeamSealsVictory() {
        // Bowling team bowls out opposition in the final session to win the Test
        val model = MomentumModel(
            sessionIndex = 12,
            dayNumber = 5,
            sessionOfDay = 2,
            innings = 4,
            battingTeam = "ENG",
            runs = 45,
            wickets = 4,
            overs = 18.0,
            runRate = 2.5,
            momentumScore = 50.0,
            momentumSwing = 30.0,
            sessionWinner = "team1",
            contextStory = "Australia bowled out England to seal a 251-run victory."
        )

        val result = SessionWinnerEvaluator.evaluate(
            model = model,
            team1ShortName = "AUS",
            team2ShortName = "ENG",
            team1FullName = "Australia",
            team2FullName = "England",
            target = 398,
            matchWinner = "Australia",
            isFinalSession = true
        )

        // Australia is team1 bowling. Australia wins session.
        assertEquals("team1", result.sessionWinner)
        assertEquals("dominant", result.sessionWinType)
        assertTrue(result.evaluatedReason.contains("Bowled out opposition to seal Test victory"))
    }
}
