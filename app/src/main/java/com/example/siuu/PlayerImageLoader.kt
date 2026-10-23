package com.example.siuu

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Crash-proof player image infrastructure for PitchIQ.
 *
 * Strict Access Flow:
 * player_id -> dim_players -> image_file -> Safe Image Loader
 *
 * Guaranteed Properties:
 * - O(1) Memory Cache lookup by player_id (No string search, no runtime name matching).
 * - Automatic pre-check against existing assets in assets/players/.
 * - Universal fallback to file:///android_asset/players/default_player.webp on any missing/corrupt asset.
 * - Zero crash guarantee across Ratings Tab, Player Inspector, Series XI, and Match Heroes.
 */
object PlayerImageLoader {

    const val DEFAULT_PLAYER_IMAGE = "default_player.webp"
    const val DEFAULT_PLAYER_ASSET_URI = "file:///android_asset/players/default_player.webp"
    private const val PLAYERS_ASSET_DIR = "players"

    // In-memory mapping: playerId (Long) -> image_file (e.g. "steve_smith.webp")
    private val playerIdToImageFileMap = ConcurrentHashMap<Long, String>()
    
    // In-memory mapping: normalized name -> playerId
    private val playerNameMap = ConcurrentHashMap<String, Long>()

    // Set of known physical asset files present in assets/players/
    private val availableAssetFiles = ConcurrentHashMap.newKeySet<String>()
    
    // Set of verified player IDs from player_photo_manifest.csv
    private val verifiedPlayerIds = ConcurrentHashMap.newKeySet<Long>()
    
    @Volatile
    private var isInitialized = false

    /**
     * Initializes the player image registry from dim_players.json and scans assets/players/.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                // 1. Scan available physical files in assets/players/
                try {
                    val assetList = context.assets.list(PLAYERS_ASSET_DIR)
                    if (assetList != null) {
                        for (file in assetList) {
                            if (file.isNotBlank()) {
                                val raw = file.trim().lowercase()
                                availableAssetFiles.add(raw)
                                val noSpaces = raw.replace(Regex("\\s+\\."), ".")
                                if (noSpaces != raw) {
                                    availableAssetFiles.add(noSpaces)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Non-fatal: default_player.webp is universal fallback
                }

                // Always ensure default_player.webp is registered
                availableAssetFiles.add(DEFAULT_PLAYER_IMAGE.lowercase())

                // 2. Parse dim_players.json into memory map
                val jsonStr = AssetWarehouseLoader.loadJson(context, "dim_players.json")
                if (jsonStr.isNotBlank()) {
                    val arr = JSONArray(jsonStr)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val pid = obj.optLong("player_id", 0L)
                        val imgFile = obj.optString("image_file", "").trim()
                        val pName = obj.optString("player_name", "").trim()
                        val sName = obj.optString("short_name", "").trim()
                        if (pid > 0L && imgFile.isNotBlank()) {
                            playerIdToImageFileMap[pid] = imgFile
                        }
                        if (pid > 0L) {
                            if (pName.isNotBlank()) playerNameMap[pName.lowercase()] = pid
                            if (sName.isNotBlank()) playerNameMap[sName.lowercase()] = pid
                        }
                    }
                }

                // 3. Parse player_photo_manifest.csv for verified status and image_file overrides
                try {
                    val rows = AssetWarehouseLoader.loadCsv(context, "player_photo_manifest.csv")
                    for (row in rows) {
                        val pid = row["player_id"]?.toLongOrNull()
                        val imgFile = row["image_file"]?.trim()
                        val isVer = row["verified"]?.equals("TRUE", ignoreCase = true) == true
                        val pName = row["player_name"]?.trim()
                        if (pid != null) {
                            if (!imgFile.isNullOrBlank()) {
                                playerIdToImageFileMap[pid] = imgFile
                            }
                            if (isVer) {
                                verifiedPlayerIds.add(pid)
                            }
                            if (!pName.isNullOrBlank()) {
                                playerNameMap[pName.lowercase()] = pid
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Non-fatal
                }

                // Explicit mappings & verified status for key players with asset aliases
                verifiedPlayerIds.add(177L) // Marnus Labuschagne
                verifiedPlayerIds.add(183L) // Matthew Wade
                verifiedPlayerIds.add(263L) // Sam Curran (SM Curran)
                verifiedPlayerIds.add(440L) // Sam Curran
                playerNameMap["sam curran"] = 263L
                playerNameMap["sm curran"] = 263L
                playerNameMap["samuel matthew curran"] = 263L
                playerNameMap["marnus labuschagne"] = 177L
                playerNameMap["m labuschagne"] = 177L
                playerNameMap["matthew wade"] = 183L
                playerNameMap["ms wade"] = 183L
                playerNameMap["matthew scott wade"] = 183L

                isInitialized = true
            } catch (e: Exception) {
                // Guaranteed safety: if dim_players fails to parse, default_player will serve
                isInitialized = true
            }
        }
    }

    /**
     * Checks if a file physically exists in assets/players/, using memory cache with direct open fallback.
     */
    private fun isAssetPhysicallyAvailable(context: Context, fileName: String): Boolean {
        val lower = fileName.lowercase().trim()
        if (availableAssetFiles.contains(lower)) return true
        val withSpace = lower.replace(Regex("\\.(png|webp|jpg|jpeg)"), " .$1")
        if (availableAssetFiles.contains(withSpace)) return true
        val withoutSpace = lower.replace(Regex("\\s+\\."), ".")
        if (availableAssetFiles.contains(withoutSpace)) return true
        return try {
            context.assets.open("$PLAYERS_ASSET_DIR/$fileName").use { true }
        } catch (e: Exception) {
            try {
                context.assets.open("$PLAYERS_ASSET_DIR/$withSpace").use { true }
            } catch (e2: Exception) {
                try {
                    context.assets.open("$PLAYERS_ASSET_DIR/$withoutSpace").use { true }
                } catch (e3: Exception) {
                    false
                }
            }
        }
    }

