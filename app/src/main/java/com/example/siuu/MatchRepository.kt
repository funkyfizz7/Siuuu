package com.example.siuu

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.abs

/**
 * Strips parentheses and brackets around years/seasons (e.g. "(2019)" -> "2019", "(2016/17)" -> "2016/17", "(2020-21)" -> "2020-21")
 */
fun String.cleanYearBrackets(): String {
    return this
        .replace(Regex("""\s*[\(\{\[]\s*(\d{4}(?:[/-]\d{2,4})?)\s*[\)\}\]]""")) { " " + it.groupValues[1] }
        .replace("(", "")
        .replace(")", "")
        .replace("{", "")
        .replace("}", "")
        .replace("[", "")
        .replace("]", "")
        .replace(Regex("""\s+"""), " ")
        .trim()
}

/**
 * Match Summary Header Model
 */
data class MatchHeaderInfo(
    val matchTitle: String,
    val team1Name: String,
    val team1ShortName: String,
    val team1Color: Color,
    val team2Name: String,
    val team2ShortName: String,
    val team2Color: Color,
    val winner: String,
    val victoryMargin: String,
    val venue: String,
    val seriesName: String = "",
    val matchNumber: String = "",
    val seriesAndMatch: String = "",
    val playerOfMatch: String = ""
)

/**
 * Match Control Share Model (Computed in Repository)
 */
data class MatchControlShareInfo(
    val team1ShortName: String,
    val team1Color: Color,
    val team1Pct: Int,
    val team2ShortName: String,
    val team2Color: Color,
    val team2Pct: Int,
    val evenPct: Int
)

/**
 * Match Turning Point Model for Match Screen
 */
data class TurningPointItem(
    val rank: Int = 1,
    val inningsNo: Int = 1,
    val eventType: String = "WICKET",
    val title: String = "",
    val description: String = "",
    val importanceScore: Double = 9.0,
    val importanceTier: String = "Major Turning Point",
    val player: String = "",
    val team: String = "",
    val overNo: Double = 0.0
)

/**
 * Player of the Match Summary for Summary Tab Header
 */
data class PlayerOfMatchSummary(
    val playerId: Long? = null,
    val name: String = "",
    val team: String = "",
    val headlinePerformance: String = "",
    val battingRating: Double = 0.0,
    val bowlingRating: Double = 0.0,
    val impactRating: Double = 0.0,
    val overallRating: Double = 0.0
)

/**
 * Unified Summary Data Model for Summary Tab (Production FotMob Style)
 */
data class MatchSummaryData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val snapshot: MatchSnapshotScore = MatchSnapshotScore("", "", "", "", "5.0"),
    val matchStory: String = "",
    val matchStorySentences: List<String> = emptyList(),
    val heroes: List<MatchHero> = emptyList(),
    val keyNumbers: KeyMatchNumbers = KeyMatchNumbers(),
    val controlSummary: MatchControlSummary = MatchControlSummary("", "", "", 0, "", 0, 0),
    val topTurningPoints: List<TurningPointItem> = emptyList(),
    val controlShare: MatchControlShareInfo = MatchControlShareInfo("", Color.Unspecified, 50, "", Color.Unspecified, 50, 0),
    val summaryPoints: List<String> = emptyList(),
    val playerOfMatchSummary: PlayerOfMatchSummary = PlayerOfMatchSummary(),
    val playerTeamMap: Map<String, String> = emptyMap(),
    val momentumPoints: List<MomentumModel> = emptyList()
)

/**
 * Unified Story Tab Data Model for Story Tab
 */
data class StoryTabDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val timeline: List<MatchStoryModel>,
    val primaryCollapse: CollapseModel,
    val primaryRecovery: RecoveryModel,
    val narrative: String
)

/**
 * Unified Momentum Tab Data Model for Momentum Tab
 */
data class MomentumTabDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val points: List<MomentumModel>,
    val topSwings: List<MomentumModel>
)

/**
 * Match Verdict Node Model for Causal Sequence
 */
data class MatchVerdictNode(
    val stepIndex: Int = 1,
    val phase: String = "",
    val headline: String = "",
    val description: String = "",
    val scoreSnippet: String = "",
    val isDecisive: Boolean = false,
    val nodeType: String = "TURNING_POINT"
)

/**
 * Unified Turning Points Tab Data Model
 */
data class TurningPointsDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val topTurningPoints: List<TurningPointItem> = emptyList(),
    val turningPoints: List<TurningPointItem> = emptyList(),
    val matchVerdictChain: List<MatchVerdictNode> = emptyList(),
    val inningsOvers: List<Double> = emptyList()
)

/**
 * Unified Pressure Tab Data Model
 */
data class PressureTabDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val pressurePoints: List<PressureModel>,
    val highestPressureMoments: List<PressureModel>
)

/**
 * Unified Ratings Tab Data Model
 * Sources: fact_player_match_ratings & fact_player_match_impact
 */
data class RatingsTabDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val team1Players: List<PlayerMatchRatingModel>,
    val team2Players: List<PlayerMatchRatingModel>,
    val mvpPlayer: PlayerMatchRatingModel,
    val impactRankings: List<PlayerMatchImpactModel>,
    val playerInningsDetails: Map<Int, PlayerInningsDetailsProfile> = emptyMap()
)

/**
 * Single Innings Scorecard Data
 */
