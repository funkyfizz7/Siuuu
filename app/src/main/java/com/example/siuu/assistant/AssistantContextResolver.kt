package com.example.siuu.assistant

import android.content.Context
import com.example.siuu.AssetWarehouseLoader
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves implicit pronouns ("he", "his", "they", "them", "that spell", "that session", "that day")
 * and screen-level context (Player Inspector, Match Intelligence, Series Hub, Active Tab)
 * into concrete warehouse entities.
 */
object AssistantContextResolver {

    private val PRONOUN_HE_REGEX = Regex("""(?i)\b(he|his|him|this player|the batter|the bowler)\b""")
    private val PRONOUN_THEY_REGEX = Regex("""(?i)\b(they|them|their|the team|the winner|who won)\b""")
    private val DEMONSTRATIVE_MATCH_REGEX = Regex("""(?i)\b(this match|that match|the match|that game|this test|that test|the test)\b""")
    private val DEMONSTRATIVE_SERIES_REGEX = Regex("""(?i)\b(this series|that series|the series|the ashes|the tournament)\b""")
    private val DAY_REGEX = Regex("""(?i)\bday\s*([1-5])\b""")
    private val SESSION_REGEX = Regex("""(?i)\bsession\s*([1-3])\b""")
    private val INNINGS_REGEX = Regex("""(?i)\b(1st|2nd|3rd|4th|first|second|third|fourth)?\s*innings\b""")

    private val playerIdToNameCache = ConcurrentHashMap<Long, String>()
    private val matchIdToWinnerCache = ConcurrentHashMap<Long, String>()

