package com.example.siuu

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages human-reviewed narrative overrides for PitchIQ.
 *
 * Precedence Rule:
 * 1. Bundled warehouse JSON provides the permanent baseline.
 * 2. Locally cached overrides (context.filesDir) supersede the baseline.
 * 3. Background remote refresh fetches published Firestore overrides and updates the local cache.
 * 4. Factual statistics (scores, wickets, overs, ratings, calculations) remain strictly unchanged.
 */
object NarrativeOverrideManager {

    private val memoryCache = ConcurrentHashMap<Long, MatchNarrativeOverrides>()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Flow that emits matchId whenever remote overrides are fetched and cached
    private val _overrideUpdates = MutableSharedFlow<Long>(extraBufferCapacity = 10)
    val overrideUpdates: SharedFlow<Long> = _overrideUpdates.asSharedFlow()

    // Optional remote Firestore Project ID (can be configured via string resource or code)
    var remoteFirestoreProjectId: String? = null

    /**
     * Retrieves active published overrides for a match, checking memory first, then internal storage.
     */
    fun getOverridesForMatch(matchId: Long, context: Context?): Map<String, PublishedNarrativeOverride> {
        memoryCache[matchId]?.let { return it.overrides }

        if (context == null) return emptyMap()

        val loaded = loadFromDisk(context, matchId)
        if (loaded != null) {
            memoryCache[matchId] = loaded
            return loaded.overrides
        }

        return emptyMap()
    }

    /**
     * Applies narrative overrides to MatchSummaryData.
     */
    fun applySummaryOverrides(matchId: Long, original: MatchSummaryData, context: Context?): MatchSummaryData {
        val overrides = getOverridesForMatch(matchId, context)
        if (overrides.isEmpty()) return original

        // Override summary points
        val newSummaryPoints = original.summaryPoints.mapIndexed { idx, point ->
            val key = "${matchId}_summary_point_$idx"
            overrides[key]?.publishedText ?: point
        }

        // Override match story
        val storyKey = "${matchId}_summary_story_main"
        val newMatchStory = overrides[storyKey]?.publishedText ?: original.matchStory

        val newSentences = if (original.matchStorySentences.isNotEmpty()) {
            original.matchStorySentences.mapIndexed { idx, sentence ->
                val key = "${matchId}_summary_point_$idx"
                overrides[key]?.publishedText ?: sentence
            }
        } else {
            newSummaryPoints
        }

        return original.copy(
            summaryPoints = newSummaryPoints,
            matchStory = newMatchStory,
            matchStorySentences = newSentences
        )
    }

    /**
     * Applies narrative overrides to StoryTabDisplayData.
     */
    fun applyStoryTabOverrides(matchId: Long, original: StoryTabDisplayData, context: Context?): StoryTabDisplayData {
        val overrides = getOverridesForMatch(matchId, context)
        if (overrides.isEmpty()) return original

        // Override timeline session summaries
        val newTimeline = original.timeline.mapIndexed { idx, story ->
            val day = story.dayNumber
            val session = story.sessionOfDay ?: (idx + 1)
            val key = "${matchId}_timeline_item_d${day}_s${session}"
            val overrideText = overrides[key]?.publishedText
            if (overrideText != null) {
                story.copy(summary = overrideText)
            } else {
                story
            }
        }

        // Override collapse explanation
        val collapseKey = "${matchId}_collapse_primary"
        val newCollapse = overrides[collapseKey]?.publishedText?.let {
            original.primaryCollapse.copy(explanation = it)
        } ?: original.primaryCollapse

        // Override recovery explanation
        val recoveryKey = "${matchId}_recovery_primary"
        val newRecovery = overrides[recoveryKey]?.publishedText?.let {
            original.primaryRecovery.copy(explanation = it)
        } ?: original.primaryRecovery

        return original.copy(
            timeline = newTimeline,
            primaryCollapse = newCollapse,
            primaryRecovery = newRecovery
        )
    }

    /**
     * Applies narrative overrides to MomentumTabDisplayData.
     */
    fun applyMomentumOverrides(matchId: Long, original: MomentumTabDisplayData, context: Context?): MomentumTabDisplayData {
        val overrides = getOverridesForMatch(matchId, context)
        if (overrides.isEmpty()) return original

        val newPoints = original.points.map { pt ->
            val key = "${matchId}_momentum_story_s${pt.sessionIndex}"
            val overrideText = overrides[key]?.publishedText
            if (overrideText != null) {
                pt.copy(contextStory = overrideText)
            } else {
                pt
            }
        }

        val newTopSwings = original.topSwings.map { pt ->
            val key = "${matchId}_momentum_story_s${pt.sessionIndex}"
            val overrideText = overrides[key]?.publishedText
            if (overrideText != null) {
                pt.copy(contextStory = overrideText)
            } else {
                pt
            }
        }

        return original.copy(
            points = newPoints,
            topSwings = newTopSwings
        )
    }

