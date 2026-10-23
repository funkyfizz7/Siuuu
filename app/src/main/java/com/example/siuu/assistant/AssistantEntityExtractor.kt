package com.example.siuu.assistant

import android.content.Context
import com.example.siuu.AssetWarehouseLoader
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Entity extractor for cricket entities:
 * - Player Name & ID
 * - Team Name
 * - Series Name & ID
 * - Metric Name
 * - Target Rating
 */
object AssistantEntityExtractor {

    private val playerNameToIdMap = ConcurrentHashMap<String, Long>()
    private val playerIdToNameMap = ConcurrentHashMap<Long, String>()
    private val playerAliases = ConcurrentHashMap<String, Long>()
    private val seriesNameToIdMap = ConcurrentHashMap<String, String>()

    private val TEAMS = listOf(
        "Australia" to listOf("australia", "aus", "aussie", "aussies"),
        "England" to listOf("england", "eng", "english"),
        "India" to listOf("india", "ind", "indians"),
        "South Africa" to listOf("south africa", "sa", "proteas"),
        "New Zealand" to listOf("new zealand", "nz", "blackcaps", "kiwis"),
        "Pakistan" to listOf("pakistan", "pak"),
        "Sri Lanka" to listOf("sri lanka", "sl"),
        "West Indies" to listOf("west indies", "wi", "windies"),
        "Bangladesh" to listOf("bangladesh", "ban", "tigers"),
        "Ireland" to listOf("ireland", "ire"),
        "Zimbabwe" to listOf("zimbabwe", "zim")
    )

    private val METRICS = listOf(
        "runs" to listOf("runs", "run", "scores", "scored", "highest score", "most runs"),
        "wickets" to listOf("wickets", "wicket", "wkts", "wkt", "most wickets", "leading wicket"),
        "rating" to listOf("rating", "ratings", "rated", "score", "mvp score"),
        "average" to listOf("average", "avg"),
        "strike_rate" to listOf("strike rate", "sr"),
        "turning_point" to listOf("turning point", "game changer", "key moment"),
        "pressure" to listOf("pressure", "pressure index", "peak pressure"),
        "momentum" to listOf("momentum", "swing", "dominance"),
        "partnership" to listOf("partnership", "stand", "highest partnership")
    )

