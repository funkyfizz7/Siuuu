package com.example.siuu

/**
 * Core Data Model for the Player Inspector Screen.
 * Route: player/{matchId}/{playerId}
 */
data class PlayerInspectorData(
    val matchId: Long,
    val playerId: Long,
    val playerName: String,
    val teamName: String,
    val role: String,
    val matchRating: Double,
    val impactRating: Double,
    val impactRank: Int = 0,
    val battingImpact: Double = 0.0,
    val bowlingImpact: Double = 0.0,
    val matchStoryNarrative: String = "",
    val contributions: List<String> = emptyList(),
    val matchSummary: PlayerMatchSummary,
    val battingInnings: List<PlayerInspectorBattingInnings> = emptyList(),
    val bowlingInnings: List<PlayerInspectorBowlingInnings> = emptyList(),
    val bowlingSpells: List<PlayerInspectorBowlingSpell> = emptyList(),
    val turningPoints: List<PlayerInspectorTurningPoint> = emptyList(),
    val partnerships: List<PlayerInspectorPartnership> = emptyList()
)

/**
 * Match Summary All-Round Aggregates (from fact_player_match.csv)
 */
data class PlayerMatchSummary(
    // Batting
    val inningsBatted: Int = 0,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val dots: Int = 0,
    val outs: Int = 0,
    val highestScore: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val ducks: Int = 0,
    val battingAverage: Double = 0.0,
    val battingStrikeRate: Double = 0.0,
    val dotPct: Double = 0.0,
    val boundaryPct: Double = 0.0,
    val controlPct: Double = 0.0,

    // Bowling
    val inningsBowled: Int = 0,
    val spellsBowled: Int = 0,
    val ballsBowled: Int = 0,
    val oversBowled: Double = 0.0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val bestBowlingInnings: String = "0/0",
    val fiveWicketHauls: Int = 0,
    val tenWicketMatch: Int = 0,
    val bowlingAverage: Double = 0.0,
    val bowlingEconomy: Double = 0.0,
    val bowlingStrikeRate: Double = 0.0,
    val bowlingDotPct: Double = 0.0,
    val lineAccuracyPct: Double = 0.0,
    val lengthAccuracyPct: Double = 0.0,

    // Fielding
    val catches: Int = 0,
    val runOuts: Int = 0,
    val stumpings: Int = 0
)

/**
 * Individual Batting Innings Record (from fact_player_innings.csv)
 */
data class PlayerInspectorBattingInnings(
    val innings: Int,
    val battingPosition: Int,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val dots: Int,
    val singles: Int = 0,
    val doubles: Int = 0,
    val triples: Int = 0,
    val strikeRate: Double,
    val ballsTo50: Int = 0,
    val ballsTo100: Int = 0,
    val arrivalRuns: Int = 0,
    val arrivalWickets: Int = 0,
    val arrivalScore: String,
    val arrivalOvers: Double,
    val departureRuns: Int = 0,
    val departureWickets: Int = 0,
    val departureScore: String,
    val departureOvers: Double,
    val isOut: Boolean,
    val dismissalType: String,
    val dismissedBy: String,
    val fielder: String,
    val controlPct: Double,
    val falseShotPct: Double,
    val attackingPct: Double,
    val leavePct: Double,
    val dotPct: Double,
    val boundaryPct: Double,
    val highestPartnershipRuns: Int,
    val highestPartnershipPartner: String,
    val partnerships: List<PlayerInspectorPartnership> = emptyList(),
    val scorecardDismissal: String = "",
    val wagonWheel: PlayerWagonWheelData? = null
)

/**
 * Continuous Bowling Spell Record (from fact_bowling_spells.csv)
 */
data class PlayerInspectorBowlingSpell(
    val innings: Int,
    val spellNumber: Int,
    val bowlerId: Long,
    val bowlerName: String,
    val team: String,
    val startOver: Int,
    val endOver: Int,
    val oversCount: Int,
    val ballsBowled: Int,
    val maidens: Int,
    val runsConceded: Int,
    val wickets: Int,
    val economy: Double,
    val dots: Int,
    val dotPct: Double,
    val foursConceded: Int,
    val sixesConceded: Int,
    val wicketsTakenList: String,
    val spellImpactScore: Double
)

/**
 * Turning Point Involving Player (from fact_match_turning_points.csv)
 */
