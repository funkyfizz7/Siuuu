package com.example.siuu

import androidx.compose.ui.graphics.Color

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

data class SessionMomentumPoint(
    val sessionIndex: Int,
    val dayNumber: Int,
    val sessionOfDay: Int,
    val innings: Int,
    val battingTeam: String,
    val runs: Int,
    val wickets: Int,
    val overs: Float,
    val runRate: Float,
    val momentumScore: Float,
    val momentumSwing: Float,
    val momentumLabel: String,
    val contextStory: String
)

data class SessionPressurePoint(
    val sessionIndex: Int,
    val dayNumber: Int,
    val sessionOfDay: Int,
    val innings: Int,
    val battingTeam: String,
    val runs: Int,
    val wickets: Int,
    val overs: Float,
    val runRate: Float,
    val pressureScore: Float,
    val pressureZone: String,
    val explanation: String
)

data class MatchHero(
    val name: String,
    val role: String,
    val team: String,
    val rating: Float,
    val headlineStat: String,
    val impactSnippet: String
)

data class KeyMatchNumbers(
    val firstInningsLead: String,
    val targetSet: String,
    val manOfTheMatch: String,
    val manOfTheMatchDetails: String
)

data class TopTurningPoint(
    val title: String,
    val subtitle: String,
    val winProbShift: String,
    val impactHero: String,
    val importanceScore: String,
    val narrative: String
)

data class RankedEvent(
    val rank: Int,
    val title: String,
    val daySession: String,
    val explanation: String
)

data class MatchCollapseInfo(
    val team: String,
    val score: String,
    val severity: Float,
    val classification: String,
    val explanation: String
)

data class MatchRecoveryInfo(
    val team: String,
    val transition: String,
    val recoveryScore: Float,
    val classification: String,
    val explanation: String
)

data class PlayerXI(
    val id: String,
    val jerseyNumber: Int,
    val name: String,
    val shortName: String,
    val team: String,
    val role: String,
    val tier: Int,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false,
    val isMotm: Boolean = false,
    val overallRating: Float,
    val battingRating: Float,
    val bowlingRating: Float,
    val impactRating: Float,
    val runs: String,
    val balls: String,
    val strikeRate: String,
    val fours: String,
    val sixes: String,
    val wickets: String,
    val overs: String,
    val economy: String,
    val bestBowling: String,
    val primaryHighlight: String,
    val story: String
)

// Scorecard Data Structures
data class BatterScore(
    val name: String,
    val dismissal: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Float
)

data class BowlerScore(
    val name: String,
    val overs: Float,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Float
)

data class FOW(
    val score: String,
    val batterName: String,
    val over: String
)

data class DetailedPartnership(
    val wicketNumber: Int,
    val batter1Name: String,
    val batter1Runs: Int,
    val batter1Balls: Int,
    val batter2Name: String,
    val batter2Runs: Int,
    val batter2Balls: Int,
    val totalRuns: Int,
    val totalBalls: Int,
    val isKey: Boolean = false,
    val intelligenceLabel: String = ""
)

data class InningsScorecard(
    val inningsNumber: Int,
    val teamName: String,
    val totalScore: String,
    val runRate: Float,
    val extras: String,
    val batters: List<BatterScore>,
    val bowlers: List<BowlerScore>,
    val fallOfWickets: List<FOW>,
    val allPartnerships: List<DetailedPartnership>
)

data class MatchIntelligenceData(
    val matchId: Long,
    val matchTitle: String,
    val subtitle: String,
    val venue: String,
    val team1: Team,
    val team2: Team,
    val winner: String,
    val victoryMargin: String,
    val heroNarrative: String,
    val team1ControlPct: Int,
    val team2ControlPct: Int,
    val evenControlPct: Int,
    val matchSummaryPoints: List<String>,
    val keyNumbers: KeyMatchNumbers,
    val heroes: List<MatchHero>,
    val points: List<SessionMomentumPoint>,
    val pressurePoints: List<SessionPressurePoint>,
    val topTurningPoint: TopTurningPoint,
    val topEvents: List<RankedEvent>,
    val primaryCollapse: MatchCollapseInfo,
    val primaryRecovery: MatchRecoveryInfo,
    val australiaXI: List<PlayerXI>,
    val englandXI: List<PlayerXI>,
    val scorecards: List<InningsScorecard>
)

object PrototypeMatchData {
    val australia = Team(
        id = "AUS",
        name = "Australia",
        shortName = "AUS",
        primaryColor = Color(0xFFFFC72C),
        secondaryColor = Color(0xFF005A36)
    )

    val england = Team(
        id = "ENG",
        name = "England",
        shortName = "ENG",
        primaryColor = Color(0xFF00A3FF),
        secondaryColor = Color(0xFF0C2340)
    )

