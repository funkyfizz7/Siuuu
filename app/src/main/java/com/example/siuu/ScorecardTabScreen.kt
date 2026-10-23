package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
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
private val PitchCardInnerBg = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchNegative = Color(0xFFEF4444)
private val PitchAccentBlue = Color(0xFF00A3FF)
private val PitchAccentYellow = Color(0xFFFFC72C)

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

private fun getWicketSuffix(w: Int): String = when (w) {
    1 -> "st"
    2 -> "nd"
    3 -> "rd"
    else -> "th"
}

/**
 * Scorecard UI State Representation
 */
sealed interface ScorecardUiState {
    object Loading : ScorecardUiState
    data class Success(val data: ScorecardTabDisplayData) : ScorecardUiState
    data class Error(val message: String) : ScorecardUiState
}

/**
 * Reusable Match Scorecard Tab
 *
 * TARGET USER FLOW:
 * Match -> Scorecard
 * - Team Match Summary Header
 * - Step 1: Innings Selector Tabs ([1st Inn], [2nd Inn], [3rd Inn], [4th Inn])
 * - Step 2: Team Scorecard Header (Team Name, Total Score, Overs, Run Rate, Lead/Trail/Target)
 * - Step 3 & 4: Batting Scorecard (Batter, Dismissal underneath, R, B, 4s, 6s, SR) -> Tap row to open Player Detail Screen
 * - Step 5: Fall of Wickets (Clean line-by-line FOW table)
 * - Step 6: Top Partnerships (Interactive proportional split bars + runs/balls)
 * - Step 7: Bowling Scorecard (Bowler, O, M, R, W, Econ) -> Tap row to open Player Detail Screen
 * - Step 8: Match Context Badges (Highest Score, Best Bowling, Biggest Partnership, Fastest Fifty)
 */
