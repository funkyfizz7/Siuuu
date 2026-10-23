package com.example.siuu.assistant

import android.content.Context
import com.example.siuu.AssetWarehouseLoader
import org.json.JSONArray

/**
 * Structured raw facts bundle retrieved from warehouse tables.
 */
data class AssistantWarehouseFacts(
    val matchDetails: Map<String, String> = emptyMap(),
    val storyDetails: Map<String, String> = emptyMap(),
    val turningPoints: List<Map<String, String>> = emptyList(),
    val playerRating: Map<String, String> = emptyMap(),
    val playerImpact: Map<String, String> = emptyMap(),
    val bowlingSpells: List<Map<String, String>> = emptyList(),
    val sessionData: List<Map<String, String>> = emptyList(),
    val commentaryData: List<Map<String, String>> = emptyList(),
    val seriesBattingStats: List<Map<String, String>> = emptyList(),
    val seriesBowlingStats: List<Map<String, String>> = emptyList(),
    val seriesStory: Map<String, String> = emptyMap(),
    val tablesQueried: List<String> = emptyList(),
    val rawFactStrings: List<String> = emptyList()
)

/**
 * Multi-table factual evidence retriever.
 */
object AssistantEvidenceRetriever {

    fun retrieveEvidence(
        context: Context,
        intent: AssistantQuestionType,
        entities: AssistantEntities
    ): AssistantWarehouseFacts {
        val matchId = entities.matchId ?: 1144993L
        val playerId = entities.playerId ?: 267L
        val seriesId = entities.seriesId ?: "37"

        val tablesQueried = mutableListOf<String>()
        val rawFactStrings = mutableListOf<String>()

        val matchDetails = mutableMapOf<String, String>()
        val storyDetails = mutableMapOf<String, String>()
        val turningPoints = mutableListOf<Map<String, String>>()
        val playerRating = mutableMapOf<String, String>()
        val playerImpact = mutableMapOf<String, String>()
        val bowlingSpells = mutableListOf<Map<String, String>>()
        val sessionData = mutableListOf<Map<String, String>>()
        val commentaryData = mutableListOf<Map<String, String>>()
        val seriesBattingStats = mutableListOf<Map<String, String>>()
        val seriesBowlingStats = mutableListOf<Map<String, String>>()
        val seriesStory = mutableMapOf<String, String>()

        // 1. Match Details from dim_matches.json
        try {
            val jsonStr = AssetWarehouseLoader.loadJson(context, "dim_matches.json")
            if (jsonStr.isNotBlank()) {
                tablesQueried.add("dim_matches.json")
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    if (obj.optLong("match_id") == matchId) {
                        matchDetails["match_id"] = matchId.toString()
                        matchDetails["winner"] = obj.optString("winner", "")
                        matchDetails["margin"] = obj.optString("victory_margin", "")
                        matchDetails["venue"] = obj.optString("venue", "")
                        matchDetails["player_of_match"] = obj.optString("player_of_match", "")
                        matchDetails["team1"] = obj.optString("team1_name", "")
                        matchDetails["team2"] = obj.optString("team2_name", "")
                        matchDetails["match_number"] = obj.optString("match_number", "")
                        rawFactStrings.add("Match: ${matchDetails["team1"]} vs ${matchDetails["team2"]}, Winner: ${matchDetails["winner"]} by ${matchDetails["margin"]} at ${matchDetails["venue"]}")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            // Non-fatal
        }

        // 2. Story & Turning Points
        val storyRows = AssetWarehouseLoader.loadCsv(context, "fact_match_story.csv")
        if (storyRows.isNotEmpty()) {
            tablesQueried.add("fact_match_story.csv")
            for (row in storyRows) {
                if (row["match_id"] == matchId.toString()) {
                    storyDetails.putAll(row)
                    rawFactStrings.add("Story: ${row["story_type"]}, Turning Point: ${row["turning_point_event"]}")
                    break
                }
            }
        }

        val tpRows = AssetWarehouseLoader.loadCsv(context, "fact_match_turning_points.csv")
        if (tpRows.isNotEmpty()) {
            tablesQueried.add("fact_match_turning_points.csv")
            for (row in tpRows) {
                if (row["match_id"] == matchId.toString()) {
                    turningPoints.add(row)
                    rawFactStrings.add("Turning Point Rank ${row["rank"]}: ${row["title"]} (${row["importance_score"]}/100)")
                }
            }
        }

        // 3. Player Ratings & Impact
        val ratingRows = AssetWarehouseLoader.loadCsv(context, "fact_player_match_rating.csv")
        if (ratingRows.isNotEmpty()) {
            tablesQueried.add("fact_player_match_rating.csv")
            for (row in ratingRows) {
                val pid = row["player_id"]?.toLongOrNull()
                if (pid == playerId) {
                    playerRating.putAll(row)
                    rawFactStrings.add("Player Rating: ${row["player_name"]} rated ${row["overall_rating"]} (Bat: ${row["batting_score"]}, Bowl: ${row["bowling_score"]})")
                    break
                }
            }
        }

        val impactRows = AssetWarehouseLoader.loadCsv(context, "fact_player_match_impact.csv")
        if (impactRows.isNotEmpty()) {
            tablesQueried.add("fact_player_match_impact.csv")
            for (row in impactRows) {
                val pid = row["player_id"]?.substringBefore(".")?.toLongOrNull()
                if (pid == playerId) {
                    playerImpact.putAll(row)
                    rawFactStrings.add("Player Impact: WPA ${row["win_probability_added_percent"]}%, Leverage ${row["avg_leverage_index"]}")
                    break
                }
            }
        }

        // 4. Bowling Spells
        val spellRows = AssetWarehouseLoader.loadCsv(context, "fact_bowling_spells.csv")
        if (spellRows.isNotEmpty()) {
            tablesQueried.add("fact_bowling_spells.csv")
            for (row in spellRows) {
                if (row["match_id"] == matchId.toString() || spellRows.size < 50) {
                    val bowlerPid = row["bowler_id"]?.toLongOrNull()
                    if (bowlerPid == playerId || entities.playerName?.let { row["bowler_name"]?.contains(it, true) } == true) {
                        bowlingSpells.add(row)
                        rawFactStrings.add("Spell: ${row["bowler_name"]} ${row["spell_figures"]} (${row["spell_quality_score"]}/100)")
                    }
                }
            }
        }

        // 5. Session Data & Commentary
        val sessRows = AssetWarehouseLoader.loadCsv(context, "fact_match_sessions.csv")
        if (sessRows.isNotEmpty()) {
            tablesQueried.add("fact_match_sessions.csv")
            for (row in sessRows) {
                if (row["match_id"] == matchId.toString()) {
                    val day = entities.dayNumber
                    val sess = entities.sessionNumber
                    if (day != null && row["day_number"] != day.toString()) continue
                    if (sess != null && row["session_number"] != sess.toString()) continue
                    sessionData.add(row)
                }
            }
        }

        val commRows = AssetWarehouseLoader.loadCsv(context, "fact_session_commentary.csv")
        if (commRows.isNotEmpty()) {
            tablesQueried.add("fact_session_commentary.csv")
            for (row in commRows) {
                if (row["match_id"] == matchId.toString()) {
                    val day = entities.dayNumber
                    val sess = entities.sessionNumber
                    if (day != null && row["day_no"] != day.toString()) continue
                    if (sess != null && row["session_no"] != sess.toString()) continue
                    commentaryData.add(row)
                }
            }
        }

        // 6. Series Rankings & Story
        val batRankRows = AssetWarehouseLoader.loadCsv(context, "series_batting_rankings.csv")
        if (batRankRows.isNotEmpty()) {
            tablesQueried.add("series_batting_rankings.csv")
            seriesBattingStats.addAll(batRankRows.filter { it["series_id"] == seriesId })
        }

        val bowlRankRows = AssetWarehouseLoader.loadCsv(context, "series_bowling_rankings.csv")
        if (bowlRankRows.isNotEmpty()) {
            tablesQueried.add("series_bowling_rankings.csv")
            seriesBowlingStats.addAll(bowlRankRows.filter { it["series_id"] == seriesId })
        }

        val serStoryRows = AssetWarehouseLoader.loadCsv(context, "fact_series_story.csv")
        if (serStoryRows.isNotEmpty()) {
            tablesQueried.add("fact_series_story.csv")
            for (row in serStoryRows) {
                if (row["series_id"] == seriesId) {
                    seriesStory.putAll(row)
                    break
                }
            }
        }

        return AssistantWarehouseFacts(
            matchDetails = matchDetails,
            storyDetails = storyDetails,
            turningPoints = turningPoints,
            playerRating = playerRating,
            playerImpact = playerImpact,
            bowlingSpells = bowlingSpells,
            sessionData = sessionData,
            commentaryData = commentaryData,
            seriesBattingStats = seriesBattingStats,
            seriesBowlingStats = seriesBowlingStats,
            seriesStory = seriesStory,
            tablesQueried = tablesQueried.distinct(),
            rawFactStrings = rawFactStrings
        )
    }
}

