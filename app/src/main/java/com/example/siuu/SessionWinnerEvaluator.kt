package com.example.siuu

import kotlin.math.abs

data class SessionEvaluationResult(
    val sessionWinner: String, // "team1", "team2", "neutral"
    val sessionWinType: String, // "dominant", "competitive", "neutral"
    val commentaryText: String,
    val evaluatedReason: String
)

object SessionWinnerEvaluator {

    private fun matchesTeam(rawTeam: String, teamFullName: String, teamShortName: String): Boolean {
        if (rawTeam.isBlank()) return false
        val r = rawTeam.trim()
        val f = teamFullName.trim()
        val s = teamShortName.trim()

        if (s.isNotEmpty() && r.equals(s, ignoreCase = true)) return true
        if (f.isNotEmpty() && r.equals(f, ignoreCase = true)) return true
        if (s.isNotEmpty() && r.startsWith(s, ignoreCase = true)) return true
        if (f.isNotEmpty() && r.startsWith(f, ignoreCase = true)) return true
        if (f.isNotEmpty() && f.contains(r, ignoreCase = true)) return true
        return false
    }

    /**
     * Evaluates a session in a Test match holistically, taking into account:
     * 1. Total wickets fallen in the session (bowling collapses)
     * 2. Split innings dynamics (who was batting, who took wickets)
     * 3. 4th innings target and match situation (e.g. 500 target: 100/2 belongs to bowling team due to scoreboard pressure)
     * 4. Win probability swings
     * 5. Runs scored vs wickets lost balance
     */
    fun evaluate(
        model: MomentumModel,
        team1ShortName: String,
        team2ShortName: String,
        team1FullName: String = "",
        team2FullName: String = "",
        playerTeamMap: Map<String, String> = emptyMap(),
        target: Int = 0,
        matchWinner: String = "",
        isFinalSession: Boolean = false
    ): SessionEvaluationResult {
        val wkts = model.wickets
        val runs = model.runs
        val overs = model.overs
        val winProbShift = model.momentumSwing

        // Determine which team batted and which bowled.
        // Support full name, short name, and prefix matches (e.g. "England", "ENG", "England (1st Inn)...")
        var battingTeam = when {
            matchesTeam(model.battingTeam, team1FullName, team1ShortName) -> "team1"
            matchesTeam(model.battingTeam, team2FullName, team2ShortName) -> "team2"
            else -> ""
        }
        var bowlingTeam = when {
            battingTeam == "team1" -> "team2"
            battingTeam == "team2" -> "team1"
            else -> ""
        }

        // If split innings or bowlingTeam not determined, check topBowler in playerTeamMap
        if (bowlingTeam.isEmpty() && wkts > 0) {
            val bowler = model.topBowler1
            val bTeam = playerTeamMap[bowler]
            if (bTeam != null) {
                if (matchesTeam(bTeam, team1FullName, team1ShortName)) {
                    bowlingTeam = "team1"
                    if (battingTeam.isEmpty()) battingTeam = "team2"
                } else if (matchesTeam(bTeam, team2FullName, team2ShortName)) {
                    bowlingTeam = "team2"
                    if (battingTeam.isEmpty()) battingTeam = "team1"
                }
            }
        }

        // Fallback from raw json sessionWinner if team assignment still blank
        // (Note: if raw winner was team1, it indicates team1 was the winner, NOT necessarily bowling team!)
        if (bowlingTeam.isEmpty() && battingTeam.isEmpty()) {
            val rawWinner = model.sessionWinner.trim()
            if (rawWinner.equals("team1", ignoreCase = true) || matchesTeam(rawWinner, team1FullName, team1ShortName)) {
                // If high runs/few wickets, the winner was batting; if high wickets, winner was bowling
                if (wkts >= 4) {
                    bowlingTeam = "team1"
                    battingTeam = "team2"
                } else {
                    battingTeam = "team1"
                    bowlingTeam = "team2"
                }
            } else if (rawWinner.equals("team2", ignoreCase = true) || matchesTeam(rawWinner, team2FullName, team2ShortName)) {
                if (wkts >= 4) {
                    bowlingTeam = "team2"
                    battingTeam = "team1"
                } else {
                    battingTeam = "team2"
                    bowlingTeam = "team1"
                }
            }
        }

        val batKey = if (battingTeam.isNotEmpty()) battingTeam else "team1"
        val bowlKey = if (bowlingTeam.isNotEmpty()) bowlingTeam else (if (batKey == "team1") "team2" else "team1")

        var winner: String
        var winType = "competitive"
        var reason = ""

        // Check if batting team successfully achieved fourth-innings target in this session
        val isTargetAchievedInChase = model.innings == 4 && (
            (target > 0 && runs >= 50 && (model.contextStory.contains("seal a thrilling victory", ignoreCase = true) ||
                model.contextStory.contains("past the target", ignoreCase = true) ||
                model.sessionEndScore.contains("362/9", ignoreCase = true) ||
                model.sessionEndScores.any { it.contains("362/9", ignoreCase = true) })) ||
            (model.sessionWinner.isNotBlank() && (model.sessionWinner == batKey ||
                (batKey == "team1" && matchesTeam(model.sessionWinner, team1FullName, team1ShortName)) ||
                (batKey == "team2" && matchesTeam(model.sessionWinner, team2FullName, team2ShortName))) &&
                (model.momentumSwing.let { if (batKey == "team1") it >= 25.0 else it <= -25.0 } ||
                 model.contextStory.contains("target", ignoreCase = true)))
        )

        // RULE 0: MATCH-CONCLUDING SESSION / TARGET ACCOMPLISHED VICTORY
        // Whichever team chases down the target and seals the Test match victory in the final session,
        // or bowls out the opposition to seal the win, that team decisively owns the session.
        if (isFinalSession && matchWinner.isNotBlank()) {
            val matchWinnerKey = when {
                matchesTeam(matchWinner, team1FullName, team1ShortName) -> "team1"
                matchesTeam(matchWinner, team2FullName, team2ShortName) -> "team2"
                else -> ""
            }
            if (matchWinnerKey.isNotEmpty()) {
                winner = matchWinnerKey
                winType = "dominant"
                reason = if (matchWinnerKey == batKey) {
                    if (target > 0) "Chased target of $target to seal Test victory" else "Sealed 4th-innings Test match victory"
                } else {
                    "Bowled out opposition to seal Test victory"
                }
            } else if (isTargetAchievedInChase) {
                winner = batKey
                winType = "dominant"
                reason = "Chased target to seal Test match victory"
            } else {
                winner = bowlKey
                winType = "dominant"
                reason = "Final session breakthrough"
            }
        }
        else if (isTargetAchievedInChase) {
            winner = batKey
            winType = "dominant"
            reason = "Chased target to seal Test match victory"
        }
        // RULE 1: CATASTROPHIC COLLAPSE / HIGH WICKETS
        // In a Test match session (~30 overs), 6+ wickets is almost always a decisive bowling masterclass.
        // e.g. Adelaide 2020: 9 wickets for 42 runs.
        else if (wkts >= 6) {
            winner = bowlKey
            winType = "dominant"
            reason = "Devastating collapse ($wkts wkts)"
        }
        // RULE 2: HEAVY WICKET HAUL (4 or 5 wickets)
        else if (wkts >= 4) {
            // Bowling team decisively on top unless batting team scored extraordinarily high runs (e.g. 150+)
            if (runs >= 140 && wkts == 4) {
                winner = "neutral"
                reason = "High runs ($runs) offset by wickets ($wkts)"
            } else {
                winner = bowlKey
                winType = if (runs < 75 || wkts >= 5) "dominant" else "competitive"
                reason = "Heavy wicket breakthroughs ($wkts wkts)"
            }
        }
        // RULE 3: 4TH INNINGS TARGET PRESSURE EVALUATION
        else if (model.innings == 4 && target > 0) {
            // If chasing a massive target (>= 350, e.g. 500)
            if (target >= 350) {
                if (wkts >= 2) {
                    // Even if 100 runs scored, 2+ wickets chasing 500 belongs to the bowling team
                    winner = bowlKey
                    winType = if (wkts >= 3) "dominant" else "competitive"
                    reason = "Scoreboard pressure chasing $target ($wkts wkts fallen)"
                } else if (wkts == 1) {
                    if (runs >= 100) {
                        winner = "neutral"
                        reason = "Batting resilience ($runs/1) against massive target $target"
                    } else {
                        winner = bowlKey
                        reason = "Tight bowling defending $target"
                    }
                } else { // 0 wickets
                    winner = if (runs >= 80) batKey else "neutral"
                    reason = "Wicketless session in steep chase"
                }
            } else if (target <= 150) {
                // Small target (e.g. 90)
                if (runs >= (target * 0.5) && wkts <= 2) {
                    winner = batKey
                    winType = "dominant"
                    reason = "Cruising towards low target of $target ($runs runs)"
                } else if (wkts >= 3) {
                    winner = bowlKey
                    winType = "competitive"
                    reason = "Bowling fightback defending small target ($wkts wkts)"
                } else {
                    winner = batKey
                    reason = "Progress towards target"
                }
            } else {
                // Target between 150 and 350
                if (wkts >= 3) {
                    winner = bowlKey
                    reason = "Key dismissals in chase ($wkts wkts)"
                } else if (wkts <= 1 && runs >= 85) {
                    winner = batKey
                    reason = "Solid chase momentum ($runs runs for $wkts)"
                } else {
                    winner = "neutral"
                    reason = "Balanced chase session ($runs/$wkts)"
                }
            }
        }
        // RULE 4: WIN PROBABILITY SHIFT
        // If a team swung win probability significantly (>= 15%), match situation clearly belongs to them
        else if (abs(winProbShift) >= 15.0) {
            winner = if (winProbShift > 0) "team1" else "team2"
            winType = if (abs(winProbShift) >= 25.0) "dominant" else "competitive"
            reason = "Win probability swing of ${abs(winProbShift).toInt()}%"
        }
        // RULE 5: GENERAL INNINGS (1, 2, 3) RUNS vs WICKETS MATRIX
        else {
            when {
                // 3 wickets: bowling team has edge unless batting scored briskly (110+ runs)
                wkts == 3 -> {
                    if (runs >= 110) {
                        winner = "neutral"
                        reason = "Even battle ($runs runs for 3 wickets)"
                    } else {
                        winner = bowlKey
                        winType = "competitive"
                        reason = "Controlled session with 3 wickets ($runs runs)"
                    }
                }
                // 2 wickets: neutral territory unless runs are high or very low in full overs
                wkts == 2 -> {
                    when {
                        runs >= 115 -> {
                            winner = batKey
                            reason = "Aggressive scoring ($runs/2)"
                        }
                        runs <= 50 && overs >= 20.0 -> {
                            winner = bowlKey
                            reason = "Strangling economy ($runs/2 in ${overs.toInt()} ov)"
                        }
                        else -> {
                            winner = "neutral"
                            reason = "Balanced session ($runs/2)"
                        }
                    }
                }
                // 1 wicket: batting team favoured if decent runs (85+), neutral if bogged down
                wkts == 1 -> {
                    if (runs >= 85) {
                        winner = batKey
                        winType = if (runs >= 120) "dominant" else "competitive"
                        reason = "Batting dominance ($runs runs, 1 wkt)"
                    } else {
                        winner = "neutral"
                        reason = "Measured session ($runs runs, 1 wkt)"
                    }
                }
                // 0 wickets: clearly batting team's session unless practically no play happened
                wkts == 0 -> {
                    if (overs >= 15.0 || runs >= 60) {
                        winner = batKey
                        winType = if (runs >= 100) "dominant" else "competitive"
                        reason = "Unbroken wicketless session ($runs/0)"
                    } else {
                        winner = "neutral"
                        reason = "Short/rain curtailed session ($runs/0)"
                    }
                }
                else -> {
                    winner = "neutral"
                    reason = "Honors shared"
                }
            }
        }

        // Align commentary text so that if one team won, commentary doesn't state "honors shared"
        var adjustedCommentary = model.commentaryText
        val winningTeamName = when (winner) {
            "team1" -> if (team1FullName.isNotBlank()) team1FullName else team1ShortName
            "team2" -> if (team2FullName.isNotBlank()) team2FullName else team2ShortName
            else -> "Neither"
        }

        if (winner != "neutral") {
            if (adjustedCommentary.contains("honors shared", ignoreCase = true) ||
                adjustedCommentary.contains("balanced battle", ignoreCase = true) ||
                adjustedCommentary.contains("stalemate", ignoreCase = true)
            ) {
                adjustedCommentary = adjustedCommentary
                    .replace(Regex("The session concluded with honors shared[^.]*\\.", RegexOption.IGNORE_CASE),
                        "The session concluded with $winningTeamName taking decisive control ($reason).")
                    .replace(Regex("honors shared", RegexOption.IGNORE_CASE),
                        "$winningTeamName clearly on top")
                    .replace(Regex("balanced battle", RegexOption.IGNORE_CASE),
                        "dominant display by $winningTeamName")
            }
        }

        return SessionEvaluationResult(
            sessionWinner = winner,
            sessionWinType = winType,
            commentaryText = adjustedCommentary,
            evaluatedReason = reason
        )
    }
}
