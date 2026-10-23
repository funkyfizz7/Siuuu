package com.example.siuu

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a human-reviewed published narrative override.
 * Identifies the exact match and narrative item without altering any factual match statistics.
 */
data class PublishedNarrativeOverride(
    val id: String, // e.g. "1152846_summary_point_0"
    val matchId: Long,
    val narrativeType: String, // "summary_point", "summary_story", "timeline_item", "collapse", "recovery", "momentum_story", "pressure_explanation", "pressure_verdict", "turning_point", "verdict_node"
    val itemKey: String, // "0", "d1_s1", "primary", "s1", "1"
    val publishedText: String,
    val updatedAt: String = "",
    val publishedBy: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("matchId", matchId)
        json.put("narrativeType", narrativeType)
        json.put("itemKey", itemKey)
        json.put("publishedText", publishedText)
        json.put("updatedAt", updatedAt)
        json.put("publishedBy", publishedBy)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): PublishedNarrativeOverride? {
            val text = json.optString("publishedText", "").trim()
            if (text.isBlank()) return null

            return PublishedNarrativeOverride(
                id = json.optString("id", ""),
                matchId = json.optLong("matchId", 0L),
                narrativeType = json.optString("narrativeType", ""),
                itemKey = json.optString("itemKey", ""),
                publishedText = text,
                updatedAt = json.optString("updatedAt", ""),
                publishedBy = json.optString("publishedBy", "")
            )
        }
    }
}

/**
 * Container holding all active published overrides for a specific match.
 */
data class MatchNarrativeOverrides(
    val matchId: Long,
    val overrides: Map<String, PublishedNarrativeOverride> = emptyMap(),
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val root = JSONObject()
        root.put("matchId", matchId)
        root.put("cachedAt", cachedAt)
        val arr = JSONArray()
        overrides.values.forEach { arr.put(it.toJson()) }
        root.put("overrides", arr)
        return root
    }

    companion object {
        fun fromJson(json: JSONObject): MatchNarrativeOverrides {
            val matchId = json.optLong("matchId", 0L)
            val cachedAt = json.optLong("cachedAt", System.currentTimeMillis())
            val map = mutableMapOf<String, PublishedNarrativeOverride>()

            val arr = json.optJSONArray("overrides")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val item = PublishedNarrativeOverride.fromJson(obj)
                    if (item != null && item.id.isNotBlank()) {
                        map[item.id] = item
                    }
                }
            }
            return MatchNarrativeOverrides(matchId, map, cachedAt)
        }
    }
}
