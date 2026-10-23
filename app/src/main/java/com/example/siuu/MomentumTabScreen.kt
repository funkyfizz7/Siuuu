package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
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

/**
 * Momentum UI State Representation (FutureBuilder pattern in Jetpack Compose)
 */
sealed interface MomentumUiState {
    object Loading : MomentumUiState
    data class Success(val data: MomentumTabDisplayData) : MomentumUiState
    data class Error(val message: String) : MomentumUiState
}

/**
 * Reusable Match Momentum Tab
 * Accepts matchId, queries MatchRepository, generates dynamic momentum curve from fact_match_momentum,
 * and renders interactive session inspector.
 */
@Composable
fun MomentumTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }

    val uiState by produceState<MomentumUiState>(
        initialValue = MomentumUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = MomentumUiState.Loading
        try {
            val momentumData = repository.getMomentumTabData(matchId)
            value = MomentumUiState.Success(momentumData)
        } catch (e: Exception) {
            value = MomentumUiState.Error(e.localizedMessage ?: "Failed to load match momentum. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is MomentumUiState.Loading -> {
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
                            text = "Loading Momentum Flow...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is MomentumUiState.Error -> {
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
                            .border(1.dp, PitchNegative.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = PitchNegative,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to Load Momentum Data",
                                color = PitchPrimaryText,
                                fontSize = 16.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                color = PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PitchBlackBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive),
                                modifier = Modifier.clickable { retryTrigger++ }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = PitchPositive,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Retry",
                                        color = PitchPositive,
                                        fontSize = 13.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is MomentumUiState.Success -> {
                MomentumContent(data = state.data)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun MomentumTabScreen(
    matchId: Int,
    modifier: Modifier = Modifier
) {
    MomentumTabScreen(
        matchId = matchId.toLong(),
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun MomentumTabScreen(
    matchData: MatchIntelligenceData,
    modifier: Modifier = Modifier
) {
    MomentumTabScreen(
        matchId = matchData.matchId,
        modifier = modifier
    )
}

@Composable
private fun MomentumContent(
    data: MomentumTabDisplayData,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember(data) {
        mutableStateOf<MomentumModel?>(null)
    }
    var showMomentumHelp by remember { mutableStateOf(false) }

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
        // Section 1: Match Momentum Flow Card (Dynamic Graph)
        item {
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MATCH MOMENTUM FLOW",
                                color = PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PitchIQHelpIcon(
                                onClick = { showMomentumHelp = true }
                            )
                        }

                        Text(
                            text = "Touch to inspect",
                            color = PitchSecondaryText,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DynamicMomentumChart(
                        points = data.points,
                        team1ShortName = data.header.team1ShortName,
                        team1Color = data.header.team1Color,
                        team2ShortName = data.header.team2ShortName,
                        team2Color = data.header.team2Color,
                        selectedIndex = selectedPoint?.let { sp -> data.points.indexOfFirst { it.sessionIndex == sp.sessionIndex } },
                        onPointSelected = { clicked -> selectedPoint = if (selectedPoint?.sessionIndex == clicked?.sessionIndex) null else clicked },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        breeFont = BreeSerifFontFamily
                    )
                }
            }
        }

        // Section 2: Session Intelligence Inspector Card (Dynamic from selected MomentumModel)
        item {
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { point ->
                    val isTeam1Leading = point.momentumScore >= 0
                    val accentColor = if (isTeam1Leading) data.header.team1Color else data.header.team2Color

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    val badgeText = if (point.isSplitInnings || point.inningsBadge.isNotBlank()) {
                                        "DAY ${point.dayNumber} · SESS ${point.sessionOfDay} (${point.inningsBadge.ifBlank { "SPLIT INNINGS" }})"
                                    } else {
                                        "DAY ${point.dayNumber} · SESS ${point.sessionOfDay} (INN ${point.innings})"
                                    }
                                    Text(
                                        text = badgeText,
                                        color = PitchSecondaryText,
                                        fontSize = 10.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val headlineText = if (point.isSplitInnings || point.sessionTitle.isNotBlank()) {
                                        point.sessionTitle.ifBlank { "${point.battingTeam} Batting" }
                                    } else {
                                        "${point.battingTeam} Batting"
                                    }
                                    Text(
                                        text = headlineText,
                                        color = PitchPrimaryText,
                                        fontSize = when {
                                            headlineText.length > 30 -> 12.5.sp
                                            headlineText.length > 20 -> 13.5.sp
                                            else -> 14.5.sp
                                        },
                                        fontFamily = BreeSerifFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val scoreLines = when {
                                    point.sessionEndScores.isNotEmpty() -> point.sessionEndScores
                                    point.sessionEndScore.isNotBlank() -> {
                                        point.sessionEndScore
                                            .removePrefix("Score:")
                                            .trim()
                                            .split(" • ")
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() }
                                    }
                                    else -> listOf("${point.runs}/${point.wickets}")
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PitchBlackBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        scoreLines.forEachIndexed { idx, sText ->
                                            if (idx > 0) Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sText,
                                                color = accentColor,
                                                fontSize = 12.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 Metric Tiles
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MetricTile("RUNS", "${point.runs}")
                                MetricTile("WICKETS", "${point.wickets}")
                                MetricTile("OVERS", point.formattedOvers)
                                MetricTile("RUN RATE", "%.2f".format(point.runRate))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Narrative Story Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(34.dp)
                                        .background(accentColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point.contextStory,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 17.sp
                                )
                            }

                            // Structured Session Heroes Breakdown (Batters, Bowlers, Partnership)
                            if (point.topBatter1.isNotEmpty() || point.topBowler1.isNotEmpty() || point.bestPartnershipRuns > 0) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PitchBlackBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 1. KEY BATTERS
                                    if (point.topBatter1.isNotEmpty()) {
                                        Column {
                                            Text(
                                                text = "KEY BATTERS",
                                                color = Color(0xFF22C55E),
                                                fontSize = 10.5.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• ${point.topBatter1} ${point.topBatter1Runs}" + (if (point.topBatter1Balls > 0) " (${point.topBatter1Balls}b)" else ""),
                                                color = PitchPrimaryText,
                                                fontSize = 12.sp,
                                                fontFamily = BreeSerifFontFamily
                                            )
                                            if (point.topBatter2.isNotEmpty() && point.topBatter2Runs > 0) {
                                                Text(
                                                    text = "• ${point.topBatter2} ${point.topBatter2Runs}" + (if (point.topBatter2Balls > 0) " (${point.topBatter2Balls}b)" else ""),
                                                    color = PitchPrimaryText,
                                                    fontSize = 12.sp,
                                                    fontFamily = BreeSerifFontFamily
                                                )
                                            }
                                        }
                                    }

                                    // 2. KEY BOWLERS
                                    if (point.topBowler1.isNotEmpty()) {
                                        HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
                                        Column {
                                            Text(
                                                text = "KEY BOWLERS",
                                                color = Color(0xFF00A3FF),
                                                fontSize = 10.5.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• ${point.topBowler1} ${point.topBowler1Wickets}/${point.topBowler1Runs}",
                                                color = PitchPrimaryText,
                                                fontSize = 12.sp,
                                                fontFamily = BreeSerifFontFamily
                                            )
                                            if (point.topBowler2.isNotEmpty() && (point.topBowler2Wickets > 0 || point.topBowler2Runs > 0)) {
                                                Text(
                                                    text = "• ${point.topBowler2} ${point.topBowler2Wickets}/${point.topBowler2Runs}",
                                                    color = PitchPrimaryText,
                                                    fontSize = 12.sp,
                                                    fontFamily = BreeSerifFontFamily
                                                )
                                            }
                                        }
                                    }

                                    // 3. BEST PARTNERSHIP
                                    if (point.bestPartnershipRuns > 0 && point.bestPartnershipBatter1.isNotEmpty()) {
                                        HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
                                        Column {
                                            Text(
                                                text = "BEST PARTNERSHIP",
                                                color = Color(0xFFFFC72C),
                                                fontSize = 10.5.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val partner2Str = if (point.bestPartnershipBatter2.isNotEmpty()) "–${point.bestPartnershipBatter2}" else ""
                                            Text(
                                                text = "• ${point.bestPartnershipBatter1}$partner2Str ${point.bestPartnershipRuns} runs",
                                                color = PitchPrimaryText,
                                                fontSize = 12.sp,
                                                fontFamily = BreeSerifFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showMomentumHelp) {
        PitchIQHelpDialog(
            title = MATCH_MOMENTUM_FLOW_HELP_TITLE,
            paragraphs = MATCH_MOMENTUM_FLOW_HELP_PARAGRAPHS,
            onDismiss = { showMomentumHelp = false }
        )
    }
}

@Composable
private fun MetricTile(title: String, value: String) {
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
            fontSize = 15.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}
