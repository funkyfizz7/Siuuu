package com.example.siuu

import org.json.JSONObject

/**
 * PitchIQ Data Warehouse Model: Match Story
 * Source: fact_match_story.csv / JSON
 */
data class MatchStoryModel(
    val matchId: Long,
    val dayNumber: Int,
    val sessionOfDay: Int? = null,
    val headline: String,
    val summary: String,
    val keyMoment: String? = null,
    val winProbHome: Double = 50.0,
    val winProbAway: Double = 50.0,
    val controlHome: Double = 50.0,
    val controlAway: Double = 50.0
) {
    companion object {
        fun fromJson(json: JSONObject): MatchStoryModel {
            return MatchStoryModel(
                matchId = json.optLong("match_id", json.optLong("matchId", 0L)),
                dayNumber = json.optInt("day_number", json.optInt("dayNumber", 1)),
                sessionOfDay = if (json.has("session_of_day") || json.has("sessionOfDay")) {
                    json.optInt("session_of_day", json.optInt("sessionOfDay", 0)).takeIf { it > 0 }
                } else null,
                headline = json.optString("headline", ""),
                summary = json.optString("summary", ""),
                keyMoment = json.optString("key_moment", json.optString("keyMoment", "")).takeIf { it.isNotBlank() },
                winProbHome = json.optDouble("win_prob_home", json.optDouble("winProbHome", 50.0)),
                winProbAway = json.optDouble("win_prob_away", json.optDouble("winProbAway", 50.0)),
                controlHome = json.optDouble("control_home", json.optDouble("controlHome", 50.0)),
                controlAway = json.optDouble("control_away", json.optDouble("controlAway", 50.0))
            )
        }

        fun fromCsvMap(row: Map<String, String>): MatchStoryModel {
            return MatchStoryModel(
                matchId = row["match_id"]?.toDoubleOrNull()?.toLong() ?: 0L,
                dayNumber = row["day_number"]?.toDoubleOrNull()?.toInt() ?: 1,
                sessionOfDay = row["session_of_day"]?.toDoubleOrNull()?.toInt() ?: row["session_no"]?.toDoubleOrNull()?.toInt(),
                headline = row["headline"] ?: "Key Match Highlights",
                summary = row["summary"] ?: "Match event timeline progression.",
                keyMoment = row["key_moment"]?.takeIf { it.isNotBlank() },
                winProbHome = row["win_prob_home"]?.toDoubleOrNull() ?: 50.0,
                winProbAway = row["win_prob_away"]?.toDoubleOrNull() ?: 50.0,
                controlHome = row["control_home"]?.toDoubleOrNull() ?: 50.0,
                controlAway = row["control_away"]?.toDoubleOrNull() ?: 50.0
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("match_id", matchId)
        json.put("day_number", dayNumber)
        sessionOfDay?.let { json.put("session_of_day", it) }
        json.put("headline", headline)
        json.put("summary", summary)
        keyMoment?.let { json.put("key_moment", it) }
        json.put("win_prob_home", winProbHome)
        json.put("win_prob_away", winProbAway)
        json.put("control_home", controlHome)
        json.put("control_away", controlAway)
        return json
    }
}

/**
 * PitchIQ Data Warehouse Model: Momentum
 * Source: fact_momentum.csv / fact_match_momentum.csv
 */
data class MomentumModel(
    val sessionIndex: Int,
    val dayNumber: Int,
    val sessionOfDay: Int,
    val innings: Int,
    val battingTeam: String,
    val runs: Int,
    val wickets: Int,
    val balls: Int = 0,
    val overs: Double,
    val runRate: Double,
    val momentumScore: Double,
    val momentumSwing: Double = 0.0,
    val momentumLabel: String = "",
    val contextStory: String = "",
    val isSplitInnings: Boolean = false,
    val inningsBadge: String = "",
    val sessionTitle: String = "",
    val sessionEndScore: String = "",
    val sessionEndScores: List<String> = emptyList(),
    val topBatter1: String = "",
    val topBatter1Runs: Int = 0,
    val topBatter1Balls: Int = 0,
    val topBatter2: String = "",
    val topBatter2Runs: Int = 0,
    val topBatter2Balls: Int = 0,
    val topBowler1: String = "",
    val topBowler1Wickets: Int = 0,
    val topBowler1Runs: Int = 0,
    val topBowler2: String = "",
    val topBowler2Wickets: Int = 0,
    val topBowler2Runs: Int = 0,
    val bestPartnershipRuns: Int = 0,
    val bestPartnershipBatter1: String = "",
    val bestPartnershipBatter2: String = "",
    val sessionWinner: String = "",
    val team1SessionScore: Int = 0,
    val team2SessionScore: Int = 0,
    val sessionWinType: String = "",
    val scoreDifference: Int = 0,
    val battingControlPct: Double = 0.0,
    val falseShotPct: Double = 0.0,
    val sessionKeyReasons: List<String> = emptyList(),
    val commentaryText: String = ""
) {
    val formattedOvers: String
        get() = when {
            balls > 0 -> {
                val fullOvers = balls / 6
                val remBalls = balls % 6
                if (remBalls == 0) "$fullOvers" else "$fullOvers.$remBalls"
            }
            else -> {
                val intPart = overs.toInt()
                val frac = ((overs - intPart) * 10).toInt()
                if (frac == 6 || frac == 0) "${if (frac == 6) intPart + 1 else intPart}" else "%.1f".format(overs)
            }
        }

    companion object {
        fun fromJson(json: JSONObject): MomentumModel {
            val b = json.optInt("balls", 0)
            val ov = json.optDouble("overs", if (b > 0) (b / 6) + (b % 6) / 10.0 else 0.0)
            val reasonsArray = json.optJSONArray("sessionKeyReasons")
            val reasonsList = mutableListOf<String>()
            if (reasonsArray != null) {
                for (i in 0 until reasonsArray.length()) {
                    val r = reasonsArray.optString(i)
                    if (r.isNotBlank()) reasonsList.add(r)
                }
            }
            val scoresArray = json.optJSONArray("sessionEndScores")
            val endScoresList = mutableListOf<String>()
            if (scoresArray != null) {
                for (i in 0 until scoresArray.length()) {
                    val s = scoresArray.optString(i)
                    if (s.isNotBlank()) endScoresList.add(s)
                }
            }
            val endScoreStr = json.optString("sessionEndScore", json.optString("session_end_score", ""))
            if (endScoresList.isEmpty() && endScoreStr.isNotBlank()) {
                endScoresList.addAll(endScoreStr.split(" • ").map { it.trim() })
            }
            return MomentumModel(
                sessionIndex = json.optInt("session_index", json.optInt("sessionIndex", json.optInt("session_no", 1))),
                dayNumber = json.optInt("day_number", json.optInt("dayNumber", 1)),
                sessionOfDay = json.optInt("session_of_day", json.optInt("sessionOfDay", 1)),
                innings = json.optInt("innings", 1),
                battingTeam = json.optString("batting_team", json.optString("battingTeam", "")),
                runs = json.optInt("runs", 0),
                wickets = json.optInt("wickets", 0),
                balls = b,
                overs = ov,
                runRate = json.optDouble("run_rate", json.optDouble("runRate", 0.0)),
                momentumScore = json.optDouble("momentum_score", json.optDouble("momentumScore", json.optDouble("momentum_index", 0.0))),
                momentumSwing = json.optDouble("momentum_swing", json.optDouble("momentumSwing", 0.0)),
                momentumLabel = json.optString("momentum_label", json.optString("momentumLabel", "")),
                contextStory = json.optString("context_story", json.optString("contextStory", "")),
                isSplitInnings = json.optBoolean("isSplitInnings", json.optInt("is_split_innings", 0) == 1),
                inningsBadge = json.optString("inningsBadge", json.optString("innings_badge", "")),
                sessionTitle = json.optString("sessionTitle", json.optString("session_title", "")),
                sessionEndScore = endScoreStr,
                sessionEndScores = endScoresList,
                topBatter1 = json.optString("topBatter1", json.optString("top_batter_1", "")),
                topBatter1Runs = json.optInt("topBatter1Runs", json.optInt("top_batter_1_runs", 0)),
                topBatter1Balls = json.optInt("topBatter1Balls", json.optInt("top_batter_1_balls", 0)),
                topBatter2 = json.optString("topBatter2", json.optString("top_batter_2", "")),
                topBatter2Runs = json.optInt("topBatter2Runs", json.optInt("top_batter_2_runs", 0)),
                topBatter2Balls = json.optInt("topBatter2Balls", json.optInt("top_batter_2_balls", 0)),
                topBowler1 = json.optString("topBowler1", json.optString("top_bowler_1", "")),
                topBowler1Wickets = json.optInt("topBowler1Wickets", json.optInt("top_bowler_1_wickets", 0)),
                topBowler1Runs = json.optInt("topBowler1Runs", json.optInt("top_bowler_1_runs", 0)),
                topBowler2 = json.optString("topBowler2", json.optString("top_bowler_2", "")),
                topBowler2Wickets = json.optInt("topBowler2Wickets", json.optInt("top_bowler_2_wickets", 0)),
                topBowler2Runs = json.optInt("topBowler2Runs", json.optInt("top_bowler_2_runs", 0)),
                bestPartnershipRuns = json.optInt("bestPartnershipRuns", json.optInt("best_partnership_runs", 0)),
                bestPartnershipBatter1 = json.optString("bestPartnershipBatter1", json.optString("best_partnership_batter_1", "")),
                bestPartnershipBatter2 = json.optString("bestPartnershipBatter2", json.optString("best_partnership_batter_2", "")),
                sessionWinner = json.optString("sessionWinner", json.optString("session_winner", "")),
                team1SessionScore = json.optInt("team1SessionScore", 0),
                team2SessionScore = json.optInt("team2SessionScore", 0),
                sessionWinType = json.optString("sessionWinType", ""),
                scoreDifference = json.optInt("scoreDifference", 0),
                battingControlPct = json.optDouble("battingControlPct", 0.0),
                falseShotPct = json.optDouble("falseShotPct", 0.0),
                sessionKeyReasons = reasonsList,
                commentaryText = json.optString("commentaryText", json.optString("commentary_text", ""))
            )
        }

        fun fromCsvMap(row: Map<String, String>): MomentumModel {
            val b = row["balls"]?.toIntOrNull() ?: 0
            val ov = row["overs"]?.toDoubleOrNull() ?: if (b > 0) (b / 6) + (b % 6) / 10.0 else 0.0
            return MomentumModel(
                sessionIndex = row["session_no"]?.toDoubleOrNull()?.toInt() ?: row["session_index"]?.toIntOrNull() ?: 1,
                dayNumber = row["day_number"]?.toIntOrNull() ?: 1,
                sessionOfDay = row["session_of_day"]?.toIntOrNull() ?: 1,
                innings = row["innings"]?.toDoubleOrNull()?.toInt() ?: 1,
                battingTeam = row["batting_team"] ?: "",
                runs = row["runs"]?.toDoubleOrNull()?.toInt() ?: 0,
                wickets = row["wickets"]?.toDoubleOrNull()?.toInt() ?: 0,
                balls = b,
                overs = ov,
                runRate = row["run_rate"]?.toDoubleOrNull() ?: 0.0,
                momentumScore = row["momentum_index"]?.toDoubleOrNull() ?: row["momentum_score"]?.toDoubleOrNull() ?: 0.0,
                momentumSwing = row["momentum_swing"]?.toDoubleOrNull() ?: 0.0,
                momentumLabel = row["momentum_label"] ?: "",
                contextStory = row["context_story"] ?: row["narrative"] ?: "",
                isSplitInnings = (row["is_split_innings"] == "1" || row["is_split_innings"] == "true"),
                inningsBadge = row["innings_badge"] ?: "",
                sessionTitle = row["session_title"] ?: "",
                sessionEndScore = row["session_end_score"] ?: "",
                topBatter1 = row["top_batter_1"] ?: "",
                topBatter1Runs = row["top_batter_1_runs"]?.toIntOrNull() ?: 0,
                topBatter1Balls = row["top_batter_1_balls"]?.toIntOrNull() ?: 0,
                topBatter2 = row["top_batter_2"] ?: "",
                topBatter2Runs = row["top_batter_2_runs"]?.toIntOrNull() ?: 0,
                topBatter2Balls = row["top_batter_2_balls"]?.toIntOrNull() ?: 0,
                topBowler1 = row["top_bowler_1"] ?: "",
                topBowler1Wickets = row["top_bowler_1_wickets"]?.toIntOrNull() ?: 0,
                topBowler1Runs = row["top_bowler_1_runs"]?.toIntOrNull() ?: 0,
                topBowler2 = row["top_bowler_2"] ?: "",
                topBowler2Wickets = row["top_bowler_2_wickets"]?.toIntOrNull() ?: 0,
                topBowler2Runs = row["top_bowler_2_runs"]?.toIntOrNull() ?: 0,
                bestPartnershipRuns = row["best_partnership_runs"]?.toIntOrNull() ?: 0,
                bestPartnershipBatter1 = row["best_partnership_batter_1"] ?: "",
                bestPartnershipBatter2 = row["best_partnership_batter_2"] ?: ""
            )
        }
    }
}

/**
 * Single Ball-by-ball Pressure Curve Sample Point
 */
/**
 * PitchIQ Data Warehouse Model: Pressure Session Segment
 * Handles single-innings breakdown segments within multi-innings sessions
 */
data class PressureSessionSegment(
    val innings: Int,
    val team: String,
    val runs: Int,
    val wickets: Int,
    val overs: String,
    val balls: Int,
    val startScore: String,
    val endScore: String
) {
    companion object {
        fun fromJson(json: JSONObject): PressureSessionSegment {
            return PressureSessionSegment(
                innings = json.optInt("innings", json.optInt("inningsNo", 1)),
                team = json.optString("team", json.optString("battingTeam", "")),
                runs = json.optInt("runs", 0),
                wickets = json.optInt("wickets", 0),
                overs = json.optString("overs", "0.0"),
                balls = json.optInt("balls", 0),
                startScore = json.optString("startScore", json.optString("start_score", "0/0")),
                endScore = json.optString("endScore", json.optString("end_score", "0/0"))
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("innings", innings)
        json.put("team", team)
        json.put("runs", runs)
        json.put("wickets", wickets)
        json.put("overs", overs)
        json.put("balls", balls)
        json.put("startScore", startScore)
        json.put("endScore", endScore)
        return json
    }
}

/**
 * PitchIQ Data Warehouse Model: Pressure
 * Source: fact_match_pressure.csv
 */
data class PressureModel(
    val sessionIndex: Int,
    val dayNumber: Int,
    val sessionOfDay: Int,
    val innings: Int,
    val battingTeam: String,
    val runs: Int,
    val wickets: Int,
    val balls: Int = 0,
    val overs: Double,
    val runRate: Double,
    val pressureScore: Double,
    val pressureZone: String = "Low",
    val explanation: String = "",
    val sessionEndScore: String = "",
    val isSplitInnings: Boolean = false,
    val inningsBadge: String = "",
    val sessionTitle: String = "",
    val topBatter1: String = "",
    val topBatter1Runs: Int = 0,
    val topBatter1Balls: Int = 0,
    val topBatter2: String = "",
    val topBatter2Runs: Int = 0,
    val topBatter2Balls: Int = 0,
    val topBowler1: String = "",
    val topBowler1Wickets: Int = 0,
    val topBowler1Runs: Int = 0,
    val topBowler2: String = "",
    val topBowler2Wickets: Int = 0,
    val topBowler2Runs: Int = 0,
    val bestPartnershipRuns: Int = 0,
    val bestPartnershipBatter1: String = "",
    val bestPartnershipBatter2: String = "",
    val verdict: String = "",
    val segments: List<PressureSessionSegment> = emptyList()
) {
    val formattedOvers: String
        get() = when {
            balls > 0 -> {
                val fullOvers = balls / 6
                val remBalls = balls % 6
                if (remBalls == 0) "$fullOvers" else "$fullOvers.$remBalls"
            }
            else -> {
                val intPart = overs.toInt()
                val frac = ((overs - intPart) * 10).toInt()
                if (frac == 6 || frac == 0) "${if (frac == 6) intPart + 1 else intPart}" else "%.1f".format(overs)
            }
        }

    companion object {
        fun cleanSplitExplanation(exp: String): String {
            if (!exp.contains("Split-Innings") && !exp.contains("Total session aggregate")) return exp
            return exp.replace(Regex("""\.\s*Total session aggregate:.*$"""), ".")
                .replace(Regex("""^Split-Innings Session:\s*"""), "")
                .trim()
        }

        fun fromJson(json: JSONObject): PressureModel {
            val b = json.optInt("balls", 0)
            val ov = json.optDouble("overs", if (b > 0) (b / 6) + (b % 6) / 10.0 else 0.0)
            val score = json.optDouble("pressureScore", json.optDouble("pressure_score", json.optDouble("pressure_index", 0.0)))
            val zone = json.optString("pressureZone", json.optString("pressure_zone", json.optString("pressure_label", when {
                score >= 70.0 -> "High"
                score >= 40.0 -> "Moderate"
                else -> "Low"
            })))
            val segList = mutableListOf<PressureSessionSegment>()
            val segArr = json.optJSONArray("segments")
            if (segArr != null) {
                for (i in 0 until segArr.length()) {
                    segList.add(PressureSessionSegment.fromJson(segArr.getJSONObject(i)))
                }
            }
            val sessOfDay = json.optInt("sessionOfDay", json.optInt("session_of_day", json.optInt("sessionNumber", json.optInt("session_number", 1))))
            val rawExp = json.optString("explanation", "")
            return PressureModel(
                sessionIndex = json.optInt("sessionIndex", json.optInt("session_index", json.optInt("session_no", 1))),
                dayNumber = json.optInt("dayNumber", json.optInt("day_number", 1)),
                sessionOfDay = if (sessOfDay > 0) sessOfDay else 1,
                innings = json.optInt("innings", 1),
                battingTeam = json.optString("battingTeam", json.optString("batting_team", "")),
                runs = json.optInt("runs", 0),
                wickets = json.optInt("wickets", 0),
                balls = b,
                overs = ov,
                runRate = json.optDouble("runRate", json.optDouble("run_rate", 0.0)),
                pressureScore = score,
                pressureZone = zone,
                explanation = cleanSplitExplanation(rawExp),
                sessionEndScore = json.optString("sessionEndScore", json.optString("session_end_score", "")),
                isSplitInnings = json.optBoolean("isSplitInnings", json.optInt("is_split_innings", 0) == 1),
                inningsBadge = json.optString("inningsBadge", json.optString("innings_badge", "")),
                sessionTitle = json.optString("sessionTitle", json.optString("session_title", "")),
                topBatter1 = json.optString("topBatter1", json.optString("top_batter_1", "")),
                topBatter1Runs = json.optInt("topBatter1Runs", json.optInt("top_batter_1_runs", 0)),
                topBatter1Balls = json.optInt("topBatter1Balls", json.optInt("top_batter_1_balls", 0)),
                topBatter2 = json.optString("topBatter2", json.optString("top_batter_2", "")),
                topBatter2Runs = json.optInt("topBatter2Runs", json.optInt("top_batter_2_runs", 0)),
                topBatter2Balls = json.optInt("topBatter2Balls", json.optInt("top_batter_2_balls", 0)),
                topBowler1 = json.optString("topBowler1", json.optString("top_bowler_1", "")),
                topBowler1Wickets = json.optInt("topBowler1Wickets", json.optInt("top_bowler_1_wickets", 0)),
                topBowler1Runs = json.optInt("topBowler1Runs", json.optInt("top_bowler_1_runs", 0)),
                topBowler2 = json.optString("topBowler2", json.optString("top_bowler_2", "")),
                topBowler2Wickets = json.optInt("topBowler2Wickets", json.optInt("top_bowler_2_wickets", 0)),
                topBowler2Runs = json.optInt("topBowler2Runs", json.optInt("top_bowler_2_runs", 0)),
                bestPartnershipRuns = json.optInt("bestPartnershipRuns", json.optInt("best_partnership_runs", 0)),
                bestPartnershipBatter1 = json.optString("bestPartnershipBatter1", json.optString("best_partnership_batter_1", "")),
                bestPartnershipBatter2 = json.optString("bestPartnershipBatter2", json.optString("best_partnership_batter_2", "")),
                verdict = json.optString("verdict", ""),
                segments = segList
            )
        }

        fun fromCsvMap(row: Map<String, String>): PressureModel {
            val score = row["pressure_index"]?.toDoubleOrNull() ?: row["pressure_score"]?.toDoubleOrNull() ?: 0.0
            val zone = row["pressure_label"] ?: row["pressure_zone"] ?: when {
                score >= 70.0 -> "High"
                score >= 40.0 -> "Moderate"
                else -> "Low"
            }
            val b = row["balls"]?.toIntOrNull() ?: 0
            val ov = row["overs"]?.toDoubleOrNull() ?: if (b > 0) (b / 6) + (b % 6) / 10.0 else 0.0
            val segList = mutableListOf<PressureSessionSegment>()
            val segJsonStr = row["segments_json"]
            if (!segJsonStr.isNullOrBlank()) {
                try {
                    val arr = org.json.JSONArray(segJsonStr)
                    for (i in 0 until arr.length()) {
                        segList.add(PressureSessionSegment.fromJson(arr.getJSONObject(i)))
                    }
                } catch (_: Exception) {}
            }
            val sessOfDay = row["session_number"]?.toIntOrNull() ?: row["session_of_day"]?.toIntOrNull() ?: 1
            val rawExp = row["explanation"] ?: ""
            return PressureModel(
                sessionIndex = row["session_no"]?.toDoubleOrNull()?.toInt() ?: row["session_index"]?.toIntOrNull() ?: 1,
                dayNumber = row["day_number"]?.toIntOrNull() ?: 1,
                sessionOfDay = if (sessOfDay > 0) sessOfDay else 1,
                innings = row["innings"]?.toDoubleOrNull()?.toInt() ?: 1,
                battingTeam = row["batting_team"] ?: "",
                runs = row["runs"]?.toDoubleOrNull()?.toInt() ?: 0,
                wickets = row["wickets"]?.toDoubleOrNull()?.toInt() ?: 0,
                balls = b,
                overs = ov,
                runRate = row["run_rate"]?.toDoubleOrNull() ?: 0.0,
                pressureScore = score,
                pressureZone = zone,
                explanation = cleanSplitExplanation(rawExp),
                sessionEndScore = row["session_end_score"] ?: "",
                isSplitInnings = (row["is_split_innings"] == "1" || row["is_split_innings"] == "true"),
                inningsBadge = row["innings_badge"] ?: "",
                sessionTitle = row["session_title"] ?: "",
                topBatter1 = row["top_batter_1"] ?: "",
                topBatter1Runs = row["top_batter_1_runs"]?.toIntOrNull() ?: 0,
                topBatter1Balls = row["top_batter_1_balls"]?.toIntOrNull() ?: 0,
                topBatter2 = row["top_batter_2"] ?: "",
                topBatter2Runs = row["top_batter_2_runs"]?.toIntOrNull() ?: 0,
                topBatter2Balls = row["top_batter_2_balls"]?.toIntOrNull() ?: 0,
                topBowler1 = row["top_bowler_1"] ?: "",
                topBowler1Wickets = row["top_bowler_1_wickets"]?.toIntOrNull() ?: 0,
                topBowler1Runs = row["top_bowler_1_runs"]?.toIntOrNull() ?: 0,
                topBowler2 = row["top_bowler_2"] ?: "",
                topBowler2Wickets = row["top_bowler_2_wickets"]?.toIntOrNull() ?: 0,
                topBowler2Runs = row["top_bowler_2_runs"]?.toIntOrNull() ?: 0,
                bestPartnershipRuns = row["best_partnership_runs"]?.toIntOrNull() ?: 0,
                bestPartnershipBatter1 = row["best_partnership_batter_1"] ?: "",
                bestPartnershipBatter2 = row["best_partnership_batter_2"] ?: "",
                verdict = row["verdict"] ?: "",
                segments = segList
            )
        }
    }
}

/**
 * PitchIQ Data Warehouse Model: Collapse
 * Source: fact_match_collapses.csv
 */
data class CollapseModel(
    val collapseId: String? = null,
    val matchId: Long? = null,
    val innings: Int = 1,
    val team: String = "",
    val score: String = "",
    val wicketsLost: Int = 0,
    val runsAdded: Int = 0,
    val oversTaken: Double = 0.0,
    val severity: Double = 0.0,
    val classification: String = "Collapse",
    val explanation: String = ""
) {
    companion object {
        fun fromCsvMap(row: Map<String, String>, defaultTeam: String = ""): CollapseModel {
            val wLost = row["wickets_lost"]?.toDoubleOrNull()?.toInt() ?: 0
            val rScored = row["runs_scored"]?.toDoubleOrNull()?.toInt() ?: row["runs_added"]?.toDoubleOrNull()?.toInt() ?: 0
            val rBefore = row["runs_before"] ?: ""
            val rAfter = row["runs_after"] ?: ""
            val wBefore = row["wickets_before"] ?: ""
            val wAfter = row["wickets_after"] ?: ""
            val startBall = row["collapse_start_ball"] ?: ""
            val endBall = row["collapse_end_ball"] ?: ""
            val severity = row["collapse_severity"]?.toDoubleOrNull() ?: row["severity"]?.toDoubleOrNull() ?: 0.0
            val label = row["collapse_label"] ?: row["classification"] ?: "Major Collapse"
            val inn = row["innings"]?.toDoubleOrNull()?.toInt() ?: 1

            val scoreStr = if (rBefore.isNotBlank() && rAfter.isNotBlank()) {
                "$rBefore/$wBefore -> $rAfter/$wAfter ($wLost wkts for $rScored runs)"
            } else {
                row["score"] ?: "$wLost wickets for $rScored runs"
            }

            val explStr = row["explanation"]?.takeIf { it.isNotBlank() }
                ?: "Innings $inn: Lost $wLost wickets for $rScored runs from over $startBall to $endBall ($label, Severity: $severity)."

            return CollapseModel(
                collapseId = "col_${row["match_id"]}_$inn",
                matchId = row["match_id"]?.toDoubleOrNull()?.toLong(),
                innings = inn,
                team = row["team"] ?: defaultTeam,
                score = scoreStr,
                wicketsLost = wLost,
                runsAdded = rScored,
                oversTaken = 0.0,
                severity = severity,
                classification = label,
                explanation = explStr
            )
        }
    }
}

/**
 * PitchIQ Data Warehouse Model: Recovery
 * Source: fact_match_recoveries.csv
 */
data class RecoveryModel(
    val recoveryId: String? = null,
    val matchId: Long? = null,
    val innings: Int = 1,
    val team: String = "",
    val transition: String = "",
    val partnershipRuns: Int = 0,
    val recoveryScore: Double = 0.0,
    val classification: String = "Recovery",
    val explanation: String = ""
) {
    companion object {
        fun fromCsvMap(row: Map<String, String>, defaultTeam: String = ""): RecoveryModel {
            val rAdded = row["runs_added"]?.toDoubleOrNull()?.toInt() ?: row["partnership_runs"]?.toDoubleOrNull()?.toInt() ?: 0
            val wLost = row["wickets_lost"]?.toDoubleOrNull()?.toInt() ?: 1
            val rBefore = row["runs_before"] ?: ""
            val rAfter = row["runs_after"] ?: ""
            val wBefore = row["wickets_before"] ?: ""
            val wAfter = row["wickets_after"] ?: ""
            val startBall = row["recovery_start_ball"] ?: ""
            val endBall = row["recovery_end_ball"] ?: ""
            val score = row["recovery_severity"]?.toDoubleOrNull() ?: row["recovery_score"]?.toDoubleOrNull() ?: 0.0
            val label = row["recovery_label"] ?: row["classification"] ?: "Major Recovery"
            val inn = row["innings"]?.toDoubleOrNull()?.toInt() ?: 1

            val transStr = if (rBefore.isNotBlank() && rAfter.isNotBlank()) {
                "$rBefore/$wBefore -> $rAfter/$wAfter ($rAdded runs added)"
            } else {
                row["transition"] ?: "$rAdded runs added"
            }

            val explStr = row["explanation"]?.takeIf { it.isNotBlank() }
                ?: "Innings $inn: Resilient $rAdded-run stand from over $startBall to $endBall lifting score to $rAfter/$wAfter ($label, Score: $score)."

            return RecoveryModel(
                recoveryId = "rec_${row["match_id"]}_$inn",
                matchId = row["match_id"]?.toDoubleOrNull()?.toLong(),
                innings = inn,
                team = row["team"] ?: defaultTeam,
                transition = transStr,
                partnershipRuns = rAdded,
                recoveryScore = score,
                classification = label,
                explanation = explStr
            )
        }
    }
}

/**
 * PitchIQ Data Warehouse Model: Turning Point
 * Source: fact_match_turning_points.csv
 */
data class TurningPointWarehouseModel(
    val matchId: Long,
    val rank: Int,
    val innings: Int,
    val partnershipName: String,
    val batter1Id: Int,
    val batter2Id: Int,
    val runsAdded: Double,
    val ballsFaced: Double,
    val impactScore: Double,
    val startBall: Double,
    val endBall: Double
) {
    companion object {
        fun fromCsvMap(row: Map<String, String>): TurningPointWarehouseModel {
            return TurningPointWarehouseModel(
                matchId = row["match_id"]?.toDoubleOrNull()?.toLong() ?: 0L,
                rank = row["turning_point_rank"]?.toDoubleOrNull()?.toInt() ?: 1,
                innings = row["innings"]?.toDoubleOrNull()?.toInt() ?: 1,
                partnershipName = row["partnership_name"] ?: "Key Stand",
                batter1Id = row["batter1_id"]?.toDoubleOrNull()?.toInt() ?: 0,
                batter2Id = row["batter2_id"]?.toDoubleOrNull()?.toInt() ?: 0,
                runsAdded = row["runs_added"]?.toDoubleOrNull() ?: 0.0,
                ballsFaced = row["balls_faced"]?.toDoubleOrNull() ?: 0.0,
                impactScore = row["impact_score"]?.toDoubleOrNull() ?: 0.0,
                startBall = row["start_ball"]?.toDoubleOrNull() ?: 0.0,
                endBall = row["end_ball"]?.toDoubleOrNull() ?: 0.0
            )
        }
    }
}

/**
 * PitchIQ Data Warehouse Model: Player Match Rating
 * Source: fact_player_match_rating.csv
 */
data class PlayerMatchRatingModel(
    val matchId: Long = 0L,
    val playerId: String? = null,
    val playerName: String,
    val team: String,
    val role: String,
    val jerseyNumber: Int? = null,
    val overallRating: Double,
    val battingRating: Double = 0.0,
    val bowlingRating: Double = 0.0,
    val impactRating: Double = 0.0,
    val impactRank: String = "-",
    val battingImpactRating: Double = 0.0,
    val bowlingImpactRating: Double = 0.0,
    val runs: String = "-",
    val balls: String = "-",
    val strikeRate: String = "-",
    val fours: String = "-",
    val sixes: String = "-",
    val wickets: String = "-",
    val overs: String = "-",
    val economy: String = "-",
    val bestBowling: String = "-",
    val primaryHighlight: String = "",
    val story: String = "",
    // Batting Intelligence
    val controlPct: String = "-",
    val falseShotPct: String = "-",
    val attackingPct: String = "-",
    val leavePct: String = "-",
    val battingDotPct: String = "-",
    val boundaryRunPct: String = "-",
    // Bowling Intelligence
    val bowlingDotPct: String = "-",
    val lineAcc: String = "-",
    val lengthAcc: String = "-",
    val boundariesConceded: String = "-",
    // Innings Progression
    val inningsProgression: List<InningsRatingModel> = emptyList()
)

data class InningsRatingModel(
    val innings: Int = 1,
    val label: String = "Inn 1",
    val rating: Double = 0.0,
    val battingRating: Double = 0.0,
    val bowlingRating: Double = 0.0
)

/**
 * Single Innings Detailed Breakdown Model
 * Source: player_innings_details.json / match JSON playerInningsDetails
 */
data class PlayerInningsDetailModel(
    val inningsNumber: Int = 1,
    val participationType: String = "DNB",
    val inningsScorecard: String = "DNB",
    val arrivalScore: String = "-",
    // Batting stats
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val strikeRate: Double = 0.0,
    val controlPct: Double = 0.0,
    val falseShotPct: Double = 0.0,
    val attackingPct: Double = 0.0,
    val leavePct: Double = 0.0,
    val dotBallPct: Double = 0.0,
    val boundaryRunPct: Double = 0.0,
    val battingRating: Double = 0.0,
    val battingImpactRating: Double = 0.0,
    val dismissed: Boolean = false,
    val dismissalType: String = "",
    val dismissedBy: String = "",
    // Bowling stats
    val overs: String = "0.0",
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val economy: Double = 0.0,
    val bowlingDotPct: Double = 0.0,
    val lineAccuracyPct: Double = 0.0,
    val lengthAccuracyPct: Double = 0.0,
    val boundariesConceded: Int = 0,
    val bowlingRating: Double = 0.0,
    val bowlingImpactRating: Double = 0.0
)

data class PlayerInningsDetailsProfile(
    val playerId: Int = 0,
    val playerName: String = "",
    val team: String = "",
    val innings: List<PlayerInningsDetailModel> = emptyList()
)

typealias PlayerRatingModel = PlayerMatchRatingModel

/**
 * PitchIQ Data Warehouse Model: Player Match Impact
 * Source: fact_player_match_impact.csv
 */
data class PlayerMatchImpactModel(
    val matchId: Long = 0L,
    val playerId: String? = null,
    val playerName: String,
    val team: String,
    val role: String,
    val impactRank: Int = 1,
    val impactScore: Double = 0.0,
    val battingImpact: Double = 0.0,
    val bowlingImpact: Double = 0.0,
    val headlineImpact: String = "",
    val impactExplanation: String = ""
)

/**
 * PitchIQ Data Warehouse Model: Player Innings
 * Source: fact_player_innings.csv
 */
data class PlayerInningsModel(
    val matchId: Long,
    val inningsNumber: Int,
    val playerId: Int,
    val playerName: String = "",
    val battingPosition: Int,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Double,
    val isNotOut: Boolean = false,
    val dismissal: String = "not out"
)

/**
 * PitchIQ Data Warehouse Model: Bowling Innings
 * Source: fact_bowling_innings.csv
 */
data class BowlingInningsModel(
    val matchId: Long,
    val inningsNumber: Int,
    val playerId: Int,
    val bowlerName: String = "",
    val overs: String,
    val maidens: Int = 0,
    val runsConceded: Int,
    val wickets: Int,
    val economy: Double,
    val dots: Int = 0
)

/**
 * Series Story Data Model (4 Bullets)
 * Source: fact_series_story.csv
 */
data class SeriesStoryData(
    val seriesId: Int = 0,
    val seriesName: String = "",
    val bulletStart: String = "",
    val bulletTurning: String = "",
    val bulletMomentum: String = "",
    val bulletConclusion: String = ""
)

/**
 * Series Momentum Timeline Node Model
 * Sources: fact_series_matches.csv & fact_series_standings.csv
 */
data class SeriesMomentumNode(
    val seriesId: Int = 0,
    val matchId: Long = 0L,
    val matchNumber: String = "",
    val scoreAfterMatch: String = "",
    val winner: String = "",
    val margin: String = "",
    val venue: String = "",
    val daysConsumed: Int = 5,
    val keyTurningPoint: String = "",
    val turningPointPlayer: String = "",
    val mvpPlayer: String = "",
    val mvpStat: String = "",
    val team1: String = "",
    val team2: String = ""
)

/**
 * Series Leaderboard Item for Stats Tab
 */
data class SeriesStatLeaderItem(
    val rank: Int = 1,
    val playerId: Long = 0L,
    val playerName: String = "",
    val country: String = "",
    val statValue: String = "",
    val secondaryStat: String = "",
    val matchId: Long = 0L
)

/**
 * Series Leaderboard Category
 */
data class SeriesLeaderboardCategory(
    val categoryTitle: String = "",
    val leaders: List<SeriesStatLeaderItem> = emptyList()
)

/**
 * Series XI Player Model
 */
data class SeriesXIPlayer(
    val playerId: Long = 0L,
    val playerName: String = "",
    val country: String = "",
    val role: String = "Middle Order",
    val ratingAvg: Double = 0.0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val statSummary: String = "",
    val selectionReason: String = "",
    val isCaptain: Boolean = false,
    val isMVP: Boolean = false,
    val matchId: Long = 0L
)

/**
 * Series News Item Model
 */
data class SeriesNewsItem(
    val id: String = "",
    val headline: String = "",
    val date: String = "",
    val source: String = "",
    val snippet: String = "",
    val imageUrl: String = ""
)

/**
 * Comprehensive Series Intelligence Display Data Model (4 Tabs)
 */
data class SeriesIntelligenceData(
    val seriesId: Int = 0,
    val header: SeriesHeaderData,
    val seriesDuration: String = "",
    val matchesPlayed: Int = 0,
    val daysConsumed: Int = 0,
    val story: SeriesStoryData = SeriesStoryData(),
    val momentumNodes: List<SeriesMomentumNode> = emptyList(),
    val statCategories: List<SeriesLeaderboardCategory> = emptyList(),
    val seriesXI: List<SeriesXIPlayer> = emptyList(),
    val captain: SeriesXIPlayer? = null,
    val playerOfSeries: SeriesXIPlayer? = null,
    val newsList: List<SeriesNewsItem> = emptyList()
)

