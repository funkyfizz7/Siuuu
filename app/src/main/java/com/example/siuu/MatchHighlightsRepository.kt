package com.example.siuu

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Validated Match Highlights Model
 * Source: warehouse/match_highlights.csv
 */
data class MatchHighlights(
    val matchId: Long,
    val youtubeVideoId: String?,
    val highlightsUrl: String?,
    val thumbnailUrl: String?,
    val sourceName: String?,
    val verified: Boolean,
    val verificationDate: String?,
    val day1Url: String?,
    val day2Url: String?,
    val day3Url: String?,
    val day4Url: String?,
    val day5Url: String?,
    val day1Verified: Boolean = verified,
    val day2Verified: Boolean = false,
    val day3Verified: Boolean = false,
    val day4Verified: Boolean = false,
    val day5Verified: Boolean = false
) {
    /**
     * Resolves the verified target destination URL for main highlights or Day 1.
     * Prefers canonical YouTube watch URL if youtubeVideoId exists, otherwise highlightsUrl.
     */
    val resolvedPlayUrl: String?
        get() = getPlayUrlForDay(1) ?: run {
            if (!verified) return null
            val ytid = youtubeVideoId?.trim()
            if (!ytid.isNullOrEmpty()) {
                return "https://www.youtube.com/watch?v=$ytid"
            }
            val raw = highlightsUrl?.trim()
            if (!raw.isNullOrEmpty() && isValidHttpUrl(raw)) {
                return raw
            }
            null
        }

    /**
     * Fallback thumbnail URL if thumbnail_url in registry is blank or invalid
     */
    val resolvedThumbnailUrl: String?
        get() = getThumbnailUrlForDay(1) ?: run {
            val thumb = thumbnailUrl?.trim()
            if (!thumb.isNullOrEmpty() && isValidHttpUrl(thumb)) {
                return thumb
            }
            val ytid = youtubeVideoId?.trim() ?: extractYouTubeId(resolvedPlayUrl)
            if (!ytid.isNullOrEmpty()) {
                return "https://i.ytimg.com/vi/$ytid/hqdefault.jpg"
            }
            null
        }

    /**
     * Resolves verified playback URL for a specific match day (1..5).
     * If the day is unverified or URL is missing/invalid, returns null.
     */
    fun getPlayUrlForDay(day: Int): String? {
        val (rawUrl, isVer) = when (day) {
            1 -> (day1Url ?: highlightsUrl ?: youtubeVideoId?.let { "https://www.youtube.com/watch?v=$it" }) to day1Verified
            2 -> day2Url to day2Verified
            3 -> day3Url to day3Verified
            4 -> day4Url to day4Verified
            5 -> day5Url to day5Verified
            else -> null to false
        }
        if (!isVer || rawUrl.isNullOrBlank()) return null
        val trimmed = rawUrl.trim()
        val ytid = extractYouTubeId(trimmed)
        if (ytid != null) {
            return "https://www.youtube.com/watch?v=$ytid"
        }
        return if (isValidHttpUrl(trimmed)) trimmed else null
    }

    /**
     * Resolves thumbnail URL for a specific match day (1..5).
     */
    fun getThumbnailUrlForDay(day: Int): String? {
        if (day == 1) {
            val thumb = thumbnailUrl?.trim()
            if (!thumb.isNullOrEmpty() && isValidHttpUrl(thumb)) {
                return thumb
            }
        }
        val playUrl = getPlayUrlForDay(day) ?: return null
        val ytid = extractYouTubeId(playUrl)
        if (ytid != null) {
            return "https://i.ytimg.com/vi/$ytid/hqdefault.jpg"
        }
        return null
    }

    /**
     * Checks if a specific day (1..5) has an individually verified, playable video.
     */
    fun isDayAvailable(day: Int): Boolean {
        return getPlayUrlForDay(day) != null
    }

    companion object {
        fun isValidHttpUrl(url: String?): Boolean {
            if (url.isNullOrBlank()) return false
            val trimmed = url.trim()
            return trimmed.startsWith("http://", ignoreCase = true) ||
                   trimmed.startsWith("https://", ignoreCase = true)
        }

        fun extractYouTubeId(url: String?): String? {
            if (url.isNullOrBlank()) return null
            val regex = Regex("""(?:v=|youtu\.be/|embed/|vi/)([a-zA-Z0-9_-]{11})""")
            return regex.find(url)?.groupValues?.get(1)
        }
    }
}

/**
 * Repository interface for match highlights
 */
interface MatchHighlightsRepository {
    suspend fun getMatchHighlights(matchId: Long): MatchHighlights?
}

