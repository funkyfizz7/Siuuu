package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchFieldBg = Color(0xFF0A0D14)
private val PitchFieldBorder = Color(0xFF1F2633)
private val PitchFieldLine = Color(0x18FFFFFF)
private val PitchCardInnerBg = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchAccentBlue = Color(0xFF00A3FF)
private val PitchModerate = Color(0xFFFF9F1C)
private val PitchNegative = Color(0xFFEF4444)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Thin),
    Font(R.font.bree_serif, FontWeight.ExtraLight),
    Font(R.font.bree_serif, FontWeight.Light),
    Font(R.font.bree_serif, FontWeight.Normal),
    Font(R.font.bree_serif, FontWeight.Medium),
    Font(R.font.bree_serif, FontWeight.SemiBold),
    Font(R.font.bree_serif, FontWeight.Bold),
    Font(R.font.bree_serif, FontWeight.ExtraBold),
    Font(R.font.bree_serif, FontWeight.Black)
)

fun getPitchIQRatingColor(rating: Double): Color = when {
    rating > 8.5 -> Color(0xFF00A3FF) // Blue (> 8.5)
    rating >= 7.0 -> Color(0xFF22C55E) // Green (<= 8.5, >= 7.0)
    rating > 5.0 -> Color(0xFFF59E0B) // Yellowish (<= 7.0, > 5.0)
    else -> Color(0xFFEF4444)         // Red (<= 5.0)
}

enum class RatingsTeamSelection { TEAM1, TEAM2 }
enum class RatingsViewTab { FIELD, LIST, IMPACT }

/**
 * Ratings UI State Representation (FutureBuilder pattern in Jetpack Compose)
 */
sealed interface RatingsUiState {
    object Loading : RatingsUiState
    data class Success(val data: RatingsTabDisplayData) : RatingsUiState
    data class Error(val message: String) : RatingsUiState
}

/**
 * Reusable Match Ratings Tab
 * Sources:
 * - fact_player_match_ratings
 * - fact_player_match_impact
 *
 * Generates:
 * - Field View (FotMob formation layout)
 * - Player Ratings (Full roster list view)
 * - MVP Card (Top Match MVP performer)
 * - Impact Rankings (Ranked #1 to #10 by impact)
 *
 * No hardcoded player names.
 */