@Composable
fun ScorecardTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }

    val uiState by produceState<ScorecardUiState>(
        initialValue = ScorecardUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = ScorecardUiState.Loading
        try {
            val data = repository.getScorecardTabData(matchId)
            value = ScorecardUiState.Success(data)
        } catch (e: Exception) {
            value = ScorecardUiState.Error(e.localizedMessage ?: "Failed to load scorecard data. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is ScorecardUiState.Loading -> {
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
                            text = "Loading Full Match Scorecard...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is ScorecardUiState.Error -> {
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
                                text = "Unable to load scorecard",
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

            is ScorecardUiState.Success -> {
                ScorecardContent(data = state.data, onNavigateToPlayer = onNavigateToPlayer)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun ScorecardTabScreen(
    matchId: Int,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ScorecardTabScreen(
        matchId = matchId.toLong(),
        onNavigateToPlayer = onNavigateToPlayer,
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun ScorecardTabScreen(
    matchData: MatchIntelligenceData,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ScorecardTabScreen(
        matchId = matchData.matchId,
        onNavigateToPlayer = onNavigateToPlayer,
        modifier = modifier
    )
}

@Composable
private fun ScorecardContent(
    data: ScorecardTabDisplayData,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (data.scorecards.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PitchBlackBg)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No scorecards recorded for this match.",
                color = PitchSecondaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    var selectedInningsIdx by remember { mutableStateOf(0) }
    val safeIdx = selectedInningsIdx.coerceIn(0, data.scorecards.size - 1)
    val currentInnings = data.scorecards.getOrNull(safeIdx) ?: data.scorecards.first()
    val teamColor = if (currentInnings.teamName == data.header.team1Name || currentInnings.teamName == data.header.team1ShortName) data.header.team1Color else data.header.team2Color

    // Helper to navigate to player inspector on click
    fun navigateToPlayer(playerId: Long?, name: String) {
        val targetId = playerId?.takeIf { it > 0L }
            ?: data.allRatings.firstOrNull {
                it.playerName.trim().equals(name.trim(), ignoreCase = true) ||
                it.playerName.trim().contains(name.trim(), ignoreCase = true) ||
                name.trim().contains(it.playerName.trim(), ignoreCase = true)
            }?.playerId?.toLongOrNull()
        if (targetId != null && targetId > 0L) {
            onNavigateToPlayer?.invoke(targetId)
        }
    }

    // Match Overview scores for Top Box
    val team1Inns = data.scorecards.filter { it.teamName == data.header.team1Name || it.teamName == data.header.team1ShortName }
    val team2Inns = data.scorecards.filter { it.teamName == data.header.team2Name || it.teamName == data.header.team2ShortName }
    val t1ScoreStr = team1Inns.joinToString(" & ") { formatCricketScore(it.totalScore.substringBefore(" (")) }
    val t2ScoreStr = team2Inns.joinToString(" & ") { formatCricketScore(it.totalScore.substringBefore(" (")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 56.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── TOP MATCH SUMMARY HEADER (Matching SummaryTabScreen) ──────────────────
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
                    // Row 1: Match Number Pill + Venue
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchAccentBlue.copy(alpha = 0.16f))
                                .border(1.dp, PitchAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = data.header.matchNumber.ifBlank { data.header.matchTitle.cleanYearBrackets().ifBlank { "Test Match" } },
                                color = PitchAccentBlue,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        if (data.header.venue.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${data.header.venue}",
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2: Team 1 CountryBadge (Flag + Code) + Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryBadge(
                            teamCode = data.header.team1ShortName.ifBlank { data.header.team1Name }
                        )
                        Text(
                            text = t1ScoreStr.ifBlank { "-" },
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Team 2 CountryBadge (Flag + Code) + Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryBadge(
                            teamCode = data.header.team2ShortName.ifBlank { data.header.team2Name }
                        )
                        Text(
                            text = t2ScoreStr.ifBlank { "-" },
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = PitchCardBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 4: Match Outcome Headline
                    val isDrawn = data.header.victoryMargin.contains("draw", ignoreCase = true) || data.header.winner.contains("draw", ignoreCase = true)
                    Text(
                        text = data.header.victoryMargin,
                        color = if (isDrawn) PitchAccentYellow else PitchPositive,
                        fontSize = 13.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // ── STEP 1: INNINGS SELECTOR TABS ────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchCardBg, RoundedCornerShape(10.dp))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                data.scorecards.forEachIndexed { idx, inn ->
                    val isSelected = selectedInningsIdx == idx
                    val innColor = if (inn.teamName == data.header.team1Name || inn.teamName == data.header.team1ShortName) data.header.team1Color else data.header.team2Color
                    val ordLabel = when (inn.inningsNumber) {
                        1 -> "1st Inn"
                        2 -> "2nd Inn"
                        3 -> "3rd Inn"
                        4 -> "4th Inn"
                        else -> "${inn.inningsNumber}th Inn"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, innColor.copy(alpha = 0.6f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedInningsIdx = idx }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = ordLabel,
                                color = if (isSelected) innColor else PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = inn.teamName.take(3).uppercase(),
                                color = if (isSelected) PitchPrimaryText else PitchSecondaryText.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ── STEP 2: TEAM SCORECARD HEADER ────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, teamColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${currentInnings.teamName.uppercase()} • INNINGS ${currentInnings.inningsNumber}",
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCricketScore(currentInnings.totalScore),
                                color = PitchPrimaryText,
                                fontSize = 26.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, teamColor)
                        ) {
                            Text(
                                text = "RR: %.2f".format(currentInnings.runRate),
                                color = teamColor,
                                fontSize = 13.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── STEP 3 & 4: BATTING SCORECARD ────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "BATTING",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BATTER",
                            color = PitchSecondaryText,
                            fontSize = 11.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier.width(150.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScoreHeaderCell("R", width = 28.dp)
                            ScoreHeaderCell("B", width = 28.dp)
                            ScoreHeaderCell("4s", width = 24.dp)
                            ScoreHeaderCell("6s", width = 24.dp)
                            ScoreHeaderCell("SR", width = 42.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Batter Rows (Clickable -> opens Player Breakdown Screen)
                    currentInnings.batters.forEach { batter ->
                        val formattedDismissal = formatCricketDismissal(batter.dismissal)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .clickable { navigateToPlayer(batter.playerId, batter.name) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            // Top Row: Batter Name and stats on exact same baseline
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = batter.name,
                                    color = PitchPrimaryText,
                                    fontSize = 14.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    modifier = Modifier.width(150.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ScoreDataCell("${batter.runs}", width = 28.dp, isBold = true, color = if (batter.runs >= 50) PitchPositive else PitchPrimaryText)
                                    ScoreDataCell("${batter.balls}", width = 28.dp)
                                    ScoreDataCell("${batter.fours}", width = 24.dp)
                                    ScoreDataCell("${batter.sixes}", width = 24.dp)
                                    ScoreDataCell("%.1f".format(batter.strikeRate), width = 42.dp)
                                }
                            }

                            // Bottom Row: Dismissal description underneath
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = formattedDismissal,
                                color = if (formattedDismissal.contains("not out", ignoreCase = true)) PitchPositive else PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Extras Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Extras",
                            color = PitchSecondaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        Text(
                            text = currentInnings.extras,
                            color = PitchPrimaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            color = PitchPrimaryText,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = formatCricketScore(currentInnings.totalScore),
                            color = PitchPositive,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // ── YET TO BAT SECTION ─────────────────────────────────────
                    val battedPlayerNames = currentInnings.batters.map { it.name.trim().lowercase() }.toSet()
                    val teamRatings = data.allRatings.filter { r ->
                        val t = r.team.trim()
                        val curT = currentInnings.teamName.trim()
                        t.equals(curT, ignoreCase = true) ||
                        t.contains(curT, ignoreCase = true) ||
                        curT.contains(t, ignoreCase = true) ||
                        (curT.startsWith("Aus", ignoreCase = true) && t.startsWith("Aus", ignoreCase = true)) ||
                        (curT.startsWith("Eng", ignoreCase = true) && t.startsWith("Eng", ignoreCase = true)) ||
                        (curT.startsWith("Ind", ignoreCase = true) && t.startsWith("Ind", ignoreCase = true)) ||
                        (curT.startsWith("Pak", ignoreCase = true) && t.startsWith("Pak", ignoreCase = true)) ||
                        (curT.startsWith("SA", ignoreCase = true) && t.startsWith("SA", ignoreCase = true)) ||
                        (curT.startsWith("NZ", ignoreCase = true) && t.startsWith("NZ", ignoreCase = true)) ||
                        (curT.startsWith("WI", ignoreCase = true) && t.startsWith("WI", ignoreCase = true)) ||
                        (curT.startsWith("SL", ignoreCase = true) && t.startsWith("SL", ignoreCase = true))
                    }
                    val yetToBatPlayers = teamRatings.filter { r ->
                        val pName = r.playerName.trim().lowercase()
                        !battedPlayerNames.any { bName ->
                            pName == bName || pName.contains(bName) || bName.contains(pName)
                        }
                    }

                    if (yetToBatPlayers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                                .border(0.5.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "YET TO BAT",
                                color = PitchSecondaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                yetToBatPlayers.forEach { p ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PitchBlackBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder),
                                        modifier = Modifier.clickable {
                                            navigateToPlayer(p.playerId?.toLongOrNull(), p.playerName)
                                        }
                                    ) {
                                        Text(
                                            text = p.playerName,
                                            color = PitchAccentBlue,
                                            fontSize = 12.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── STEP 7: BOWLING SCORECARD ────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "BOWLING",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BOWLER",
                            color = PitchSecondaryText,
                            fontSize = 11.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier.width(150.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScoreHeaderCell("O", width = 30.dp)
                            ScoreHeaderCell("M", width = 24.dp)
                            ScoreHeaderCell("R", width = 28.dp)
                            ScoreHeaderCell("W", width = 26.dp)
                            ScoreHeaderCell("ECON", width = 38.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bowler Rows (Clickable -> opens Player Breakdown Screen)
                    currentInnings.bowlers.forEach { bowler ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .clickable { navigateToPlayer(bowler.playerId, bowler.name) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bowler.name,
                                color = PitchPrimaryText,
                                fontSize = 14.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                modifier = Modifier.width(150.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ScoreDataCell(formatCricketOversDisplay(bowler.overs.toDouble()), width = 30.dp)
                                ScoreDataCell("${bowler.maidens}", width = 24.dp)
                                ScoreDataCell("${bowler.runs}", width = 28.dp)
                                ScoreDataCell("${bowler.wickets}", width = 26.dp, isBold = true, color = if (bowler.wickets >= 3) PitchPositive else PitchPrimaryText)
                                ScoreDataCell("%.2f".format(bowler.economy), width = 38.dp)
                            }
                        }
                    }
                }
            }
        }

        // ── STEP 5: FALL OF WICKETS ──────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FALL OF WICKETS",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCORE",
                            color = PitchSecondaryText,
                            fontSize = 11.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.width(64.dp)
                        )
                        Text(
                            text = "BATTER",
                            color = PitchSecondaryText,
                            fontSize = 11.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "OVER",
                            color = PitchSecondaryText,
                            fontSize = 11.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Line-by-line FOW Rows
                    currentInnings.fallOfWickets.forEach { fow ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .clickable { navigateToPlayer(fow.playerId, fow.batterName) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fow.score,
                                color = PitchPositive,
                                fontSize = 13.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.width(64.dp)
                            )
                            Text(
                                text = fow.batterName,
                                color = PitchPrimaryText,
                                fontSize = 13.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatCricketOversString(fow.over),
                                color = PitchSecondaryText,
                                fontSize = 12.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(60.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── STEP 6: PARTNERSHIPS ─────────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
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
                        Text(
                            text = "TOP PARTNERSHIPS",
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "${currentInnings.allPartnerships.size} Stands",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    currentInnings.allPartnerships.forEach { p ->
                        val b1Share = if (p.totalRuns > 0) (p.batter1Runs.toFloat() / p.totalRuns).coerceIn(0.05f, 0.95f) else 0.5f
                        val b2Share = 1f - b1Share

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .background(PitchBlackBg, RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (p.isKey) PitchPositive.copy(alpha = 0.6f) else PitchCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            // Header Row: Wicket Number + Total Partnership Runs + Key Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PitchCardBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder)
                                    ) {
                                        Text(
                                            text = "${p.wicketNumber}${getWicketSuffix(p.wicketNumber)} Wkt",
                                            color = PitchSecondaryText,
                                            fontSize = 11.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (p.isKey && p.intelligenceLabel.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PitchPositive.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                                        ) {
                                            Text(
                                                text = p.intelligenceLabel,
                                                color = PitchPositive,
                                                fontSize = 10.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${p.totalRuns} runs (${p.totalBalls}b)",
                                    color = if (p.isKey) PitchPositive else PitchPrimaryText,
                                    fontSize = 14.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Batters Individual Contribution Labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${p.batter1Name}: ${p.batter1Runs} (${p.batter1Balls})",
                                    color = PitchPrimaryText,
                                    fontSize = 12.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.clickable { navigateToPlayer(p.batter1Id, p.batter1Name) }
                                )
                                Text(
                                    text = "${p.batter2Name}: ${p.batter2Runs} (${p.batter2Balls})",
                                    color = PitchSecondaryText,
                                    fontSize = 12.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.clickable { navigateToPlayer(p.batter2Id, p.batter2Name) }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Proportional Dual Color Split Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(b1Share)
                                        .fillMaxHeight()
                                        .background(teamColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(b2Share)
                                        .fillMaxHeight()
                                        .background(Color(0xFF333D4F))
                                )
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
}

/**
 * Formats cricket overs correctly so that completed overs ending in .6 are rendered as the next integer
 * (e.g. 70.6 -> "71", 81.6 -> "82", 18.0 -> "18.0" / "18", 18.2 -> "18.2").
 */
private fun formatCricketOversDisplay(oversValue: Double): String {
    val intPart = oversValue.toInt()
    val frac = ((oversValue - intPart) * 10).toInt()
    return if (frac == 6) "${intPart + 1}" else "%.1f".format(oversValue)
}

internal fun formatCricketOversString(oversStr: String): String {
    if (oversStr.isBlank()) return oversStr
    return oversStr.replace(Regex("(\\d+)\\.6(?=\\s*(?:ov|overs|\\)|$))")) { match ->
        (match.groupValues[1].toInt() + 1).toString()
    }
}

internal fun formatCricketScore(scoreStr: String): String {
    if (scoreStr.isBlank()) return scoreStr
    val stripped = scoreStr.replace(Regex("/\\s*10(?=\\s*(?:\\(|$|&|,|;|dec|d))"), "")
    return formatCricketOversString(stripped)
}

/**
 * Formats raw dismissal strings into standard cricket scorecard notation:
 * - "c JE Root b SCJ Broad" -> "c JE Root b SCJ Broad"
 * - "b SCJ Broad" -> "b SCJ Broad"
 * - "lbw b SCJ Broad" -> "lbw b SCJ Broad"
 * - "c & b Peter Siddle" -> "c & b Peter Siddle"
 * - "st JM Bairstow b Nathan Lyon" -> "st JM Bairstow b Nathan Lyon"
 * - "not out" -> "not out"
 */
private fun formatCricketDismissal(raw: String): String {
    val valTrim = raw.trim()
    if (valTrim.isBlank()) return "not out"
    val low = valTrim.lowercase()

    if (low == "not out" || low.contains("not out")) return "not out"

    // If already pre-formatted like "c Stokes b Broad", "b Broad", "lbw b Broad", "st Bairstow b Leach", "c & b Broad", "run out (Stokes)", "hit wicket b Broad"
    if (valTrim.startsWith("c ") || valTrim.startsWith("b ") || valTrim.startsWith("lbw ") || valTrim.startsWith("st ") || valTrim.startsWith("c & b ") || valTrim.startsWith("run out") || valTrim.startsWith("hit wicket") || valTrim.startsWith("retired")) {
        return valTrim
    }

    if (low.startsWith("caught and bowled") || low.startsWith("c and b") || low.startsWith("c & b")) {
        val bowler = valTrim.replaceFirst(Regex("^(caught and bowled|c and b|c & b)\\s*(b\\s*)?", RegexOption.IGNORE_CASE), "").trim()
        return if (bowler.isNotBlank()) "c & b $bowler" else "c & b"
    }

    if (low.startsWith("caught")) {
        val remainder = valTrim.replaceFirst(Regex("^caught\\s*", RegexOption.IGNORE_CASE), "").trim()
        return "c $remainder"
    }

    if (low.startsWith("bowled")) {
        val bowler = valTrim.replaceFirst(Regex("^bowled\\s*(b\\s*)?", RegexOption.IGNORE_CASE), "").trim()
        return if (bowler.isNotBlank()) "b $bowler" else "bowled"
    }

    if (low.startsWith("lbw")) {
        val remainder = valTrim.replaceFirst(Regex("^lbw\\s*", RegexOption.IGNORE_CASE), "").trim()
        val bowler = remainder.replaceFirst(Regex("^b\\s*", RegexOption.IGNORE_CASE), "").trim()
        return if (bowler.isNotBlank()) "lbw b $bowler" else "lbw"
    }

    if (low.startsWith("stumped")) {
        val remainder = valTrim.replaceFirst(Regex("^stumped\\s*", RegexOption.IGNORE_CASE), "").trim()
        return "st $remainder"
    }

    if (low.startsWith("run out")) {
        return valTrim
    }

    if (low.startsWith("hit wicket")) {
        val bowler = valTrim.replaceFirst(Regex("^hit wicket\\s*(b\\s*)?", RegexOption.IGNORE_CASE), "").trim()
        return if (bowler.isNotBlank()) "hit wicket b $bowler" else "hit wicket"
    }

    return valTrim
}

@Composable
private fun ScoreHeaderCell(title: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = title,
        color = PitchSecondaryText,
        fontSize = 11.5.sp,
        fontFamily = BreeSerifFontFamily,
        textAlign = TextAlign.End,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun ScoreDataCell(
    value: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    color: Color = PitchPrimaryText
) {
    Text(
        text = value,
        color = color,
        fontSize = 13.5.sp,
        fontFamily = BreeSerifFontFamily,
        fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.End,
        modifier = Modifier.width(width)
    )
}