    /**
     * Applies narrative overrides to PressureTabDisplayData.
     */
    fun applyPressureOverrides(matchId: Long, original: PressureTabDisplayData, context: Context?): PressureTabDisplayData {
        val overrides = getOverridesForMatch(matchId, context)
        if (overrides.isEmpty()) return original

        val newPoints = original.pressurePoints.map { pt ->
            val expKey = "${matchId}_pressure_explanation_s${pt.sessionIndex}"
            val verdKey = "${matchId}_pressure_verdict_s${pt.sessionIndex}"

            var updated = pt
            overrides[expKey]?.publishedText?.let { updated = updated.copy(explanation = it) }
            overrides[verdKey]?.publishedText?.let { updated = updated.copy(verdict = it) }
            updated
        }

        val newHighest = original.highestPressureMoments.map { pt ->
            val expKey = "${matchId}_pressure_explanation_s${pt.sessionIndex}"
            val verdKey = "${matchId}_pressure_verdict_s${pt.sessionIndex}"

            var updated = pt
            overrides[expKey]?.publishedText?.let { updated = updated.copy(explanation = it) }
            overrides[verdKey]?.publishedText?.let { updated = updated.copy(verdict = it) }
            updated
        }

        return original.copy(
            pressurePoints = newPoints,
            highestPressureMoments = newHighest
        )
    }

    /**
     * Applies narrative overrides to TurningPointsDisplayData.
     */
    fun applyTurningPointsOverrides(matchId: Long, original: TurningPointsDisplayData, context: Context?): TurningPointsDisplayData {
        val overrides = getOverridesForMatch(matchId, context)
        if (overrides.isEmpty()) return original

        val newTop = original.topTurningPoints.map { tp ->
            val key = "${matchId}_turning_point_${tp.rank}"
            overrides[key]?.publishedText?.let { tp.copy(description = it) } ?: tp
        }

        val newAll = original.turningPoints.map { tp ->
            val key = "${matchId}_turning_point_${tp.rank}"
            overrides[key]?.publishedText?.let { tp.copy(description = it) } ?: tp
        }

        val newChain = original.matchVerdictChain.map { node ->
            val key = "${matchId}_verdict_node_${node.stepIndex}"
            overrides[key]?.publishedText?.let { node.copy(description = it) } ?: node
        }

        return original.copy(
            topTurningPoints = newTop,
            turningPoints = newAll,
            matchVerdictChain = newChain
        )
    }

    private val inFlightRefreshes = ConcurrentHashMap<Long, Long>()

    /**
     * Asynchronously initiates a non-blocking background refresh for a match's published overrides.
     * Throttled to avoid rapid repeated network calls. Never blocks UI rendering.
     */
    fun triggerBackgroundRefresh(matchId: Long, context: Context?) {
        if (context == null) return
        val now = System.currentTimeMillis()
        val lastRefresh = inFlightRefreshes[matchId] ?: 0L
        if (now - lastRefresh < 4000L) {
            return
        }
        inFlightRefreshes[matchId] = now
        scope.launch {
            try {
                refreshOverridesFromRemote(context, matchId)
            } catch (e: Exception) {
                // Silently swallow errors during background sync — app uses cached or baseline content
            }
        }
    }

    /**
     * Fetches published overrides from local Vite dev bridge (if available) or Firestore REST API,
     * updates disk cache and memory cache, and notifies observers only if content actually changed.
     */
    suspend fun refreshOverridesFromRemote(context: Context, matchId: Long): Boolean {
        // 1. Try local dev bridge endpoints first (supports USB adb reverse & emulator loopback)
        val localEndpoints = listOf(
            "http://127.0.0.1:5173/api/overrides/$matchId",
            "http://10.0.2.2:5173/api/overrides/$matchId"
        )
        for (localEp in localEndpoints) {
            if (fetchFromLocalBridge(context, matchId, localEp)) {
                return true
            }
        }

        val projectId = remoteFirestoreProjectId ?: getStoredProjectId(context) ?: return false

        val endpoint = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/match_narrative_overrides"
        var conn: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode != 200) {
                return false
            }

            val responseStr = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val root = JSONObject(responseStr)
            val docs = root.optJSONArray("documents") ?: return false

            val overridesMap = mutableMapOf<String, PublishedNarrativeOverride>()