@Composable
fun PlayerRatingsTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }

    val uiState by produceState<RatingsUiState>(
        initialValue = RatingsUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = RatingsUiState.Loading
        try {
            val data = repository.getRatingsTabData(matchId)
            value = RatingsUiState.Success(data)
        } catch (e: Exception) {
            value = RatingsUiState.Error(e.localizedMessage ?: "Failed to load player ratings. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is RatingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PitchPositive,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading Match Ratings...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is RatingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = PitchNegative,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to load player ratings",
                                color = PitchPrimaryText,
                                fontSize = 16.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { retryTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = PitchPositive)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = PitchBlackBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Retry",
                                        color = PitchBlackBg,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is RatingsUiState.Success -> {
                RatingsContent(data = state.data, onNavigateToPlayer = onNavigateToPlayer)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun PlayerRatingsTabScreen(
    matchId: Int,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PlayerRatingsTabScreen(
        matchId = matchId.toLong(),
        onNavigateToPlayer = onNavigateToPlayer,
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun PlayerRatingsTabScreen(
    matchData: MatchIntelligenceData,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PlayerRatingsTabScreen(
        matchId = matchData.matchId,
        onNavigateToPlayer = onNavigateToPlayer,
        modifier = modifier
    )
}

@Composable
private fun RatingsContent(
    data: RatingsTabDisplayData,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTeam by remember { mutableStateOf(RatingsTeamSelection.TEAM1) }
    var viewTab by remember { mutableStateOf(RatingsViewTab.FIELD) }

    val activePlayers = if (selectedTeam == RatingsTeamSelection.TEAM1) data.team1Players else data.team2Players

    var selectedPlayer by remember(selectedTeam, viewTab) {
        mutableStateOf<PlayerMatchRatingModel?>(null)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 48.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: MVP Card (Dynamic from fact_player_match_ratings & fact_player_match_impact)
        item {
            val mvp = data.mvpPlayer
            val mvpRatingColor = getPitchIQRatingColor(mvp.overallRating)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchPositive.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        selectedPlayer = mvp
                        val pid = mvp.playerId?.toLongOrNull() ?: 0L
                        if (pid > 0L) {
                            onNavigateToPlayer?.invoke(pid)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = PitchPositive,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MATCH MVP",
                                    color = PitchPositive,
                                    fontSize = 11.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, mvpRatingColor)
                        ) {
                            Text(
                                text = "Rating %.1f".format(mvp.overallRating),
                                color = mvpRatingColor,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = mvp.playerName,
                        color = PitchPrimaryText,
                        fontSize = 25.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${mvp.role.uppercase()} • ${mvp.team.uppercase()}",
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 MVP Stat Highlights
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RatingMetric("BATTING", if (mvp.battingRating > 0.0) "%.1f".format(mvp.battingRating) else "-", PitchPrimaryText)
                        RatingMetric("BOWLING", if (mvp.bowlingRating > 0.0) "%.1f".format(mvp.bowlingRating) else "-", PitchPrimaryText)
                        RatingMetric("IMPACT", "%.1f".format(mvp.impactRating), PitchPositive)
                        RatingMetric("OVERALL", "%.1f".format(mvp.overallRating), mvpRatingColor)
                    }
                }
            }
        }

        // Section 2: View Switcher (Field View, Impact Ratings)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // View Mode Tabs: [ Field View ] [ Impact Ratings ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ViewModeTabButton(
                        label = "Field View",
                        isSelected = viewTab == RatingsViewTab.FIELD,
                        modifier = Modifier.weight(1f),
                        onClick = { viewTab = RatingsViewTab.FIELD }
                    )
                    ViewModeTabButton(
                        label = "Impact Ratings",
                        isSelected = viewTab == RatingsViewTab.IMPACT,
                        modifier = Modifier.weight(1f),
                        onClick = { viewTab = RatingsViewTab.IMPACT }
                    )
                }

                // Team Switcher Pills (only for Field view)
                if (viewTab == RatingsViewTab.FIELD) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TeamTabPill(
                            label = "${data.header.team1ShortName} (XI)",
                            isSelected = selectedTeam == RatingsTeamSelection.TEAM1,
                            activeColor = data.header.team1Color,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTeam = RatingsTeamSelection.TEAM1 }
                        )
                        TeamTabPill(
                            label = "${data.header.team2ShortName} (XI)",
                            isSelected = selectedTeam == RatingsTeamSelection.TEAM2,
                            activeColor = data.header.team2Color,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTeam = RatingsTeamSelection.TEAM2 }
                        )
                    }
                }
            }
        }

        // Section 3: Field View / Impact Ratings
        item {
            when (viewTab) {
                RatingsViewTab.FIELD -> {
                    FotMobDynamicPitchField(
                        players = activePlayers,
                        mvpPlayerName = data.mvpPlayer.playerName,
                        selectedPlayer = selectedPlayer,
                        onPlayerSelected = { player ->
                            selectedPlayer = player
                            val pid = player.playerId?.toLongOrNull() ?: 0L
                            if (pid > 0L) {
                                onNavigateToPlayer?.invoke(pid)
                            }
                        }
                    )
                }

                RatingsViewTab.LIST,
                RatingsViewTab.IMPACT -> {
                    DynamicImpactRankingsList(
                        rankedImpacts = data.impactRankings,
                        allRatings = data.team1Players + data.team2Players,
                        selectedPlayer = selectedPlayer,
                        onPlayerSelected = { player ->
                            selectedPlayer = player
                            val pid = player.playerId?.toLongOrNull() ?: 0L
                            if (pid > 0L) {
                                onNavigateToPlayer?.invoke(pid)
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FotMobDynamicPitchField(
    players: List<PlayerMatchRatingModel>,
    mvpPlayerName: String,
    selectedPlayer: PlayerMatchRatingModel?,
    onPlayerSelected: (PlayerMatchRatingModel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchFieldBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchFieldBorder, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp)
        ) {
            // Field Lines Overlay
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    color = PitchFieldLine,
                    radius = w * 0.42f,
                    center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                val pitchW = 44.dp.toPx()
                val pitchH = h * 0.55f
                drawRect(
                    color = Color(0x0CFFFFFF),
                    topLeft = androidx.compose.ui.geometry.Offset((w - pitchW) / 2f, (h - pitchH) / 2f),
                    size = androidx.compose.ui.geometry.Size(pitchW, pitchH)
                )
                drawRect(
                    color = Color(0x20FFFFFF),
                    topLeft = androidx.compose.ui.geometry.Offset((w - pitchW) / 2f, (h - pitchH) / 2f),
                    size = androidx.compose.ui.geometry.Size(pitchW, pitchH),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }

            // 4 Formation Rows (Split 11 players into 2 - 3 - 2 - 4)
            val tier1 = players.take(2)
            val tier2 = players.drop(2).take(3)
            val tier3 = players.drop(5).take(2)
            val tier4 = players.drop(7)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Tier 1: Openers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    tier1.forEach { player ->
                        FotMobDynamicPlayerNode(player, player.playerName == mvpPlayerName, selectedPlayer?.playerName == player.playerName) { onPlayerSelected(player) }
                    }
                }

                // Tier 2: Top Order
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    tier2.forEach { player ->
                        FotMobDynamicPlayerNode(player, player.playerName == mvpPlayerName, selectedPlayer?.playerName == player.playerName) { onPlayerSelected(player) }
                    }
                }

                // Tier 3: Middle Order
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    tier3.forEach { player ->
                        FotMobDynamicPlayerNode(player, player.playerName == mvpPlayerName, selectedPlayer?.playerName == player.playerName) { onPlayerSelected(player) }
                    }
                }

                // Tier 4: Bowlers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    tier4.forEach { player ->
                        FotMobDynamicPlayerNode(player, player.playerName == mvpPlayerName, selectedPlayer?.playerName == player.playerName) { onPlayerSelected(player) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FotMobDynamicPlayerNode(
    player: PlayerMatchRatingModel,
    isMvp: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ratingColor = getPitchIQRatingColor(player.overallRating)
    val pid = player.playerId?.toLongOrNull() ?: 0L
    val context = androidx.compose.ui.platform.LocalContext.current
    val isImageReady = remember(pid, player.playerName) { PlayerImageLoader.isPlayerImageReady(context, pid, player.playerName) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 68.dp, max = 94.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            val avatarBorderWidth = if (isSelected) 2.dp else 1.dp
            val avatarBorderColor = if (isSelected) ratingColor else Color(0xFF2D3546)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF22283A) else Color(0xFF292D38)),
                contentAlignment = Alignment.Center
            ) {
                if (pid > 0 && isImageReady) {
                    PitchIQPlayerAvatar(
                        playerId = pid,
                        playerName = player.playerName,
                        contentDescription = player.playerName,
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(avatarBorderWidth, avatarBorderColor),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = player.playerName.split(" ").lastOrNull()?.take(2)?.uppercase() ?: player.playerName.take(2).uppercase(),
                        color = PitchPrimaryText,
                        fontSize = 14.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(avatarBorderWidth, avatarBorderColor, CircleShape)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ratingColor,
                modifier = Modifier.offset(x = 8.dp, y = (-5).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%.1f".format(player.overallRating),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    if (isMvp) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        val lastName = player.playerName.split(" ").lastOrNull() ?: player.playerName
        val labelText = if (player.jerseyNumber != null && player.jerseyNumber > 0) {
            "${player.jerseyNumber} $lastName"
        } else {
            lastName
        }

        Text(
            text = labelText.trim(),
            color = PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun DynamicPlayerRatingsList(
    players: List<PlayerMatchRatingModel>,
    mvpPlayerName: String,
    selectedPlayer: PlayerMatchRatingModel?,
    onPlayerSelected: (PlayerMatchRatingModel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            players.forEach { player ->
                val isSelected = selectedPlayer?.playerName == player.playerName
                val ratingColor = getPitchIQRatingColor(player.overallRating)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(if (isSelected) PitchCardInnerBg else PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (isSelected) ratingColor.copy(alpha = 0.6f) else PitchCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onPlayerSelected(player) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder)
                        ) {
                            Text(
                                text = if (player.jerseyNumber != null && player.jerseyNumber > 0) "${player.jerseyNumber}" else "-",
                                color = PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.playerName,
                                    color = PitchPrimaryText,
                                    fontSize = 14.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1
                                )
                                if (player.playerName == mvpPlayerName) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "(MoM)", color = PitchPositive, fontSize = 11.sp, fontFamily = BreeSerifFontFamily)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${player.role} • ${player.primaryHighlight}",
                                color = PitchSecondaryText,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ratingColor
                    ) {
                        Text(
                            text = "%.1f".format(player.overallRating),
                            color = Color.Black,
                            fontSize = 13.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicImpactRankingsList(
    rankedImpacts: List<PlayerMatchImpactModel>,
    allRatings: List<PlayerMatchRatingModel>,
    selectedPlayer: PlayerMatchRatingModel?,
    onPlayerSelected: (PlayerMatchRatingModel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "MATCH IMPACT RANKINGS",
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            rankedImpacts.take(10).forEachIndexed { index, impactItem ->
                val matchingPlayer = allRatings.firstOrNull { it.playerName == impactItem.playerName }
                val isSelected = selectedPlayer?.playerName == impactItem.playerName
                val ratingColor = getPitchIQRatingColor(impactItem.impactScore)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(if (isSelected) PitchCardInnerBg else PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (isSelected) ratingColor.copy(alpha = 0.6f) else PitchCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { matchingPlayer?.let { onPlayerSelected(it) } }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (index < 3) PitchPositive.copy(alpha = 0.15f) else PitchCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (index < 3) PitchPositive else PitchCardBorder)
                        ) {
                            Text(
                                text = "#${impactItem.impactRank}",
                                color = if (index < 3) PitchPositive else PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = impactItem.playerName,
                                color = PitchPrimaryText,
                                fontSize = 14.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${impactItem.team} • ${impactItem.role} • ${impactItem.headlineImpact}",
                                color = PitchSecondaryText,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ratingColor
                    ) {
                        Text(
                            text = "Impact %.1f".format(impactItem.impactScore),
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicPlayerInspectorCard(
    player: PlayerMatchRatingModel,
    isMvp: Boolean,
    onInspectClick: (() -> Unit)? = null
) {
    val ratingColor = getPitchIQRatingColor(player.overallRating)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ratingColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Player Name + Overall Rating + Innings Breakdown Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.playerName,
                            color = PitchPrimaryText,
                            fontSize = 20.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        if (isMvp) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PitchPositive.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                            ) {
                                Text(
                                    text = "★ MoM",
                                    color = PitchPositive,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${player.role.uppercase()} • ${player.team.uppercase()}",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onInspectClick != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PitchAccentBlue.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchAccentBlue),
                            modifier = Modifier
                                .clickable { onInspectClick() }
                                .padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Innings ▶",
                                color = PitchAccentBlue,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PitchBlackBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ratingColor)
                    ) {
                        Text(
                            text = "Rating %.1f".format(player.overallRating),
                            color = ratingColor,
                            fontSize = 14.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Advanced PitchIQ Ratings Grid (fact_player_match_ratings)
            Text(
                text = "PITCHIQ ADVANCED RATINGS (FACT_PLAYER_MATCH_RATINGS)",
                color = PitchSecondaryText,
                fontSize = 10.sp,
                fontFamily = BreeSerifFontFamily,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RatingMetric("OVERALL", "%.1f".format(player.overallRating), ratingColor)
                RatingMetric("BATTING", if (player.battingRating > 0.0) "%.1f".format(player.battingRating) else "-", if (player.battingRating > 0.0) PitchPrimaryText else PitchSecondaryText)
                RatingMetric("BOWLING", if (player.bowlingRating > 0.0) "%.1f".format(player.bowlingRating) else "-", if (player.bowlingRating > 0.0) PitchPrimaryText else PitchSecondaryText)
                val impactLabel = if (player.impactRank != "-" && player.impactRank.isNotBlank()) "%.1f (%s)".format(player.impactRating, player.impactRank) else "%.1f".format(player.impactRating)
                RatingMetric("IMPACT", impactLabel, ratingColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Production Stats Grid
            Text(
                text = "MATCH PRODUCTION STATS",
                color = PitchSecondaryText,
                fontSize = 10.sp,
                fontFamily = BreeSerifFontFamily,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (player.role.contains("Bowler", ignoreCase = true) || player.wickets != "-") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatsMetric("RUNS", player.runs)
                    StatsMetric("WICKETS", player.wickets)
                    StatsMetric("OVERS", player.overs)
                    StatsMetric("ECONOMY", player.economy)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatsMetric("RUNS", player.runs)
                    StatsMetric("BALLS", player.balls)
                    StatsMetric("SR", player.strikeRate)
                    StatsMetric("4s / 6s", "${player.fours} / ${player.sixes}")
                }
            }



            // 3. Batting Intelligence Section
            val showBattingIntel = player.controlPct != "-" && player.controlPct.isNotBlank()
            if (showBattingIntel) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BATTING INTELLIGENCE",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                    if (player.battingImpactRating > 0.0) {
                        Text(
                            text = "BAT IMPACT: %.1f".format(player.battingImpactRating),
                            color = ratingColor,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatsMetric("CONTROL", player.controlPct)
                        StatsMetric("FALSE SHOT", player.falseShotPct)
                        StatsMetric("ATTACKING", player.attackingPct)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatsMetric("LEAVE", player.leavePct)
                        StatsMetric("DOT %", player.battingDotPct)
                        StatsMetric("BOUNDARY %", player.boundaryRunPct)
                    }
                }
            }

            // 4. Bowling Intelligence Section
            val showBowlingIntel = player.bowlingDotPct != "-" && player.bowlingDotPct.isNotBlank()
            if (showBowlingIntel) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BOWLING INTELLIGENCE",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                    if (player.bowlingImpactRating > 0.0) {
                        Text(
                            text = "BOWL IMPACT: %.1f".format(player.bowlingImpactRating),
                            color = ratingColor,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatsMetric("DOT %", player.bowlingDotPct)
                        StatsMetric("LINE ACC", player.lineAcc)
                        StatsMetric("LENGTH ACC", player.lengthAcc)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatsMetric("ECONOMY", player.economy)
                        StatsMetric("BOUNDARIES", player.boundariesConceded)
                        StatsMetric("IMPACT RAT", if (player.bowlingImpactRating > 0.0) "%.1f".format(player.bowlingImpactRating) else "-")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Narrative Story Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(34.dp)
                        .background(ratingColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = player.story,
                    color = PitchSecondaryText,
                    fontSize = 12.sp,
                    fontFamily = BreeSerifFontFamily,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun TeamTabPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ViewModeTabButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) PitchPrimaryText else PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RatingMetric(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = PitchSecondaryText,
            fontSize = 10.sp,
            fontFamily = BreeSerifFontFamily,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun StatsMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = PitchSecondaryText,
            fontSize = 9.sp,
            fontFamily = BreeSerifFontFamily,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = PitchPrimaryText,
            fontSize = 13.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}