    val ausXI = listOf(
        PlayerXI(id = "aus_1", jerseyNumber = 31, name = "David Warner", shortName = "Warner", team = "Australia", role = "Opener", tier = 1, overallRating = 3.4f, battingRating = 3.2f, bowlingRating = 0.0f, impactRating = 3.0f, runs = "10 (2 & 8)", balls = "18", strikeRate = "55.55", fours = "1", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "10 Runs (2 & 8)", story = "Dismissed early in both innings by Stuart Broad."),
        PlayerXI(id = "aus_2", jerseyNumber = 8, name = "Cameron Bancroft", shortName = "Bancroft", team = "Australia", role = "Opener", tier = 1, overallRating = 3.8f, battingRating = 3.6f, bowlingRating = 0.0f, impactRating = 3.5f, runs = "15 (8 & 7)", balls = "61", strikeRate = "24.59", fours = "2", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "15 Runs (8 & 7)", story = "Fought hard against new ball swing."),
        PlayerXI(id = "aus_3", jerseyNumber = 1, name = "Usman Khawaja", shortName = "Khawaja", team = "Australia", role = "Top Order", tier = 2, overallRating = 5.5f, battingRating = 5.8f, bowlingRating = 0.0f, impactRating = 5.2f, runs = "53 (13 & 40)", balls = "104", strikeRate = "50.96", fours = "7", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "53 Runs (13 & 40)", story = "Solid start in second innings."),
        PlayerXI(id = "aus_4", jerseyNumber = 49, name = "Steve Smith", shortName = "Smith", team = "Australia", role = "Top Order", tier = 2, isMotm = true, overallRating = 9.8f, battingRating = 9.9f, bowlingRating = 0.0f, impactRating = 9.8f, runs = "286 (144 & 142)", balls = "426", strikeRate = "67.14", fours = "38", sixes = "4", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "286 Runs (144 & 142) • MoM", story = "Masterclass twin centuries. Rescued from 122/8."),
        PlayerXI(id = "aus_5", jerseyNumber = 62, name = "Travis Head", shortName = "Head", team = "Australia", role = "Middle Order", tier = 2, overallRating = 7.4f, battingRating = 7.8f, bowlingRating = 0.0f, impactRating = 7.2f, runs = "86 (35 & 51)", balls = "180", strikeRate = "47.77", fours = "10", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "86 Runs (35 & 51)", story = "Vital 130-run stand with Smith."),
        PlayerXI(id = "aus_6", jerseyNumber = 13, name = "Matthew Wade", shortName = "Wade", team = "Australia", role = "Middle Order", tier = 3, overallRating = 8.5f, battingRating = 8.9f, bowlingRating = 0.0f, impactRating = 8.2f, runs = "111 (1 & 110)", balls = "154", strikeRate = "72.07", fours = "17", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "111 Runs (110 on Day 4)", story = "Blistering 110 on Day 4."),
        PlayerXI(id = "aus_7", jerseyNumber = 67, name = "Tim Paine", shortName = "Paine", team = "Australia", role = "Wicketkeeper", tier = 3, isCaptain = true, isWicketKeeper = true, overallRating = 6.2f, battingRating = 5.4f, bowlingRating = 0.0f, impactRating = 6.5f, runs = "39 (5 & 34)", balls = "65", strikeRate = "60.00", fours = "4", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "39 Runs • 4 Catches", story = "Captained aggressively and declared on Day 4."),
        PlayerXI(id = "aus_8", jerseyNumber = 19, name = "James Pattinson", shortName = "Pattinson", team = "Australia", role = "Bowler", tier = 4, overallRating = 7.2f, battingRating = 6.8f, bowlingRating = 7.4f, impactRating = 7.1f, runs = "47 (0 & 47*)", balls = "77", strikeRate = "61.03", fours = "6", sixes = "1", wickets = "2", overs = "38.0", economy = "2.92", bestBowling = "2/82", primaryHighlight = "2 Wkts • 47* Runs", story = "Useful tail runs and hostile bowling."),
        PlayerXI(id = "aus_9", jerseyNumber = 30, name = "Pat Cummins", shortName = "Cummins", team = "Australia", role = "Bowler", tier = 4, overallRating = 8.8f, battingRating = 5.0f, bowlingRating = 9.1f, impactRating = 8.9f, runs = "31 (5 & 26*)", balls = "72", strikeRate = "43.05", fours = "4", sixes = "0", wickets = "7", overs = "47.3", economy = "2.44", bestBowling = "4/32", primaryHighlight = "7 Wickets (4/32 Day 5)", story = "Relentless pace and 7 match wickets."),
        PlayerXI(id = "aus_10", jerseyNumber = 10, name = "Peter Siddle", shortName = "Siddle", team = "Australia", role = "Bowler", tier = 4, overallRating = 7.9f, battingRating = 7.8f, bowlingRating = 7.6f, impactRating = 8.0f, runs = "44 (44 & 0)", balls = "85", strikeRate = "51.76", fours = "4", sixes = "0", wickets = "2", overs = "34.0", economy = "1.94", bestBowling = "2/52", primaryHighlight = "44 Runs (88 Stand with Smith)", story = "88-run 10th-wicket partnership with Smith."),
        PlayerXI(id = "aus_11", jerseyNumber = 67, name = "Nathan Lyon", shortName = "Lyon", team = "Australia", role = "Bowler", tier = 4, overallRating = 9.3f, battingRating = 4.0f, bowlingRating = 9.8f, impactRating = 9.5f, runs = "17 (12* & 5)", balls = "39", strikeRate = "43.59", fours = "2", sixes = "0", wickets = "9", overs = "63.5", economy = "2.52", bestBowling = "6/49 (Day 5)", primaryHighlight = "9 Wickets (6/49 on Day 5)", story = "Spin masterclass claiming 6/49 on Day 5.")
    )