data class PlayerInspectorTurningPoint(
    val rank: Int,
    val innings: Int,
    val eventType: String,
    val title: String,
    val description: String,
    val impactScore: Double,
    val runsAdded: Int = 0,
    val ballsFaced: Int = 0,
    val startBall: Double = 0.0,
    val endBall: Double = 0.0
)

/**
 * Player Partnership Record (from fact_player_partnerships.csv)
 */
data class PlayerInspectorPartnership(
    val innings: Int,
    val partnerId: Long,
    val partnerName: String,
    val runs: Int,
    val balls: Int,
    val playerRuns: Int = 0,
    val partnerRuns: Int = 0,
    val wicketsBefore: Int = 0,
    val wicketsAfter: Int = 0,
    val startScore: String = "0/0",
    val endScore: String = "0/0",
    val importanceScore: Double = 0.0
)

/**
 * Innings Bowling Figures Record
 */
data class PlayerInspectorBowlingInnings(
    val innings: Int,
    val overs: Double,
    val maidens: Int,
    val runsConceded: Int,
    val wickets: Int,
    val economy: Double,
    val dotPct: Double = 0.0,
    val spellsCount: Int = 1,
    val lineAccuracyPct: Double = 0.0,
    val lengthAccuracyPct: Double = 0.0,
    val spells: List<PlayerInspectorBowlingSpell> = emptyList(),
    val heatmap: BowlingHeatmapData? = null
)

/**
 * Commentary-derived Wagon Wheel Sector Breakdown
 */
data class WagonWheelSector(
    val sectorId: String,
    val label: String,
    val shotsCount: Int = 0,
    val runsTotal: Int = 0,
    val dots: Int = 0,
    val singles: Int = 0,
    val doubles: Int = 0,
    val triples: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0
)

/**
 * Individual Classified Shot Delivery for Wagon Wheel
 */
data class WagonWheelDelivery(
    val key: String,
    val over: Int,
    val ball: Int,
    val runs: Int,
    val totalRuns: Int,
    val sector: String,
    val angle: Float?,
    val provenance: String
)

/**
 * Batting Innings Wagon Wheel Aggregate Data
 */
data class PlayerWagonWheelData(
    val totalBalls: Int = 0,
    val totalRuns: Int = 0,
    val scoringBalls: Int = 0,
    val directlyClassifiedBalls: Int = 0,
    val unknownDirectionBalls: Int = 0,
    val directlyClassifiedBoundaries: Int = 0,
    val totalBoundaries: Int = 0,
    val isLeftHanded: Boolean = false,
    val sectors: Map<String, WagonWheelSector> = emptyMap(),
    val deliveries: List<WagonWheelDelivery> = emptyList()
) {
    val coveragePct: Double
        get() = if (scoringBalls > 0) (directlyClassifiedBalls.toDouble() / scoringBalls) * 100.0 else 0.0

    val boundaryCoveragePct: Double
        get() = if (totalBoundaries > 0) (directlyClassifiedBoundaries.toDouble() / totalBoundaries) * 100.0 else 0.0
}

/**
 * Bowling Innings Combined 3x3 Heatmap Data
 * Rows: Full (<6m), Good (6-8m), Short (>8m)
 * Columns: Outside Off, Off/Middle/Stumps, Leg Side
 */
data class BowlingHeatmapData(
    val totalBalls: Int = 0,
    val classifiedLengthCount: Int = 0,
    val classifiedLineCount: Int = 0,
    val bothLengthAndLineCount: Int = 0,
    val fullTossCount: Int = 0,
    val unknownLengthCount: Int = 0,
    val unknownLineCount: Int = 0,
    val matrix: Map<String, Map<String, Int>> = emptyMap(),
    val lengthTotals: Map<String, Int> = emptyMap(),
    val lineTotals: Map<String, Int> = emptyMap()
) {
    fun getCellCount(length: String, line: String): Int {
        return matrix[length]?.get(line) ?: 0
    }

    val lengthCoveragePct: Double
        get() = if (totalBalls > 0) (classifiedLengthCount.toDouble() / totalBalls) * 100.0 else 0.0

    val bothCoveragePct: Double
        get() = if (totalBalls > 0) (bothLengthAndLineCount.toDouble() / totalBalls) * 100.0 else 0.0
}