/**
 * Default implementation parsing warehouse/match_highlights.csv with strict verification rules.
 */
class DefaultMatchHighlightsRepository(
    private val context: Context? = null
) : MatchHighlightsRepository {

    private val cache = mutableMapOf<Long, MatchHighlights>()
    private var isLoaded = false

    private fun getContext(): Context? {
        return context ?: _appContext
    }

    private fun safeLog(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            // Ignored in unit test environments
        }
    }

    override suspend fun getMatchHighlights(matchId: Long): MatchHighlights? = withContext(Dispatchers.IO) {
        if (!isLoaded) {
            loadHighlights()
        }
        val result = cache[matchId]
        safeLog("PitchIQHighlights", "getMatchHighlights($matchId): found=${result != null}, verified=${result?.verified}, playUrl=${result?.resolvedPlayUrl}")
        result
    }

    @Synchronized
    fun loadFromLines(lines: List<Map<String, String>>) {
        cache.clear()
        for (row in lines) {
            val mId = (row["match_id"] ?: row["\uFEFFmatch_id"])?.trim()?.toLongOrNull() ?: continue
            // Reject duplicates deterministically (keep first verified record or first entry)
            if (cache.containsKey(mId)) {
                continue
            }

            val rawVerified = row["verified"]?.trim()
            val isVerified = rawVerified.equals("TRUE", ignoreCase = true) || rawVerified.equals("1")
            val ytid = row["youtube_video_id"]?.trim()?.takeIf { it.isNotEmpty() }
            val rawHighlightUrl = row["highlights_url"]?.trim()?.takeIf { it.isNotEmpty() }
            val thumbUrl = row["thumbnail_url"]?.trim()?.takeIf { it.isNotEmpty() }

            // Validate URLs
            val validHighlightUrl = if (MatchHighlights.isValidHttpUrl(rawHighlightUrl)) rawHighlightUrl else null
            val validThumbUrl = if (MatchHighlights.isValidHttpUrl(thumbUrl)) thumbUrl else null

            fun parseDayVerified(col: String, defaultVal: Boolean): Boolean {
                val raw = row[col]?.trim()
                if (!raw.isNullOrEmpty()) {
                    return raw.equals("TRUE", ignoreCase = true) || raw.equals("1")
                }
                return defaultVal
            }

            fun getValidDayUrl(col: String): String? {
                val raw = row[col]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
                return if (MatchHighlights.isValidHttpUrl(raw)) raw else null
            }

            val d1Ver = parseDayVerified("day1_verified", defaultVal = isVerified)
            val d2Ver = parseDayVerified("day2_verified", defaultVal = false)
            val d3Ver = parseDayVerified("day3_verified", defaultVal = false)
            val d4Ver = parseDayVerified("day4_verified", defaultVal = false)
            val d5Ver = parseDayVerified("day5_verified", defaultVal = false)

            val highlights = MatchHighlights(
                matchId = mId,
                youtubeVideoId = ytid,
                highlightsUrl = validHighlightUrl,
                thumbnailUrl = validThumbUrl,
                sourceName = row["source_name"]?.trim()?.takeIf { it.isNotEmpty() },
                verified = isVerified,
                verificationDate = row["verification_date"]?.trim()?.takeIf { it.isNotEmpty() },
                day1Url = getValidDayUrl("day1_url"),
                day2Url = getValidDayUrl("day2_url"),
                day3Url = getValidDayUrl("day3_url"),
                day4Url = getValidDayUrl("day4_url"),
                day5Url = getValidDayUrl("day5_url"),
                day1Verified = d1Ver,
                day2Verified = d2Ver,
                day3Verified = d3Ver,
                day4Verified = d4Ver,
                day5Verified = d5Ver
            )
            cache[mId] = highlights
            safeLog("PitchIQHighlights", "Loaded record for match $mId: verified=$isVerified, ytid=$ytid, playUrl=${highlights.resolvedPlayUrl}")
        }
        isLoaded = true
    }

    @Synchronized
    private fun loadHighlights() {
        if (isLoaded) return
        val ctx = getContext() ?: return
        try {
            val rows = AssetWarehouseLoader.loadCsv(ctx, "match_highlights.csv")
            loadFromLines(rows)
        } catch (e: Exception) {
            e.printStackTrace()
            isLoaded = true
        }
    }

    companion object {
        private var _appContext: Context? = null

        fun initialize(context: Context) {
            _appContext = context.applicationContext
        }

        val instance: MatchHighlightsRepository by lazy {
            DefaultMatchHighlightsRepository(_appContext)
        }

        fun getInstance(context: Context): MatchHighlightsRepository {
            initialize(context)
            return instance
        }
    }
}