    val engXI = listOf(
        PlayerXI(id = "eng_1", jerseyNumber = 27, name = "Rory Burns", shortName = "Burns", team = "England", role = "Opener", tier = 1, overallRating = 8.8f, battingRating = 9.0f, bowlingRating = 0.0f, impactRating = 8.6f, runs = "144 (133 & 11)", balls = "343", strikeRate = "41.98", fours = "18", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "144 Runs (133 1st Inn)", story = "Epic 512-minute 133 in 1st innings."),
        PlayerXI(id = "eng_2", jerseyNumber = 20, name = "Jason Roy", shortName = "Roy", team = "England", role = "Opener", tier = 1, overallRating = 4.2f, battingRating = 4.2f, bowlingRating = 0.0f, impactRating = 4.0f, runs = "38 (10 & 28)", balls = "66", strikeRate = "57.57", fours = "5", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "38 Runs (10 & 28)", story = "Struggled against Cummins and Pattinson."),
        PlayerXI(id = "eng_3", jerseyNumber = 66, name = "Joe Root", shortName = "Root", team = "England", role = "Top Order", tier = 2, isCaptain = true, overallRating = 6.8f, battingRating = 7.0f, bowlingRating = 4.0f, impactRating = 6.6f, runs = "85 (57 & 28)", balls = "179", strikeRate = "47.48", fours = "9", sixes = "0", wickets = "0", overs = "8.0", economy = "4.25", bestBowling = "0/34", primaryHighlight = "85 Runs (57 & 28)", story = "Scored fluent 57 in 1st innings."),
        PlayerXI(id = "eng_4", jerseyNumber = 24, name = "Joe Denly", shortName = "Denly", team = "England", role = "Top Order", tier = 2, overallRating = 4.5f, battingRating = 4.6f, bowlingRating = 4.0f, impactRating = 4.3f, runs = "29 (18 & 11)", balls = "71", strikeRate = "40.84", fours = "5", sixes = "0", wickets = "1", overs = "5.0", economy = "4.60", bestBowling = "1/23", primaryHighlight = "29 Runs • 1 Wkt", story = "Failed to convert starts."),
        PlayerXI(id = "eng_5", jerseyNumber = 63, name = "Jos Buttler", shortName = "Buttler", team = "England", role = "Middle Order", tier = 2, overallRating = 4.1f, battingRating = 3.8f, bowlingRating = 0.0f, impactRating = 4.0f, runs = "6 (5 & 1)", balls = "33", strikeRate = "18.18", fours = "0", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "6 Runs (5 & 1)", story = "Cheap dismissals in both innings."),
        PlayerXI(id = "eng_6", jerseyNumber = 55, name = "Ben Stokes", shortName = "Stokes", team = "England", role = "All-Rounder", tier = 3, overallRating = 6.7f, battingRating = 6.8f, bowlingRating = 6.5f, impactRating = 6.9f, runs = "56 (50 & 6)", balls = "109", strikeRate = "51.37", fours = "8", sixes = "0", wickets = "4", overs = "40.0", economy = "4.05", bestBowling = "3/85", primaryHighlight = "56 Runs (50 1st Inn) • 4 Wkts", story = "Gritty 50 and 4 match wickets."),
        PlayerXI(id = "eng_7", jerseyNumber = 51, name = "Jonny Bairstow", shortName = "Bairstow", team = "England", role = "Wicketkeeper", tier = 3, isWicketKeeper = true, overallRating = 4.0f, battingRating = 3.6f, bowlingRating = 0.0f, impactRating = 4.2f, runs = "14 (8 & 6)", balls = "36", strikeRate = "38.88", fours = "2", sixes = "0", wickets = "-", overs = "-", economy = "-", bestBowling = "-", primaryHighlight = "14 Runs • 4 Catches", story = "Clean keeper, struggled with bat."),
        PlayerXI(id = "eng_8", jerseyNumber = 18, name = "Moeen Ali", shortName = "Moeen", team = "England", role = "All-Rounder", tier = 4, overallRating = 5.2f, battingRating = 3.0f, bowlingRating = 6.4f, impactRating = 5.0f, runs = "4 (0 & 4)", balls = "26", strikeRate = "15.38", fours = "0", sixes = "0", wickets = "3", overs = "42.0", economy = "4.09", bestBowling = "2/42", primaryHighlight = "3 Wickets (2/42 1st Inn)", story = "Dismissed by Lyon in both innings."),
        PlayerXI(id = "eng_9", jerseyNumber = 19, name = "Chris Woakes", shortName = "Woakes", team = "England", role = "Bowler", tier = 4, overallRating = 7.6f, battingRating = 6.9f, bowlingRating = 7.8f, impactRating = 7.5f, runs = "68 (37* & 31)", balls = "113", strikeRate = "60.17", fours = "11", sixes = "0", wickets = "4", overs = "47.0", economy = "2.21", bestBowling = "3/58", primaryHighlight = "4 Wkts • 68 Runs (37* & 31)", story = "Superb with both ball and bat."),
        PlayerXI(id = "eng_10", jerseyNumber = 8, name = "Stuart Broad", shortName = "Broad", team = "England", role = "Bowler", tier = 4, overallRating = 8.4f, battingRating = 4.0f, bowlingRating = 9.0f, impactRating = 8.5f, runs = "9 (9 & 0)", balls = "21", strikeRate = "42.86", fours = "1", sixes = "0", wickets = "6", overs = "45.4", economy = "3.48", bestBowling = "5/86", primaryHighlight = "6 Wickets (5/86 Day 1)", story = "Day 1 5/86 triggered collapse."),
        PlayerXI(id = "eng_11", jerseyNumber = 9, name = "James Anderson", shortName = "Anderson", team = "England", role = "Bowler", tier = 4, overallRating = 5.0f, battingRating = 4.0f, bowlingRating = 5.5f, impactRating = 4.5f, runs = "7 (3 & 4*)", balls = "16", strikeRate = "43.75", fours = "1", sixes = "0", wickets = "0", overs = "4.0", economy = "0.25", bestBowling = "0/1", primaryHighlight = "Injured Day 1 (4.0 Overs)", story = "Calf injury restricted him to 4 overs.")
    )