    @Volatile
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                // 1. Load players
                val jsonStr = AssetWarehouseLoader.loadJson(context, "dim_players.json")
                if (jsonStr.isNotBlank()) {
                    val arr = JSONArray(jsonStr)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val pid = obj.optLong("player_id", 0L)
                        val name = obj.optString("player_name", "").trim()
                        if (pid > 0L && name.isNotBlank()) {
                            val lowerName = name.lowercase()
                            playerNameToIdMap[lowerName] = pid
                            playerIdToNameMap[pid] = name

                            // Build alias: last name and parts
                            val parts = name.split(" ").filter { it.length > 2 }
                            for (p in parts) {
                                val lp = p.lowercase()
                                if (!playerAliases.containsKey(lp)) {
                                    playerAliases[lp] = pid
                                }
                            }
                        }
                    }
                }

                // Explicit priority aliases for unambiguous major stars
                val prominentAliases = mapOf(
                    "smith" to 267L,          // Steve Smith
                    "steve smith" to 267L,
                    "kohli" to 326L,          // Virat Kohli
                    "virat kohli" to 326L,
                    "root" to 148L,           // Joe Root
                    "joe root" to 148L,
                    "stokes" to 31L,          // Ben Stokes
                    "ben stokes" to 31L,
                    "cummins" to 236L,        // Pat Cummins
                    "pat cummins" to 236L,
                    "warner" to 77L,          // David Warner
                    "david warner" to 77L,
                    "lyon" to 222L,           // Nathan Lyon
                    "nathan lyon" to 222L,
                    "khawaja" to 308L,        // Usman Khawaja
                    "usman khawaja" to 308L,
                    "bancroft" to 57L,        // Cameron Bancroft
                    "siddle" to 240L,         // Peter Siddle
                    "wade" to 216L,           // Matthew Wade
                    "paine" to 303L,          // Tim Paine
                    "pattinson" to 141L,      // James Pattinson
                    "head" to 310L,           // Travis Head
                    "broad" to 292L,          // Stuart Broad
                    "stuart broad" to 292L,
                    "anderson" to 134L,       // James Anderson
                    "jimmy anderson" to 134L,
                    "buttler" to 147L,        // Jos Buttler
                    "bairstow" to 146L,       // Jonny Bairstow
                    "woakes" to 60L,          // Chris Woakes
                    "ali" to 189L,            // Moeen Ali
                    "moeen ali" to 189L,
                    "burns" to 243L,          // Rory Burns
                    "denly" to 138L,          // Joe Denly
                    "rabada" to 139L,         // Kagiso Rabada
                    "elgar" to 68L,           // Dean Elgar
                    "pujara" to 58L,          // Cheteshwar Pujara
                    "rahane" to 20L,          // Ajinkya Rahane
                    "shami" to 212L,          // Mohammed Shami
                    "ashwin" to 237L,         // Ravichandran Ashwin
                    "jadeja" to 242L,         // Ravindra Jadeja
                    "williamson" to 157L,     // Kane Williamson
                    "boult" to 311L,          // Trent Boult
                    "southee" to 302L         // Tim Southee
                )
                for ((alias, pid) in prominentAliases) {
                    playerAliases[alias] = pid
                }

                // 2. Load series
                val seriesRows = AssetWarehouseLoader.loadCsv(context, "dim_series.csv")
                for (row in seriesRows) {
                    val sid = row["series_id"]?.trim() ?: continue
                    val sname = row["series_name"]?.trim() ?: continue
                    if (sid.isNotBlank() && sname.isNotBlank()) {
                        seriesNameToIdMap[sname.lowercase()] = sid
                    }
                }
                // Custom common aliases for series
                seriesNameToIdMap["ashes 2019"] = "37"
                seriesNameToIdMap["the ashes 2019"] = "37"
                seriesNameToIdMap["ashes"] = "37"
                seriesNameToIdMap["the ashes"] = "37"
                seriesNameToIdMap["ashes 2017-18"] = "9"

                isInitialized = true
            } catch (e: Exception) {
                isInitialized = true
            }
        }
    }

    /**
     * Extracts entities from a user query string and optional context.
     */
    fun extract(
        context: Context,
        question: String,
        assistantContext: AssistantContext = AssistantContext()
    ): AssistantEntities {
        if (!isInitialized) initialize(context)

        val lower = question.lowercase().trim()

        // 1. Extract Players (Supports multiple players for comparison like "Smith or Labuschagne", "Cummins vs Lyon")
        val foundPlayers = mutableListOf<Pair<Long, String>>()

        // Check full names first (longest match)
        for ((pName, pId) in playerNameToIdMap) {
            val regex = Regex("""\b${Regex.escape(pName)}\b""", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(lower)) {
                val realName = playerIdToNameMap[pId] ?: pName
                if (foundPlayers.none { it.first == pId }) {
                    foundPlayers.add(pId to realName)
                }
            }
        }

        // Check aliases if needed
        val sortedAliases = playerAliases.keys.sortedByDescending { it.length }
        for (alias in sortedAliases) {
            val regex = Regex("""\b${Regex.escape(alias)}\b""", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(lower)) {
                val pId = playerAliases[alias] ?: continue
                val realName = playerIdToNameMap[pId] ?: alias.replaceFirstChar { it.uppercase() }
                if (foundPlayers.none { it.first == pId }) {
                    foundPlayers.add(pId to realName)
                }
            }
        }

        var extractedPlayerId: Long? = foundPlayers.firstOrNull()?.first ?: assistantContext.currentPlayerId
        var extractedPlayerName: String? = foundPlayers.firstOrNull()?.second
        var extractedPlayer2Id: Long? = if (foundPlayers.size > 1) foundPlayers[1].first else null
        var extractedPlayer2Name: String? = if (foundPlayers.size > 1) foundPlayers[1].second else null

        if (extractedPlayerName == null && extractedPlayerId != null && extractedPlayerId > 0) {
            extractedPlayerName = playerIdToNameMap[extractedPlayerId]
        }

        // Comparison Target (century vs century, partnership vs partnership)
        var compTarget: String? = null
        if (lower.contains("century") || lower.contains("centuries") || lower.contains("100")) {
            compTarget = "century"
        } else if (lower.contains("partnership") || lower.contains("stand")) {
            compTarget = "partnership"
        }

        // 2. Extract Team
        var extractedTeam: String? = null
        for ((teamCanonical, variants) in TEAMS) {
            if (variants.any { Regex("""\b${Regex.escape(it)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower) }) {
                extractedTeam = teamCanonical
                break
            }
        }

        // 3. Extract Series
        var extractedSeriesId: String? = assistantContext.currentSeriesId?.toString()
        var extractedSeriesName: String? = null

        for ((sName, sId) in seriesNameToIdMap) {
            if (lower.contains(sName)) {
                extractedSeriesId = sId
                extractedSeriesName = sName.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                break
            }
        }

        // 4. Extract Metric
        var extractedMetric: String? = null
        for ((metricCanonical, keywords) in METRICS) {
            if (keywords.any { lower.contains(it) }) {
                extractedMetric = metricCanonical
                break
            }
        }

        // 5. Extract Target Rating (e.g. 9.7 or 9.3)
        var extractedRating: Double? = null
        val ratingMatch = Regex("""\b(\d{1,2}\.\d)\b""").find(question)
        if (ratingMatch != null) {
            extractedRating = ratingMatch.groupValues[1].toDoubleOrNull()
        }

        return AssistantEntities(
            playerName = extractedPlayerName,
            playerId = extractedPlayerId,
            player2Name = extractedPlayer2Name,
            player2Id = extractedPlayer2Id,
            teamName = extractedTeam,
            seriesName = extractedSeriesName,
            seriesId = extractedSeriesId,
            metricName = extractedMetric,
            targetRating = extractedRating,
            comparisonTarget = compTarget
        )
    }
}
