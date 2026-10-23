package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextOverflow
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
private val PitchModerate = Color(0xFFFFC72C)
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
 * Pressure UI State Representation (FutureBuilder pattern in Jetpack Compose)
 */
sealed interface PressureUiState {
    object Loading : PressureUiState
    data class Success(val data: PressureTabDisplayData) : PressureUiState
    data class Error(val message: String) : PressureUiState
}

/**
 * Reusable Match Pressure Tab
 * Accepts matchId, queries MatchRepository, and dynamically generates:
 * - Dynamic Pressure Flow Chart from fact_match_pressure
 * - Dynamic Session Inspector with metric tiles and zone pill
 * - Dynamic Pressure Explanation Box
 * - Dynamic Highest Pressure Moments from fact_match_pressure
 */
@Composable
fun PressureTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }

    val uiState by produceState<PressureUiState>(
        initialValue = PressureUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = PressureUiState.Loading
        try {
            val data = repository.getPressureTabData(matchId)
            value = PressureUiState.Success(data)
        } catch (e: Exception) {
            value = PressureUiState.Error(e.localizedMessage ?: "Failed to load pressure data. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is PressureUiState.Loading -> {
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
                            text = "Loading Pressure Flow...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is PressureUiState.Error -> {
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
                                text = "Unable to Load Pressure Data",
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

            is PressureUiState.Success -> {
                PressureContent(data = state.data)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun PressureTabScreen(
    matchId: Int,
    modifier: Modifier = Modifier
) {
    PressureTabScreen(
        matchId = matchId.toLong(),
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun PressureTabScreen(
    matchData: MatchIntelligenceData,
    modifier: Modifier = Modifier
) {
    PressureTabScreen(
        matchId = matchData.matchId,
        modifier = modifier
    )
}

@Composable
private fun PressureContent(
    data: PressureTabDisplayData,
    modifier: Modifier = Modifier
) {
    // Default selected point: Day 1 Sess 2 or first peak
    var selectedPoint by remember(data) {
        mutableStateOf<PressureModel?>(null)
    }
    var showPressureHelp by remember { mutableStateOf(false) }

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
        // Section 1: Session Pressure Flow Graph Card (Dynamic Graph)
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
                                text = "PRESSURE FLOW GRAPH",
                                color = PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PitchIQHelpIcon(
                                onClick = { showPressureHelp = true }
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

                    DynamicPressureFlowChart(
                        points = data.pressurePoints,
                        selectedIndex = selectedPoint?.let { sp -> data.pressurePoints.indexOfFirst { it.sessionIndex == sp.sessionIndex } },
                        onPointSelected = { clicked -> selectedPoint = if (selectedPoint?.sessionIndex == clicked?.sessionIndex) null else clicked },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        breeFont = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Pressure Severity Badges Legend (Cricket Language)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZoneBadge("Low (0–39)", PitchPositive)
                        ZoneBadge("Moderate (40–69)", PitchModerate)
                        ZoneBadge("High (70–100)", PitchNegative)
                    }
                }
            }
        }

        // Section 2 & 3: Session Inspector & Pressure Explanation Card (Dynamic)
        item {
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { point ->
                    val zoneColor = when {
                        point.pressureScore >= 70.0 -> PitchNegative
                        point.pressureScore >= 40.0 -> PitchModerate
                        else -> PitchPositive
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, zoneColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row: Session & Pressure Score Badge
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
                                        "DAY ${point.dayNumber} • SESSION ${point.sessionOfDay} (${point.inningsBadge.ifBlank { "SPLIT INNINGS" }})"
                                    } else {
                                        "DAY ${point.dayNumber} • SESSION ${point.sessionOfDay} (INN ${point.innings})"
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
                                    point.sessionEndScore.isNotBlank() && point.sessionEndScore.contains(" • ") -> {
                                        point.sessionEndScore
                                            .removePrefix("Score:")
                                            .trim()
                                            .split(" • ")
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() }
                                    }
                                    point.sessionEndScore.isNotBlank() -> {
                                        listOf(point.sessionEndScore.removePrefix("Score:").trim())
                                    }
                                    else -> listOf("${point.runs}/${point.wickets}")
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PitchBlackBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PitchModerate.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        scoreLines.forEachIndexed { idx, sText ->
                                            if (idx > 0) Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sText,
                                                color = PitchModerate,
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

                            // Step 5: Session Breakdown for Split Sessions / Flat Metric Tiles for Single Session
                            if (point.segments.size > 1) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PitchBlackBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "SESSION BREAKDOWN",
                                        color = PitchSecondaryText,
                                        fontSize = 10.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    point.segments.forEachIndexed { sIdx, seg ->
                                        if (sIdx > 0) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = PitchCardBorder
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${seg.team} (Inn ${seg.innings})",
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${seg.startScore} → ${seg.endScore}",
                                                    color = PitchSecondaryText,
                                                    fontSize = 11.5.sp,
                                                    fontFamily = BreeSerifFontFamily
                                                )
                                            }
                                            Text(
                                                text = "${seg.runs}/${seg.wickets} in ${seg.overs} ov",
                                                color = PitchPrimaryText,
                                                fontSize = 13.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            } else {
                                // 4 Flat Metric Tiles
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
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Section 3: Pressure Explanation Narrative Box
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
                                        .background(zoneColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point.explanation,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 17.sp
                                )
                            }

                            // Phase 2: Pressure Drivers (Session Heroes at bottom of pressure card)
                            val hasBatters = point.topBatter1.isNotEmpty()
                            val hasBowlers = point.topBowler1.isNotEmpty()

                            if (hasBatters || hasBowlers) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PitchBlackBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "PRESSURE DRIVERS",
                                        color = PitchSecondaryText,
                                        fontSize = 10.5.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Determine session hero drivers display:
                                    // 1. Bowling dominance (wickets >= 4 or low runs/wickets): Bowlers first
                                    // 2. Batting dominance (runs >= 80, wickets <= 2): Batters first
                                    // 3. Mixed / Balanced: Top Batter + Top Bowler
                                    when {
                                        point.wickets >= 4 -> {
                                            // Bowling Session
                                            if (point.topBowler1.isNotEmpty()) {
                                                Text(
                                                    text = "• ${point.topBowler1} ${point.topBowler1Wickets}/${point.topBowler1Runs}",
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                            if (point.topBowler2.isNotEmpty() && (point.topBowler2Wickets > 0 || point.topBowler2Runs > 0)) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "• ${point.topBowler2} ${point.topBowler2Wickets}/${point.topBowler2Runs}",
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                        point.runs >= 80 && point.wickets <= 2 -> {
                                            // Batting Session
                                            if (point.topBatter1.isNotEmpty()) {
                                                Text(
                                                    text = "• ${point.topBatter1} ${point.topBatter1Runs}" + (if (point.topBatter1Balls > 0) " (${point.topBatter1Balls}b)" else "*"),
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                            if (point.topBatter2.isNotEmpty() && point.topBatter2Runs > 0) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "• ${point.topBatter2} ${point.topBatter2Runs}" + (if (point.topBatter2Balls > 0) " (${point.topBatter2Balls}b)" else "*"),
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                        else -> {
                                            // Mixed Session (Top Batter + Top Bowler)
                                            if (point.topBatter1.isNotEmpty()) {
                                                Text(
                                                    text = "• ${point.topBatter1} ${point.topBatter1Runs}" + (if (point.topBatter1Balls > 0) " (${point.topBatter1Balls}b)" else "*"),
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                            if (point.topBowler1.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "• ${point.topBowler1} ${point.topBowler1Wickets}/${point.topBowler1Runs}",
                                                    color = PitchPrimaryText,
                                                    fontSize = 13.sp,
                                                    fontFamily = BreeSerifFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Phase 5: Session Verdict (Optional takeaway at bottom of session card)
                            if (point.verdict.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E2330), RoundedCornerShape(8.dp))
                                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "SESSION VERDICT",
                                            color = PitchSecondaryText,
                                            fontSize = 9.5.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = point.verdict,
                                            color = PitchPrimaryText,
                                            fontSize = 13.5.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
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

    if (showPressureHelp) {
        PitchIQHelpDialog(
            title = PRESSURE_FLOW_GRAPH_HELP_TITLE,
            paragraphs = PRESSURE_FLOW_GRAPH_HELP_PARAGRAPHS,
            onDismiss = { showPressureHelp = false }
        )
    }
}

@Composable
private fun ZoneBadge(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = PitchSecondaryText,
            fontSize = 10.sp,
            fontFamily = BreeSerifFontFamily
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