data class SingleInningsScorecardData(
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

/**
 * Unified Scorecard Tab Data Model
 */

/**
 * Match Index Item Model for Match Browser
 */
data class MatchIndexItem(
    val matchId: Long,
    val matchTitle: String,
    val seriesHeader: String = "",
    val testSubHeader: String = "",
    val team1: String,
    val team1Short: String = "",
    val team1Score: String = "",
    val team2: String,
    val team2Short: String = "",
    val team2Score: String = "",
    val winner: String,
    val victoryMargin: String = "",
    val venue: String,
    val season: String,
    val file: String
)

/**
 * Series Index Item Model for Series Browser
 */
data class SeriesIndexItem(
    val seriesId: Int,
    val seriesName: String,
    val season: String,
    val startDate: String,
    val endDate: String,
    val team1: String,
    val team2: String,
    val totalMatches: Int,
    val winner: String,
    val resultMargin: String,
    val venueCountries: String
)

/**
 * Series Header Data Model for SeriesScreen
 */
data class SeriesHeaderData(
    val seriesId: Int,
    val seriesName: String,
    val season: String,
    val startDate: String,
    val endDate: String,
    val team1: String,
    val team2: String,
    val totalMatches: Int,
    val winner: String,
    val resultMargin: String,
    val finalSeriesScore: String,
    val venueCountries: String
)

/**
 * Series Player Stat Item for Series Overview & Analytics
 * Source: fact_series_player_stats.csv & dim_players.json
 */
data class SeriesPlayerStatItem(
    val seriesId: Int,
    val playerId: Long,
    val playerName: String,
    val shortName: String,
    val country: String,
    val matches: Int,
    val runs: Int,
    val balls: Int,
    val average: Double,
    val centuries: Int,
    val fifties: Int,
    val wickets: Int,
    val bestBowling: String,
    val catches: Int,
    val ratingAvg: Double,
    val impactAvg: Double,
    val totalImpact: Double
)

/**
 * Series Match Item for Overview match breakdown
 * Source: fact_series_matches.csv
 */
data class SeriesMatchSummaryItem(
    val seriesId: Int,
    val matchId: Long,
    val matchNumber: String,
    val winner: String,
    val margin: String
)

/**
 * Series Overview Tab Display Data Model
 * Aggregates Series MVP, Leading Batter, Leading Bowler, and Series Result breakdown.
 */
data class SeriesOverviewData(
    val header: SeriesHeaderData,
    val mvp: SeriesPlayerStatItem,
    val leadingBatter: SeriesPlayerStatItem,
    val leadingBowler: SeriesPlayerStatItem,
    val matches: List<SeriesMatchSummaryItem>,
    val allPlayers: List<SeriesPlayerStatItem> = emptyList()
)

/**
 * Series Timeline Match Card Item
 * Sources: fact_series_matches.csv, fact_series_standings.csv, matches_index.json
 */
data class SeriesTimelineItem(
    val seriesId: Int,
    val matchId: Long,
    val matchIndex: Int, // 1, 2, 3...
    val matchNumber: String, // "1st Test", "2nd Test"...
    val winner: String, // "Australia", "England", "Draw"
    val margin: String, // "251 runs", "Match Drawn", "1 wicket"
    val seriesScoreAfterMatch: String, // "AUS 1-0", "1-1", "AUS 2-1", "2-2"
    val team1Wins: Int,
    val team2Wins: Int,
    val draws: Int,
    val venue: String = "",
    val team1: String = "",
    val team1Short: String = "",
    val team1Score: String = "",
    val team2: String = "",
    val team2Short: String = "",
    val team2Score: String = ""
)

/**
 * Series Timeline Tab Display Data Model
 */
data class SeriesTimelineData(
    val header: SeriesHeaderData,
    val timeline: List<SeriesTimelineItem>
)

/**
 * Featured Match Item for PitchIQ Home Screen 7-Card Carousel
 * Source: fact_home_featured_matches.csv & matches_index.json
 */
data class HomeFeaturedMatchItem(
    val matchId: Long,
    val seriesId: Int,
    val featuredOrder: Int,
    val matchNumber: String, // e.g. "1st Test", "3rd Test"
    val seriesName: String, // e.g. "Ashes 2019", "Border-Gavaskar Trophy 2020-21"
    val venue: String, // e.g. "Edgbaston", "The Gabba, Brisbane"
    val team1: String,
    val team1Short: String,
    val team1Score: String, // e.g. "374 & 146"
    val team2: String,
    val team2Short: String,
    val team2Score: String, // e.g. "487 & 401/8d"
    val winner: String,
    val victoryMargin: String // e.g. "Australia won by 251 runs", "England won by 1 wicket"
)

data class ScorecardTabDisplayData(
    val matchId: Long,
    val header: MatchHeaderInfo,
    val scorecards: List<SingleInningsScorecardData>,
    val allRatings: List<PlayerMatchRatingModel> = emptyList(),
    val playerInningsDetails: Map<Int, PlayerInningsDetailsProfile> = emptyMap()
)

/**
 * PitchIQ Match Repository Interface
 * Provides access to warehouse analytics across all 166 matches.
 */
interface MatchRepository {
    suspend fun getMatchStory(matchId: Long): List<MatchStoryModel>
    suspend fun getMatchMomentum(matchId: Long): List<MomentumModel>
    suspend fun getMatchPressure(matchId: Long): List<PressureModel>
    suspend fun getMatchCollapses(matchId: Long): List<CollapseModel>
    suspend fun getMatchRecoveries(matchId: Long): List<RecoveryModel>
    suspend fun getMatchRatings(matchId: Long): List<PlayerRatingModel>
    suspend fun getPlayerMatchRatings(matchId: Long): List<PlayerMatchRatingModel>
    suspend fun getPlayerMatchImpact(matchId: Long): List<PlayerMatchImpactModel>
    suspend fun getPlayerInnings(matchId: Long): List<PlayerInningsModel>
    suspend fun getBowlingInnings(matchId: Long): List<BowlingInningsModel>
    suspend fun getMatchSummary(matchId: Long): MatchSummaryData
    suspend fun getStoryTabData(matchId: Long): StoryTabDisplayData
    suspend fun getMomentumTabData(matchId: Long): MomentumTabDisplayData
    suspend fun getTurningPointsTabData(matchId: Long): TurningPointsDisplayData
    suspend fun getPressureTabData(matchId: Long): PressureTabDisplayData
    suspend fun getRatingsTabData(matchId: Long): RatingsTabDisplayData
    suspend fun getScorecardTabData(matchId: Long): ScorecardTabDisplayData
    suspend fun getPlayerInspectorData(matchId: Long, playerId: Long): PlayerInspectorData
    suspend fun getMatchList(): List<MatchIndexItem>
    suspend fun getSeriesList(): List<SeriesIndexItem>
    suspend fun getSeriesHeader(seriesId: Int): SeriesHeaderData
    suspend fun getSeriesOverview(seriesId: Int): SeriesOverviewData
    suspend fun getSeriesTimeline(seriesId: Int): SeriesTimelineData
    suspend fun getSeriesIntelligence(seriesId: Int): SeriesIntelligenceData
    suspend fun getHomeFeaturedMatches(): List<HomeFeaturedMatchItem>

    // Synchronous accessors
    fun getMatchStorySync(matchId: Long): List<MatchStoryModel>
    fun getMatchMomentumSync(matchId: Long): List<MomentumModel>
    fun getMatchPressureSync(matchId: Long): List<PressureModel>
    fun getMatchCollapsesSync(matchId: Long): List<CollapseModel>
    fun getMatchRecoveriesSync(matchId: Long): List<RecoveryModel>
    fun getMatchRatingsSync(matchId: Long): List<PlayerRatingModel>
    fun getPlayerMatchRatingsSync(matchId: Long): List<PlayerMatchRatingModel>
    fun getPlayerMatchImpactSync(matchId: Long): List<PlayerMatchImpactModel>
    fun getPlayerInningsSync(matchId: Long): List<PlayerInningsModel>
    fun getBowlingInningsSync(matchId: Long): List<BowlingInningsModel>
    fun getMatchSummarySync(matchId: Long): MatchSummaryData
    fun getStoryTabDataSync(matchId: Long): StoryTabDisplayData
    fun getMomentumTabDataSync(matchId: Long): MomentumTabDisplayData
    fun getTurningPointsTabDataSync(matchId: Long): TurningPointsDisplayData
    fun getPressureTabDataSync(matchId: Long): PressureTabDisplayData
    fun getRatingsTabDataSync(matchId: Long): RatingsTabDisplayData
    fun getScorecardTabDataSync(matchId: Long): ScorecardTabDisplayData
    fun getPlayerInspectorDataSync(matchId: Long, playerId: Long): PlayerInspectorData
    fun getMatchListSync(): List<MatchIndexItem>
    fun getSeriesListSync(): List<SeriesIndexItem>
    fun getSeriesHeaderSync(seriesId: Int): SeriesHeaderData
    fun getSeriesOverviewSync(seriesId: Int): SeriesOverviewData
    fun getSeriesTimelineSync(seriesId: Int): SeriesTimelineData
    fun getSeriesIntelligenceSync(seriesId: Int): SeriesIntelligenceData
    fun getHomeFeaturedMatchesSync(): List<HomeFeaturedMatchItem>
}

/**
 * Default Implementation of MatchRepository
 * Fetches pre-compiled Gold Match Intelligence for any of the 166 warehouse matches,
 * with zero blank spaces and instant loading.
 */
class DefaultMatchRepository(private val context: Context? = null) : MatchRepository {

    private fun getContext(): Context? {
        return context ?: _appContext
    }

    private fun getMatchJson(matchId: Long): JSONObject? {
        jsonCache[matchId]?.let { return it }
        val ctx = getContext() ?: return null
        val jsonStr = AssetWarehouseLoader.loadJson(ctx, "matches/match_$matchId.json")
        if (jsonStr.isBlank()) return null
        return try {
            val obj = JSONObject(jsonStr)
            jsonCache[matchId] = obj
            obj
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getMatchStory(matchId: Long): List<MatchStoryModel> = withContext(Dispatchers.IO) {
        getMatchStorySync(matchId)
    }

    override suspend fun getMatchMomentum(matchId: Long): List<MomentumModel> = withContext(Dispatchers.IO) {
        getMatchMomentumSync(matchId)
    }

    override suspend fun getMatchPressure(matchId: Long): List<PressureModel> = withContext(Dispatchers.IO) {
        getMatchPressureSync(matchId)
    }

    override suspend fun getMatchCollapses(matchId: Long): List<CollapseModel> = withContext(Dispatchers.IO) {
        getMatchCollapsesSync(matchId)
    }

    override suspend fun getMatchRecoveries(matchId: Long): List<RecoveryModel> = withContext(Dispatchers.IO) {
        getMatchRecoveriesSync(matchId)
    }

    override suspend fun getMatchRatings(matchId: Long): List<PlayerRatingModel> = withContext(Dispatchers.IO) {
        getMatchRatingsSync(matchId)
    }

    override suspend fun getPlayerMatchRatings(matchId: Long): List<PlayerMatchRatingModel> = withContext(Dispatchers.IO) {
        getPlayerMatchRatingsSync(matchId)
    }

    override suspend fun getPlayerMatchImpact(matchId: Long): List<PlayerMatchImpactModel> = withContext(Dispatchers.IO) {
        getPlayerMatchImpactSync(matchId)
    }

    override suspend fun getPlayerInnings(matchId: Long): List<PlayerInningsModel> = withContext(Dispatchers.IO) {
        getPlayerInningsSync(matchId)
    }

    override suspend fun getBowlingInnings(matchId: Long): List<BowlingInningsModel> = withContext(Dispatchers.IO) {
        getBowlingInningsSync(matchId)
    }

    override suspend fun getMatchSummary(matchId: Long): MatchSummaryData = withContext(Dispatchers.IO) {
        getMatchSummarySync(matchId)
    }

    override suspend fun getStoryTabData(matchId: Long): StoryTabDisplayData = withContext(Dispatchers.IO) {
        getStoryTabDataSync(matchId)
    }

    override suspend fun getMomentumTabData(matchId: Long): MomentumTabDisplayData = withContext(Dispatchers.IO) {
        getMomentumTabDataSync(matchId)
    }

    override suspend fun getTurningPointsTabData(matchId: Long): TurningPointsDisplayData = withContext(Dispatchers.IO) {
        getTurningPointsTabDataSync(matchId)
    }

    override suspend fun getPressureTabData(matchId: Long): PressureTabDisplayData = withContext(Dispatchers.IO) {
        getPressureTabDataSync(matchId)
    }

    override suspend fun getRatingsTabData(matchId: Long): RatingsTabDisplayData = withContext(Dispatchers.IO) {
        getRatingsTabDataSync(matchId)
    }

    override suspend fun getScorecardTabData(matchId: Long): ScorecardTabDisplayData = withContext(Dispatchers.IO) {
        getScorecardTabDataSync(matchId)
    }

    override suspend fun getMatchList(): List<MatchIndexItem> = withContext(Dispatchers.IO) {
        getMatchListSync()
    }

    override suspend fun getSeriesList(): List<SeriesIndexItem> = withContext(Dispatchers.IO) {
        getSeriesListSync()
    }

    override suspend fun getSeriesHeader(seriesId: Int): SeriesHeaderData = withContext(Dispatchers.IO) {
        getSeriesHeaderSync(seriesId)
    }

    override suspend fun getSeriesOverview(seriesId: Int): SeriesOverviewData = withContext(Dispatchers.IO) {
        getSeriesOverviewSync(seriesId)
    }

    override suspend fun getSeriesTimeline(seriesId: Int): SeriesTimelineData = withContext(Dispatchers.IO) {
        getSeriesTimelineSync(seriesId)
    }

    private fun parseHexColor(hex: String?): Color? {
        if (hex.isNullOrBlank() || !hex.startsWith("#")) return null
        return try {
            val colorInt = android.graphics.Color.parseColor(hex)
            Color(colorInt)
        } catch (e: Exception) {
            null
        }
    }

    private fun getTeamBrandColor(teamName: String): Color {
        val t = teamName.lowercase().trim()
        return when {
            t.contains("west indies") || t.contains("windies") || t == "wi" -> Color(0xFF9E1B32) // Maroon
            t.contains("australia") || t == "aus" -> Color(0xFFFFC72C) // Yellow
            t.contains("india") || t == "ind" -> Color(0xFF1E88E5)     // Royal Blue
            t.contains("england") || t == "eng" -> Color(0xFF38BDF8)   // Light Blue
            t.contains("pakistan") || t == "pak" -> Color(0xFF16A34A)  // Green
            t.contains("bangladesh") || t == "ban" -> Color(0xFF006A4E) // Dark Green
            t.contains("new zealand") || t == "nz" -> Color(0xFF475569) // Charcoal Black
            t.contains("sri lanka") || t == "sl" -> Color(0xFF0F3875)   // Dark Blue
            t.contains("south africa") || t == "sa" -> Color(0xFF007A3D) // Protea Green
            t.contains("afghanistan") || t == "afg" -> Color(0xFF0066CC) // Cobalt Blue
            t.contains("ireland") || t == "ire" -> Color(0xFF2ECC71)     // Emerald Green
            t.contains("zimbabwe") || t == "zim" -> Color(0xFFDC2626)    // Red
            else -> Color(0xFFFFC72C)
        }
    }

    private fun addPlayerVariants(map: MutableMap<String, String>, fullName: String, team: String) {
        val clean = fullName.trim()
        if (clean.isBlank()) return
        map[clean.lowercase()] = team
        val parts = clean.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val first = parts.first()
            val last = parts.last()
            map[last.lowercase()] = team
            map["${first.take(1)} $last".lowercase()] = team
            map["${first.take(1)}. $last".lowercase()] = team
            if (parts.size > 2) {
                map["${parts[0]} ${parts[1]}".lowercase()] = team
            }
        }
    }

    private fun parseHeader(json: JSONObject): MatchHeaderInfo {
        val matchId = json.optLong("matchId", 0L)
        val h = json.optJSONObject("header") ?: JSONObject()
        val sumTab = json.optJSONObject("summaryTab")
        val sumHeader = sumTab?.optJSONObject("header")

        val t1Name = sumHeader?.optString("team1Name") ?: h.optString("team1Name", "Team 1")
        val t2Name = sumHeader?.optString("team2Name") ?: h.optString("team2Name", "Team 2")
        val t1Color = parseHexColor(sumHeader?.optString("team1Color") ?: h.optString("team1Color")) ?: getTeamBrandColor(t1Name)
        val t2Color = parseHexColor(sumHeader?.optString("team2Color") ?: h.optString("team2Color")) ?: getTeamBrandColor(t2Name)

        val indexItem = if (matchId > 0) getMatchListSync().firstOrNull { it.matchId == matchId } else null

        val rawTitle = h.optString("matchTitle", indexItem?.matchTitle ?: "Test Match").cleanYearBrackets()
        val rawMargin = sumHeader?.optString("resultTitle") ?: h.optString("victoryMargin", "").trim()
        val winner = sumHeader?.optString("winner") ?: h.optString("winner", indexItem?.winner ?: t1Name)
        val venue = (sumHeader?.optString("venue") ?: h.optString("venue", indexItem?.venue ?: "Cricket Stadium")).substringBefore(",").trim()

        val victoryMargin = when {
            rawMargin.isNotBlank() && !rawMargin.equals("Won", ignoreCase = true) && !rawMargin.contains("won the match", ignoreCase = true) -> rawMargin
            indexItem != null && indexItem.victoryMargin.isNotBlank() -> indexItem.victoryMargin
            winner.equals("Match Drawn", ignoreCase = true) || winner.equals("Draw", ignoreCase = true) -> "Match Drawn"
            winner.isNotBlank() -> "$winner won"
            else -> "Match Drawn"
        }

        val seriesName = (sumHeader?.optString("seriesName") ?: (indexItem?.seriesHeader ?: "")).cleanYearBrackets()
        val matchNumber = sumHeader?.optString("matchNumber") ?: indexItem?.testSubHeader?.split(",")?.firstOrNull()?.trim() ?: "Test Match"
        val seriesAndMatch = sumHeader?.optString("seriesAndMatch") ?: (if (seriesName.isNotBlank()) "$seriesName • $matchNumber" else rawTitle)
        val playerOfMatch = sumHeader?.optString("playerOfMatch")
            ?: json.optJSONObject("ratingsTab")?.optJSONObject("mvpPlayer")?.optString("playerName")
            ?: json.optJSONObject("keyNumbers")?.optString("manOfTheMatch")
            ?: "Match MVP"

        return MatchHeaderInfo(
            matchTitle = rawTitle,
            team1Name = t1Name,
            team1ShortName = sumHeader?.optString("team1ShortName") ?: h.optString("team1ShortName", t1Name.take(3).uppercase()),
            team1Color = t1Color,
            team2Name = t2Name,
            team2ShortName = sumHeader?.optString("team2ShortName") ?: h.optString("team2ShortName", t2Name.take(3).uppercase()),
            team2Color = t2Color,
            winner = winner,
            victoryMargin = victoryMargin,
            venue = venue,
            seriesName = seriesName,
            matchNumber = matchNumber,
            seriesAndMatch = seriesAndMatch,
            playerOfMatch = playerOfMatch
        )
    }

    fun getPlayerIdByName(matchId: Long, name: String): Long? {
        if (name.isBlank()) return null
        val clean = name.trim().lowercase()
        val surname = clean.substringAfterLast(" ")
        // 1. Check fact_player_match.csv
        val ctx = getContext()
        if (ctx != null) {
            val fpmList = playerMatchCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_match.csv").also { playerMatchCsvCache = it }
            val matchRows = fpmList.filter { it["match_id"]?.toLongOrNull() == matchId }
            val direct = matchRows.firstOrNull { 
                val pName = (it["player_name"] ?: "").trim().lowercase()
                pName == clean || pName.contains(clean) || clean.contains(pName)
            }
            if (direct != null) {
                val pid = direct["player_id"]?.toLongOrNull()
                if (pid != null) return pid
            }
            if (surname.isNotBlank()) {
                val surnameMatches = matchRows.filter {
                    val pName = (it["player_name"] ?: "").trim().lowercase()
                    pName.endsWith(surname) || pName.substringAfterLast(" ") == surname
                }
                if (surnameMatches.size == 1) {
                    val pid = surnameMatches.first()["player_id"]?.toLongOrNull()
                    if (pid != null) return pid
                }
            }
        }
        // 2. Check JSON ratingsTab if available
        val root = getMatchJson(matchId)
        if (root != null) {
            val rTab = root.optJSONObject("ratingsTab")
            if (rTab != null) {
                val t1 = rTab.optJSONArray("team1Players")
                val t2 = rTab.optJSONArray("team2Players")
                val arrs = listOfNotNull(t1, t2)
                for (arr in arrs) {
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pName = pObj.optString("playerName", "").trim().lowercase()
                        if (pName == clean || pName.contains(clean) || clean.contains(pName) || (surname.isNotBlank() && pName.substringAfterLast(" ") == surname)) {
                            val pid = pObj.optString("playerId", "").toLongOrNull()
                            if (pid != null) return pid
                        }
                    }
                }
            }
        }
        return null
    }

    override fun getMatchSummarySync(matchId: Long): MatchSummaryData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val sumTab = root.optJSONObject("summaryTab")

            // 1. Match Snapshot
            val snapObj = sumTab?.optJSONObject("snapshot")
            val snapshot = if (snapObj != null) {
                MatchSnapshotScore(
                    team1Name = snapObj.optString("team1Name", header.team1Name),
                    team1Scores = snapObj.optString("team1Scores", "-"),
                    team2Name = snapObj.optString("team2Name", header.team2Name),
                    team2Scores = snapObj.optString("team2Scores", "-"),
                    daysUsed = snapObj.optString("daysUsed", "5.0")
                )
            } else {
                MatchSnapshotScore(
                    team1Name = header.team1Name,
                    team1Scores = "-",
                    team2Name = header.team2Name,
                    team2Scores = "-",
                    daysUsed = "5.0"
                )
            }

            // 2. Match Story
            val matchStory = sumTab?.optString("matchStory") ?: ""
            val matchStorySentences = mutableListOf<String>()
            val stArr = sumTab?.optJSONArray("matchStorySentences")
            if (stArr != null) {
                for (i in 0 until stArr.length()) matchStorySentences.add(stArr.getString(i))
            } else {
                val spArr = root.optJSONArray("summaryPoints")
                if (spArr != null) {
                    for (i in 0 until spArr.length()) matchStorySentences.add(spArr.getString(i))
                }
            }

            // 3. Match Heroes (Top 3)
            val heroes = mutableListOf<MatchHero>()
            val heroesArr = sumTab?.optJSONArray("heroes") ?: root.optJSONArray("heroes")
            if (heroesArr != null) {
                for (i in 0 until heroesArr.length().coerceAtMost(3)) {
                    val obj = heroesArr.getJSONObject(i)
                    val hName = obj.optString("name", "Player")
                    val hPid = if (obj.has("playerId") && !obj.isNull("playerId")) obj.optLong("playerId") else getPlayerIdByName(matchId, hName)
                    heroes.add(
                        MatchHero(
                            playerId = hPid,
                            name = hName,
                            role = obj.optString("role", "Player"),
                            team = obj.optString("team", header.team1Name),
                            rating = obj.optDouble("rating", 9.0).toFloat(),
                            headlineStat = obj.optString("headlineStat", "Performance"),
                            impactSnippet = obj.optString("impactSnippet", "Top Performer")
                        )
                    )
                }
            }

            // 4. Key Numbers
            val knObj = sumTab?.optJSONObject("keyNumbers") ?: root.optJSONObject("keyNumbers") ?: JSONObject()
            val keyNumbers = KeyMatchNumbers(
                mostRuns = knObj.optString("mostRuns", "-"),
                mostWickets = knObj.optString("mostWickets", "-"),
                highestPartnership = knObj.optString("highestPartnership", "-"),
                bestBowling = knObj.optString("bestBowling", "-"),
                firstInningsLead = knObj.optString("firstInningsLead", "-"),
                targetSet = knObj.optString("targetSet", "-"),
                manOfTheMatch = knObj.optString("manOfTheMatch", header.playerOfMatch),
                manOfTheMatchDetails = knObj.optString("manOfTheMatchDetails", "")
            )

            // 5. Match Control Summary
            val ctrlObj = sumTab?.optJSONObject("controlSummary")
            val controlSummary = if (ctrlObj != null) {
                MatchControlSummary(
                    momentumWinner = ctrlObj.optString("momentumWinner", header.winner),
                    pressureAppliedBetter = ctrlObj.optString("pressureAppliedBetter", header.winner),
                    team1Name = ctrlObj.optString("team1Name", header.team1Name),
                    team1SessionsWon = ctrlObj.optInt("team1SessionsWon", 0),
                    team2Name = ctrlObj.optString("team2Name", header.team2Name),
                    team2SessionsWon = ctrlObj.optInt("team2SessionsWon", 0),
                    neutralSessions = ctrlObj.optInt("neutralSessions", 0)
                )
            } else {
                MatchControlSummary(
                    momentumWinner = header.winner,
                    pressureAppliedBetter = header.winner,
                    team1Name = header.team1Name,
                    team1SessionsWon = 0,
                    team2Name = header.team2Name,
                    team2SessionsWon = 0,
                    neutralSessions = 0
                )
            }

            // 6. Turning Points (Top 3)
            val turningPoints = mutableListOf<TurningPointItem>()
            val tpArr = sumTab?.optJSONArray("topTurningPoints")
                ?: root.optJSONObject("turningPointsTab")?.optJSONArray("topTurningPoints")
                ?: root.optJSONObject("turningPointsTab")?.optJSONArray("turningPoints")
            if (tpArr != null) {
                for (i in 0 until tpArr.length().coerceAtMost(3)) {
                    val obj = tpArr.getJSONObject(i)
                    turningPoints.add(
                        TurningPointItem(
                            rank = obj.optInt("rank", i + 1),
                            inningsNo = obj.optInt("innings_no", obj.optInt("inningsNo", 1)),
                            eventType = obj.optString("eventType", obj.optString("event_type", "WICKET")),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            importanceScore = obj.optDouble("importanceScore", obj.optDouble("importance_score", 9.0))
                        )
                    )
                }
            }

            // 7. Control Share
            val csObj = root.optJSONObject("controlShare") ?: JSONObject()
            val t1Pct = csObj.optInt("team1Pct", 50)
            val t2Pct = csObj.optInt("team2Pct", 40)
            val evPct = csObj.optInt("evenPct", (100 - t1Pct - t2Pct).coerceAtLeast(0))
            val controlShare = MatchControlShareInfo(
                team1ShortName = csObj.optString("team1ShortName", header.team1ShortName),
                team1Color = parseHexColor(csObj.optString("team1Color")) ?: header.team1Color,
                team1Pct = t1Pct,
                team2ShortName = csObj.optString("team2ShortName", header.team2ShortName),
                team2Color = parseHexColor(csObj.optString("team2Color")) ?: header.team2Color,
                team2Pct = t2Pct,
                evenPct = evPct
            )

            // 8. Player of the Match Summary
            val pomName = header.playerOfMatch
            val mvpObj = root.optJSONObject("ratingsTab")?.optJSONObject("mvpPlayer")
            val pomHeroesArr = sumTab?.optJSONArray("heroes") ?: root.optJSONArray("heroes")
            var matchedHeroObj: JSONObject? = null
            if (pomHeroesArr != null) {
                for (i in 0 until pomHeroesArr.length()) {
                    val h = pomHeroesArr.getJSONObject(i)
                    val hName = h.optString("name")
                    if (hName.equals(pomName, ignoreCase = true) || pomName.contains(hName, ignoreCase = true) || hName.contains(pomName, ignoreCase = true)) {
                        matchedHeroObj = h
                        break
                    }
                }
                if (matchedHeroObj == null && pomHeroesArr.length() > 0) {
                    matchedHeroObj = pomHeroesArr.getJSONObject(0)
                }
            }

            val pomPlayerId = matchedHeroObj?.optLong("playerId")?.takeIf { it > 0L }
                ?: mvpObj?.optString("playerId")?.toLongOrNull()
                ?: getPlayerIdByName(matchId, pomName)

            val pomTeam = matchedHeroObj?.optString("team")
                ?: mvpObj?.optString("team")
                ?: header.team1Name

            val pomHeadline = matchedHeroObj?.optString("headlineStat")
                ?: (if (mvpObj != null) {
                    val r = mvpObj.optString("runs", "-")
                    val w = mvpObj.optString("wickets", "-")
                    if (w != "-" && w != "0" && w.isNotBlank()) "$r Runs & $w Wkts" else if (r != "-") "$r Runs" else "Match-winning performance"
                } else "Match-winning performance")

            val pomBatRating = mvpObj?.optDouble("battingRating", 0.0) ?: 0.0
            val pomBowlRating = mvpObj?.optDouble("bowlingRating", 0.0) ?: 0.0
            val pomImpactRating = mvpObj?.optDouble("impactRating", matchedHeroObj?.optDouble("rating", 9.0) ?: 9.0) ?: 9.0
            val pomOverallRating = mvpObj?.optDouble("overallRating", matchedHeroObj?.optDouble("rating", 9.0) ?: 9.0) ?: 9.0

            val playerOfMatchSummary = PlayerOfMatchSummary(
                playerId = pomPlayerId,
                name = pomName.ifBlank { mvpObj?.optString("playerName", "Match MVP") ?: "Match MVP" },
                team = pomTeam,
                headlinePerformance = pomHeadline,
                battingRating = pomBatRating,
                bowlingRating = pomBowlRating,
                impactRating = pomImpactRating,
                overallRating = pomOverallRating
            )

            // 9. Build Player Team Mapping for UI Pills
            val playerTeamMap = mutableMapOf<String, String>()
            playerTeamMap[header.team1Name.lowercase()] = header.team1Name
            playerTeamMap[header.team1ShortName.lowercase()] = header.team1Name
            playerTeamMap[header.team2Name.lowercase()] = header.team2Name
            playerTeamMap[header.team2ShortName.lowercase()] = header.team2Name

            val rTab = root.optJSONObject("ratingsTab")
            if (rTab != null) {
                val t1Arr = rTab.optJSONArray("team1Players")
                if (t1Arr != null) {
                    for (i in 0 until t1Arr.length()) {
                        val p = t1Arr.getJSONObject(i)
                        val pName = p.optString("playerName").trim()
                        if (pName.isNotBlank()) addPlayerVariants(playerTeamMap, pName, header.team1Name)
                    }
                }
                val t2Arr = rTab.optJSONArray("team2Players")
                if (t2Arr != null) {
                    for (i in 0 until t2Arr.length()) {
                        val p = t2Arr.getJSONObject(i)
                        val pName = p.optString("playerName").trim()
                        if (pName.isNotBlank()) addPlayerVariants(playerTeamMap, pName, header.team2Name)
                    }
                }
            }

            val scTab = root.optJSONObject("scorecardTab")
            val scList = scTab?.optJSONArray("scorecards")
            if (scList != null) {
                for (i in 0 until scList.length()) {
                    val sc = scList.getJSONObject(i)
                    val innNo = sc.optInt("inningsNumber", i + 1)
                    val battingTeam = if (innNo in listOf(1, 3)) header.team1Name else header.team2Name
                    val bowlingTeam = if (innNo in listOf(1, 3)) header.team2Name else header.team1Name
                    val bArr = sc.optJSONArray("batters")
                    if (bArr != null) {
                        for (j in 0 until bArr.length()) {
                            val b = bArr.getJSONObject(j)
                            val bName = b.optString("playerName").trim()
                            if (bName.isNotBlank()) addPlayerVariants(playerTeamMap, bName, battingTeam)
                        }
                    }
                    val bwArr = sc.optJSONArray("bowlers")
                    if (bwArr != null) {
                        for (j in 0 until bwArr.length()) {
                            val bw = bwArr.getJSONObject(j)
                            val bwName = bw.optString("playerName").trim()
                            if (bwName.isNotBlank()) addPlayerVariants(playerTeamMap, bwName, bowlingTeam)
                        }
                    }
                }
            }

            val momTab = root.optJSONObject("momentumTab")
            val momPtsList = mutableListOf<MomentumModel>()
            val momPtsArr = momTab?.optJSONArray("points")
            val targetVal = Regex("\\d+").find(keyNumbers.targetSet)?.value?.toIntOrNull()
                ?: keyNumbers.targetSet.toIntOrNull() ?: 0
            if (momPtsArr != null) {
                val totalPoints = momPtsArr.length()
                for (i in 0 until totalPoints) {
                    val rawModel = MomentumModel.fromJson(momPtsArr.getJSONObject(i))
                    val eval = SessionWinnerEvaluator.evaluate(
                        model = rawModel,
                        team1ShortName = header.team1ShortName,
                        team2ShortName = header.team2ShortName,
                        team1FullName = header.team1Name,
                        team2FullName = header.team2Name,
                        playerTeamMap = playerTeamMap,
                        target = targetVal,
                        matchWinner = header.winner,
                        isFinalSession = (i == totalPoints - 1)
                    )
                    momPtsList.add(
                        rawModel.copy(
                            sessionWinner = eval.sessionWinner,
                            sessionWinType = eval.sessionWinType,
                            commentaryText = eval.commentaryText
                        )
                    )
                }
            }

            val finalControlSummary = if (momPtsList.isNotEmpty() && momPtsList.any { it.sessionWinner.isNotBlank() }) {
                val t1Count = momPtsList.count { it.sessionWinner == "team1" || it.sessionWinner.equals(header.team1Name, ignoreCase = true) || (header.team1ShortName.isNotBlank() && it.sessionWinner.equals(header.team1ShortName, ignoreCase = true)) }
                val t2Count = momPtsList.count { it.sessionWinner == "team2" || it.sessionWinner.equals(header.team2Name, ignoreCase = true) || (header.team2ShortName.isNotBlank() && it.sessionWinner.equals(header.team2ShortName, ignoreCase = true)) }
                val neuCount = momPtsList.size - t1Count - t2Count
                controlSummary.copy(
                    team1SessionsWon = t1Count,
                    team2SessionsWon = t2Count,
                    neutralSessions = neuCount.coerceAtLeast(0)
                )
            } else {
                controlSummary
            }

            val baseSummary = MatchSummaryData(
                matchId = matchId,
                header = header,
                snapshot = snapshot,
                matchStory = matchStory,
                matchStorySentences = matchStorySentences,
                heroes = heroes,
                keyNumbers = keyNumbers,
                controlSummary = finalControlSummary,
                topTurningPoints = turningPoints,
                controlShare = controlShare,
                summaryPoints = matchStorySentences,
                playerOfMatchSummary = playerOfMatchSummary,
                playerTeamMap = playerTeamMap,
                momentumPoints = momPtsList
            )
            return NarrativeOverrideManager.applySummaryOverrides(matchId, baseSummary, getContext())
        }

        // Fallback default
        val baseData = PrototypeMatchData.ashes2019FirstTest
        return MatchSummaryData(
            matchId = matchId,
            header = MatchHeaderInfo(
                matchTitle = baseData.matchTitle,
                team1Name = baseData.team1.name,
                team1ShortName = baseData.team1.shortName,
                team1Color = baseData.team1.primaryColor,
                team2Name = baseData.team2.name,
                team2ShortName = baseData.team2.shortName,
                team2Color = baseData.team2.primaryColor,
                winner = baseData.winner,
                victoryMargin = baseData.victoryMargin,
                venue = baseData.venue,
                seriesName = "Ashes 2019",
                matchNumber = "1st Test",
                seriesAndMatch = "Ashes 2019 • 1st Test",
                playerOfMatch = "Steve Smith"
            ),
            snapshot = MatchSnapshotScore(
                team1Name = baseData.team1.name,
                team1Scores = "284 & 487/7d",
                team2Name = baseData.team2.name,
                team2Scores = "374 & 146",
                daysUsed = "4.7"
            ),
            matchStory = "Australia recovered from early trouble behind Steve Smith's hundred to post 284. England replied with 374 before Australia declared at 487/7 to set a target of 398. Nathan Lyon bowled Australia to victory on the final day.",
            matchStorySentences = listOf(
                "Australia recovered from early trouble behind Steve Smith's hundred to post 284.",
                "England replied with 374 to take a first-innings lead.",
                "Australia declared at 487/7 to set a massive target of 398.",
                "Nathan Lyon took 6 wickets as Australia secured a 251-run victory."
            ),
            keyNumbers = baseData.keyNumbers,
            heroes = baseData.heroes,
            controlSummary = MatchControlSummary("Australia", "Australia", baseData.team1.name, 6, baseData.team2.name, 5, 3)
        )
    }

    override fun getStoryTabDataSync(matchId: Long): StoryTabDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val stObj = root.optJSONObject("storyTab") ?: JSONObject()

            val timeline = mutableListOf<MatchStoryModel>()
            val tArr = stObj.optJSONArray("timeline")
            if (tArr != null) {
                for (i in 0 until tArr.length()) {
                    val obj = tArr.getJSONObject(i)
                    timeline.add(
                        MatchStoryModel(
                            matchId = matchId,
                            dayNumber = obj.optInt("dayNumber", i + 1),
                            sessionOfDay = obj.optInt("sessionOfDay", 1),
                            headline = obj.optString("headline", "Day ${i + 1} Action"),
                            summary = obj.optString("summary", "Match events."),
                            keyMoment = obj.optString("keyMoment", ""),
                            winProbHome = obj.optDouble("winProbHome", 50.0),
                            winProbAway = obj.optDouble("winProbAway", 50.0),
                            controlHome = obj.optDouble("controlHome", 50.0),
                            controlAway = obj.optDouble("controlAway", 50.0)
                        )
                    )
                }
            }

            val colObj = stObj.optJSONObject("primaryCollapse") ?: JSONObject()
            val collapse = CollapseModel(
                collapseId = colObj.optString("collapseId", "col_$matchId"),
                matchId = matchId,
                innings = colObj.optInt("innings", 1),
                team = colObj.optString("team", header.team1Name),
                score = colObj.optString("score", "Wickets Lost"),
                wicketsLost = colObj.optInt("wicketsLost", 4),
                runsAdded = colObj.optInt("runsAdded", 50),
                oversTaken = colObj.optDouble("oversTaken", 0.0),
                severity = colObj.optDouble("severity", 75.0),
                classification = colObj.optString("classification", "Major Collapse"),
                explanation = colObj.optString("explanation", "Top order collapse.")
            )

            val recObj = stObj.optJSONObject("primaryRecovery") ?: JSONObject()
            val recovery = RecoveryModel(
                recoveryId = recObj.optString("recoveryId", "rec_$matchId"),
                matchId = matchId,
                innings = recObj.optInt("innings", 1),
                team = recObj.optString("team", header.team1Name),
                transition = recObj.optString("transition", "Recovery Stand"),
                partnershipRuns = recObj.optInt("partnershipRuns", 80),
                recoveryScore = recObj.optDouble("recoveryScore", 75.0),
                classification = recObj.optString("classification", "Major Recovery"),
                explanation = recObj.optString("explanation", "Key rescue partnership.")
            )

            val baseStoryData = StoryTabDisplayData(
                matchId = matchId,
                header = header,
                timeline = timeline,
                primaryCollapse = collapse,
                primaryRecovery = recovery,
                narrative = stObj.optString("narrative", "Match Narrative")
            )
            return NarrativeOverrideManager.applyStoryTabOverrides(matchId, baseStoryData, getContext())
        }

        val collapses = getMatchCollapsesSync(matchId)
        val recoveries = getMatchRecoveriesSync(matchId)
        val defaultCollapse = CollapseModel("col_$matchId", matchId, 1, "Team 1", "0/0", 0, 0, 0.0, 0.0, "Collapse", "Early wickets lost.")
        val defaultRecovery = RecoveryModel("rec_$matchId", matchId, 1, "Team 1", "0/0", 0, 0.0, "Recovery", "Rebuilding stand.")

        return StoryTabDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo("Ashes 2019 - 1st Test", "Australia", "AUS", Color(0xFFFFC72C), "England", "ENG", Color(0xFF00A3FF), "Australia", "Won by 251 runs", "Edgbaston"),
            timeline = getMatchStorySync(matchId),
            primaryCollapse = collapses.firstOrNull() ?: defaultCollapse,
            primaryRecovery = recoveries.firstOrNull() ?: defaultRecovery,
            narrative = "Australia won by 251 runs at Edgbaston."
        )
    }

    override fun getMomentumTabDataSync(matchId: Long): MomentumTabDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val momObj = root.optJSONObject("momentumTab") ?: JSONObject()

            val points = mutableListOf<MomentumModel>()
            val pArr = momObj.optJSONArray("points")
            val rawTargetStr = root.optJSONObject("keyNumbers")?.optString("targetSet", "")
                ?: root.optJSONObject("summaryTab")?.optJSONObject("keyNumbers")?.optString("targetSet", "") ?: ""
            val targetVal = Regex("\\d+").find(rawTargetStr)?.value?.toIntOrNull()
                ?: rawTargetStr.toIntOrNull() ?: 0
            if (pArr != null) {
                val totalP = pArr.length()
                for (i in 0 until totalP) {
                    val rawModel = MomentumModel.fromJson(pArr.getJSONObject(i))
                    val eval = SessionWinnerEvaluator.evaluate(
                        model = rawModel,
                        team1ShortName = header.team1ShortName,
                        team2ShortName = header.team2ShortName,
                        team1FullName = header.team1Name,
                        team2FullName = header.team2Name,
                        target = targetVal,
                        matchWinner = header.winner,
                        isFinalSession = (i == totalP - 1)
                    )
                    points.add(
                        rawModel.copy(
                            sessionWinner = eval.sessionWinner,
                            sessionWinType = eval.sessionWinType,
                            commentaryText = eval.commentaryText
                        )
                    )
                }
            }

            val topSwings = points.sortedByDescending { abs(it.momentumSwing) }.take(5)

            val baseMomentum = MomentumTabDisplayData(
                matchId = matchId,
                header = header,
                points = points,
                topSwings = topSwings
            )
            return NarrativeOverrideManager.applyMomentumOverrides(matchId, baseMomentum, getContext())
        }

        val baseData = PrototypeMatchData.ashes2019FirstTest
        return MomentumTabDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo(baseData.matchTitle, baseData.team1.name, baseData.team1.shortName, baseData.team1.primaryColor, baseData.team2.name, baseData.team2.shortName, baseData.team2.primaryColor, baseData.winner, baseData.victoryMargin, baseData.venue),
            points = getMatchMomentumSync(matchId),
            topSwings = getMatchMomentumSync(matchId).take(5)
        )
    }

    override fun getPressureTabDataSync(matchId: Long): PressureTabDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val presObj = root.optJSONObject("pressureTab") ?: JSONObject()

            val points = mutableListOf<PressureModel>()
            val pArr = presObj.optJSONArray("pressurePoints")
            if (pArr != null) {
                for (i in 0 until pArr.length()) {
                    val obj = pArr.getJSONObject(i)
                    points.add(PressureModel.fromJson(obj))
                }
            }

            val topPressure = points.sortedByDescending { it.pressureScore }.take(5)

            val basePressure = PressureTabDisplayData(
                matchId = matchId,
                header = header,
                pressurePoints = points,
                highestPressureMoments = topPressure
            )
            return NarrativeOverrideManager.applyPressureOverrides(matchId, basePressure, getContext())
        }

        return PressureTabDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo("Ashes 2019", "Australia", "AUS", Color(0xFFFFC72C), "England", "ENG", Color(0xFF00A3FF), "Australia", "Won by 251 runs", "Edgbaston"),
            pressurePoints = getMatchPressureSync(matchId),
            highestPressureMoments = getMatchPressureSync(matchId).take(5)
        )
    }

    override fun getTurningPointsTabDataSync(matchId: Long): TurningPointsDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val tpObj = root.optJSONObject("turningPointsTab") ?: JSONObject()

            val topList = mutableListOf<TurningPointItem>()
            val topArr = tpObj.optJSONArray("topTurningPoints")
            if (topArr != null) {
                for (i in 0 until topArr.length().coerceAtMost(5)) {
                    val obj = topArr.getJSONObject(i)
                    topList.add(
                        TurningPointItem(
                            rank = i + 1,
                            inningsNo = obj.optInt("innings_no", obj.optInt("inningsNo", 1)),
                            eventType = obj.optString("event_type", obj.optString("eventType", "WICKET")),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            importanceScore = obj.optDouble("importance_score", obj.optDouble("importanceScore", 9.0)),
                            importanceTier = obj.optString("importance_tier", "Major Turning Point"),
                            player = obj.optString("player", ""),
                            team = obj.optString("team", ""),
                            overNo = obj.optDouble("over_no", obj.optDouble("overNo", 0.0))
                        )
                    )
                }
            }

            val allList = mutableListOf<TurningPointItem>()
            val allArr = tpObj.optJSONArray("turningPoints")
            if (allArr != null) {
                for (i in 0 until allArr.length()) {
                    val obj = allArr.getJSONObject(i)
                    allList.add(
                        TurningPointItem(
                            rank = obj.optInt("turning_point_id", i + 1),
                            inningsNo = obj.optInt("innings_no", obj.optInt("inningsNo", 1)),
                            eventType = obj.optString("event_type", obj.optString("eventType", "WICKET")),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            importanceScore = obj.optDouble("importance_score", obj.optDouble("importanceScore", 9.0)),
                            importanceTier = obj.optString("importance_tier", "Major Turning Point"),
                            player = obj.optString("player", ""),
                            team = obj.optString("team", ""),
                            overNo = obj.optDouble("over_no", obj.optDouble("overNo", 0.0))
                        )
                    )
                }
            }

            // Parse Match Verdict Chain
            val verdictChain = mutableListOf<MatchVerdictNode>()
            val vArr = tpObj.optJSONArray("matchVerdictChain")
            if (vArr != null) {
                for (i in 0 until vArr.length()) {
                    val obj = vArr.getJSONObject(i)
                    verdictChain.add(
                        MatchVerdictNode(
                            stepIndex = obj.optInt("stepIndex", i + 1),
                            phase = obj.optString("phase", "Phase ${i + 1}"),
                            headline = obj.optString("headline", ""),
                            description = obj.optString("description", ""),
                            scoreSnippet = obj.optString("scoreSnippet", ""),
                            isDecisive = obj.optBoolean("isDecisive", false),
                            nodeType = obj.optString("nodeType", "TURNING_POINT")
                        )
                    )
                }
            }

            // Parse Innings Overs
            val inningsOvers = mutableListOf<Double>()
            val ovArr = tpObj.optJSONArray("inningsOvers")
            if (ovArr != null) {
                for (i in 0 until ovArr.length()) {
                    inningsOvers.add(ovArr.optDouble(i, 80.0))
                }
            }

            // Fallback top 5 if topTurningPoints array was missing
            val finalTop = if (topList.isNotEmpty()) topList else allList.sortedByDescending { it.importanceScore }.take(5).mapIndexed { idx, item -> item.copy(rank = idx + 1) }

            val baseTP = TurningPointsDisplayData(
                matchId = matchId,
                header = header,
                topTurningPoints = finalTop,
                turningPoints = allList,
                matchVerdictChain = verdictChain,
                inningsOvers = inningsOvers
            )
            return NarrativeOverrideManager.applyTurningPointsOverrides(matchId, baseTP, getContext())
        }

        return TurningPointsDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo("Test Match", "Team 1", "T1", Color(0xFFFFC72C), "Team 2", "T2", Color(0xFF00A3FF), "Team 1", "Won", "Ground"),
            topTurningPoints = emptyList(),
            turningPoints = emptyList(),
            matchVerdictChain = emptyList(),
            inningsOvers = emptyList()
        )
    }

    override fun getRatingsTabDataSync(matchId: Long): RatingsTabDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val ratObj = root.optJSONObject("ratingsTab") ?: JSONObject()

            fun parsePlayer(obj: JSONObject): PlayerMatchRatingModel {
                val pIdStr = obj.optString("playerId", "1")
                val pId = pIdStr.toLongOrNull() ?: 1L
                val pName = obj.optString("playerName", "Player")
                val trueJersey = getPlayerJerseyNumber(pId, pName)
                return PlayerMatchRatingModel(
                    matchId = matchId,
                    playerId = pIdStr,
                    playerName = pName,
                    team = obj.optString("team", header.team1Name),
                    role = obj.optString("role", "All-rounder"),
                    jerseyNumber = trueJersey ?: obj.optInt("jerseyNumber").takeIf { it > 0 },
                    overallRating = obj.optDouble("overallRating", 6.5),
                    battingRating = obj.optDouble("battingRating", 0.0),
                    bowlingRating = obj.optDouble("bowlingRating", 0.0),
                    impactRating = obj.optDouble("impactRating", 6.5),
                    impactRank = obj.optString("impactRank", "-"),
                    battingImpactRating = obj.optDouble("battingImpactRating", 0.0),
                    bowlingImpactRating = obj.optDouble("bowlingImpactRating", 0.0),
                    runs = obj.optString("runs", "-"),
                    balls = obj.optString("balls", "-"),
                    strikeRate = obj.optString("strikeRate", "-"),
                    fours = obj.optString("fours", "-"),
                    sixes = obj.optString("sixes", "-"),
                    wickets = obj.optString("wickets", "-"),
                    overs = obj.optString("overs", "-"),
                    economy = obj.optString("economy", "-"),
                    bestBowling = obj.optString("bestBowling", "-"),
                    primaryHighlight = obj.optString("primaryHighlight", "Key Player"),
                    story = obj.optString("story", "Player story."),
                    controlPct = obj.optString("controlPct", "-"),
                    falseShotPct = obj.optString("falseShotPct", "-"),
                    attackingPct = obj.optString("attackingPct", "-"),
                    leavePct = obj.optString("leavePct", "-"),
                    battingDotPct = obj.optString("battingDotPct", "-"),
                    boundaryRunPct = obj.optString("boundaryRunPct", "-"),
                    bowlingDotPct = obj.optString("bowlingDotPct", "-"),
                    lineAcc = obj.optString("lineAcc", "-"),
                    boundariesConceded = obj.optString("boundariesConceded", "-"),
                    inningsProgression = run {
                        val arr = obj.optJSONArray("inningsProgression") ?: return@run emptyList<InningsRatingModel>()
                        val list = mutableListOf<InningsRatingModel>()
                        for (i in 0 until arr.length()) {
                            val innObj = arr.optJSONObject(i) ?: continue
                            list.add(
                                InningsRatingModel(
                                    innings = innObj.optInt("innings", i + 1),
                                    label = innObj.optString("label", "Inn ${i + 1}"),
                                    rating = innObj.optDouble("rating", 0.0),
                                    battingRating = innObj.optDouble("battingRating", 0.0),
                                    bowlingRating = innObj.optDouble("bowlingRating", 0.0)
                                )
                            )
                        }
                        list
                    }
                )
            }

            val team1Players = mutableListOf<PlayerMatchRatingModel>()
            val t1Arr = ratObj.optJSONArray("team1Players")
            if (t1Arr != null) {
                for (i in 0 until t1Arr.length()) team1Players.add(parsePlayer(t1Arr.getJSONObject(i)))
            }

            val team2Players = mutableListOf<PlayerMatchRatingModel>()
            val t2Arr = ratObj.optJSONArray("team2Players")
            if (t2Arr != null) {
                for (i in 0 until t2Arr.length()) team2Players.add(parsePlayer(t2Arr.getJSONObject(i)))
            }

            val mvpJson = ratObj.optJSONObject("mvpPlayer") ?: (t1Arr?.optJSONObject(0) ?: JSONObject())
            val mvp = parsePlayer(mvpJson)

            val impactRankings = mutableListOf<PlayerMatchImpactModel>()
            val impArr = ratObj.optJSONArray("impactRankings")
            if (impArr != null) {
                for (i in 0 until impArr.length()) {
                    val obj = impArr.getJSONObject(i)
                    impactRankings.add(
                        PlayerMatchImpactModel(
                            matchId = matchId,
                            playerId = obj.optString("playerId", "1"),
                            playerName = obj.optString("playerName", "Player"),
                            team = obj.optString("team", header.team1Name),
                            role = obj.optString("role", "Player"),
                            impactRank = obj.optInt("impactRank", i + 1),
                            impactScore = obj.optDouble("impactScore", 7.0),
                            battingImpact = obj.optDouble("battingImpact", 0.0),
                            bowlingImpact = obj.optDouble("bowlingImpact", 0.0),
                            headlineImpact = obj.optString("headlineImpact", "Key Impact"),
                            impactExplanation = obj.optString("impactExplanation", "Impact performance.")
                        )
                    )
                }
            }

            val playerInningsDetailsMap = mutableMapOf<Int, PlayerInningsDetailsProfile>()
            val detailsArr = root.optJSONArray("playerInningsDetails") ?: ratObj.optJSONArray("playerInningsDetails")
            if (detailsArr != null) {
                for (i in 0 until detailsArr.length()) {
                    val pObj = detailsArr.optJSONObject(i) ?: continue
                    val pid = pObj.optInt("playerId", 0)
                    val pName = pObj.optString("playerName", "")
                    val pTeam = pObj.optString("team", "")
                    val innsArr = pObj.optJSONArray("innings")
                    val innsList = mutableListOf<PlayerInningsDetailModel>()
                    if (innsArr != null) {
                        for (j in 0 until innsArr.length()) {
                            val innObj = innsArr.optJSONObject(j) ?: continue
                            innsList.add(
                                PlayerInningsDetailModel(
                                    inningsNumber = innObj.optInt("inningsNumber", j + 1),
                                    participationType = innObj.optString("participationType", "DNB"),
                                    inningsScorecard = innObj.optString("inningsScorecard", "DNB"),
                                    arrivalScore = innObj.optString("arrivalScore", "-"),
                                    runs = innObj.optInt("runs", 0),
                                    balls = innObj.optInt("balls", 0),
                                    fours = innObj.optInt("fours", 0),
                                    sixes = innObj.optInt("sixes", 0),
                                    strikeRate = innObj.optDouble("strikeRate", 0.0),
                                    controlPct = innObj.optDouble("controlPct", 0.0),
                                    falseShotPct = innObj.optDouble("falseShotPct", 0.0),
                                    attackingPct = innObj.optDouble("attackingPct", 0.0),
                                    leavePct = innObj.optDouble("leavePct", 0.0),
                                    dotBallPct = innObj.optDouble("dotBallPct", 0.0),
                                    boundaryRunPct = innObj.optDouble("boundaryRunPct", 0.0),
                                    battingRating = innObj.optDouble("battingRating", 0.0),
                                    battingImpactRating = innObj.optDouble("battingImpactRating", 0.0),
                                    dismissed = innObj.optBoolean("dismissed", false),
                                    dismissalType = innObj.optString("dismissalType", ""),
                                    dismissedBy = innObj.optString("dismissedBy", ""),
                                    overs = innObj.optString("overs", "0.0"),
                                    maidens = innObj.optInt("maidens", 0),
                                    runsConceded = innObj.optInt("runsConceded", 0),
                                    wickets = innObj.optInt("wickets", 0),
                                    economy = innObj.optDouble("economy", 0.0),
                                    bowlingDotPct = innObj.optDouble("bowlingDotPct", 0.0),
                                    lineAccuracyPct = innObj.optDouble("lineAccuracyPct", 0.0),
                                    lengthAccuracyPct = innObj.optDouble("lengthAccuracyPct", 0.0),
                                    boundariesConceded = innObj.optInt("boundariesConceded", 0),
                                    bowlingRating = innObj.optDouble("bowlingRating", 0.0),
                                    bowlingImpactRating = innObj.optDouble("bowlingImpactRating", 0.0)
                                )
                            )
                        }
                    }
                    if (pid > 0) {
                        playerInningsDetailsMap[pid] = PlayerInningsDetailsProfile(
                            playerId = pid,
                            playerName = pName,
                            team = pTeam,
                            innings = innsList
                        )
                    }
                }
            }

            return RatingsTabDisplayData(
                matchId = matchId,
                header = header,
                team1Players = team1Players,
                team2Players = team2Players,
                mvpPlayer = mvp,
                impactRankings = impactRankings,
                playerInningsDetails = playerInningsDetailsMap
            )
        }

        val baseData = PrototypeMatchData.ashes2019FirstTest
        val allRatings = getPlayerMatchRatingsSync(matchId)
        val defaultMvp = PlayerMatchRatingModel(
            matchId = matchId,
            playerId = "1",
            playerName = "Match Winner",
            team = "Team 1",
            role = "All-rounder",
            jerseyNumber = 1,
            overallRating = 9.0,
            battingRating = 9.0,
            bowlingRating = 9.0,
            impactRating = 9.0,
            impactRank = "#1",
            battingImpactRating = 9.0,
            bowlingImpactRating = 9.0,
            primaryHighlight = "Match Winner",
            story = "Key match performance."
        )

        return RatingsTabDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo(baseData.matchTitle, baseData.team1.name, baseData.team1.shortName, baseData.team1.primaryColor, baseData.team2.name, baseData.team2.shortName, baseData.team2.primaryColor, baseData.winner, baseData.victoryMargin, baseData.venue),
            team1Players = allRatings.take(11),
            team2Players = allRatings.drop(11).take(11),
            mvpPlayer = allRatings.firstOrNull() ?: defaultMvp,
            impactRankings = getPlayerMatchImpactSync(matchId)
        )
    }

    override fun getScorecardTabDataSync(matchId: Long): ScorecardTabDisplayData {
        val root = getMatchJson(matchId)
        if (root != null) {
            val header = parseHeader(root)
            val scObj = root.optJSONObject("scorecardTab") ?: JSONObject()
            val scArr = scObj.optJSONArray("scorecards")

            val scorecards = mutableListOf<SingleInningsScorecardData>()
            if (scArr != null) {
                for (i in 0 until scArr.length()) {
                    val innObj = scArr.getJSONObject(i)
                    val innNum = innObj.optInt("inningsNumber", i + 1)
                    val tName = innObj.optString("teamName", header.team1Name)
                    val totScore = innObj.optString("totalScore", "0/0")
                    val rr = innObj.optDouble("runRate", 0.0).toFloat()
                    val extras = innObj.optString("extras", "12 (b 4, lb 4, w 2, nb 2)")

                    val batters = mutableListOf<BatterScore>()
                    val batArr = innObj.optJSONArray("batters")
                    if (batArr != null) {
                        for (b in 0 until batArr.length()) {
                            val bObj = batArr.getJSONObject(b)
                            val bName = bObj.optString("name", "Batter")
                            val bPid = bObj.optString("playerId", "").toLongOrNull() ?: getPlayerIdByName(matchId, bName)
                            batters.add(
                                BatterScore(
                                    playerId = bPid,
                                    name = bName,
                                    dismissal = bObj.optString("dismissal", "not out"),
                                    runs = bObj.optInt("runs", 0),
                                    balls = bObj.optInt("balls", 0),
                                    fours = bObj.optInt("fours", 0),
                                    sixes = bObj.optInt("sixes", 0),
                                    strikeRate = bObj.optDouble("strikeRate", 0.0).toFloat()
                                )
                            )
                        }
                    }

                    val bowlers = mutableListOf<BowlerScore>()
                    val bowlArr = innObj.optJSONArray("bowlers")
                    if (bowlArr != null) {
                        for (bw in 0 until bowlArr.length()) {
                            val bwObj = bowlArr.getJSONObject(bw)
                            val bwName = bwObj.optString("name", "Bowler")
                            val bwPid = bwObj.optString("playerId", "").toLongOrNull() ?: getPlayerIdByName(matchId, bwName)
                            bowlers.add(
                                BowlerScore(
                                    playerId = bwPid,
                                    name = bwName,
                                    overs = bwObj.optDouble("overs", 0.0).toFloat(),
                                    maidens = bwObj.optInt("maidens", 0),
                                    runs = bwObj.optInt("runs", 0),
                                    wickets = bwObj.optInt("wickets", 0),
                                    economy = bwObj.optDouble("economy", 0.0).toFloat()
                                )
                            )
                        }
                    }

                    val fow = mutableListOf<FOW>()
                    val fowArr = innObj.optJSONArray("fallOfWickets")
                    if (fowArr != null) {
                        for (f in 0 until fowArr.length()) {
                            val fObj = fowArr.getJSONObject(f)
                            val fName = fObj.optString("batterName", "")
                            val fPid = fObj.optString("playerId", "").toLongOrNull() ?: getPlayerIdByName(matchId, fName)
                            fow.add(
                                FOW(
                                    playerId = fPid,
                                    score = fObj.optString("score", ""),
                                    batterName = fName,
                                    over = fObj.optString("over", "")
                                )
                            )
                        }
                    }

                    val partnerships = mutableListOf<DetailedPartnership>()
                    val partArr = innObj.optJSONArray("allPartnerships")
                    if (partArr != null) {
                        for (p in 0 until partArr.length()) {
                            val pObj = partArr.getJSONObject(p)
                            val b1Name = pObj.optString("batter1Name", "")
                            val b2Name = pObj.optString("batter2Name", "")
                            val b1Pid = pObj.optString("batter1Id", "").toLongOrNull() ?: getPlayerIdByName(matchId, b1Name)
                            val b2Pid = pObj.optString("batter2Id", "").toLongOrNull() ?: getPlayerIdByName(matchId, b2Name)
                            partnerships.add(
                                DetailedPartnership(
                                    wicketNumber = pObj.optInt("wicketNumber", p + 1),
                                    batter1Id = b1Pid,
                                    batter1Name = b1Name,
                                    batter1Runs = pObj.optInt("batter1Runs", 0),
                                    batter1Balls = pObj.optInt("batter1Balls", 0),
                                    batter2Id = b2Pid,
                                    batter2Name = b2Name,
                                    batter2Runs = pObj.optInt("batter2Runs", 0),
                                    batter2Balls = pObj.optInt("batter2Balls", 0),
                                    totalRuns = pObj.optInt("totalRuns", 0),
                                    totalBalls = pObj.optInt("totalBalls", 0),
                                    isKey = pObj.optBoolean("isKey", false),
                                    intelligenceLabel = pObj.optString("intelligenceLabel", "")
                                )
                            )
                        }
                    }

                    scorecards.add(
                        SingleInningsScorecardData(
                            inningsNumber = innNum,
                            teamName = tName,
                            totalScore = totScore,
                            runRate = rr,
                            extras = extras,
                            batters = batters,
                            bowlers = bowlers,
                            fallOfWickets = fow,
                            allPartnerships = partnerships
                        )
                    )
                }
            }

            val ratingsData = getRatingsTabDataSync(matchId)

            return ScorecardTabDisplayData(
                matchId = matchId,
                header = header,
                scorecards = scorecards,
                allRatings = ratingsData.team1Players + ratingsData.team2Players,
                playerInningsDetails = ratingsData.playerInningsDetails
            )
        }

        val baseData = PrototypeMatchData.ashes2019FirstTest
        val scorecards = baseData.scorecards.map { inn ->
            SingleInningsScorecardData(
                inningsNumber = inn.inningsNumber,
                teamName = inn.teamName,
                totalScore = inn.totalScore,
                runRate = inn.runRate,
                extras = inn.extras,
                batters = inn.batters,
                bowlers = inn.bowlers,
                fallOfWickets = inn.fallOfWickets,
                allPartnerships = inn.allPartnerships
            )
        }

        val ratingsData = getRatingsTabDataSync(matchId)

        return ScorecardTabDisplayData(
            matchId = matchId,
            header = MatchHeaderInfo(baseData.matchTitle, baseData.team1.name, baseData.team1.shortName, baseData.team1.primaryColor, baseData.team2.name, baseData.team2.shortName, baseData.team2.primaryColor, baseData.winner, baseData.victoryMargin, baseData.venue),
            scorecards = scorecards,
            allRatings = ratingsData.team1Players + ratingsData.team2Players,
            playerInningsDetails = ratingsData.playerInningsDetails
        )
    }

    override suspend fun getPlayerInspectorData(matchId: Long, playerId: Long): PlayerInspectorData = withContext(Dispatchers.IO) {
        getPlayerInspectorDataSync(matchId, playerId)
    }

    fun getCanonicalPlayerName(matchId: Long, playerId: Long, rawName: String = ""): String {
        // 1. Check match JSON ratingsTab / scorecards
        val root = getMatchJson(matchId)
        if (root != null) {
            val rTab = root.optJSONObject("ratingsTab")
            if (rTab != null) {
                val t1 = rTab.optJSONArray("team1Players")
                val t2 = rTab.optJSONArray("team2Players")
                val arrs = listOfNotNull(t1, t2)
                for (arr in arrs) {
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pIdStr = pObj.optString("playerId", "")
                        if (pIdStr == playerId.toString() || pObj.optLong("playerId", -1L) == playerId) {
                            val name = pObj.optString("playerName", "")
                            if (name.isNotBlank() && !name.startsWith("Player ")) return name
                        }
                    }
                }
            }
        }
        // 2. Specific known Test stars
        when (playerId) {
            291L -> return "Steve Smith"
            236L -> return "Pat Cummins"
            66L  -> return "David Warner"
            323L -> return "Usman Khawaja"
            318L -> return "Travis Head"
            183L -> return "Matthew Wade"
            312L -> return "Tim Paine"
            115L -> return "James Pattinson"
            240L -> return "Peter Siddle"
            222L -> return "Nathan Lyon"
            43L  -> return "Cameron Bancroft"
            259L -> return "Rory Burns"
            118L -> return "Jason Roy"
            135L -> return "Joe Root"
            136L -> return "Joe Denly"
            134L -> return "Jos Buttler"
            31L  -> return "Ben Stokes"
            131L -> return "Jonny Bairstow"
            193L -> return "Moeen Ali"
            55L  -> return "Chris Woakes"
            292L -> return "Stuart Broad"
            116L -> return "James Anderson"
            84L  -> return "Faf du Plessis"
            85L  -> return "Dimuth Karunaratne"
            30L  -> return "Kusal Mendis"
            71L  -> return "Dhananjaya de Silva"
            164L -> return "Dinesh Chandimal"
            330L -> return "Upul Tharanga"
            121L -> return "Kaushal Silva"
            20L  -> return "Angelo Mathews"
            27L  -> return "Suranga Lakmal"
            48L  -> return "Rangana Herath"
            101L -> return "Dilruwan Perera"
            252L -> return "Kusal Perera"
        }
        if (rawName.isNotBlank() && !rawName.startsWith("Player ")) {
            val parts = rawName.trim().split(Regex("\\s+"))
            if (parts.size >= 3) {
                if (rawName.endsWith("de Silva", ignoreCase = true)) return "${parts[0]} de Silva"
                if (rawName.endsWith("du Plessis", ignoreCase = true)) return "Faf du Plessis"
                return "${parts[0]} ${parts.last()}"
            }
            return rawName.trim()
        }
        return if (rawName.isNotBlank()) rawName.trim() else "Player $playerId"
    }

    fun getPlayerJerseyNumber(playerId: Long, playerName: String = ""): Int? {
        val ctx = getContext()
        if (ctx != null) {
            val cache = jerseyMapCache ?: run {
                val map = mutableMapOf<String, Int>()
                val rows = AssetWarehouseLoader.loadCsv(ctx, "jersey_coverage_audit.csv")
                for (row in rows) {
                    val pid = row["player_id"]?.trim()
                    val pName = row["player_name"]?.trim()?.lowercase()
                    val numStr = row["jersey_number"]?.trim()
                    val num = numStr?.toIntOrNull()
                    if (num != null && num > 0) {
                        if (!pid.isNullOrBlank()) {
                            map["id_$pid"] = num
                        }
                        if (!pName.isNullOrBlank()) {
                            map["name_$pName"] = num
                            val parts = pName.split(Regex("\\s+"))
                            if (parts.size >= 2) {
                                map["name_${parts.first()} ${parts.last()}"] = num
                            }
                        }
                    }
                }
                jerseyMapCache = map
                map
            }

            if (playerId > 0) {
                cache["id_$playerId"]?.let { return it }
            }

            if (playerName.isNotBlank()) {
                val norm = playerName.trim().lowercase()
                cache["name_$norm"]?.let { return it }
                val parts = norm.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    cache["name_${parts.first()} ${parts.last()}"]?.let { return it }
                }
            }
        }

        return when (playerId) {
            291L -> 49 // Steve Smith
            236L -> 30 // Pat Cummins
            66L  -> 31 // David Warner
            323L -> 1  // Usman Khawaja
            318L -> 62 // Travis Head
            183L -> 13 // Matthew Wade
            312L -> 7  // Tim Paine
            115L -> 19 // James Pattinson
            240L -> 10 // Peter Siddle
            222L -> 67 // Nathan Lyon
            43L  -> 8  // Cameron Bancroft
            169L -> 33 // Marnus Labuschagne
            138L -> 38 // Josh Hazlewood
            217L -> 56 // Mitchell Starc
            259L -> 27 // Rory Burns
            118L -> 20 // Jason Roy
            135L -> 66 // Joe Root
            136L -> 24 // Joe Denly
            134L -> 63 // Jos Buttler
            31L  -> 55 // Ben Stokes
            131L -> 51 // Jonny Bairstow
            193L -> 18 // Moeen Ali
            55L  -> 19 // Chris Woakes
            292L -> 8  // Stuart Broad
            116L -> 9  // James Anderson
            122L -> 22 // Jofra Archer
            111L -> 77 // Jack Leach
            325L -> 18 // Virat Kohli
            254L -> 45 // Rohit Sharma
            250L -> 17 // Rishabh Pant
            261L -> 8  // Ravindra Jadeja
            260L -> 99 // Ravichandran Ashwin
            else -> {
                val lower = playerName.trim().lowercase()
                when {
                    lower.contains("smith") -> 49
                    lower.contains("cummins") -> 30
                    lower.contains("warner") -> 31
                    lower.contains("khawaja") -> 1
                    lower.contains("labuschagne") -> 33
                    lower.contains("harris") -> 14
                    lower.contains("head") -> 62
                    lower.contains("wade") -> 13
                    lower.contains("paine") -> 7
                    lower.contains("lyon") -> 67
                    lower.contains("hazlewood") -> 38
                    lower.contains("pattinson") -> 19
                    lower.contains("siddle") -> 10
                    lower.contains("starc") -> 56
                    lower.contains("stokes") -> 55
                    lower.contains("root") -> 66
                    lower.contains("bairstow") -> 51
                    lower.contains("burns") -> 27
                    lower.contains("buttler") -> 63
                    lower.contains("broad") -> 8
                    lower.contains("anderson") -> 9
                    lower.contains("archer") -> 22
                    lower.contains("leach") -> 77
                    lower.contains("woakes") -> 19
                    lower.contains("kohli") -> 18
                    lower.contains("rohit") -> 45
                    lower.contains("pant") -> 17
                    lower.contains("jadeja") -> 8
                    lower.contains("ashwin") -> 99
                    lower.contains("bumrah") -> 93
                    lower.contains("shami") -> 11
                    lower.contains("siraj") -> 73
                    lower.contains("pujara") -> 25
                    lower.contains("rahane") -> 3
                    lower.contains("rahul") -> 1
                    lower.contains("gill") -> 77
                    else -> null
                }
            }
        }
    }

    fun getPlayerImageUri(playerId: Long): String {
        val ctx = getContext() ?: return PlayerImageLoader.DEFAULT_PLAYER_ASSET_URI
        return PlayerImageLoader.getPlayerImageUri(ctx, playerId)
    }

    fun getPlayerImageFile(playerId: Long): String? {
        val ctx = getContext() ?: return null
        return PlayerImageLoader.getPlayerImageFile(ctx, playerId)
    }

    private fun ensureDimPlayersLoaded(ctx: Context) {
        if (dimPlayersMapCache == null) {
            val playersJson = AssetWarehouseLoader.loadJson(ctx, "dim_players.json")
            if (playersJson.isNotBlank()) {
                try {
                    val arr = org.json.JSONArray(playersJson)
                    val map = mutableMapOf<Long, JSONObject>()
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pId = pObj.optLong("player_id", 0L)
                        if (pId > 0) map[pId] = pObj
                    }
                    dimPlayersMapCache = map
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getPlayerSpatialAnalyticsSync(ctx: Context, matchId: Long): JSONObject? {
        return try {
            val jsonStr = AssetWarehouseLoader.loadJson(ctx, "player_spatial/match_${matchId}_spatial.json")
            if (jsonStr.isBlank()) return null
            val root = JSONObject(jsonStr)
            val schemaVer = root.optString("schemaVersion", "")
            if (schemaVer.isNotBlank() && !schemaVer.startsWith("1.")) {
                return null
            }
            root
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWagonWheel(spatialJson: JSONObject?, playerId: Long, innNo: Int, isLeftHanded: Boolean): PlayerWagonWheelData? {
        if (spatialJson == null) return null
        val battingAnalytics = spatialJson.optJSONObject("battingAnalytics") ?: return null

        // 1. Direct lookup by warehouse playerId
        var playerObj = battingAnalytics.optJSONObject(playerId.toString())

        // 2. Fallback: Search by batterId or espnId
        if (playerObj == null) {
            val keys = battingAnalytics.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val candidate = battingAnalytics.optJSONObject(k) ?: continue
                val bId = candidate.optLong("batterId", -1L)
                val eId = candidate.optLong("espnId", -1L)
                if (bId == playerId || eId == playerId) {
                    playerObj = candidate
                    break
                }
            }
        }

        // 3. Fallback: Search by player surname matching
        if (playerObj == null) {
            val canonicalName = dimPlayersMapCache?.get(playerId)?.optString("player_name", "") ?: ""
            val targetSurname = canonicalName.trim().split(" ").lastOrNull()?.lowercase() ?: ""
            if (targetSurname.length > 2) {
                val keys = battingAnalytics.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val candidate = battingAnalytics.optJSONObject(k) ?: continue
                    val bName = candidate.optString("batterName", "").trim().lowercase()
                    if (bName.split(" ").lastOrNull() == targetSurname) {
                        playerObj = candidate
                        break
                    }
                }
            }
        }

        val innSpatial = playerObj?.optJSONObject("innings")
            ?.optJSONObject(innNo.toString()) ?: return null

        val secMap = mutableMapOf<String, WagonWheelSector>()
        val sectorsObj = innSpatial.optJSONObject("sectors")
        if (sectorsObj != null) {
            val keys = sectorsObj.keys()
            while (keys.hasNext()) {
                val sKey = keys.next()
                val sObj = sectorsObj.getJSONObject(sKey)
                secMap[sKey] = WagonWheelSector(
                    sectorId = sKey,
                    label = sKey.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    shotsCount = sObj.optInt("shotsCount", 0),
                    runsTotal = sObj.optInt("runsTotal", 0),
                    dots = sObj.optInt("dots", 0),
                    singles = sObj.optInt("singles", 0),
                    doubles = sObj.optInt("doubles", 0),
                    triples = sObj.optInt("triples", 0),
                    fours = sObj.optInt("fours", 0),
                    sixes = sObj.optInt("sixes", 0)
                )
            }
        }

        val delList = mutableListOf<WagonWheelDelivery>()
        val delArr = innSpatial.optJSONArray("deliveries")
        if (delArr != null) {
            for (i in 0 until delArr.length()) {
                val dObj = delArr.getJSONObject(i)
                val angleVal = if (dObj.has("angle") && !dObj.isNull("angle")) dObj.getDouble("angle").toFloat() else null
                delList.add(
                    WagonWheelDelivery(
                        key = dObj.optString("key", ""),
                        over = dObj.optInt("over", 0),
                        ball = dObj.optInt("ball", 0),
                        runs = dObj.optInt("runs", 0),
                        totalRuns = dObj.optInt("totRuns", 0),
                        sector = dObj.optString("sector", "UNKNOWN"),
                        angle = angleVal,
                        provenance = dObj.optString("prov", "UNKNOWN")
                    )
                )
            }
        }

        return PlayerWagonWheelData(
            totalBalls = innSpatial.optInt("totalBalls", 0),
            totalRuns = innSpatial.optInt("totalRuns", 0),
            scoringBalls = innSpatial.optInt("scoringBalls", 0),
            directlyClassifiedBalls = innSpatial.optInt("directlyClassifiedBalls", 0),
            unknownDirectionBalls = innSpatial.optInt("unknownDirectionBalls", 0),
            directlyClassifiedBoundaries = innSpatial.optInt("directlyClassifiedBoundaries", 0),
            totalBoundaries = innSpatial.optInt("totalBoundaries", 0),
            isLeftHanded = isLeftHanded,
            sectors = secMap,
            deliveries = delList
        )
    }

    private fun parseBowlingHeatmap(spatialJson: JSONObject?, playerId: Long, innNum: Int): BowlingHeatmapData? {
        if (spatialJson == null) return null
        val bowlingAnalytics = spatialJson.optJSONObject("bowlingAnalytics") ?: return null

        // 1. Direct lookup by warehouse playerId
        var playerObj = bowlingAnalytics.optJSONObject(playerId.toString())

        // 2. Fallback: Search by bowlerId or espnId
        if (playerObj == null) {
            val keys = bowlingAnalytics.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val candidate = bowlingAnalytics.optJSONObject(k) ?: continue
                val bId = candidate.optLong("bowlerId", -1L)
                val eId = candidate.optLong("espnId", -1L)
                if (bId == playerId || eId == playerId) {
                    playerObj = candidate
                    break
                }
            }
        }

        // 3. Fallback: Search by bowler surname matching
        if (playerObj == null) {
            val canonicalName = dimPlayersMapCache?.get(playerId)?.optString("player_name", "") ?: ""
            val targetSurname = canonicalName.trim().split(" ").lastOrNull()?.lowercase() ?: ""
            if (targetSurname.length > 2) {
                val keys = bowlingAnalytics.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val candidate = bowlingAnalytics.optJSONObject(k) ?: continue
                    val bName = candidate.optString("bowlerName", "").trim().lowercase()
                    if (bName.split(" ").lastOrNull() == targetSurname) {
                        playerObj = candidate
                        break
                    }
                }
            }
        }

        val bowlInnSpatial = playerObj?.optJSONObject("innings")
            ?.optJSONObject(innNum.toString()) ?: return null

        val matrixMap = mutableMapOf<String, Map<String, Int>>()
        val matrixObj = bowlInnSpatial.optJSONObject("lengthLineMatrix")
        if (matrixObj != null) {
            val lenKeys = matrixObj.keys()
            while (lenKeys.hasNext()) {
                val lenKey = lenKeys.next()
                val lineObj = matrixObj.getJSONObject(lenKey)
                val lineMap = mutableMapOf<String, Int>()
                val lineKeys = lineObj.keys()
                while (lineKeys.hasNext()) {
                    val lk = lineKeys.next()
                    lineMap[lk] = lineObj.optInt(lk, 0)
                }
                matrixMap[lenKey] = lineMap
            }
        }

        val lenTotals = mutableMapOf<String, Int>()
        val lenTotObj = bowlInnSpatial.optJSONObject("lengthTotals")
        if (lenTotObj != null) {
            val lKeys = lenTotObj.keys()
            while (lKeys.hasNext()) {
                val lk = lKeys.next()
                lenTotals[lk] = lenTotObj.optInt(lk, 0)
            }
        }

        val lineTotals = mutableMapOf<String, Int>()
        val lineTotObj = bowlInnSpatial.optJSONObject("lineTotals")
        if (lineTotObj != null) {
            val lkKeys = lineTotObj.keys()
            while (lkKeys.hasNext()) {
                val lk = lkKeys.next()
                lineTotals[lk] = lineTotObj.optInt(lk, 0)
            }
        }

        return BowlingHeatmapData(
            totalBalls = bowlInnSpatial.optInt("totalBalls", 0),
            classifiedLengthCount = bowlInnSpatial.optInt("classifiedLengthCount", 0),
            classifiedLineCount = bowlInnSpatial.optInt("classifiedLineCount", 0),
            bothLengthAndLineCount = bowlInnSpatial.optInt("bothLengthAndLineCount", 0),
            fullTossCount = bowlInnSpatial.optInt("fullTossCount", 0),
            unknownLengthCount = bowlInnSpatial.optInt("unknownLengthCount", 0),
            unknownLineCount = bowlInnSpatial.optInt("unknownLineCount", 0),
            matrix = matrixMap,
            lengthTotals = lenTotals,
            lineTotals = lineTotals
        )
    }

    override fun getPlayerInspectorDataSync(matchId: Long, playerId: Long): PlayerInspectorData {
        val ctx = getContext() ?: return PlayerInspectorData(
            matchId = matchId,
            playerId = playerId,
            playerName = getCanonicalPlayerName(matchId, playerId),
            teamName = "Unknown",
            role = "Player",
            matchRating = 6.0,
            impactRating = 0.0,
            matchSummary = PlayerMatchSummary()
        )

        // Pre-load dim_players and spatial analytics
        ensureDimPlayersLoaded(ctx)
        val isLeftHanded = dimPlayersMapCache?.get(playerId)?.optString("batting_style", "")?.contains("Left", ignoreCase = true) == true
        val spatialJson = getPlayerSpatialAnalyticsSync(ctx, matchId)

        // 1. Load player match summary from fact_player_match.csv
        val fpmList = playerMatchCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_match.csv").also { playerMatchCsvCache = it }
        val fpmRow = fpmList.firstOrNull {
            it["match_id"]?.toLongOrNull() == matchId && it["player_id"]?.toLongOrNull() == playerId
        }

        val playerName = getCanonicalPlayerName(matchId, playerId, fpmRow?.get("player_name") ?: "")
        val teamName = fpmRow?.get("team") ?: ""

        val summary = if (fpmRow != null) {
            PlayerMatchSummary(
                inningsBatted = fpmRow["innings_batted"]?.toIntOrNull() ?: 0,
                runs = fpmRow["runs"]?.toIntOrNull() ?: 0,
                balls = fpmRow["balls"]?.toIntOrNull() ?: 0,
                fours = fpmRow["fours"]?.toIntOrNull() ?: 0,
                sixes = fpmRow["sixes"]?.toIntOrNull() ?: 0,
                dots = fpmRow["dots"]?.toIntOrNull() ?: 0,
                outs = fpmRow["outs"]?.toIntOrNull() ?: 0,
                highestScore = fpmRow["highest_score"]?.toIntOrNull() ?: 0,
                fifties = fpmRow["fifties"]?.toIntOrNull() ?: 0,
                hundreds = fpmRow["hundreds"]?.toIntOrNull() ?: 0,
                ducks = fpmRow["ducks"]?.toIntOrNull() ?: 0,
                battingAverage = fpmRow["batting_average"]?.toDoubleOrNull() ?: 0.0,
                battingStrikeRate = fpmRow["batting_sr"]?.toDoubleOrNull() ?: 0.0,
                dotPct = fpmRow["dot_pct"]?.toDoubleOrNull() ?: 0.0,
                boundaryPct = fpmRow["boundary_pct"]?.toDoubleOrNull() ?: 0.0,
                controlPct = fpmRow["control_pct"]?.toDoubleOrNull() ?: 0.0,
                inningsBowled = fpmRow["innings_bowled"]?.toIntOrNull() ?: 0,
                spellsBowled = fpmRow["spells_bowled"]?.toIntOrNull() ?: 0,
                ballsBowled = fpmRow["balls_bowled"]?.toIntOrNull() ?: 0,
                oversBowled = fpmRow["overs_bowled"]?.toDoubleOrNull() ?: 0.0,
                maidens = fpmRow["maidens"]?.toIntOrNull() ?: 0,
                runsConceded = fpmRow["runs_conceded"]?.toIntOrNull() ?: 0,
                wickets = fpmRow["wickets"]?.toIntOrNull() ?: 0,
                bestBowlingInnings = fpmRow["best_bowling_innings"] ?: "0/0",
                fiveWicketHauls = fpmRow["five_wicket_hauls"]?.toIntOrNull() ?: 0,
                tenWicketMatch = fpmRow["ten_wicket_match"]?.toIntOrNull() ?: 0,
                bowlingAverage = fpmRow["bowling_average"]?.toDoubleOrNull() ?: 0.0,
                bowlingEconomy = fpmRow["bowling_economy"]?.toDoubleOrNull() ?: 0.0,
                bowlingStrikeRate = fpmRow["bowling_strike_rate"]?.toDoubleOrNull() ?: 0.0,
                bowlingDotPct = fpmRow["bowling_dot_pct"]?.toDoubleOrNull() ?: 0.0,
                catches = fpmRow["catches"]?.toIntOrNull() ?: 0,
                runOuts = fpmRow["run_outs"]?.toIntOrNull() ?: 0,
                stumpings = fpmRow["stumpings"]?.toIntOrNull() ?: 0
            )
        } else {
            PlayerMatchSummary()
        }

        // 2. Load Partnerships from fact_player_partnerships.csv
        val fppList = playerPartnershipsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_partnerships.csv").also { playerPartnershipsCsvCache = it }
        val playerPartnerships = fppList.filter {
            it["match_id"]?.toLongOrNull() == matchId && it["player_id"]?.toLongOrNull() == playerId
        }.map { r ->
            val pId2 = r["partner_id"]?.toLongOrNull() ?: 0L
            val pName2 = getCanonicalPlayerName(matchId, pId2, r["partner_name"] ?: "")
            PlayerInspectorPartnership(
                innings = r["innings"]?.toIntOrNull() ?: 1,
                partnerId = pId2,
                partnerName = pName2,
                runs = r["runs"]?.toIntOrNull() ?: 0,
                balls = r["balls"]?.toIntOrNull() ?: 0,
                playerRuns = r["player_runs"]?.toIntOrNull() ?: 0,
                partnerRuns = r["partner_runs"]?.toIntOrNull() ?: 0,
                wicketsBefore = r["wickets_before"]?.toIntOrNull() ?: 0,
                wicketsAfter = r["wickets_after"]?.toIntOrNull() ?: 0,
                startScore = r["start_score"] ?: "0/0",
                endScore = r["end_score"] ?: "0/0",
                importanceScore = r["importance_score"]?.toDoubleOrNull() ?: 0.0
            )
        }

        // 3. Load Batting Innings from fact_player_innings.csv
        val fpiList = playerInningsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_innings.csv").also { playerInningsCsvCache = it }
        val playerInningsRows = fpiList.filter {
            it["match_id"]?.toLongOrNull() == matchId && it["player_id"]?.toLongOrNull() == playerId
        }.sortedBy { it["innings"]?.toIntOrNull() ?: 0 }

        // Build scorecard dismissal lookup maps from scorecardTab of the match
        val scorecardDismissalMap = mutableMapOf<Pair<Int, Long>, String>()
        val scorecardDismissalByNameMap = mutableMapOf<Pair<Int, String>, String>()
        val root = getMatchJson(matchId)
        val scArr = root?.optJSONObject("scorecardTab")?.optJSONArray("scorecards")
        if (scArr != null) {
            for (i in 0 until scArr.length()) {
                val innObj = scArr.getJSONObject(i)
                val innNum = innObj.optInt("inningsNumber", i + 1)
                val batArr = innObj.optJSONArray("batters")
                if (batArr != null) {
                    for (b in 0 until batArr.length()) {
                        val bObj = batArr.getJSONObject(b)
                        val bName = bObj.optString("name", "").trim()
                        val dism = bObj.optString("dismissal", "").trim()
                        if (dism.isNotBlank()) {
                            val bPid = bObj.optString("playerId", "").toLongOrNull() ?: getPlayerIdByName(matchId, bName)
                            if (bPid != null) {
                                scorecardDismissalMap[Pair(innNum, bPid)] = dism
                            }
                            if (bName.isNotBlank()) {
                                scorecardDismissalByNameMap[Pair(innNum, bName.lowercase())] = dism
                                val surname = bName.substringAfterLast(" ").lowercase()
                                scorecardDismissalByNameMap[Pair(innNum, surname)] = dism
                            }
                        }
                    }
                }
            }
        }

        val battingInnings = playerInningsRows.map { r ->
            val innNo = r["innings"]?.toIntOrNull() ?: 1
            val innParts = playerPartnerships.filter { it.innings == innNo }
            val rawPlayerName = r["player_name"] ?: ""
            val rawSurname = rawPlayerName.substringAfterLast(" ").lowercase()

            val scDismissal = scorecardDismissalMap[Pair(innNo, playerId)]
                ?: scorecardDismissalByNameMap[Pair(innNo, playerName.lowercase())]
                ?: scorecardDismissalByNameMap[Pair(innNo, rawPlayerName.lowercase())]
                ?: scorecardDismissalByNameMap[Pair(innNo, rawSurname)]
                ?: ""

            val (parsedType, parsedBowler, parsedFielder) = if (scDismissal.isNotBlank()) {
                parseScorecardDismissal(scDismissal)
            } else {
                Triple("", "", "")
            }

            val rawDismissalType = r["dismissal_type"] ?: "not out"
            val rawDismissedBy = r["dismissed_by"] ?: ""
            val rawFielder = r["fielder"] ?: ""

            val finalDismissalType = when {
                parsedType.isNotBlank() -> parsedType
                else -> rawDismissalType
            }
            val finalDismissedBy = when {
                parsedBowler.isNotBlank() -> parsedBowler
                else -> rawDismissedBy
            }
            val finalFielder = when {
                parsedFielder.isNotBlank() -> parsedFielder
                else -> rawFielder
            }

            val finalIsOut = when {
                scDismissal.isNotBlank() -> !scDismissal.contains("not out", ignoreCase = true) && !scDismissal.contains("retired", ignoreCase = true)
                else -> r["is_out"]?.toBooleanStrictOrNull() ?: (r["is_out"] == "1" || r["is_out"]?.equals("true", ignoreCase = true) == true)
            }

            PlayerInspectorBattingInnings(
                innings = innNo,
                battingPosition = r["batting_position"]?.toIntOrNull() ?: 0,
                runs = r["runs"]?.toIntOrNull() ?: 0,
                balls = r["balls"]?.toIntOrNull() ?: 0,
                fours = r["fours"]?.toIntOrNull() ?: 0,
                sixes = r["sixes"]?.toIntOrNull() ?: 0,
                dots = r["dots"]?.toIntOrNull() ?: 0,
                singles = r["singles"]?.toIntOrNull() ?: 0,
                doubles = r["doubles"]?.toIntOrNull() ?: 0,
                triples = r["triples"]?.toIntOrNull() ?: 0,
                strikeRate = r["strike_rate"]?.toDoubleOrNull() ?: 0.0,
                ballsTo50 = r["balls_to_50"]?.toIntOrNull() ?: 0,
                ballsTo100 = r["balls_to_100"]?.toIntOrNull() ?: 0,
                arrivalRuns = r["arrival_runs"]?.toIntOrNull() ?: 0,
                arrivalWickets = r["arrival_wickets"]?.toIntOrNull() ?: 0,
                arrivalScore = r["arrival_score"] ?: "0/0",
                arrivalOvers = r["arrival_overs"]?.toDoubleOrNull() ?: 0.0,
                departureRuns = r["departure_runs"]?.toIntOrNull() ?: 0,
                departureWickets = r["departure_wickets"]?.toIntOrNull() ?: 0,
                departureScore = r["departure_score"] ?: "",
                departureOvers = r["departure_overs"]?.toDoubleOrNull() ?: 0.0,
                isOut = finalIsOut,
                dismissalType = finalDismissalType,
                dismissedBy = finalDismissedBy,
                fielder = finalFielder,
                controlPct = r["control_pct"]?.toDoubleOrNull() ?: 0.0,
                falseShotPct = r["false_shot_pct"]?.toDoubleOrNull() ?: 0.0,
                attackingPct = r["attacking_pct"]?.toDoubleOrNull() ?: 0.0,
                leavePct = r["leave_pct"]?.toDoubleOrNull() ?: 0.0,
                dotPct = r["dot_pct"]?.toDoubleOrNull() ?: 0.0,
                boundaryPct = r["boundary_pct"]?.toDoubleOrNull() ?: 0.0,
                highestPartnershipRuns = r["highest_partnership_runs"]?.toIntOrNull() ?: 0,
                highestPartnershipPartner = r["highest_partnership_partner"] ?: "",
                partnerships = innParts,
                scorecardDismissal = scDismissal,
                wagonWheel = parseWagonWheel(spatialJson, playerId, innNo, isLeftHanded)
            )
        }

        // 4. Load Bowling Spells from fact_bowling_spells.csv
        val fbsList = bowlingSpellsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_bowling_spells.csv").also { bowlingSpellsCsvCache = it }
        val bowlingSpells = fbsList.filter {
            it["match_id"]?.toLongOrNull() == matchId && it["bowler_id"]?.toLongOrNull() == playerId
        }.sortedWith(compareBy({ it["innings"]?.toIntOrNull() ?: 0 }, { it["spell_number"]?.toIntOrNull() ?: 0 })).map { r ->
            PlayerInspectorBowlingSpell(
                innings = r["innings"]?.toIntOrNull() ?: 1,
                spellNumber = r["spell_number"]?.toIntOrNull() ?: 1,
                bowlerId = r["bowler_id"]?.toLongOrNull() ?: playerId,
                bowlerName = playerName,
                team = r["team"] ?: teamName,
                startOver = r["start_over"]?.toIntOrNull() ?: 1,
                endOver = r["end_over"]?.toIntOrNull() ?: 1,
                oversCount = r["overs_count"]?.toIntOrNull() ?: 1,
                ballsBowled = r["balls_bowled"]?.toIntOrNull() ?: 0,
                maidens = r["maidens"]?.toIntOrNull() ?: 0,
                runsConceded = r["runs_conceded"]?.toIntOrNull() ?: 0,
                wickets = r["wickets"]?.toIntOrNull() ?: 0,
                economy = r["economy"]?.toDoubleOrNull() ?: 0.0,
                dots = r["dots"]?.toIntOrNull() ?: 0,
                dotPct = r["dot_pct"]?.toDoubleOrNull() ?: 0.0,
                foursConceded = r["fours_conceded"]?.toIntOrNull() ?: 0,
                sixesConceded = r["sixes_conceded"]?.toIntOrNull() ?: 0,
                wicketsTakenList = r["wickets_taken_list"] ?: "",
                spellImpactScore = r["spell_impact_score"]?.toDoubleOrNull() ?: 50.0
            )
        }

        // 5. Load Match Rating & Impact (Single Source of Truth: RatingsTab / Match JSON, with CSV fallback)
        val ratingsData = getRatingsTabDataSync(matchId)
        val ratingModel = (ratingsData.team1Players + ratingsData.team2Players).firstOrNull {
            val pidMatch = it.playerId?.toLongOrNull() == playerId
            val nameMatch = it.playerName.trim().equals(playerName.trim(), ignoreCase = true) ||
                            it.playerName.trim().contains(playerName.trim(), ignoreCase = true) ||
                            playerName.trim().contains(it.playerName.trim(), ignoreCase = true)
            pidMatch || nameMatch
        }

        val playerDetail = ratingsData.playerInningsDetails[playerId.toInt()]
            ?: ratingsData.playerInningsDetails.values.firstOrNull { it.playerName.equals(playerName, ignoreCase = true) }

        // Line and Length accuracy calculation for match summary
        val matchLineAcc = if (ratingModel?.lineAcc != null && ratingModel.lineAcc.isNotBlank() && ratingModel.lineAcc != "-") {
            ratingModel.lineAcc.removeSuffix("%").trim().toDoubleOrNull() ?: 0.0
        } else if (summary.oversBowled > 0.0) {
            (summary.bowlingDotPct * 0.55 + (10 - summary.bowlingEconomy).coerceIn(0.0, 10.0) * 4.5).coerceIn(52.0, 94.0)
        } else 0.0

        val matchLengthAcc = if (ratingModel?.lengthAcc != null && ratingModel.lengthAcc.isNotBlank() && ratingModel.lengthAcc != "-") {
            ratingModel.lengthAcc.removeSuffix("%").trim().toDoubleOrNull() ?: 0.0
        } else if (summary.oversBowled > 0.0) {
            (summary.bowlingDotPct * 0.50 + (10 - summary.bowlingEconomy).coerceIn(0.0, 10.0) * 4.2 + (if (summary.wickets > 0) 5.0 else 0.0)).coerceIn(50.0, 92.0)
        } else 0.0

        val finalSummary = summary.copy(
            lineAccuracyPct = matchLineAcc,
            lengthAccuracyPct = matchLengthAcc
        )

        // 4b. Synthesize Innings Bowling Figures with line & length accuracy
        val bowlingInnings = bowlingSpells.groupBy { it.innings }.map { (innNum, spells) ->
            val totalBalls = spells.sumOf { it.ballsBowled }
            val totalMaidens = spells.sumOf { it.maidens }
            val totalRuns = spells.sumOf { it.runsConceded }
            val totalWkts = spells.sumOf { it.wickets }
            val oversVal = (totalBalls / 6) + (totalBalls % 6) / 10.0
            val econVal = if (totalBalls > 0) (totalRuns.toDouble() / (totalBalls.toDouble() / 6.0)) else 0.0
            val totalDots = spells.sumOf { it.dots }
            val dotPctVal = if (totalBalls > 0) (totalDots.toDouble() / totalBalls.toDouble()) * 100.0 else 0.0

            val innDetail = playerDetail?.innings?.firstOrNull { it.inningsNumber == innNum }
            val innLineAcc = if (innDetail != null && innDetail.lineAccuracyPct > 0.0) {
                innDetail.lineAccuracyPct
            } else if (oversVal > 0.0) {
                (dotPctVal * 0.55 + (10 - econVal).coerceIn(0.0, 10.0) * 4.5).coerceIn(52.0, 94.0)
            } else 0.0

            val innLengthAcc = if (innDetail != null && innDetail.lengthAccuracyPct > 0.0) {
                innDetail.lengthAccuracyPct
            } else if (oversVal > 0.0) {
                (dotPctVal * 0.50 + (10 - econVal).coerceIn(0.0, 10.0) * 4.2 + (if (totalWkts > 0) 5.0 else 0.0)).coerceIn(50.0, 92.0)
            } else 0.0

            PlayerInspectorBowlingInnings(
                innings = innNum,
                overs = oversVal,
                maidens = totalMaidens,
                runsConceded = totalRuns,
                wickets = totalWkts,
                economy = econVal,
                dotPct = dotPctVal,
                spellsCount = spells.size,
                lineAccuracyPct = innLineAcc,
                lengthAccuracyPct = innLengthAcc,
                spells = spells,
                heatmap = parseBowlingHeatmap(spatialJson, playerId, innNum)
            )
        }.sortedBy { it.innings }

        // CSV Fallback values
        val fprList = playerMatchRatingCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_match_rating.csv").also { playerMatchRatingCsvCache = it }
        val fprRow = fprList.firstOrNull {
            val mid = (it["match_id"] ?: it["1"])?.toLongOrNull()
            val pid = it["player_id"]?.toLongOrNull()
            mid == matchId && pid == playerId
        }
        val csvMatchRating = fprRow?.get("overall_match_rating")?.toDoubleOrNull() ?: 6.5

        val fpiImpList = playerMatchImpactCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_player_match_impact.csv").also { playerMatchImpactCsvCache = it }
        val fpiImpRow = fpiImpList.firstOrNull {
            val mid = it["match_id"]?.toDoubleOrNull()?.toLong() ?: it["match_id"]?.toLongOrNull()
            val pid = it["player_id"]?.toDoubleOrNull()?.toLong() ?: it["player_id"]?.toLongOrNull()
            mid == matchId && pid == playerId
        }
        val csvImpactRating = fpiImpRow?.get("total_impact")?.toDoubleOrNull() ?: fpiImpRow?.get("match_mvp_score")?.toDoubleOrNull() ?: 0.0
        val csvImpactRank = fpiImpRow?.get("impact_rank")?.toDoubleOrNull()?.toInt() ?: fpiImpRow?.get("impact_rank")?.toIntOrNull() ?: 0
        val csvBattingImpact = fpiImpRow?.get("batting_impact")?.toDoubleOrNull() ?: 0.0
        val csvBowlingImpact = fpiImpRow?.get("bowling_impact")?.toDoubleOrNull() ?: 0.0

        val matchRating = ratingModel?.overallRating ?: csvMatchRating

        // Impact Rankings from Ratings Tab
        val impRankModel = ratingsData.impactRankings.firstOrNull {
            it.playerId?.toLongOrNull() == playerId ||
            it.playerName.trim().equals(playerName.trim(), ignoreCase = true) ||
            it.playerName.trim().contains(playerName.trim(), ignoreCase = true) ||
            playerName.trim().contains(it.playerName.trim(), ignoreCase = true)
        }

        val impactRating = when {
            impRankModel != null && impRankModel.impactScore > 0.0 -> impRankModel.impactScore
            ratingModel != null && ratingModel.impactRating > 0.0 -> ratingModel.impactRating
            else -> csvImpactRating
        }

        val impactRank = when {
            impRankModel != null && impRankModel.impactRank > 0 -> impRankModel.impactRank
            ratingModel != null && ratingModel.impactRank.isNotBlank() && ratingModel.impactRank != "-" -> {
                ratingModel.impactRank.removePrefix("#").trim().toIntOrNull() ?: csvImpactRank
            }
            else -> csvImpactRank
        }

        val battingImpact = when {
            impRankModel != null && impRankModel.battingImpact > 0.0 -> impRankModel.battingImpact
            ratingModel != null && ratingModel.battingImpactRating > 0.0 -> ratingModel.battingImpactRating
            else -> csvBattingImpact
        }

        val bowlingImpact = when {
            impRankModel != null && impRankModel.bowlingImpact > 0.0 -> impRankModel.bowlingImpact
            ratingModel != null && ratingModel.bowlingImpactRating > 0.0 -> ratingModel.bowlingImpactRating
            else -> csvBowlingImpact
        }

        // 7. Load Turning Points from fact_match_turning_points.csv
        val tpList = turningPointsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_match_turning_points.csv").also { turningPointsCsvCache = it }
        val turningPoints = tpList.filter {
            val mid = it["match_id"]?.toDoubleOrNull()?.toLong() ?: it["match_id"]?.toLongOrNull()
            val b1 = it["batter1_id"]?.toDoubleOrNull()?.toLong() ?: it["batter1_id"]?.toLongOrNull()
            val b2 = it["batter2_id"]?.toDoubleOrNull()?.toLong() ?: it["batter2_id"]?.toLongOrNull()
            mid == matchId && (b1 == playerId || b2 == playerId)
        }.map { r ->
            PlayerInspectorTurningPoint(
                rank = r["turning_point_rank"]?.toDoubleOrNull()?.toInt() ?: 1,
                innings = r["innings"]?.toDoubleOrNull()?.toInt() ?: 1,
                eventType = "PARTNERSHIP",
                title = r["partnership_name"] ?: "Crucial Partnership",
                description = "${r["runs_added"]?.toDoubleOrNull()?.toInt() ?: 0} runs added in ${r["balls_faced"]?.toDoubleOrNull()?.toInt() ?: 0} balls",
                impactScore = r["impact_score"]?.toDoubleOrNull() ?: 0.0,
                runsAdded = r["runs_added"]?.toDoubleOrNull()?.toInt() ?: 0,
                ballsFaced = r["balls_faced"]?.toDoubleOrNull()?.toInt() ?: 0,
                startBall = r["start_ball"]?.toDoubleOrNull() ?: 0.0,
                endBall = r["end_ball"]?.toDoubleOrNull() ?: 0.0
            )
        }

        // 8. Load Player Role from dim_players.json or Ratings Tab
        if (dimPlayersMapCache == null) {
            val playersJson = AssetWarehouseLoader.loadJson(ctx, "dim_players.json")
            if (playersJson.isNotBlank()) {
                try {
                    val arr = org.json.JSONArray(playersJson)
                    val map = mutableMapOf<Long, JSONObject>()
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pId = pObj.optLong("player_id", 0L)
                        if (pId > 0) map[pId] = pObj
                    }
                    dimPlayersMapCache = map
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val pObj = dimPlayersMapCache?.get(playerId)
        var role = pObj?.optString("role", "") ?: ""
        if (role.isBlank() || role.equals("null", ignoreCase = true) || role.equals("Player", ignoreCase = true)) {
            if (ratingModel != null && ratingModel.role.isNotBlank() && !ratingModel.role.equals("Player", ignoreCase = true)) {
                role = ratingModel.role
            } else {
                role = when {
                    summary.oversBowled > 10.0 && summary.runs > 40 -> "All-Rounder"
                    summary.oversBowled > 15.0 -> "Bowler"
                    summary.stumpings > 0 || (summary.catches >= 2 && summary.oversBowled == 0.0) -> "Wicketkeeper Batter"
                    else -> "Batter"
                }
            }
        }

        // 9. Generate Match Story Narrative (Phase 6D) with Contextual Cricket Intelligence
        val topInnings = battingInnings.maxByOrNull { it.runs }
        val topPart = playerPartnerships.maxByOrNull { it.runs }
        val storyBuilder = StringBuilder()
        val totalRuns = summary.runs
        val totalWickets = summary.wickets
        val overs = summary.oversBowled

        when {
            // Elite match-winning / MVP performance
            impactRank == 1 || impactRating >= 300.0 -> {
                if (topInnings != null && topInnings.runs >= 100) {
                    if (topInnings.arrivalWickets >= 2 && topInnings.arrivalRuns <= 50) {
                        storyBuilder.append("$playerName delivered a legendary masterclass, rescuing $teamName from ${topInnings.arrivalScore} with an iconic ${topInnings.runs} (${topInnings.balls}b). ")
                    } else {
                        storyBuilder.append("$playerName spearheaded $teamName's dominant performance with an emphatic match aggregate of $totalRuns runs (HS ${summary.highestScore}). ")
                    }
                } else if (totalWickets >= 6) {
                    storyBuilder.append("$playerName delivered a devastating bowling performance, capturing $totalWickets wickets (BBI ${summary.bestBowlingInnings}) to dismantle the opposition. ")
                } else {
                    storyBuilder.append("$playerName delivered an outstanding all-round masterclass to anchor $teamName throughout the match. ")
                }
                if (topPart != null && topPart.runs >= 70) {
                    storyBuilder.append("His ${topPart.runs}-run stand with ${topPart.partnerName} completely altered the match trajectory. ")
                }
                storyBuilder.append("Named Match MVP with a massive impact score of ${String.format(java.util.Locale.US, "%.1f", impactRating)}.")
            }

            // Strong High Impact Performance (Impact >= 150 or 80+ runs / 4+ wkts)
            impactRating >= 150.0 || totalRuns >= 80 || totalWickets >= 4 -> {
                if (totalRuns >= 80) {
                    if (topInnings != null && topInnings.arrivalWickets >= 2 && topInnings.arrivalRuns <= 50) {
                        storyBuilder.append("$playerName led a crucial rescue effort for $teamName from ${topInnings.arrivalScore}, compiling ${topInnings.runs} runs in Innings ${topInnings.innings}. ")
                    } else {
                        storyBuilder.append("$playerName anchored $teamName's batting lineup with $totalRuns match runs (HS ${summary.highestScore}). ")
                    }
                }
                if (totalWickets >= 4) {
                    storyBuilder.append("With the ball, he took $totalWickets key wickets across ${String.format(java.util.Locale.US, "%.1f", overs)} overs (BBI ${summary.bestBowlingInnings}). ")
                }
                if (topPart != null && topPart.runs >= 50) {
                    storyBuilder.append("He combined with ${topPart.partnerName} for a vital ${topPart.runs}-run partnership. ")
                }
                storyBuilder.append("A high-impact performance that heavily influenced key sessions (Rank #$impactRank).")
            }

            // Solid Contributor (Impact 70-149 or 35-79 runs / 2-3 wkts)
            impactRating >= 70.0 || totalRuns >= 35 || totalWickets in 2..3 -> {
                if (totalRuns >= 35) {
                    storyBuilder.append("$playerName contributed solid runs for $teamName, scoring $totalRuns runs across ${summary.inningsBatted} innings with a best of ${summary.highestScore}. ")
                }
                if (totalWickets in 2..3) {
                    storyBuilder.append("He provided crucial breakthroughs with the ball, picking up $totalWickets wickets (Econ ${String.format(java.util.Locale.US, "%.2f", summary.bowlingEconomy)}). ")
                } else if (overs > 10.0 && totalWickets < 2) {
                    storyBuilder.append("He maintained disciplined pressure with the ball across ${String.format(java.util.Locale.US, "%.1f", overs)} overs. ")
                }
                if (summary.catches >= 2) {
                    storyBuilder.append("He was sharp in the field, claiming ${summary.catches} catches. ")
                }
                storyBuilder.append("Played an effective supporting role throughout the Test.")
            }

            // Modest / Quiet / Low Return Performance (Impact < 70, e.g. Warner 10 runs)
            else -> {
                if (summary.inningsBatted > 0 && totalRuns < 25) {
                    if (totalRuns <= 10) {
                        storyBuilder.append("$playerName experienced a difficult match with the bat, dismissed cheaply in both innings to finish with just $totalRuns runs (HS ${summary.highestScore}). ")
                    } else {
                        storyBuilder.append("$playerName had a modest return with the bat, managing $totalRuns runs across ${summary.inningsBatted} innings. ")
                    }
                } else if (totalRuns in 25..34) {
                    storyBuilder.append("$playerName made modest starts with $totalRuns match runs, but was unable to convert into a substantial score. ")
                }

                if (overs > 0.0) {
                    if (totalWickets > 0) {
                        storyBuilder.append("He bowled ${String.format(java.util.Locale.US, "%.1f", overs)} overs taking $totalWickets wicket${if (totalWickets > 1) "s" else ""}. ")
                    } else {
                        storyBuilder.append("He delivered a short spell of ${String.format(java.util.Locale.US, "%.1f", overs)} overs without a breakthrough. ")
                    }
                }

                if (summary.catches > 0) {
                    storyBuilder.append("He took ${summary.catches} catch${if (summary.catches > 1) "es" else ""} in the field. ")
                }

                if (summary.inningsBatted == 0 && overs == 0.0 && summary.catches == 0) {
                    storyBuilder.append("$playerName had limited involvement across the match.")
                } else {
                    storyBuilder.append("A quiet outing with limited overall match impact (Rank #$impactRank).")
                }
            }
        }
        val matchStoryNarrative = storyBuilder.toString()

        val contributions = mutableListOf<String>()
        if (summary.runs > 0) {
            contributions.add("${summary.runs} match runs (${summary.inningsBatted} innings)")
        }
        if (summary.hundreds > 0) {
            contributions.add("${if (summary.hundreds == 1) "Century" else "${summary.hundreds} Centuries"} scored")
        } else if (summary.fifties > 0) {
            contributions.add("${summary.fifties} Half-Century scored")
        }
        if (topInnings != null && topInnings.runs >= 50 && topInnings.arrivalWickets >= 2 && topInnings.arrivalRuns <= 50) {
            contributions.add("Crisis recovery from ${topInnings.arrivalScore}")
        }
        if (topPart != null && topPart.runs >= 50) {
            contributions.add("${topPart.runs}-run stand with ${topPart.partnerName}")
        }
        if (summary.wickets >= 5) {
            contributions.add("${summary.wickets}-wicket match haul (${summary.bestBowlingInnings} BBI)")
        } else if (summary.wickets > 0) {
            contributions.add("${summary.wickets} wickets taken (Econ ${String.format(java.util.Locale.US, "%.2f", summary.bowlingEconomy)})")
        }
        if (summary.catches >= 2) {
            contributions.add("${summary.catches} catches in the field")
        }
        if (impactRating > 200.0) {
            contributions.add("Match Defining Performance (Impact ${String.format(java.util.Locale.US, "%.1f", impactRating)})")
        }

        return PlayerInspectorData(
            matchId = matchId,
            playerId = playerId,
            playerName = playerName,
            teamName = teamName,
            role = role,
            matchRating = matchRating,
            impactRating = impactRating,
            impactRank = impactRank,
            battingImpact = battingImpact,
            bowlingImpact = bowlingImpact,
            matchStoryNarrative = matchStoryNarrative,
            contributions = contributions,
            matchSummary = finalSummary,
            battingInnings = battingInnings,
            bowlingInnings = bowlingInnings,
            bowlingSpells = bowlingSpells,
            turningPoints = turningPoints,
            partnerships = playerPartnerships
        )
    }

    override fun getMatchStorySync(matchId: Long): List<MatchStoryModel> = emptyList()
    override fun getMatchMomentumSync(matchId: Long): List<MomentumModel> = emptyList()
    override fun getMatchPressureSync(matchId: Long): List<PressureModel> = emptyList()
    override fun getMatchCollapsesSync(matchId: Long): List<CollapseModel> {
        val root = getMatchJson(matchId)
        if (root != null) {
            val colObj = root.optJSONObject("storyTab")?.optJSONObject("primaryCollapse")
            if (colObj != null) {
                return listOf(
                    CollapseModel(
                        collapseId = colObj.optString("collapseId", "col_$matchId"),
                        matchId = matchId,
                        innings = colObj.optInt("innings", 1),
                        team = colObj.optString("team", "Team"),
                        score = colObj.optString("score", "Wickets Lost"),
                        wicketsLost = colObj.optInt("wicketsLost", 4),
                        runsAdded = colObj.optInt("runsAdded", 50),
                        oversTaken = colObj.optDouble("oversTaken", 0.0),
                        severity = colObj.optDouble("severity", 75.0),
                        classification = colObj.optString("classification", "Major Collapse"),
                        explanation = colObj.optString("explanation", "Collapse.")
                    )
                )
            }
        }
        return emptyList()
    }

    override fun getMatchRecoveriesSync(matchId: Long): List<RecoveryModel> {
        val root = getMatchJson(matchId)
        if (root != null) {
            val recObj = root.optJSONObject("storyTab")?.optJSONObject("primaryRecovery")
            if (recObj != null) {
                return listOf(
                    RecoveryModel(
                        recoveryId = recObj.optString("recoveryId", "rec_$matchId"),
                        matchId = matchId,
                        innings = recObj.optInt("innings", 1),
                        team = recObj.optString("team", "Team"),
                        transition = recObj.optString("transition", "Recovery Stand"),
                        partnershipRuns = recObj.optInt("partnershipRuns", 80),
                        recoveryScore = recObj.optDouble("recoveryScore", 75.0),
                        classification = recObj.optString("classification", "Major Recovery"),
                        explanation = recObj.optString("explanation", "Key rescue partnership.")
                    )
                )
            }
        }
        return emptyList()
    }
    override fun getPlayerMatchRatingsSync(matchId: Long): List<PlayerMatchRatingModel> = emptyList()
    override fun getMatchRatingsSync(matchId: Long): List<PlayerRatingModel> = emptyList()
    override fun getPlayerMatchImpactSync(matchId: Long): List<PlayerMatchImpactModel> = emptyList()
    override fun getPlayerInningsSync(matchId: Long): List<PlayerInningsModel> = emptyList()
    override fun getBowlingInningsSync(matchId: Long): List<BowlingInningsModel> = emptyList()

    override fun getMatchListSync(): List<MatchIndexItem> {
        indexCache?.let { return it }
        val ctx = getContext() ?: return emptyList()
        val jsonStr = AssetWarehouseLoader.loadJson(ctx, "matches_index.json")
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<MatchIndexItem>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val winnerStr = obj.optString("winner", "Match Drawn")
                val cleanWinner = if (winnerStr.equals("nan", ignoreCase = true) || winnerStr.isBlank()) "Match Drawn" else winnerStr
                val vicMargin = obj.optString("victoryMargin", if (cleanWinner == "Match Drawn") "Match Drawn" else "$cleanWinner won")
                val rawTitle = obj.optString("matchTitle", "Test Match").cleanYearBrackets()
                val rawHeader = obj.optString("seriesHeader", rawTitle).cleanYearBrackets()
                list.add(
                    MatchIndexItem(
                        matchId = obj.optLong("matchId", 0L),
                        matchTitle = rawTitle,
                        seriesHeader = rawHeader,
                        testSubHeader = obj.optString("testSubHeader", "1st Test, ${obj.optString("venue", "")}"),
                        team1 = obj.optString("team1", "Team 1"),
                        team1Short = obj.optString("team1Short", obj.optString("team1", "T1").take(3).uppercase()),
                        team1Score = obj.optString("team1Score", "Yet to bat"),
                        team2 = obj.optString("team2", "Team 2"),
                        team2Short = obj.optString("team2Short", obj.optString("team2", "T2").take(3).uppercase()),
                        team2Score = obj.optString("team2Score", "Yet to bat"),
                        winner = cleanWinner,
                        victoryMargin = vicMargin,
                        venue = obj.optString("venue", "Cricket Ground"),
                        season = obj.optString("season", "2019"),
                        file = obj.optString("file", "")
                    )
                )
            }
            indexCache = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override fun getSeriesListSync(): List<SeriesIndexItem> {
        seriesCache?.let { return it }
        val ctx = getContext() ?: return emptyList()
        val rows = AssetWarehouseLoader.loadCsv(ctx, "dim_series.csv")
        val list = rows.mapNotNull { row ->
            val sId = row["series_id"]?.toIntOrNull() ?: return@mapNotNull null
            SeriesIndexItem(
                seriesId = sId,
                seriesName = (row["series_name"] ?: "").cleanYearBrackets(),
                season = row["season"] ?: "",
                startDate = row["start_date"] ?: "",
                endDate = row["end_date"] ?: "",
                team1 = row["team1"] ?: "",
                team2 = row["team2"] ?: "",
                totalMatches = row["total_matches"]?.toIntOrNull() ?: 1,
                winner = row["winner"] ?: "",
                resultMargin = row["result_margin"] ?: "",
                venueCountries = row["venue_countries"] ?: ""
            )
        }
        seriesCache = list
        return list
    }

    override fun getSeriesHeaderSync(seriesId: Int): SeriesHeaderData {
        val seriesItem = getSeriesListSync().firstOrNull { it.seriesId == seriesId }
        val ctx = getContext()
        var finalScore = seriesItem?.resultMargin ?: "2-2"
        if (ctx != null) {
            val standings = seriesStandingsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_standings.csv").also { seriesStandingsCsvCache = it }
            val seriesStands = standings.filter { it["series_id"]?.toIntOrNull() == seriesId }
            if (seriesStands.isNotEmpty()) {
                finalScore = seriesStands.last()["series_score"] ?: (seriesItem?.resultMargin ?: "2-2")
            }
        }

        return if (seriesItem != null) {
            SeriesHeaderData(
                seriesId = seriesItem.seriesId,
                seriesName = seriesItem.seriesName,
                season = seriesItem.season,
                startDate = seriesItem.startDate,
                endDate = seriesItem.endDate,
                team1 = seriesItem.team1,
                team2 = seriesItem.team2,
                totalMatches = seriesItem.totalMatches,
                winner = seriesItem.winner,
                resultMargin = seriesItem.resultMargin,
                finalSeriesScore = finalScore,
                venueCountries = seriesItem.venueCountries
            )
        } else {
            SeriesHeaderData(
                seriesId = seriesId,
                seriesName = "Series $seriesId",
                season = "2019",
                startDate = "",
                endDate = "",
                team1 = "Team 1",
                team2 = "Team 2",
                totalMatches = 5,
                winner = "Drawn",
                resultMargin = "2-2",
                finalSeriesScore = finalScore,
                venueCountries = ""
            )
        }
    }

    override fun getSeriesOverviewSync(seriesId: Int): SeriesOverviewData {
        val header = getSeriesHeaderSync(seriesId)
        val ctx = getContext()

        // 1. Ensure dim_players map is populated
        if (dimPlayersMapCache == null && ctx != null) {
            val playersJson = AssetWarehouseLoader.loadJson(ctx, "dim_players.json")
            if (playersJson.isNotBlank()) {
                try {
                    val arr = org.json.JSONArray(playersJson)
                    val map = mutableMapOf<Long, JSONObject>()
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pId = pObj.optLong("player_id", 0L)
                        if (pId > 0) map[pId] = pObj
                    }
                    dimPlayersMapCache = map
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val nicknames = mapOf(
            "Steven Peter Devereux Smith" to "Steve Smith",
            "Patrick James Cummins" to "Pat Cummins",
            "Joseph Edward Root" to "Joe Root",
            "Benjamin Andrew Stokes" to "Ben Stokes",
            "Mitchell Aaron Starc" to "Mitchell Starc",
            "Josh Reginald Hazlewood" to "Josh Hazlewood",
            "David Andrew Warner" to "David Warner",
            "Kane Stuart Williamson" to "Kane Williamson",
            "James Michael Anderson" to "James Anderson",
            "Stuart Christopher John Broad" to "Stuart Broad",
            "Cheteshwar Arvind Pujara" to "Cheteshwar Pujara",
            "Ajinkya Madhukar Rahane" to "Ajinkya Rahane",
            "Rishabh Rajendra Pant" to "Rishabh Pant",
            "Kagiso Rabada" to "Kagiso Rabada",
            "Quinton de Kock" to "Quinton de Kock",
            "Dean Elgar" to "Dean Elgar",
            "Babar Azam" to "Babar Azam",
            "Ravichandran Ashwin" to "Ravichandran Ashwin",
            "Ravindrasinh Anirudhsinh Jadeja" to "Ravindra Jadeja",
            "Rohit Gurunath Sharma" to "Rohit Sharma",
            "Virat Kohli" to "Virat Kohli"
        )

        // 2. Load series player stats from fact_series_player_stats.csv
        val allPlayerRows = if (ctx != null) {
            seriesPlayerStatsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_player_stats.csv").also { seriesPlayerStatsCsvCache = it }
        } else emptyList()

        val seriesPlayerRows = allPlayerRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        val playerStatsList = seriesPlayerRows.mapNotNull { row ->
            val pId = row["player_id"]?.toLongOrNull() ?: return@mapNotNull null
            val shortName = row["player_name"] ?: "Player"
            val pObj = dimPlayersMapCache?.get(pId)
            val fullDimName = pObj?.optString("player_name", "")?.takeIf { it.isNotBlank() }
            val countryStr = pObj?.optString("country", "") ?: ""

            val cleanFullName = when {
                fullDimName != null && nicknames.containsKey(fullDimName) -> nicknames[fullDimName]!!
                fullDimName != null && fullDimName.split(" ").filter { it.isNotBlank() }.size <= 2 -> fullDimName
                fullDimName != null && fullDimName.isNotBlank() -> fullDimName
                else -> shortName
            }

            val matchesVal = row["matches"]?.toIntOrNull() ?: 1
            val runsVal = row["runs"]?.toIntOrNull() ?: 0
            val ballsVal = row["balls"]?.toIntOrNull() ?: 0
            val avgVal = row["average"]?.toDoubleOrNull() ?: 0.0
            val c100 = row["100s"]?.toIntOrNull() ?: 0
            val c50 = row["50s"]?.toIntOrNull() ?: 0
            val wktsVal = row["wickets"]?.toIntOrNull() ?: 0
            val bbiVal = row["best_bowling"] ?: "0/0"
            val catchesVal = row["catches"]?.toIntOrNull() ?: 0
            val ratingAvgVal = row["rating_avg"]?.toDoubleOrNull() ?: 6.5
            val impactAvgVal = row["impact_avg"]?.toDoubleOrNull() ?: 50.0
            val totalImpactVal = impactAvgVal * matchesVal

            SeriesPlayerStatItem(
                seriesId = seriesId,
                playerId = pId,
                playerName = cleanFullName,
                shortName = shortName,
                country = countryStr,
                matches = matchesVal,
                runs = runsVal,
                balls = ballsVal,
                average = avgVal,
                centuries = c100,
                fifties = c50,
                wickets = wktsVal,
                bestBowling = bbiVal,
                catches = catchesVal,
                ratingAvg = ratingAvgVal,
                impactAvg = impactAvgVal,
                totalImpact = totalImpactVal
            )
        }

        // 3. Determine Series MVP (highest totalImpact, fallback ratingAvg), Leading Batter, Leading Bowler
        val fallbackPlayer = SeriesPlayerStatItem(
            seriesId = seriesId,
            playerId = 0L,
            playerName = "Player",
            shortName = "Player",
            country = "",
            matches = 1,
            runs = 0,
            balls = 0,
            average = 0.0,
            centuries = 0,
            fifties = 0,
            wickets = 0,
            bestBowling = "0/0",
            catches = 0,
            ratingAvg = 6.0,
            impactAvg = 50.0,
            totalImpact = 50.0
        )

        val mvp = playerStatsList.maxWithOrNull(compareBy({ it.totalImpact }, { it.ratingAvg })) ?: fallbackPlayer
        val leadingBatter = playerStatsList.maxWithOrNull(compareBy({ it.runs }, { it.average })) ?: fallbackPlayer
        val leadingBowler = playerStatsList.maxWithOrNull(compareBy({ it.wickets }, { -it.average })) ?: fallbackPlayer

        // 4. Load matches breakdown from fact_series_matches.csv
        val allMatchRows = if (ctx != null) {
            seriesMatchesCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_matches.csv").also { seriesMatchesCsvCache = it }
        } else emptyList()

        val seriesMatchItems = allMatchRows.filter { it["series_id"]?.toIntOrNull() == seriesId }.mapNotNull { row ->
            val mId = row["match_id"]?.toLongOrNull() ?: return@mapNotNull null
            SeriesMatchSummaryItem(
                seriesId = seriesId,
                matchId = mId,
                matchNumber = row["match_number"] ?: "Test Match",
                winner = row["winner"] ?: "Drawn",
                margin = row["margin"] ?: ""
            )
        }

        return SeriesOverviewData(
            header = header,
            mvp = mvp,
            leadingBatter = leadingBatter,
            leadingBowler = leadingBowler,
            matches = seriesMatchItems,
            allPlayers = playerStatsList
        )
    }

    override fun getSeriesTimelineSync(seriesId: Int): SeriesTimelineData {
        val header = getSeriesHeaderSync(seriesId)
        val ctx = getContext()

        // 1. Load matches for series from fact_series_matches.csv
        val matchRows = if (ctx != null) {
            seriesMatchesCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_matches.csv").also { seriesMatchesCsvCache = it }
        } else emptyList()

        val seriesMatches = matchRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        // 2. Load standings for series from fact_series_standings.csv
        val standingsRows = if (ctx != null) {
            seriesStandingsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_standings.csv").also { seriesStandingsCsvCache = it }
        } else emptyList()

        val seriesStandings = standingsRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        // 3. Load matches index for scorelines and venue
        val matchIndexMap = getMatchListSync().associateBy { it.matchId }

        val timelineItems = seriesMatches.mapIndexed { index, mRow ->
            val matchNumber = mRow["match_number"] ?: "${index + 1}th Test"
            val mId = mRow["match_id"]?.toLongOrNull() ?: 0L
            val winner = mRow["winner"] ?: "Draw"
            val margin = mRow["margin"] ?: "Match Drawn"

            val standingRow = seriesStandings.firstOrNull { it["after_match"]?.toIntOrNull() == (index + 1) }
            val seriesScore = standingRow?.get("series_score") ?: (if (index == 0) "1-0" else "Series")
            val t1Wins = standingRow?.get("team1_wins")?.toIntOrNull() ?: 0
            val t2Wins = standingRow?.get("team2_wins")?.toIntOrNull() ?: 0
            val draws = standingRow?.get("draws")?.toIntOrNull() ?: 0

            val indexItem = matchIndexMap[mId]

            SeriesTimelineItem(
                seriesId = seriesId,
                matchId = mId,
                matchIndex = index + 1,
                matchNumber = matchNumber,
                winner = winner,
                margin = margin,
                seriesScoreAfterMatch = seriesScore,
                team1Wins = t1Wins,
                team2Wins = t2Wins,
                draws = draws,
                venue = indexItem?.venue ?: "",
                team1 = indexItem?.team1 ?: header.team1,
                team1Short = indexItem?.team1Short ?: header.team1.take(3).uppercase(),
                team1Score = indexItem?.team1Score ?: "",
                team2 = indexItem?.team2 ?: header.team2,
                team2Short = indexItem?.team2Short ?: header.team2.take(3).uppercase(),
                team2Score = indexItem?.team2Score ?: ""
            )
        }

        return SeriesTimelineData(
            header = header,
            timeline = timelineItems
        )
    }

    private val starPlayerNameMap = mapOf(
        291L to "Steve Smith",
        425L to "Pat Cummins",
        31L to "Ben Stokes",
        292L to "Stuart Broad",
        259L to "Rory Burns",
        128L to "Jofra Archer",
        274L to "Joe Root",
        135L to "Joe Root",
        332L to "David Warner",
        163L to "Marnus Labuschagne",
        248L to "Quinton de Kock",
        84L to "Faf du Plessis",
        85L to "Dimuth Karunaratne",
        139L to "Kagiso Rabada",
        20L to "Angelo Mathews",
        101L to "Rangana Herath",
        68L to "Dean Elgar",
        324L to "Vernon Philander",
        236L to "Pat Cummins"
    )

    private fun resolveCleanPlayerName(playerId: Long, rawName: String): String {
        starPlayerNameMap[playerId]?.let { return it }
        val clean = rawName.trim()
        val nicknames = mapOf(
            "SPD Smith" to "Steve Smith",
            "Steven Peter Devereux Smith" to "Steve Smith",
            "BA Stokes" to "Ben Stokes",
            "Benjamin Andrew Stokes" to "Ben Stokes",
            "RJ Burns" to "Rory Burns",
            "Rory Joseph Burns" to "Rory Burns",
            "PJ Cummins" to "Pat Cummins",
            "Patrick James Cummins" to "Pat Cummins",
            "SCJ Broad" to "Stuart Broad",
            "Stuart Christopher John Broad" to "Stuart Broad",
            "JC Archer" to "Jofra Archer",
            "Jofra Chioke Archer" to "Jofra Archer",
            "JE Root" to "Joe Root",
            "Joseph Edward Root" to "Joe Root",
            "DA Warner" to "David Warner",
            "David Andrew Warner" to "David Warner",
            "M Labuschagne" to "Marnus Labuschagne",
            "Marnus Labuschagne" to "Marnus Labuschagne",
            "MA Starc" to "Mitchell Starc",
            "Mitchell Aaron Starc" to "Mitchell Starc",
            "JR Hazlewood" to "Josh Hazlewood",
            "Josh Reginald Hazlewood" to "Josh Hazlewood",
            "JM Anderson" to "James Anderson",
            "James Michael Anderson" to "James Anderson",
            "KS Williamson" to "Kane Williamson",
            "Kane Stuart Williamson" to "Kane Williamson",
            "V Kohli" to "Virat Kohli",
            "Virat Kohli" to "Virat Kohli",
            "RG Sharma" to "Rohit Sharma",
            "Rohit Gurunath Sharma" to "Rohit Sharma",
            "CA Pujara" to "Cheteshwar Pujara",
            "Cheteshwar Arvind Pujara" to "Cheteshwar Pujara",
            "AM Rahane" to "Ajinkya Rahane",
            "Ajinkya Madhukar Rahane" to "Ajinkya Rahane",
            "RR Pant" to "Rishabh Pant",
            "Rishabh Rajendra Pant" to "Rishabh Pant",
            "R Ashwin" to "Ravichandran Ashwin",
            "Ravichandran Ashwin" to "Ravichandran Ashwin",
            "RA Jadeja" to "Ravindra Jadeja",
            "Ravindrasinh Anirudhsinh Jadeja" to "Ravindra Jadeja",
            "JL Denly" to "Joe Denly",
            "JW Bairstow" to "Jonny Bairstow",
            "JC Buttler" to "Jos Buttler",
            "CR Woakes" to "Chris Woakes",
            "MM Ali" to "Moeen Ali",
            "MS Wade" to "Matthew Wade",
            "TM Head" to "Travis Head",
            "TD Paine" to "Tim Paine",
            "UT Khawaja" to "Usman Khawaja",
            "CTC Bancroft" to "Cameron Bancroft",
            "MS Harris" to "Marcus Harris",
            "MG Neser" to "Michael Neser",
            "PM Siddle" to "Peter Siddle",
            "JL Pattinson" to "James Pattinson",
            "NM Lyon" to "Nathan Lyon"
        )
        return nicknames[clean] ?: clean
    }

    override fun getSeriesIntelligenceSync(seriesId: Int): SeriesIntelligenceData {
        val header = getSeriesHeaderSync(seriesId)
        val ctx = getContext()

        // 1. Load matches from fact_series_matches.csv
        val matchRows = if (ctx != null) {
            seriesMatchesCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_matches.csv").also { seriesMatchesCsvCache = it }
        } else emptyList()
        val seriesMatches = matchRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        // 2. Load standings from fact_series_standings.csv
        val standingsRows = if (ctx != null) {
            seriesStandingsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_standings.csv").also { seriesStandingsCsvCache = it }
        } else emptyList()
        val seriesStandings = standingsRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        // 3. Load story from fact_series_story.csv
        val storyRows = if (ctx != null) {
            AssetWarehouseLoader.loadCsv(ctx, "fact_series_story.csv")
        } else emptyList()
        val sStoryRow = storyRows.firstOrNull { it["series_id"]?.toIntOrNull() == seriesId }
        val story = SeriesStoryData(
            seriesId = seriesId,
            seriesName = sStoryRow?.get("series_name") ?: header.seriesName,
            bulletStart = sStoryRow?.get("bullet_start") ?: "${header.team1} and ${header.team2} opened the series in intense competition.",
            bulletTurning = sStoryRow?.get("bullet_turning") ?: "Key middle overs and turning phases shaped the broader tactical contours.",
            bulletMomentum = sStoryRow?.get("bullet_momentum") ?: "Decisive spells shifted match momentum heading into pivotal sessions.",
            bulletConclusion = sStoryRow?.get("bullet_conclusion") ?: "${header.winner} concluded the series (${header.resultMargin})."
        )

        // 4. Calculate aggregates
        val matchesPlayed = seriesMatches.size.coerceAtLeast(header.totalMatches)
        val daysConsumed = seriesMatches.sumOf { it["days_consumed"]?.toIntOrNull() ?: 5 }.coerceAtLeast(matchesPlayed * 4)
        val seriesDuration = if (header.startDate.isNotBlank() && header.endDate.isNotBlank()) "${header.startDate} to ${header.endDate}" else header.season

        // 5. Build Momentum Timeline Nodes
        val matchIndexMap = getMatchListSync().associateBy { it.matchId }
        val momentumNodes = seriesMatches.mapIndexed { idx, mRow ->
            val mId = mRow["match_id"]?.toLongOrNull() ?: 0L
            val standing = seriesStandings.firstOrNull { it["after_match"]?.toIntOrNull() == (idx + 1) }
            val scoreAfter = standing?.get("series_score") ?: (if (idx == 0) "${header.team1.take(3).uppercase()} 1-0" else "Match ${idx + 1}")
            val mIndexItem = matchIndexMap[mId]
            SeriesMomentumNode(
                seriesId = seriesId,
                matchId = mId,
                matchNumber = mRow["match_number"] ?: "${idx + 1}th Test",
                scoreAfterMatch = scoreAfter,
                winner = mRow["winner"] ?: "Draw",
                margin = mRow["margin"] ?: "Match Drawn",
                venue = mRow["venue"] ?: mIndexItem?.venue ?: "Venue",
                daysConsumed = mRow["days_consumed"]?.toIntOrNull() ?: 5,
                keyTurningPoint = mRow["key_turning_point"] ?: "Critical Session Breakthrough",
                turningPointPlayer = mRow["turning_point_player"] ?: "",
                mvpPlayer = mRow["mvp_player"] ?: (mIndexItem?.winner ?: "Player"),
                mvpStat = mRow["mvp_stat"] ?: "Match Impact",
                team1 = mRow["team1"] ?: header.team1,
                team2 = mRow["team2"] ?: header.team2
            )
        }

        // 6. Load player stats from fact_series_player_stats.csv
        val playerStatsRows = if (ctx != null) {
            seriesPlayerStatsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "fact_series_player_stats.csv").also { seriesPlayerStatsCsvCache = it }
        } else emptyList()
        val seriesPlayerRows = playerStatsRows.filter { it["series_id"]?.toIntOrNull() == seriesId }

        val firstMatchId = seriesMatches.firstOrNull()?.get("match_id")?.toLongOrNull() ?: 0L

        val playerStatItems = seriesPlayerRows.map { r ->
            val pId = r["player_id"]?.toLongOrNull() ?: 0L
            val pName = resolveCleanPlayerName(pId, r["player_name"] ?: "Player $pId")
            val country = r["country"] ?: ""
            val role = r["role"] ?: "Middle Order"
            val matches = r["matches"]?.toIntOrNull() ?: 1
            val runs = r["runs"]?.toIntOrNull() ?: 0
            val balls = r["balls"]?.toIntOrNull() ?: 0
            val hs = r["highest_score"]?.toIntOrNull() ?: runs
            val fours = r["fours"]?.toIntOrNull() ?: 0
            val sixes = r["sixes"]?.toIntOrNull() ?: 0
            val avg = r["average"]?.toDoubleOrNull() ?: 0.0
            val c100 = r["100s"]?.toIntOrNull() ?: 0
            val c50 = r["50s"]?.toIntOrNull() ?: 0
            val wkts = r["wickets"]?.toIntOrNull() ?: 0
            val bb = r["best_bowling"] ?: "0/0"
            val catches = r["catches"]?.toIntOrNull() ?: 0
            val rating = r["rating_avg"]?.toDoubleOrNull() ?: 6.5
            val impact = r["impact_avg"]?.toDoubleOrNull() ?: 50.0
            val why = r["selection_reason"] ?: "Key contributor in series"

            SeriesXIPlayer(
                playerId = pId,
                playerName = pName,
                country = country,
                role = role,
                ratingAvg = rating,
                runs = runs,
                wickets = wkts,
                statSummary = if (wkts > 0 && runs > 50) "$runs runs & $wkts wkts" else if (wkts > 0) "$wkts wkts (BBI $bb)" else "$runs runs (${String.format(java.util.Locale.US, "%.1f", avg)} avg)",
                selectionReason = why,
                matchId = firstMatchId
            )
        }

        // 7. Build Leaderboard Categories from Pre-Ranked Views & Partnerships
        val partRows = if (ctx != null) {
            AssetWarehouseLoader.loadCsv(ctx, "fact_series_partnerships.csv")
        } else emptyList()
        val seriesPartRows = partRows.filter { it["series_id"]?.toIntOrNull() == seriesId }.sortedByDescending { it["runs"]?.toIntOrNull() ?: 0 }

        val partnershipLeaders = seriesPartRows.mapIndexed { idx, pr ->
            SeriesStatLeaderItem(
                rank = idx + 1,
                playerId = 0L,
                playerName = "${pr["batter1"]} & ${pr["batter2"]}",
                country = pr["team"] ?: "",
                statValue = "${pr["runs"]}",
                secondaryStat = "${pr["venue"]} (${pr["balls"]}b)",
                matchId = pr["match_id"]?.toLongOrNull() ?: firstMatchId
            )
        }

        val batRankings = if (ctx != null) {
            seriesBattingRankingsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "series_batting_rankings.csv").also { seriesBattingRankingsCsvCache = it }
        } else emptyList()
        val bowlRankings = if (ctx != null) {
            seriesBowlingRankingsCsvCache ?: AssetWarehouseLoader.loadCsv(ctx, "series_bowling_rankings.csv").also { seriesBowlingRankingsCsvCache = it }
        } else emptyList()

        val seriesBatRankings = batRankings.filter { it["series_id"]?.toIntOrNull() == seriesId }
        val seriesBowlRankings = bowlRankings.filter { it["series_id"]?.toIntOrNull() == seriesId }

        val statCategories = mutableListOf<SeriesLeaderboardCategory>()

        if (seriesBatRankings.isNotEmpty() || seriesBowlRankings.isNotEmpty()) {
            val desiredBattingOrder = listOf(
                "MOST RUNS",
                "HIGHEST SCORE",
                "HIGHEST AVERAGE",
                "BEST CONTROL %",
                "LOWEST FALSE SHOT %",
                "MOST CONTROLLED RUNS",
                "BEST BATTING RATING",
                "HIGHEST IMPACT RATING",
                "MOST CENTURIES",
                "MOST FIFTIES",
                "MOST SIXES",
                "MOST BOUNDARIES"
            )

            for (catName in desiredBattingOrder) {
                val matching = seriesBatRankings.filter { (it["category"] ?: "").equals(catName, ignoreCase = true) }
                if (matching.isNotEmpty()) {
                    val leaders = matching.map { r ->
                        val pId = r["player_id"]?.toLongOrNull() ?: 0L
                        val pName = resolveCleanPlayerName(pId, r["player_name"] ?: "")
                        val rawSec = r["secondary_stat"] ?: ""
                        val cleanSec = when {
                            catName.equals("BEST CONTROL %", ignoreCase = true) ||
                            catName.equals("LOWEST FALSE SHOT %", ignoreCase = true) ||
                            catName.equals("BEST BATTING RATING", ignoreCase = true) ||
                            catName.equals("HIGHEST IMPACT RATING", ignoreCase = true) -> {
                                if (rawSec.count { it == '•' } >= 2) rawSec.substringBeforeLast("•").trim() else rawSec
                            }
                            else -> rawSec
                        }
                        SeriesStatLeaderItem(
                            rank = r["rank"]?.toIntOrNull() ?: 1,
                            playerId = pId,
                            playerName = pName,
                            country = r["country"] ?: "",
                            statValue = r["stat_value"] ?: "-",
                            secondaryStat = cleanSec,
                            matchId = firstMatchId
                        )
                    }
                    statCategories.add(SeriesLeaderboardCategory(catName, leaders))
                }
            }

            if (partnershipLeaders.isNotEmpty()) {
                statCategories.add(SeriesLeaderboardCategory("HIGHEST PARTNERSHIP", partnershipLeaders))
            }

            val desiredBowlingOrder = listOf(
                "MOST WICKETS",
                "BEST BOWLING FIGURES",
                "LOWEST BOWLING AVERAGE",
                "LOWEST ECONOMY",
                "BEST LINE ACCURACY %",
                "BEST LENGTH ACCURACY %",
                "BEST PRESSURE RATING",
                "BEST BOWLING RATING",
                "HIGHEST BOWLING IMPACT",
                "MOST DOT BALLS",
                "MOST FIVE-WICKET HAULS"
            )

            for (catName in desiredBowlingOrder) {
                val matching = seriesBowlRankings.filter { (it["category"] ?: "").equals(catName, ignoreCase = true) }
                if (matching.isNotEmpty()) {
                    val leaders = matching.map { r ->
                        val pId = r["player_id"]?.toLongOrNull() ?: 0L
                        val pName = resolveCleanPlayerName(pId, r["player_name"] ?: "")
                        SeriesStatLeaderItem(
                            rank = r["rank"]?.toIntOrNull() ?: 1,
                            playerId = pId,
                            playerName = pName,
                            country = r["country"] ?: "",
                            statValue = r["stat_value"] ?: "-",
                            secondaryStat = r["secondary_stat"] ?: "",
                            matchId = firstMatchId
                        )
                    }
                    statCategories.add(SeriesLeaderboardCategory(catName, leaders))
                }
            }
        } else {
            // Fallback to legacy categories if rankings CSV not present
            val mostRuns = seriesPlayerRows.sortedByDescending { it["runs"]?.toIntOrNull() ?: 0 }.mapIndexed { idx, r ->
                val pId = r["player_id"]?.toLongOrNull() ?: 0L
                val matches = r["matches"]?.toIntOrNull() ?: 1
                val innings = r["innings"]?.toIntOrNull() ?: matches
                val avg = r["average"] ?: "0.0"
                SeriesStatLeaderItem(
                    rank = idx + 1,
                    playerId = pId,
                    playerName = resolveCleanPlayerName(pId, r["player_name"] ?: ""),
                    country = r["country"] ?: "",
                    statValue = "${r["runs"]?.toIntOrNull() ?: 0}",
                    secondaryStat = "$matches matches • $innings innings • $avg avg",
                    matchId = firstMatchId
                )
            }
            val mostWickets = seriesPlayerRows.filter { (it["wickets"]?.toIntOrNull() ?: 0) > 0 }.sortedByDescending { it["wickets"]?.toIntOrNull() ?: 0 }.mapIndexed { idx, r ->
                val pId = r["player_id"]?.toLongOrNull() ?: 0L
                val matches = r["matches"]?.toIntOrNull() ?: 1
                val innings = r["innings"]?.toIntOrNull() ?: matches
                SeriesStatLeaderItem(
                    rank = idx + 1,
                    playerId = pId,
                    playerName = resolveCleanPlayerName(pId, r["player_name"] ?: ""),
                    country = r["country"] ?: "",
                    statValue = "${r["wickets"]?.toIntOrNull() ?: 0}",
                    secondaryStat = "$matches matches • $innings innings • BBI ${r["best_bowling"]}",
                    matchId = firstMatchId
                )
            }
            statCategories.add(SeriesLeaderboardCategory("MOST RUNS", mostRuns))
            statCategories.add(SeriesLeaderboardCategory("MOST WICKETS", mostWickets))
            if (partnershipLeaders.isNotEmpty()) {
                statCategories.add(SeriesLeaderboardCategory("HIGHEST PARTNERSHIP", partnershipLeaders))
            }
        }

        // 8. Select Series XI (11 players)
        val openers = playerStatItems.filter { it.role == "Opener" }.sortedByDescending { it.ratingAvg }
        val topOrder = playerStatItems.filter { it.role == "Top Order" }.sortedByDescending { it.ratingAvg }
        val middleOrder = playerStatItems.filter { it.role == "Middle Order" }.sortedByDescending { it.ratingAvg }
        val allRounders = playerStatItems.filter { it.role == "All-rounder" }.sortedByDescending { it.ratingAvg }
        val keepers = playerStatItems.filter { it.role == "Wicketkeeper" }.sortedByDescending { it.ratingAvg }
        val bowlers = playerStatItems.filter { it.role == "Bowler" }.sortedByDescending { it.ratingAvg }

        val selectedXI = mutableListOf<SeriesXIPlayer>()
        selectedXI.addAll(openers.take(2))
        selectedXI.addAll(topOrder.take(2))
        selectedXI.addAll(middleOrder.take(2))
        if (allRounders.isNotEmpty()) selectedXI.add(allRounders.first())
        if (keepers.isNotEmpty()) selectedXI.add(keepers.first())
        val remainingSlots = 11 - selectedXI.size
        selectedXI.addAll(bowlers.take(remainingSlots.coerceAtLeast(3)))

        if (selectedXI.size < 11) {
            val usedIds = selectedXI.map { it.playerId }.toSet()
            val extra = playerStatItems.filter { it.playerId !in usedIds }.sortedByDescending { it.ratingAvg }.take(11 - selectedXI.size)
            selectedXI.addAll(extra)
        }

        val mvpPlayer = selectedXI.maxByOrNull { it.ratingAvg } ?: selectedXI.firstOrNull()
        val captainPlayer = selectedXI.filter { it != mvpPlayer }.maxByOrNull { it.ratingAvg } ?: selectedXI.firstOrNull()

        val finalXI = selectedXI.take(11).map { p ->
            p.copy(
                isMVP = p.playerId == mvpPlayer?.playerId,
                isCaptain = p.playerId == captainPlayer?.playerId
            )
        }

        // 9. Mock News List
        val newsList = listOf(
            SeriesNewsItem(
                id = "news_1",
                headline = "${header.seriesName} Retrospective: Defining Turning Points That Altered the Series",
                date = header.endDate.ifBlank { "2019-09-16" },
                source = "PitchIQ Analytics",
                snippet = "Deep tactical review of the bowling spells, momentum shifts, and heroic knocks that shaped ${header.seriesName}."
            ),
            SeriesNewsItem(
                id = "news_2",
                headline = "PitchIQ Series XI Announced: Statistical & Tactical Breakdown",
                date = header.endDate.ifBlank { "2019-09-17" },
                source = "PitchIQ Editorial",
                snippet = "How PitchIQ's impact ratings and situational performance algorithms selected the balanced Team of the Series."
            ),
            SeriesNewsItem(
                id = "news_3",
                headline = "Corridor of Uncertainty: How Bowlers Dictated Line & Length Control",
                date = header.startDate.ifBlank { "2019-08-05" },
                source = "PitchIQ Insights",
                snippet = "An analytical dissection of seam movement, false shot percentage, and pressure containment across all Tests."
            ),
            SeriesNewsItem(
                id = "news_4",
                headline = "Captaincy Masterclass: Tactical Field Placements & Bowling Rotations",
                date = header.endDate.ifBlank { "2019-09-18" },
                source = "PitchIQ Data Lab",
                snippet = "A breakdown of field setting adjustments during fourth-innings run chases and collapse situations."
            )
        )

        return SeriesIntelligenceData(
            seriesId = seriesId,
            header = header,
            seriesDuration = seriesDuration,
            matchesPlayed = matchesPlayed,
            daysConsumed = daysConsumed,
            story = story,
            momentumNodes = momentumNodes,
            statCategories = statCategories,
            seriesXI = finalXI,
            captain = finalXI.firstOrNull { it.isCaptain } ?: captainPlayer,
            playerOfSeries = finalXI.firstOrNull { it.isMVP } ?: mvpPlayer,
            newsList = newsList
        )
    }

    override suspend fun getSeriesIntelligence(seriesId: Int): SeriesIntelligenceData = withContext(Dispatchers.IO) {
        getSeriesIntelligenceSync(seriesId)
    }

    override fun getHomeFeaturedMatchesSync(): List<HomeFeaturedMatchItem> {
        homeFeaturedMatchesCache?.let { return it }
        val ctx = getContext() ?: return emptyList()
        val allMatches = getMatchListSync()
        val matchMap = allMatches.associateBy { it.matchId }
        val rows = AssetWarehouseLoader.loadCsv(ctx, "fact_home_featured_matches.csv")

        val list = mutableListOf<HomeFeaturedMatchItem>()
        if (rows.isNotEmpty()) {
            for (row in rows) {
                val mid = row["match_id"]?.toLongOrNull() ?: continue
                val order = row["featured_order"]?.toIntOrNull() ?: 0
                val sid = row["series_id"]?.toIntOrNull() ?: 0
                val sName = row["series_name"] ?: ""
                val m = matchMap[mid]
                if (m != null) {
                    val matchNumber = m.testSubHeader.split(",").firstOrNull()?.trim() ?: "Test Match"
                    val venue = m.venue.ifBlank { m.testSubHeader.split(",").getOrNull(1)?.trim() ?: "" }
                    val resolvedSeriesName = (if (sName.isNotBlank()) sName else m.seriesHeader).cleanYearBrackets()
                    list.add(
                        HomeFeaturedMatchItem(
                            matchId = mid,
                            seriesId = sid,
                            featuredOrder = order,
                            matchNumber = matchNumber,
                            seriesName = resolvedSeriesName,
                            venue = venue,
                            team1 = m.team1,
                            team1Short = m.team1Short,
                            team1Score = m.team1Score,
                            team2 = m.team2,
                            team2Short = m.team2Short,
                            team2Score = m.team2Score,
                            winner = m.winner,
                            victoryMargin = m.victoryMargin
                        )
                    )
                }
            }
        }

        // Fallback to top 7 iconic matches if CSV is empty
        if (list.isEmpty()) {
            val featuredIds = listOf(1152848L, 1223872L, 1223869L, 1062573L, 1152846L, 1243384L, 1072308L)
            for ((idx, mid) in featuredIds.withIndex()) {
                val m = matchMap[mid] ?: continue
                val matchNumber = m.testSubHeader.split(",").firstOrNull()?.trim() ?: "Test Match"
                val sid = when (mid) {
                    1152848L, 1152846L -> 37
                    1223872L, 1223869L -> 60
                    1062573L -> 8
                    1243384L -> 67
                    else -> 9
                }
                list.add(
                    HomeFeaturedMatchItem(
                        matchId = mid,
                        seriesId = sid,
                        featuredOrder = idx + 1,
                        matchNumber = matchNumber,
                        seriesName = m.seriesHeader.cleanYearBrackets(),
                        venue = m.venue,
                        team1 = m.team1,
                        team1Short = m.team1Short,
                        team1Score = m.team1Score,
                        team2 = m.team2,
                        team2Short = m.team2Short,
                        team2Score = m.team2Score,
                        winner = m.winner,
                        victoryMargin = m.victoryMargin
                    )
                )
            }
        }

        homeFeaturedMatchesCache = list
        return list
    }

    override suspend fun getHomeFeaturedMatches(): List<HomeFeaturedMatchItem> = withContext(Dispatchers.IO) {
        getHomeFeaturedMatchesSync()
    }

    companion object {
        private var _appContext: Context? = null
        val jsonCache = java.util.concurrent.ConcurrentHashMap<Long, JSONObject>()
        var indexCache: List<MatchIndexItem>? = null
        var seriesCache: List<SeriesIndexItem>? = null
        var homeFeaturedMatchesCache: List<HomeFeaturedMatchItem>? = null
        var seriesStandingsCsvCache: List<Map<String, String>>? = null
        var seriesPlayerStatsCsvCache: List<Map<String, String>>? = null
        var seriesBattingRankingsCsvCache: List<Map<String, String>>? = null
        var seriesBowlingRankingsCsvCache: List<Map<String, String>>? = null
        var seriesMatchesCsvCache: List<Map<String, String>>? = null
        var playerMatchCsvCache: List<Map<String, String>>? = null
        var playerInningsCsvCache: List<Map<String, String>>? = null
        var bowlingSpellsCsvCache: List<Map<String, String>>? = null
        var playerPartnershipsCsvCache: List<Map<String, String>>? = null
        var playerMatchRatingCsvCache: List<Map<String, String>>? = null
        var playerMatchImpactCsvCache: List<Map<String, String>>? = null
        var turningPointsCsvCache: List<Map<String, String>>? = null
        var dimPlayersMapCache: Map<Long, JSONObject>? = null
        var jerseyMapCache: Map<String, Int>? = null

        fun initialize(context: Context) {
            _appContext = context.applicationContext
            PlayerImageLoader.initialize(context.applicationContext)
        }

        val instance: MatchRepository by lazy { DefaultMatchRepository(_appContext) }
    }
}

private fun parseScorecardDismissal(dism: String): Triple<String, String, String> {
    val clean = dism.trim()
    val low = clean.lowercase()
    if (low.contains("not out") || low == "not out") {
        return Triple("not out", "", "")
    }
    if (low.contains("retired")) {
        return Triple("retired", "", "")
    }
    // "c & b Peter Siddle" or "c and b Peter Siddle" or "caught & bowled Peter Siddle"
    val cbRegex = Regex("^(?:c\\s*&\\s*b|c\\s+and\\s+b|caught\\s+and\\s+bowled|caught\\s*&\\s*bowled)\\s*(?:b\\s+)?(.+)$", RegexOption.IGNORE_CASE)
    val cbMatch = cbRegex.find(clean)
    if (cbMatch != null) {
        val bowler = cbMatch.groupValues[1].trim()
        return Triple("caught and bowled", bowler, bowler)
    }
    // "c Tim Paine b Nathan Lyon"
    val cRegex = Regex("^c\\s+(.+?)\\s+b\\s+(.+)$", RegexOption.IGNORE_CASE)
    val cMatch = cRegex.find(clean)
    if (cMatch != null) {
        val fld = cMatch.groupValues[1].trim()
        val bwl = cMatch.groupValues[2].trim()
        return Triple("caught", bwl, fld)
    }
    // "st Jonny Bairstow b Nathan Lyon"
    val stRegex = Regex("^st\\s+(.+?)\\s+b\\s+(.+)$", RegexOption.IGNORE_CASE)
    val stMatch = stRegex.find(clean)
    if (stMatch != null) {
        val fld = stMatch.groupValues[1].trim()
        val bwl = stMatch.groupValues[2].trim()
        return Triple("stumped", bwl, fld)
    }
    // "b Nathan Lyon"
    val bRegex = Regex("^b\\s+(.+)$", RegexOption.IGNORE_CASE)
    val bMatch = bRegex.find(clean)
    if (bMatch != null) {
        val bwl = bMatch.groupValues[1].trim()
        return Triple("bowled", bwl, "")
    }
    // "lbw b Nathan Lyon"
    val lbwRegex = Regex("^lbw\\s*(?:b\\s+)?(.+)$", RegexOption.IGNORE_CASE)
    val lbwMatch = lbwRegex.find(clean)
    if (lbwMatch != null) {
        val bwl = lbwMatch.groupValues[1].trim()
        return Triple("lbw", bwl, "")
    }
    // "run out (Pat Cummins)" or "run out"
    val roRegex = Regex("^run out\\s*\\(?([^)]+)?\\)?$", RegexOption.IGNORE_CASE)
    val roMatch = roRegex.find(clean)
    if (roMatch != null) {
        val fld = roMatch.groupValues[1].trim()
        return Triple("run out", "", fld)
    }
    // "hit wicket b Nathan Lyon"
    val hwRegex = Regex("^hit wicket\\s*(?:b\\s+)?(.+)$", RegexOption.IGNORE_CASE)
    val hwMatch = hwRegex.find(clean)
    if (hwMatch != null) {
        val bwl = hwMatch.groupValues[1].trim()
        return Triple("hit wicket", bwl, "")
    }
    return Triple("out", "", "")
}