    val scorecardList = listOf(
        // Innings 1: Australia 284
        InningsScorecard(
            inningsNumber = 1,
            teamName = "Australia",
            totalScore = "284 (80.4 ov)",
            runRate = 3.52f,
            extras = "15 (b 6, lb 6, w 2, nb 1)",
            batters = listOf(
                BatterScore("David Warner", "c Bairstow b Broad", 2, 4, 0, 0, 50.00f),
                BatterScore("Cameron Bancroft", "c Root b Broad", 8, 25, 1, 0, 32.00f),
                BatterScore("Usman Khawaja", "c Bairstow b Woakes", 13, 23, 1, 0, 56.52f),
                BatterScore("Steve Smith", "b Broad", 144, 219, 16, 2, 65.75f),
                BatterScore("Travis Head", "lbw b Woakes", 35, 64, 4, 0, 54.69f),
                BatterScore("Matthew Wade", "lbw b Woakes", 1, 7, 0, 0, 14.29f),
                BatterScore("Tim Paine (c/wk)", "c Burns b Woakes", 5, 14, 1, 0, 35.71f),
                BatterScore("James Pattinson", "lbw b Broad", 0, 1, 0, 0, 0.00f),
                BatterScore("Pat Cummins", "lbw b Stokes", 5, 19, 0, 0, 26.32f),
                BatterScore("Peter Siddle", "c Buttler b Moeen", 44, 85, 4, 0, 51.76f),
                BatterScore("Nathan Lyon", "not out", 12, 23, 1, 0, 52.17f)
            ),
            bowlers = listOf(
                BowlerScore("James Anderson", 4.0f, 3, 1, 0, 0.25f),
                BowlerScore("Stuart Broad", 22.4f, 4, 86, 5, 3.79f),
                BowlerScore("Chris Woakes", 21.0f, 6, 58, 3, 2.76f),
                BowlerScore("Ben Stokes", 14.0f, 1, 77, 1, 5.50f),
                BowlerScore("Moeen Ali", 19.0f, 4, 42, 1, 2.21f)
            ),
            fallOfWickets = listOf(
                FOW("17/1", "David Warner", "3.5"),
                FOW("35/2", "Cameron Bancroft", "11.2"),
                FOW("35/3", "Usman Khawaja", "14.4"),
                FOW("99/4", "Travis Head", "32.4"),
                FOW("105/5", "Matthew Wade", "34.1"),
                FOW("112/6", "Tim Paine", "36.3"),
                FOW("112/7", "James Pattinson", "36.4"),
                FOW("122/8", "Pat Cummins", "43.5"),
                FOW("210/9", "Peter Siddle", "67.1"),
                FOW("284/10", "Steve Smith", "80.4")
            ),
            allPartnerships = listOf(
                DetailedPartnership(1, "David Warner", 2, 4, "Cameron Bancroft", 8, 19, 17, 23),
                DetailedPartnership(2, "Cameron Bancroft", 0, 6, "Usman Khawaja", 13, 23, 18, 44),
                DetailedPartnership(3, "Usman Khawaja", 0, 0, "Steve Smith", 0, 20, 0, 20),
                DetailedPartnership(4, "Steve Smith", 29, 44, "Travis Head", 35, 64, 64, 108, isKey = true, intelligenceLabel = "Rebuilding Stand"),
                DetailedPartnership(5, "Steve Smith", 5, 2, "Matthew Wade", 1, 7, 6, 9),
                DetailedPartnership(6, "Steve Smith", 2, 0, "Tim Paine", 5, 14, 7, 14),
                DetailedPartnership(7, "Steve Smith", 0, 0, "James Pattinson", 0, 1, 0, 1),
                DetailedPartnership(8, "Steve Smith", 5, 24, "Pat Cummins", 5, 19, 10, 43),
                DetailedPartnership(9, "Steve Smith", 44, 62, "Peter Siddle", 44, 85, 88, 147, isKey = true, intelligenceLabel = "★ Match Saving Stand"),
                DetailedPartnership(10, "Steve Smith", 59, 58, "Nathan Lyon", 12, 23, 74, 81, isKey = true, intelligenceLabel = "Tail Resistance Stand")
            )
        ),

        // Innings 2: England 374
        InningsScorecard(
            inningsNumber = 2,
            teamName = "England",
            totalScore = "374 (135.5 ov)",
            runRate = 2.75f,
            extras = "44 (b 14, lb 13, w 8, nb 9)",
            batters = listOf(
                BatterScore("Rory Burns", "c Paine b Lyon", 133, 312, 17, 0, 42.63f),
                BatterScore("Jason Roy", "c Smith b Pattinson", 10, 22, 1, 0, 45.45f),
                BatterScore("Joe Root (c)", "c & b Siddle", 57, 119, 6, 0, 47.90f),
                BatterScore("Joe Denly", "c Wade b Pattinson", 18, 43, 3, 0, 41.86f),
                BatterScore("Jos Buttler", "c Bancroft b Cummins", 5, 19, 0, 0, 26.32f),
                BatterScore("Ben Stokes", "c Paine b Cummins", 50, 96, 7, 0, 52.08f),
                BatterScore("Jonny Bairstow", "c Warner b Siddle", 8, 16, 1, 0, 50.00f),
                BatterScore("Moeen Ali", "b Lyon", 0, 6, 0, 0, 0.00f),
                BatterScore("Chris Woakes", "not out", 37, 86, 5, 0, 43.02f),
                BatterScore("Stuart Broad", "c Pattinson b Cummins", 9, 17, 1, 0, 52.94f),
                BatterScore("James Anderson", "c Bancroft b Lyon", 3, 15, 0, 0, 20.00f)
            ),
            bowlers = listOf(
                BowlerScore("Pat Cummins", 30.0f, 7, 84, 3, 2.80f),
                BowlerScore("James Pattinson", 27.0f, 5, 82, 2, 3.04f),
                BowlerScore("Peter Siddle", 27.0f, 8, 52, 2, 1.93f),
                BowlerScore("Nathan Lyon", 43.5f, 5, 112, 3, 2.56f),
                BowlerScore("Matthew Wade", 8.0f, 0, 17, 0, 2.13f)
            ),
            fallOfWickets = listOf(
                FOW("22/1", "Jason Roy", "7.3"),
                FOW("154/2", "Joe Root", "54.1"),
                FOW("189/3", "Joe Denly", "65.5"),
                FOW("194/4", "Jos Buttler", "71.6"),
                FOW("282/5", "Ben Stokes", "102.3"),
                FOW("296/6", "Jonny Bairstow", "107.5"),
                FOW("300/7", "Moeen Ali", "110.1"),
                FOW("300/8", "Rory Burns", "110.5"),
                FOW("365/9", "Stuart Broad", "131.2"),
                FOW("374/10", "James Anderson", "135.5")
            ),
            allPartnerships = listOf(
                DetailedPartnership(1, "Rory Burns", 12, 23, "Jason Roy", 10, 22, 22, 45),
                DetailedPartnership(2, "Rory Burns", 75, 161, "Joe Root", 57, 119, 132, 280, isKey = true, intelligenceLabel = "★ Foundation Stand"),
                DetailedPartnership(3, "Rory Burns", 17, 26, "Joe Denly", 18, 43, 35, 69),
                DetailedPartnership(4, "Rory Burns", 0, 18, "Jos Buttler", 5, 19, 5, 37),
                DetailedPartnership(5, "Rory Burns", 29, 88, "Ben Stokes", 50, 96, 88, 184, isKey = true, intelligenceLabel = "Lead Securing Stand"),
                DetailedPartnership(6, "Rory Burns", 0, 15, "Jonny Bairstow", 8, 16, 14, 31),
                DetailedPartnership(7, "Rory Burns", 0, 10, "Moeen Ali", 0, 6, 4, 16),
                DetailedPartnership(8, "Rory Burns", 0, 1, "Chris Woakes", 0, 3, 0, 4),
                DetailedPartnership(9, "Chris Woakes", 28, 56, "Stuart Broad", 9, 17, 65, 123, isKey = true, intelligenceLabel = "Tail-End Boost"),
                DetailedPartnership(10, "Chris Woakes", 9, 27, "James Anderson", 3, 15, 9, 28)
            )
        ),

        // Innings 3: Australia 487/7 Declared
        InningsScorecard(
            inningsNumber = 3,
            teamName = "Australia",
            totalScore = "487/7 dec (149.0 ov)",
            runRate = 3.26f,
            extras = "22 (b 8, lb 8, w 4, nb 2)",
            batters = listOf(
                BatterScore("David Warner", "c Bairstow b Broad", 8, 14, 1, 0, 57.14f),
                BatterScore("Cameron Bancroft", "c Bairstow b Moeen", 7, 36, 1, 0, 19.44f),
                BatterScore("Usman Khawaja", "c Bairstow b Woakes", 40, 81, 6, 0, 49.38f),
                BatterScore("Steve Smith", "c Bairstow b Woakes", 142, 207, 14, 0, 68.60f),
                BatterScore("Travis Head", "c Bairstow b Stokes", 51, 116, 6, 0, 43.97f),
                BatterScore("Matthew Wade", "c Root b Stokes", 110, 143, 17, 0, 76.92f),
                BatterScore("Tim Paine (c/wk)", "c Burns b Stokes", 34, 51, 3, 0, 66.67f),
                BatterScore("James Pattinson", "not out", 47, 48, 4, 1, 97.92f),
                BatterScore("Pat Cummins", "not out", 26, 24, 3, 0, 108.33f)
            ),
            bowlers = listOf(
                BowlerScore("Stuart Broad", 23.0f, 3, 73, 1, 3.17f),
                BowlerScore("Chris Woakes", 26.0f, 5, 46, 1, 1.76f),
                BowlerScore("Ben Stokes", 26.0f, 5, 85, 3, 3.26f),
                BowlerScore("Moeen Ali", 23.0f, 2, 130, 1, 5.65f),
                BowlerScore("Joe Denly", 5.0f, 0, 23, 1, 4.60f),
                BowlerScore("Joe Root", 8.0f, 0, 34, 0, 4.25f)
            ),
            fallOfWickets = listOf(
                FOW("13/1", "David Warner", "4.4"),
                FOW("27/2", "Cameron Bancroft", "12.6"),
                FOW("75/3", "Usman Khawaja", "28.5"),
                FOW("205/4", "Travis Head", "75.3"),
                FOW("331/5", "Steve Smith", "107.5"),
                FOW("407/6", "Matthew Wade", "128.2"),
                FOW("409/7", "Tim Paine", "129.5")
            ),
            allPartnerships = listOf(
                DetailedPartnership(1, "David Warner", 8, 14, "Cameron Bancroft", 5, 14, 13, 28),
                DetailedPartnership(2, "Cameron Bancroft", 2, 22, "Usman Khawaja", 12, 26, 14, 48),
                DetailedPartnership(3, "Usman Khawaja", 28, 55, "Steve Smith", 20, 41, 48, 96),
                DetailedPartnership(4, "Steve Smith", 79, 164, "Travis Head", 51, 116, 130, 280, isKey = true, intelligenceLabel = "★ Match Altering Stand"),
                DetailedPartnership(5, "Steve Smith", 16, 43, "Matthew Wade", 110, 143, 126, 194, isKey = true, intelligenceLabel = "Dominance Partnership"),
                DetailedPartnership(6, "Matthew Wade", 0, 0, "Tim Paine", 34, 51, 76, 121),
                DetailedPartnership(7, "Tim Paine", 0, 0, "James Pattinson", 2, 7, 2, 7),
                DetailedPartnership(8, "James Pattinson", 45, 41, "Pat Cummins", 26, 24, 78, 57, isKey = true, intelligenceLabel = "Declaration Blitz")
            )
        ),

        // Innings 4: England 146
        InningsScorecard(
            inningsNumber = 4,
            teamName = "England",
            totalScore = "146 (52.3 ov)",
            runRate = 2.78f,
            extras = "16 (b 8, lb 5, w 3, nb 0)",
            batters = listOf(
                BatterScore("Rory Burns", "c Paine b Cummins", 11, 31, 1, 0, 35.48f),
                BatterScore("Jason Roy", "b Lyon", 28, 44, 4, 0, 63.64f),
                BatterScore("Joe Root (c)", "c Bancroft b Lyon", 28, 60, 3, 0, 46.67f),
                BatterScore("Joe Denly", "c Bancroft b Lyon", 11, 28, 2, 0, 39.29f),
                BatterScore("Jos Buttler", "b Cummins", 1, 14, 0, 0, 7.14f),
                BatterScore("Ben Stokes", "c Paine b Cummins", 6, 13, 1, 0, 46.15f),
                BatterScore("Jonny Bairstow", "c Bancroft b Cummins", 6, 20, 1, 0, 30.00f),
                BatterScore("Moeen Ali", "c Warner b Lyon", 4, 20, 0, 0, 20.00f),
                BatterScore("Chris Woakes", "c Smith b Lyon", 31, 54, 5, 0, 57.41f),
                BatterScore("Stuart Broad", "c Smith b Lyon", 0, 1, 0, 0, 0.00f),
                BatterScore("James Anderson", "not out", 4, 1, 1, 0, 400.00f)
            ),
            bowlers = listOf(
                BowlerScore("Pat Cummins", 16.3f, 3, 32, 4, 1.93f),
                BowlerScore("James Pattinson", 8.0f, 0, 29, 0, 3.62f),
                BowlerScore("Nathan Lyon", 20.0f, 5, 49, 6, 2.45f),
                BowlerScore("Peter Siddle", 8.0f, 3, 14, 0, 1.75f)
            ),
            fallOfWickets = listOf(
                FOW("19/1", "Rory Burns", "9.3"),
                FOW("60/2", "Jason Roy", "18.4"),
                FOW("80/3", "Joe Denly", "27.2"),
                FOW("85/4", "Joe Root", "31.4"),
                FOW("85/5", "Jos Buttler", "33.2"),
                FOW("97/6", "Ben Stokes", "37.3"),
                FOW("97/7", "Jonny Bairstow", "39.1"),
                FOW("136/8", "Moeen Ali", "49.3"),
                FOW("136/9", "Stuart Broad", "49.4"),
                FOW("146/10", "Chris Woakes", "52.3")
            ),
            allPartnerships = listOf(
                DetailedPartnership(1, "Rory Burns", 11, 31, "Jason Roy", 8, 26, 19, 57),
                DetailedPartnership(2, "Jason Roy", 20, 18, "Joe Root", 21, 37, 41, 55, isKey = true, intelligenceLabel = "Top Resistance Stand"),
                DetailedPartnership(3, "Joe Root", 7, 23, "Joe Denly", 11, 28, 20, 51),
                DetailedPartnership(4, "Joe Root", 0, 0, "Jos Buttler", 5, 25, 5, 25),
                DetailedPartnership(5, "Jos Buttler", 0, 0, "Ben Stokes", 0, 11, 0, 11),
                DetailedPartnership(6, "Ben Stokes", 6, 13, "Jonny Bairstow", 6, 12, 12, 25),
                DetailedPartnership(7, "Jonny Bairstow", 0, 8, "Moeen Ali", 0, 3, 0, 11),
                DetailedPartnership(8, "Moeen Ali", 4, 17, "Chris Woakes", 31, 45, 39, 62, isKey = true, intelligenceLabel = "Late Delaying Effort"),
                DetailedPartnership(9, "Chris Woakes", 0, 0, "Stuart Broad", 0, 1, 0, 1),
                DetailedPartnership(10, "Chris Woakes", 0, 9, "James Anderson", 4, 1, 10, 18)
            )
        )
    )