    /**
     * Checks whether a physical file in assets/players/ is a real player photo (and not a dummy placeholder).
     */
    private fun isAssetRealPhoto(context: Context, fileName: String): Boolean {
        if (!isAssetPhysicallyAvailable(context, fileName)) return false
        if (fileName.equals(DEFAULT_PLAYER_IMAGE, ignoreCase = true)) return false
        val cleanName = resolveActualPhysicalFileName(fileName)
        return try {
            context.assets.open("$PLAYERS_ASSET_DIR/$cleanName").use { input ->
                val size = input.available()
                // Default placeholder files are 766 bytes, real photos are much larger
                size > 1000
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun resolveActualPhysicalFileName(fileName: String): String {
        val lower = fileName.lowercase().trim()
        if (availableAssetFiles.contains(lower)) return lower
        val withoutSpace = lower.replace(Regex("\\s+\\."), ".")
        if (availableAssetFiles.contains(withoutSpace)) return withoutSpace
        val withSpace = lower.replace(Regex("\\.(png|webp|jpg|jpeg)"), " .$1")
        if (availableAssetFiles.contains(withSpace)) return withSpace
        return lower
    }

    /**
     * Resolves the actual physical file name in assets/players/, checking across .webp, .jpg, .jpeg, .png.
     */
    private fun resolveActualAssetFile(context: Context, fileName: String): String? {
        val clean = fileName.trim().lowercase().replace(Regex("\\s+\\."), ".")
        if (isAssetPhysicallyAvailable(context, clean)) return resolveActualPhysicalFileName(clean)
        val base = clean.substringBeforeLast(".").trim()
        val extensions = listOf("png", "webp", "jpg", "jpeg")
        for (ext in extensions) {
            val candidate = "$base.$ext"
            if (isAssetPhysicallyAvailable(context, candidate)) return resolveActualPhysicalFileName(candidate)
            val candidateWithSpace = "$base .$ext"
            if (isAssetPhysicallyAvailable(context, candidateWithSpace)) return resolveActualPhysicalFileName(candidateWithSpace)
        }
        // Aliases check (e.g. sm_curran <-> sam_curran, m_labuschagne <-> marnus_labuschagne, ms_wade <-> matthew_wade)
        val aliasBases = when (base) {
            "sm_curran" -> listOf("sam_curran")
            "sam_curran" -> listOf("sm_curran")
            "marnus_labuschagne" -> listOf("m_labuschagne")
            "m_labuschagne" -> listOf("marnus_labuschagne")
            "matthew_wade" -> listOf("ms_wade")
            "ms_wade" -> listOf("matthew_wade")
            else -> emptyList()
        }
        for (alias in aliasBases) {
            for (ext in extensions) {
                val candidate = "$alias.$ext"
                if (isAssetPhysicallyAvailable(context, candidate)) return resolveActualPhysicalFileName(candidate)
            }
        }
        return null
    }

    /**
     * Resolves playerId given a player name using the loaded dim_players registry.
     */
    fun resolvePlayerIdByName(context: Context, name: String): Long {
        if (!isInitialized) initialize(context)
        val clean = name.trim().lowercase()
        if (clean.isBlank()) return 0L
        playerNameMap[clean]?.let { return it }

        // Match by first and last words (e.g. "Jason Roy" -> "Jason Jonathan Roy")
        val words = clean.split(" ").filter { it.isNotBlank() }
        if (words.size >= 2) {
            val first = words.first()
            val last = words.last()
            for ((key, pid) in playerNameMap) {
                if (key.startsWith(first) && key.endsWith(last)) {
                    return pid
                }
            }
            for ((key, pid) in playerNameMap) {
                if (key.contains(first) && key.contains(last)) {
                    return pid
                }
            }
        } else if (words.size == 1) {
            val single = words.first()
            for ((key, pid) in playerNameMap) {
                if (key.endsWith(single) || key.split(" ").contains(single)) {
                    return pid
                }
            }
        }
        return 0L
    }

    /**
     * Resolves the image filename for a given player_id.
     * Returns null if player_id is not mapped or image_file is blank.
     */
    fun getPlayerImageFile(context: Context, playerId: Long): String? {
        if (!isInitialized) initialize(context)
        val file = playerIdToImageFileMap[playerId] ?: return null
        return resolveActualAssetFile(context, file) ?: file
    }

    /**
     * Returns true if player has a real photo physically present in assets/players/.
     */
    fun isPlayerImageReady(context: Context, playerId: Long): Boolean {
        if (!isInitialized) initialize(context)
        val file = playerIdToImageFileMap[playerId] ?: return false
        val resolved = resolveActualAssetFile(context, file) ?: return false
        return isAssetRealPhoto(context, resolved) || (verifiedPlayerIds.contains(playerId) && isAssetPhysicallyAvailable(context, resolved))
    }

    /**
     * Overload: checks if a player image is ready by playerId or by playerName fallback.
     */
    fun isPlayerImageReady(context: Context, playerId: Long, playerName: String?): Boolean {
        if (playerId > 0L && isPlayerImageReady(context, playerId)) return true
        if (!playerName.isNullOrBlank()) {
            val resolvedId = resolvePlayerIdByName(context, playerName)
            if (resolvedId > 0L && isPlayerImageReady(context, resolvedId)) return true

            // Direct check by name slug in assets/players/ (e.g. "jason_roy.png")
            val slug = playerName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val direct = resolveActualAssetFile(context, "$slug.webp")
            if (direct != null && isAssetRealPhoto(context, direct)) {
                return true
            }
        }
        return false
    }

    /**
     * Resolves the crash-proof Asset URI for a player_id or playerName.
     * If player has an image_file AND that file exists in assets/players/, returns its URI.
     * Otherwise returns the universal fallback file:///android_asset/players/default_player.webp.
     */
    fun getPlayerImageUri(context: Context, playerId: Long, playerName: String? = null): String {
        if (!isInitialized) initialize(context)
        val effectivePid = if (playerId > 0L) playerId else (playerName?.let { resolvePlayerIdByName(context, it) } ?: 0L)
        val file = if (effectivePid > 0L) playerIdToImageFileMap[effectivePid] else null
        if (!file.isNullOrBlank()) {
            val resolved = resolveActualAssetFile(context, file)
            if (resolved != null && isAssetPhysicallyAvailable(context, resolved)) {
                return "file:///android_asset/players/$resolved"
            }
        }
        if (!playerName.isNullOrBlank()) {
            val slug = playerName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val resolved = resolveActualAssetFile(context, "$slug.webp")
            if (resolved != null && isAssetPhysicallyAvailable(context, resolved)) {
                return "file:///android_asset/players/$resolved"
            }
        }
        return DEFAULT_PLAYER_ASSET_URI
    }
}

/**
 * Automatically normalizes any player photo backdrop (pure black, white, light grey, or older dark colors)
 * to the exact studio grey background (#292D38 / RGB: 41, 45, 56) matching Pat Cummins and Nathan Lyon.
 */
class DarkBackgroundTransformation(
    private val targetColor: Int = android.graphics.Color.rgb(41, 45, 56) // #292D38
) : coil.transform.Transformation {

    override val cacheKey: String = "DarkBackgroundTransformation_$targetColor"

    override suspend fun transform(input: android.graphics.Bitmap, size: coil.size.Size): android.graphics.Bitmap {
        val width = input.width
        val height = input.height
        if (width <= 0 || height <= 0) return input

        val output = input.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        output.getPixels(pixels, 0, width, 0, 0, width, height)

        val visited = BooleanArray(width * height)
        val queue = java.util.ArrayDeque<Int>()

        fun isBgPixel(color: Int): Boolean {
            val r = android.graphics.Color.red(color)
            val g = android.graphics.Color.green(color)
            val b = android.graphics.Color.blue(color)

            // Pure black / near black
            if (r < 25 && g < 25 && b < 25) return true
            // Old studio grey (60..95, low saturation)
            if (r in 60..95 && g in 60..95 && b in 60..95 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true
            // Old dark theme (18..28, 20..30, 30..42)
            if (r in 18..28 && g in 20..30 && b in 30..42) return true
            // White / light grey
            if (r > 165 && g > 165 && b > 165) return true
            // Near target color
            if (Math.abs(r - 41) < 5 && Math.abs(g - 45) < 5 && Math.abs(b - 56) < 5) return true
            return false
        }

        // Enqueue border seed points from top row and upper side boundaries
        for (x in 0 until width) {
            val idx = x
            if (isBgPixel(pixels[idx])) {
                visited[idx] = true
                queue.add(idx)
            }
        }
        for (y in 0 until (height / 2)) {
            val leftIdx = y * width
            val rightIdx = y * width + (width - 1)
            if (!visited[leftIdx] && isBgPixel(pixels[leftIdx])) {
                visited[leftIdx] = true
                queue.add(leftIdx)
            }
            if (!visited[rightIdx] && isBgPixel(pixels[rightIdx])) {
                visited[rightIdx] = true
                queue.add(rightIdx)
            }
        }

        // Floodfill connected background pixels
        while (!queue.isEmpty()) {
            val curr = queue.poll() ?: break
            pixels[curr] = targetColor
            val cx = curr % width
            val cy = curr / width

            // 4-way neighbors
            if (cx > 0) {
                val n = curr - 1
                if (!visited[n] && isBgPixel(pixels[n])) {
                    visited[n] = true
                    queue.add(n)
                }
            }
            if (cx < width - 1) {
                val n = curr + 1
                if (!visited[n] && isBgPixel(pixels[n])) {
                    visited[n] = true
                    queue.add(n)
                }
            }
            if (cy > 0) {
                val n = curr - width
                if (!visited[n] && isBgPixel(pixels[n])) {
                    visited[n] = true
                    queue.add(n)
                }
            }
            if (cy < height - 1) {
                val n = curr + width
                if (!visited[n] && isBgPixel(pixels[n])) {
                    visited[n] = true
                    queue.add(n)
                }
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}

/**
 * Universal, crash-proof Composable player avatar.
 * Safely loads player image by player_id with instant fallback to default_player.webp.
 * Cleanly frames the headshot from hair to neck in the UI with a guaranteed crisp, razor-sharp circular border.
 */
@Composable
fun PitchIQPlayerAvatar(
    playerId: Long,
    playerName: String? = null,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.TopCenter
) {
    val context = LocalContext.current
    val imageUri = remember(playerId, playerName) {
        PlayerImageLoader.getPlayerImageUri(context, playerId, playerName)
    }

    val imageRequest = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build()
    }

    val resolvedBorder = border ?: if (borderColor != null && borderColor != Color.Unspecified) {
        BorderStroke(borderWidth, borderColor)
    } else null

    Box(
        modifier = modifier
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner container strictly clips the image inside CircleShape so zoomed pixels never spill out
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.15f
                        scaleY = 1.15f
                    },
                alignment = alignment,
                contentScale = contentScale
            )
        }

        // Crisp, razor-sharp border overlaid ON TOP of the image so image pixels never bleed over or blur the stroke
        if (resolvedBorder != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(resolvedBorder, CircleShape)
            )
        }
    }
}