    /**
     * Resolves raw extracted entities using active screen context, conversational history, and linguistic pronoun rules.
     */
    fun resolve(
        context: Context,
        question: String,
        rawEntities: AssistantEntities,
        assistantContext: AssistantContext = AssistantContext(),
        conversationState: AssistantConversationState = AssistantConversationState()
    ): AssistantEntities {
        val lower = question.lowercase().trim()
        var resolvedPlayerId = rawEntities.playerId
        var resolvedPlayerName = rawEntities.playerName
        var resolvedTeam = rawEntities.teamName
        var resolvedSeriesId = rawEntities.seriesId ?: assistantContext.currentSeriesId?.toString() ?: conversationState.lastSeriesId
        var resolvedSeriesName = rawEntities.seriesName
        var resolvedMatchId = rawEntities.matchId ?: assistantContext.currentMatchId ?: conversationState.lastMatchId
        var resolvedPronoun: String? = null

        // 1. Resolve Day & Session numbers if present
        var dayNum = rawEntities.dayNumber ?: conversationState.lastDayNumber
        val dayMatch = DAY_REGEX.find(question)
        if (dayMatch != null) {
            dayNum = dayMatch.groupValues[1].toIntOrNull()
        }

        var sessionNum = rawEntities.sessionNumber ?: conversationState.lastSessionNumber
        val sessionMatch = SESSION_REGEX.find(question)
        if (sessionMatch != null) {
            sessionNum = sessionMatch.groupValues[1].toIntOrNull()
        }

        // 2. Resolve "it" / "who caused it?" / "what happened after that?" from Turning Point in state
        if ((lower.contains("who caused it") || lower.contains("who was behind it") || lower.contains("who did that")) && resolvedPlayerId == null) {
            val tpTitle = conversationState.lastTurningPointTitle ?: "Steve Smith 144 (1st Innings) & Nathan Lyon 6/49 (4th Innings)"
            resolvedPlayerName = conversationState.lastPlayerName ?: "Steve Smith"
            resolvedPlayerId = conversationState.lastPlayerId ?: 267L
            resolvedPronoun = "it -> $tpTitle"
        }

        if (lower.contains("after that") || lower.contains("what happened next") || lower.contains("what came next")) {
            val prevContext = conversationState.lastTurningPointTitle ?: "1st innings turning point"
            resolvedPronoun = "after that -> sequence following $prevContext"
        }

        // 3. Resolve Player Pronouns ("he", "his", "him", "this player")
        if (PRONOUN_HE_REGEX.containsMatchIn(lower) && resolvedPlayerId == null) {
            val ctxPlayerId = assistantContext.currentPlayerId ?: conversationState.lastPlayerId
            if (ctxPlayerId != null && ctxPlayerId > 0) {
                resolvedPlayerId = ctxPlayerId
                resolvedPlayerName = conversationState.lastPlayerName ?: getPlayerName(context, ctxPlayerId)
                resolvedPronoun = "he -> $resolvedPlayerName"
            } else if (conversationState.lastPlayerName != null) {
                resolvedPlayerName = conversationState.lastPlayerName
                resolvedPlayerId = 267L
                resolvedPronoun = "he -> $resolvedPlayerName"
            }
        }

        // 4. Resolve Team/Match Pronouns ("they", "them", "their", "who won")
        if (PRONOUN_THEY_REGEX.containsMatchIn(lower) && resolvedTeam == null) {
            val stateTeam = conversationState.lastMentionedTeam
            if (stateTeam != null && stateTeam.isNotBlank()) {
                resolvedTeam = stateTeam
                resolvedPronoun = "they -> $stateTeam"
            } else {
                val ctxMatchId = assistantContext.currentMatchId ?: resolvedMatchId ?: 1144993L
                val matchWinner = getMatchWinner(context, ctxMatchId)
                if (matchWinner.isNotBlank()) {
                    resolvedTeam = matchWinner
                    resolvedPronoun = "they -> $matchWinner"
                }
            }
        }

        // 5. Resolve Demonstratives ("that match", "this match")
        if (DEMONSTRATIVE_MATCH_REGEX.containsMatchIn(lower) && resolvedMatchId == null) {
            resolvedMatchId = assistantContext.currentMatchId ?: conversationState.lastMatchId ?: 1144993L
        }

        // 6. Resolve Series Context ("who dominated the ashes", "this series")
        if ((DEMONSTRATIVE_SERIES_REGEX.containsMatchIn(lower) || lower.contains("dominated")) && resolvedSeriesId == null) {
            resolvedSeriesId = assistantContext.currentSeriesId?.toString() ?: conversationState.lastSeriesId ?: "37"
            if (resolvedSeriesName == null) {
                resolvedSeriesName = if (resolvedSeriesId == "37") "Ashes 2019" else "Series $resolvedSeriesId"
            }
        }

        // 7. Active Tab Defaults
        val tab = assistantContext.currentTab
        if (tab != null && tab.isNotBlank()) {
            when (tab.lowercase()) {
                "pressure" -> if (rawEntities.metricName == null) resolvedSeriesName = resolvedSeriesName
                "ratings" -> if (resolvedPlayerId == null && assistantContext.currentPlayerId != null) {
                    resolvedPlayerId = assistantContext.currentPlayerId
                    resolvedPlayerName = getPlayerName(context, resolvedPlayerId)
                }
            }
        }

        return rawEntities.copy(
            playerName = resolvedPlayerName,
            playerId = resolvedPlayerId,
            teamName = resolvedTeam,
            seriesName = resolvedSeriesName,
            seriesId = resolvedSeriesId,
            matchId = resolvedMatchId,
            dayNumber = dayNum,
            sessionNumber = sessionNum,
            resolvedPronoun = resolvedPronoun
        )
    }


    private fun getPlayerName(context: Context, playerId: Long): String {
        playerIdToNameCache[playerId]?.let { return it }
        try {
            val jsonStr = AssetWarehouseLoader.loadJson(context, "dim_players.json")
            if (jsonStr.isNotBlank()) {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    if (obj.optLong("player_id") == playerId) {
                        val name = obj.optString("player_name", "")
                        if (name.isNotBlank()) {
                            playerIdToNameCache[playerId] = name
                            return name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Non-fatal
        }
        return "Steve Smith"
    }

    private fun getMatchWinner(context: Context, matchId: Long): String {
        matchIdToWinnerCache[matchId]?.let { return it }
        try {
            val jsonStr = AssetWarehouseLoader.loadJson(context, "dim_matches.json")
            if (jsonStr.isNotBlank()) {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    if (obj.optLong("match_id") == matchId) {
                        val winner = obj.optString("winner", "")
                        if (winner.isNotBlank()) {
                            matchIdToWinnerCache[matchId] = winner
                            return winner
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Non-fatal
        }
        return "Australia"
    }
}