    val ashes2019FirstTest = MatchIntelligenceData(
        matchId = 1152846L,
        matchTitle = "1st Test · The Ashes 2019",
        subtitle = "Australia vs England",
        venue = "Edgbaston, Birmingham",
        team1 = australia,
        team2 = england,
        winner = "Australia",
        victoryMargin = "won by 251 runs",
        heroNarrative = "The Test Steve Smith saved.",
        team1ControlPct = 71,
        team2ControlPct = 28,
        evenControlPct = 1,
        matchSummaryPoints = listOf(
            "Australia collapsed to 122/8 on Day 1 under heavy swing bowling.",
            "Steve Smith rescued the innings with a monumental 144.",
            "England took a 90-run first innings lead via Rory Burns' 133.",
            "Smith scored another brilliant century (142) alongside Matthew Wade (110).",
            "Nathan Lyon destroyed England on Day 5 taking 6/49 to seal victory."
        ),
        keyNumbers = KeyMatchNumbers(
            firstInningsLead = "90",
            targetSet = "398",
            manOfTheMatch = "Steve Smith",
            manOfTheMatchDetails = "144 & 142 Runs"
        ),
        heroes = listOf(
            MatchHero(
                name = "Steve Smith",
                role = "Batter",
                team = "Australia",
                rating = 9.8f,
                headlineStat = "286 Runs (144 & 142)",
                impactSnippet = "Twin centuries rescued Australia from 122/8 and set 398 target."
            ),
            MatchHero(
                name = "Nathan Lyon",
                role = "Bowler",
                team = "Australia",
                rating = 9.2f,
                headlineStat = "9 Wickets (6/49 Day 5)",
                impactSnippet = "Dismantled England's batting lineup on a deteriorating final day pitch."
            ),
            MatchHero(
                name = "Rory Burns",
                role = "Batter",
                team = "England",
                rating = 8.4f,
                headlineStat = "144 Runs (133 1st Inn)",
                impactSnippet = "Gritty 512-minute vigil gave England the only lead of the match."
            )
        ),
        topTurningPoint = TopTurningPoint(
            title = "Australia 122/8",
            subtitle = "The 10th-Wicket Resurrection",
            winProbShift = "+38%",
            impactHero = "Steve Smith rescue innings",
            importanceScore = "10/10",
            narrative = "From the brink of total collapse at 122/8, Steve Smith (144) added 162 with the tail—turning a certain match-losing first innings into a competitive total of 284."
        ),
        topEvents = listOf(
            RankedEvent(1, "Australia collapse to 122/8", "Day 1 • Session 2", "Broad and Woakes dismantle the top order, reducing Australia to 122/8."),
            RankedEvent(2, "Smith reaches century (144)", "Day 1 • Session 3", "Smith pairs with Siddle in an 88-run stand to lift Australia to 284."),
            RankedEvent(3, "Burns gives England lead (133)", "Day 3 • Session 1", "Rory Burns bats 512 minutes to secure a 90-run first innings lead for England."),
            RankedEvent(4, "Smith scores second century (142)", "Day 4 • Session 2", "Smith and Wade (110) crush England's hopes, setting a 398-run chase."),
            RankedEvent(5, "Lyon destroys England (6/49)", "Day 5 • Session 2", "Nathan Lyon spins webs on a crumbling pitch to bowl England out for 146.")
        ),
        primaryCollapse = MatchCollapseInfo(
            team = "Australia",
            score = "122/8",
            severity = 76.6f,
            classification = "Major Collapse",
            explanation = "Australia lost 5 wickets for 44 runs in 21.4 overs before Smith rescued them."
        ),
        primaryRecovery = MatchRecoveryInfo(
            team = "Australia",
            transition = "122/8 → 284",
            recoveryScore = 62.0f,
            classification = "Major Recovery",
            explanation = "Smith and the tail transformed the innings by adding 162 runs for the last two wickets."
        ),
        australiaXI = ausXI,
        englandXI = engXI,
        scorecards = scorecardList,
        points = listOf(
            SessionMomentumPoint(1, 1, 1, 1, "Australia", 92, 3, 30f, 3.07f, 15.7f, 0.0f, "Balanced", "Australia steady early, but Broad strikes before lunch."),
            SessionMomentumPoint(2, 1, 2, 1, "Australia", 87, 5, 30f, 2.90f, -38.0f, -53.7f, "Bowling Advantage (ENG)", "Catastrophic collapse: Australia slip from 35/3 to 122/8 under intense swing."),
            SessionMomentumPoint(3, 1, 3, 1, "Australia", 105, 2, 21f, 5.00f, 40.0f, 78.0f, "Dominant Batting (AUS)", "Smith masterclass (144) + Siddle (44) adds 88 runs, lifting Aus to 284."),
            SessionMomentumPoint(4, 2, 1, 2, "England", 74, 1, 30f, 2.47f, -20.0f, -60.0f, "England Rebuilding", "Rory Burns and Joe Root blunt the Australian pace attack."),
            SessionMomentumPoint(5, 2, 2, 2, "England", 111, 1, 30f, 3.70f, -35.0f, -15.0f, "Batting Advantage (ENG)", "Burns reaches century; England seize first-innings initiative."),
            SessionMomentumPoint(6, 2, 3, 2, "England", 82, 2, 30.3f, 2.70f, -17.0f, 18.0f, "Balanced Fightback", "Pattinson and Siddle strike late to halt England runaway."),
            SessionMomentumPoint(7, 3, 1, 2, "England", 77, 4, 30f, 2.57f, 18.0f, 35.0f, "Australia Bowling Blitz", "Cummins and Lyon tear through middle order; collapse restricts lead."),
            SessionMomentumPoint(8, 3, 2, 2, "England", 30, 2, 16f, 1.88f, 22.0f, 4.0f, "England Wrapped Up", "England all out for 374 (90 lead). Momentum steadily returns to Australia."),
            SessionMomentumPoint(9, 3, 3, 3, "Australia", 119, 3, 30.2f, 3.94f, 24.4f, 2.4f, "Aus Counter-Attack", "Smith walks in at 27/2 and immediately counter-attacks English bowling."),
            SessionMomentumPoint(10, 4, 1, 3, "Australia", 112, 1, 30.7f, 3.65f, 31.5f, 7.1f, "Batting Dominance (AUS)", "Smith and Head partnership (130 runs) takes game away from England."),
            SessionMomentumPoint(11, 4, 2, 3, "Australia", 128, 1, 30.3f, 4.22f, 37.2f, 5.7f, "Dominant Batting (AUS)", "Smith scores second century (142); Wade reaches thunderous 110."),
            SessionMomentumPoint(12, 4, 3, 3, "Australia", 128, 2, 22.3f, 5.73f, 47.3f, 10.1f, "Maximum Peak Dominance", "Australia declare at 487/7, setting England an insurmountable target of 398."),
            SessionMomentumPoint(13, 5, 1, 4, "England", 85, 4, 30.3f, 2.80f, 38.0f, -9.3f, "Extreme Pressure (AUS)", "Lyon spins webs on deteriorating Day 5 pitch; England lose top 4."),
            SessionMomentumPoint(14, 5, 2, 4, "England", 61, 6, 22.5f, 2.71f, 60.0f, 22.0f, "Australia Victorious", "Lyon takes 6/49; England bowled out for 146. Australia win by 251 runs.")
        ),
        pressurePoints = listOf(
            SessionPressurePoint(1, 1, 1, 1, "Australia", 92, 3, 30f, 3.07f, 42.5f, "Moderate Pressure", "Broad struck early removing Warner and Bancroft, creating moderate top-order tension."),
            SessionPressurePoint(2, 1, 2, 1, "Australia", 87, 5, 30f, 2.90f, 84.8f, "Extreme Pressure", "Pressure rose to extreme levels as Australia lost 5 wickets for 87 runs, slumping to 122/8 under intense swing."),
            SessionPressurePoint(3, 1, 3, 1, "Australia", 105, 2, 21f, 5.00f, 32.0f, "Low Pressure", "Pressure evaporated as Steve Smith (144) counter-attacked in an 88-run stand with Peter Siddle."),
            SessionPressurePoint(4, 2, 1, 2, "England", 74, 1, 30f, 2.47f, 38.2f, "Low Pressure", "Rory Burns and Joe Root blunted the Australian pace attack with disciplined defense."),
            SessionPressurePoint(5, 2, 2, 2, "England", 111, 1, 30f, 3.70f, 22.4f, "Low Pressure", "Burns reached his century with England scoring freely at 3.7 RPO in complete control."),
            SessionPressurePoint(6, 2, 3, 2, "England", 82, 2, 30.3f, 2.70f, 45.0f, "Moderate Pressure", "Pattinson and Siddle took late wickets, restricting run flow and building moderate pressure."),
            SessionPressurePoint(7, 3, 1, 2, "England", 77, 4, 30f, 2.57f, 73.7f, "Extreme Pressure", "Pressure rose sharply because England lost four wickets in the session while scoring slowly (77 runs in 30 overs)."),
            SessionPressurePoint(8, 3, 2, 2, "England", 30, 2, 16f, 1.88f, 58.0f, "Moderate Pressure", "England's tail succumbed swiftly at 1.88 RPO, ending their innings at 374."),
            SessionPressurePoint(9, 3, 3, 3, "Australia", 119, 3, 30.2f, 3.94f, 52.0f, "Moderate Pressure", "Early loss of Warner and Bancroft (27/2) exerted tension before Smith counter-punched."),
            SessionPressurePoint(10, 4, 1, 3, "Australia", 112, 1, 30.7f, 3.65f, 25.5f, "Low Pressure", "Smith and Travis Head mounted a 130-run partnership, completely removing batting pressure."),
            SessionPressurePoint(11, 4, 2, 3, "Australia", 128, 1, 30.3f, 4.22f, 18.0f, "Low Pressure", "Smith reached his second hundred (142) and Wade struck 110, batting with total freedom."),
            SessionPressurePoint(12, 4, 3, 3, "Australia", 128, 2, 22.3f, 5.73f, 12.0f, "Low Pressure", "Aggressive declaration blitz at 5.73 RPO, leaving England an insurmountable 398 target."),
            SessionPressurePoint(13, 5, 1, 4, "England", 85, 4, 30.3f, 2.80f, 69.6f, "Moderate Pressure", "Chasing 398 on Day 5, England suffered high pressure as Lyon and Cummins dismantled their top 4."),
            SessionPressurePoint(14, 5, 2, 4, "England", 61, 6, 22.5f, 2.71f, 82.0f, "Extreme Pressure", "Extreme collapse as Nathan Lyon (6/49) tore through the middle and lower order in 22 overs.")
        )
    )
}