            for (i in 0 until docs.length()) {
                val docObj = docs.getJSONObject(i)
                val fields = docObj.optJSONObject("fields") ?: continue

                val docMatchId = fields.optJSONObject("matchId")?.optLong("integerValue", 0L)
                    ?: fields.optJSONObject("matchId")?.optDouble("doubleValue", 0.0)?.toLong() ?: 0L

                if (docMatchId != matchId) continue

                val status = fields.optJSONObject("status")?.optString("stringValue", "") ?: ""
                if (status != "published") continue

                val id = fields.optJSONObject("id")?.optString("stringValue", "") ?: ""
                val text = fields.optJSONObject("publishedText")?.optString("stringValue", "") ?: ""
                val narrativeType = fields.optJSONObject("narrativeType")?.optString("stringValue", "") ?: ""
                val itemKey = fields.optJSONObject("itemKey")?.optString("stringValue", "") ?: ""
                val updatedAt = fields.optJSONObject("updatedAt")?.optString("stringValue", "") ?: ""
                val publishedBy = fields.optJSONObject("publishedBy")?.optString("stringValue", "") ?: ""

                if (id.isNotBlank() && text.isNotBlank()) {
                    overridesMap[id] = PublishedNarrativeOverride(
                        id = id,
                        matchId = matchId,
                        narrativeType = narrativeType,
                        itemKey = itemKey,
                        publishedText = text,
                        updatedAt = updatedAt,
                        publishedBy = publishedBy
                    )
                }
            }

            val currentOverrides = memoryCache[matchId]?.overrides ?: loadFromDisk(context, matchId)?.overrides ?: emptyMap()
            if (currentOverrides != overridesMap) {
                val payload = MatchNarrativeOverrides(matchId, overridesMap, System.currentTimeMillis())
                saveToDisk(context, payload)
                memoryCache[matchId] = payload
                _overrideUpdates.emit(matchId)
            }
            return true
        } catch (e: Exception) {
            return false
        } finally {
            conn?.disconnect()
        }
    }

    private suspend fun fetchFromLocalBridge(context: Context, matchId: Long, endpoint: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(endpoint)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1200
                readTimeout = 1200
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode != 200) {
                return false
            }

            val responseStr = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            if (responseStr.isBlank()) return false
            val root = JSONObject(responseStr)
            val parsed = MatchNarrativeOverrides.fromJson(root)

            val currentOverrides = memoryCache[matchId]?.overrides ?: loadFromDisk(context, matchId)?.overrides ?: emptyMap()
            if (currentOverrides != parsed.overrides) {
                saveToDisk(context, parsed)
                memoryCache[matchId] = parsed
                _overrideUpdates.emit(matchId)
            }
            true
        } catch (e: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Saves a locally authored or test override directly (useful for testing & offline sync).
     */
    fun saveLocalOverride(context: Context, override: PublishedNarrativeOverride) {
        val existing = getOverridesForMatch(override.matchId, context).toMutableMap()
        existing[override.id] = override
        val payload = MatchNarrativeOverrides(override.matchId, existing, System.currentTimeMillis())
        saveToDisk(context, payload)
        memoryCache[override.matchId] = payload
        scope.launch { _overrideUpdates.emit(override.matchId) }
    }

    /**
     * Clears all local overrides for a match, restoring the baseline.
     */
    fun clearLocalOverrides(context: Context, matchId: Long) {
        memoryCache.remove(matchId)
        val file = getOverrideFile(context, matchId)
        if (file.exists()) file.delete()
        scope.launch { _overrideUpdates.emit(matchId) }
    }

    // ── Internal File & Disk Persistence ──────────────────────────────────────────

    private fun getOverrideFile(context: Context, matchId: Long): File {
        val dir = File(context.filesDir, "narrative_overrides")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "match_$matchId.json")
    }

    private fun loadFromDisk(context: Context, matchId: Long): MatchNarrativeOverrides? {
        val file = getOverrideFile(context, matchId)
        if (!file.exists()) return null
        return try {
            val jsonStr = file.readText()
            if (jsonStr.isBlank()) return null
            MatchNarrativeOverrides.fromJson(JSONObject(jsonStr))
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToDisk(context: Context, payload: MatchNarrativeOverrides) {
        try {
            val file = getOverrideFile(context, payload.matchId)
            file.writeText(payload.toJson().toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getStoredProjectId(context: Context): String? {
        return try {
            val sp = context.getSharedPreferences("pitchiq_admin_prefs", Context.MODE_PRIVATE)
            sp.getString("firestore_project_id", null)
        } catch (e: Exception) {
            null
        }
    }
}
