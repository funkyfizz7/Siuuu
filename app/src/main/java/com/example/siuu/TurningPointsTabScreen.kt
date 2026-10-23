package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.hypot

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardInnerBg = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchNegative = Color(0xFFEF4444)
private val PitchBlue = Color(0xFF00A3FF)
private val PitchGold = Color(0xFFFFC72C)

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
 * Turning Points UI State Representation
 */
sealed interface TurningPointsUiState {
    object Loading : TurningPointsUiState
    data class Success(val data: TurningPointsDisplayData) : TurningPointsUiState
    data class Error(val message: String) : TurningPointsUiState
}

/**
 * Reusable Turning Points Tab Screen
 * Features:
 * 1. Match Header Overview
 * 2. SECTION A: Match Defining Moments (Top 5 Ranked Turning Points)
 * 3. SECTION B: TURNING POINTS IMPACT GRAPH (Interactive 0-100 Scatter Graph grouped by Innings)
 * 4. Interactive Detail Inspector Card for tapped turning point dot.
 */
@Composable
fun TurningPointsTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(matchId) {
        NarrativeOverrideManager.triggerBackgroundRefresh(matchId, context)
        NarrativeOverrideManager.overrideUpdates.collect { updatedMatchId ->
            if (updatedMatchId == matchId) {
                retryTrigger++
            }
        }
    }

    val uiState by produceState<TurningPointsUiState>(
        initialValue = TurningPointsUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = TurningPointsUiState.Loading
        try {
            val data = repository.getTurningPointsTabData(matchId)
            value = TurningPointsUiState.Success(data)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            value = TurningPointsUiState.Error(e.localizedMessage ?: "Failed to load turning points. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is TurningPointsUiState.Loading -> {
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
                            text = "Loading Turning Points...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is TurningPointsUiState.Error -> {
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
                                text = "Unable to Load Turning Points",
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

            is TurningPointsUiState.Success -> {
                TurningPointsContent(data = state.data)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun TurningPointsTabScreen(
    matchId: Int,
    modifier: Modifier = Modifier
) {
    TurningPointsTabScreen(
        matchId = matchId.toLong(),
        modifier = modifier
    )
}

/**
 * Overload for prototype data compatibility
 */
@Composable
fun TurningPointsTabScreen(
    matchData: MatchIntelligenceData,
    modifier: Modifier = Modifier
) {
    TurningPointsTabScreen(
        matchId = matchData.matchId,
        modifier = modifier
    )
}

@Composable
private fun TurningPointsContent(
    data: TurningPointsDisplayData,
    modifier: Modifier = Modifier
) {
    var selectedEvent by remember(data) {
        mutableStateOf<TurningPointItem?>(
            data.topTurningPoints.firstOrNull() ?: data.turningPoints.firstOrNull()
        )
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =========================================================================
        // SECTION A: MATCH DEFINING MOMENTS (Top 5 Highlights)
        // =========================================================================
        if (data.topTurningPoints.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MATCH DEFINING MOMENTS",
                            color = PitchPrimaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "TOP 5 IMPACT",
                            color = PitchSecondaryText,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    data.topTurningPoints.take(5).forEachIndexed { idx, tp ->
                        Phase3DefiningMomentCard(rank = idx + 1, item = tp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // =========================================================================
        // SECTION B: MATCH VERDICT CHAIN (Causal Victory Timeline)
        // =========================================================================
        if (data.matchVerdictChain.isNotEmpty()) {
            item {
                HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                MatchVerdictChainSection(
                    verdictChain = data.matchVerdictChain,
                    winner = data.header.winner,
                    victoryMargin = data.header.victoryMargin
                )
            }
        }

        // =========================================================================
        // SECTION C: TURNING POINTS IMPACT GRAPH (Dynamic Proportional Innings Widths)
        // =========================================================================
        if (data.turningPoints.isNotEmpty()) {
            item {
                HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

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
                            Text(
                                text = "TURNING POINTS IMPACT GRAPH",
                                color = PitchPrimaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Tap dot to inspect",
                                color = PitchSecondaryText,
                                fontSize = 10.5.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Simplified 2-Marker Legend (Red = Negative/Wicket, Green = Positive/Runs)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = TpRed, label = "Wickets & Collapses (Red)")
                            LegendItem(color = TpGreen, label = "Partnerships & Milestones (Green)")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Canvas Graph Component with dynamic proportional innings widths
                        TurningPointsImpactGraph(
                            turningPoints = data.turningPoints,
                            inningsOvers = data.inningsOvers,
                            selectedEvent = selectedEvent,
                            onEventSelected = { selectedEvent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                            breeFont = BreeSerifFontFamily
                        )
                    }
                }
            }

            // =========================================================================
            // INSPECTOR TOOLTIP CARD (Floating Card for Selected Turning Point)
            // =========================================================================
            item {
                AnimatedVisibility(
                    visible = selectedEvent != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedEvent?.let { item ->
                        val eventColor = getEventTypeColor(item.eventType)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.2.dp, eventColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Top Header Row: Rank Tier & Importance Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = (if (item.rank > 0) "#${item.rank} " else "") + item.importanceTier.uppercase(),
                                            color = eventColor,
                                            fontSize = 11.5.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PitchBlackBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, eventColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "IMPORTANCE",
                                                color = PitchSecondaryText,
                                                fontSize = 9.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "%.0f".format(item.importanceScore),
                                                color = PitchPrimaryText,
                                                fontSize = 11.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Event Badge + Innings & Over Subtitle Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(eventColor.copy(alpha = 0.15f))
                                            .border(0.8.dp, eventColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                    ) {
                                        Text(
                                            text = item.eventType.replace("_", " "),
                                            color = eventColor,
                                            fontSize = 10.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }

                                    Text(
                                        text = "Inn ${item.inningsNo}" + (if (item.overNo > 0.0) " • Over ${item.overNo}" else "") + (if (item.team.isNotEmpty()) " • ${item.team}" else ""),
                                        color = PitchSecondaryText,
                                        fontSize = 11.5.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Event Title
                                Text(
                                    text = item.title,
                                    color = PitchPrimaryText,
                                    fontSize = 16.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Narrative Description (3-Part Structured Narrative)
                                Text(
                                    text = item.description,
                                    color = PitchSecondaryText,
                                    fontSize = 12.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 17.5.sp
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
 * Match Verdict Chain Section (Causal Victory Sequence)
 */
@Composable
private fun MatchVerdictChainSection(
    verdictChain: List<MatchVerdictNode>,
    winner: String,
    victoryMargin: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MATCH VERDICT CHAIN",
                color = PitchPrimaryText,
                fontSize = 13.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${verdictChain.size} KEY PHASES",
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        verdictChain.forEachIndexed { index, node ->
            val isLast = index == verdictChain.lastIndex
            VerdictNodeStepperItem(
                node = node,
                isLast = isLast
            )
        }
    }
}

/**
 * Stepper Item for Match Verdict Chain
 */
@Composable
private fun VerdictNodeStepperItem(
    node: MatchVerdictNode,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val stepColor = when {
        node.nodeType == "RESULT" -> PitchGold
        node.isDecisive -> PitchGold
        node.nodeType == "OUTCOME" -> PitchBlue
        else -> PitchPositive
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Step circle & connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(stepColor.copy(alpha = 0.15f))
                    .border(1.2.dp, stepColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (node.isDecisive || node.nodeType == "RESULT") {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = stepColor,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Text(
                        text = "${node.stepIndex}",
                        color = stepColor,
                        fontSize = 10.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(72.dp)
                        .background(PitchCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Card with Phase Pill, Score Snippet, Headline, and Description
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = PitchCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 10.dp)
                .border(
                    width = if (node.isDecisive || node.nodeType == "RESULT") 1.2.dp else 0.8.dp,
                    color = if (node.isDecisive || node.nodeType == "RESULT") stepColor.copy(alpha = 0.6f) else PitchCardBorder,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header: Phase badge + Score snippet + Decisive star
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(stepColor.copy(alpha = 0.15f))
                                .border(0.6.dp, stepColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = node.phase.uppercase(),
                                color = stepColor,
                                fontSize = 9.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        if (node.scoreSnippet.isNotBlank()) {
                            Text(
                                text = node.scoreSnippet,
                                color = PitchSecondaryText,
                                fontSize = 10.5.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    if (node.isDecisive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PitchGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "DECISIVE",
                                color = PitchGold,
                                fontSize = 9.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Headline
                Text(
                    text = node.headline,
                    color = PitchPrimaryText,
                    fontSize = 13.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = node.description,
                    color = PitchSecondaryText,
                    fontSize = 11.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
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

// Simplified 2-State Impact Graph Marker System:
// Red (#FF4D4D) for negative events: Wickets, Collapses, 5-Wicket Hauls, 10-Wicket Hauls, Dismissals
// Green (#00C853) for positive events: Partnerships, Recoveries, Centuries, Double Centuries, Match-Saving Stands
private val TpRed = Color(0xFFFF4D4D)
private val TpGreen = Color(0xFF00C853)

private fun getEventTypeColor(eventType: String): Color {
    val up = eventType.uppercase()
    return when {
        // Negative Events (Red #FF4D4D)
        up.contains("WICKET") || 
        up.contains("COLLAPSE") || 
        up.contains("HAUL") || 
        up.contains("DISMISSAL") || 
        up.contains("BREAKTHROUGH") -> TpRed

        // Positive Events (Green #00C853)
        up.contains("PARTNERSHIP") || 
        up.contains("RECOVERY") || 
        up.contains("CENTURY") || 
        up.contains("150") || 
        up.contains("200") || 
        up.contains("250") || 
        up.contains("300") || 
        up.contains("STAND") || 
        up.contains("RESISTANCE") || 
        up.contains("COUNTER") -> TpGreen

        else -> TpGreen
    }
}

/**
 * Interactive Turning Points Impact Scatter Graph
 * X-axis: Proportional Innings Widths calculated dynamically from innings overs (with 15% min-width)
 * Y-axis: Importance Score (0 to 100)
 */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun TurningPointsImpactGraph(
    turningPoints: List<TurningPointItem>,
    inningsOvers: List<Double> = emptyList(),
    selectedEvent: TurningPointItem?,
    onEventSelected: (TurningPointItem) -> Unit,
    modifier: Modifier = Modifier,
    breeFont: FontFamily = BreeSerifFontFamily
) {
    val textMeasurer = rememberTextMeasurer()

    // Determine number of actual innings in this match (usually 3 or 4 in Tests)
    val totalInnings = remember(turningPoints, inningsOvers) {
        val maxInnFromPoints = turningPoints.maxOfOrNull { it.inningsNo } ?: 4
        val maxInnFromOvers = inningsOvers.size
        maxOf(maxInnFromPoints, maxInnFromOvers).coerceIn(1, 4)
    }

    // Calculate dynamic proportional width bounds for each innings
    val innBounds = remember(totalInnings, inningsOvers, turningPoints) {
        val bounds = mutableMapOf<Int, Pair<Float, Float>>()
        val count = totalInnings

        // Build list of overs per innings with fallback
        val oversList = (1..count).map { inn ->
            if (inn <= inningsOvers.size && inningsOvers[inn - 1] > 0.0) {
                inningsOvers[inn - 1]
            } else {
                val maxOvInInn = turningPoints.filter { it.inningsNo == inn }.maxOfOrNull { it.overNo } ?: 80.0
                maxOvInInn.coerceAtLeast(30.0)
            }
        }

        val totalOvers = oversList.sum().coerceAtLeast(1.0)
        val minWidthPerInn = 0.15f
        val remainingWidth = (1.0f - (count * minWidthPerInn)).coerceAtLeast(0f)

        val fractions = oversList.map { ov ->
            minWidthPerInn + (ov / totalOvers).toFloat() * remainingWidth
        }

        var currStart = 0f
        for (i in 0 until count) {
            val inn = i + 1
            val w = fractions[i]
            val currEnd = (currStart + w).coerceAtMost(1f)
            bounds[inn] = Pair(currStart, currEnd)
            currStart = currEnd
        }

        bounds
    }

    // Position calculation for each point along the dynamic X-axis
    val pointPositions = remember(turningPoints, innBounds) {
        val list = mutableListOf<Pair<TurningPointItem, Float>>()

        for (inn in 1..totalInnings) {
            val innPoints = turningPoints
                .filter { it.inningsNo == inn }
                .sortedWith(compareBy({ it.overNo }, { it.rank }))
            
            val n = innPoints.size
            if (n == 0) continue

            val (innStartFrac, innEndFrac) = innBounds[inn] ?: Pair((inn - 1) * 0.25f, inn * 0.25f)
            val innWidth = innEndFrac - innStartFrac
            val margin = innWidth * 0.08f // 8% inner padding within innings span

            val minOver = innPoints.minOfOrNull { it.overNo } ?: 0.0
            val maxOver = innPoints.maxOfOrNull { it.overNo } ?: 100.0

            innPoints.forEachIndexed { idx, item ->
                val fracWithin = if (n == 1) {
                    0.5f
                } else if (maxOver > minOver) {
                    ((item.overNo - minOver) / (maxOver - minOver)).toFloat().coerceIn(0f, 1f)
                } else {
                    idx.toFloat() / (n - 1)
                }

                val xNorm = (innStartFrac + margin) + fracWithin * (innWidth - (margin * 2))
                list.add(Pair(item, xNorm))
            }
        }
        list
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pointPositions) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val paddingLeft = 32.dp.toPx()
                        val paddingBottom = 26.dp.toPx()
                        val usableWidth = width - paddingLeft - 10.dp.toPx()
                        val usableHeight = height - paddingBottom - 10.dp.toPx()

                        // Find closest point to tap
                        var closest: TurningPointItem? = null
                        var minDistance = Float.MAX_VALUE

                        for ((item, xNorm) in pointPositions) {
                            val px = paddingLeft + xNorm * usableWidth
                            val yNorm = 1f - (item.importanceScore.toFloat() / 100f)
                            val py = 10.dp.toPx() + yNorm * usableHeight

                            val dist = hypot(tapOffset.x - px, tapOffset.y - py)
                            if (dist < minDistance && dist <= 36.dp.toPx()) { // 36dp touch radius
                                minDistance = dist
                                closest = item
                            }
                        }

                        if (closest != null) {
                            onEventSelected(closest)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 32.dp.toPx()
            val paddingBottom = 26.dp.toPx()
            val usableWidth = width - paddingLeft - 10.dp.toPx()
            val usableHeight = height - paddingBottom - 10.dp.toPx()

            val axisColor = Color(0xFF333333)
            val gridColor = Color(0xFF1E1E1E)
            val textColor = Color(0xFF888888)

            // 1. Draw Y-Axis Gridlines & Labels (100, 80, 60, 40, 20, 0)
            val yTiers = listOf(100, 80, 60, 40, 20, 0)
            yTiers.forEach { scoreVal ->
                val yNorm = 1f - (scoreVal.toFloat() / 100f)
                val py = 10.dp.toPx() + yNorm * usableHeight

                // Gridline
                if (scoreVal > 0) {
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, py),
                        end = Offset(width, py),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Label
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString("$scoreVal"),
                    style = TextStyle(
                        color = textColor,
                        fontSize = 9.sp,
                        fontFamily = breeFont
                    )
                )
                drawText(
                    textLayout,
                    topLeft = Offset(paddingLeft - textLayout.size.width.toFloat() - 6.dp.toPx(), py - textLayout.size.height.toFloat() / 2f)
                )
            }

            // 2. Draw Dynamic Proportional Innings Dividers & Header Labels
            for (inn in 1..totalInnings) {
                val (innStartFrac, innEndFrac) = innBounds[inn] ?: Pair((inn - 1) * 0.25f, inn * 0.25f)
                val sepX = paddingLeft + innEndFrac * usableWidth

                // Vertical Innings Divider Line (Dashed)
                if (inn < totalInnings) {
                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(sepX, 10.dp.toPx()),
                        end = Offset(sepX, height - paddingBottom),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // Innings Header Tag at Bottom, centered within its proportional width
                val centerX = paddingLeft + ((innStartFrac + innEndFrac) / 2f) * usableWidth
                val innLabel = textMeasurer.measure(
                    text = AnnotatedString("Inn $inn"),
                    style = TextStyle(
                        color = Color(0xFFAAAAAA),
                        fontSize = 10.5.sp,
                        fontFamily = breeFont,
                        fontWeight = FontWeight.Normal
                    )
                )
                drawText(
                    innLabel,
                    topLeft = Offset(centerX - innLabel.size.width.toFloat() / 2f, height - paddingBottom + 6.dp.toPx())
                )
            }

            // 3. Draw Axis Lines
            // Y-Axis line
            drawLine(
                color = axisColor,
                start = Offset(paddingLeft, 10.dp.toPx()),
                end = Offset(paddingLeft, height - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )
            // X-Axis line
            drawLine(
                color = axisColor,
                start = Offset(paddingLeft, height - paddingBottom),
                end = Offset(width, height - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )

            // 4. Render Turning Point Scatter Dots
            pointPositions.forEach { (item, xNorm) ->
                val px = paddingLeft + xNorm * usableWidth
                val yNorm = 1f - (item.importanceScore.toFloat() / 100f)
                val py = 10.dp.toPx() + yNorm * usableHeight

                val isSelected = selectedEvent?.title == item.title
                val dotColor = getEventTypeColor(item.eventType)

                if (isSelected) {
                    // Outer pulsating selection halo
                    drawCircle(
                        color = dotColor.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 7.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(px, py)
                    )
                } else {
                    // Regular turning point dot
                    val baseRadius = if (item.importanceScore >= 95.0) 5.5.dp.toPx() else 4.dp.toPx()
                    drawCircle(
                        color = dotColor,
                        radius = baseRadius,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = PitchBlackBg,
                        radius = baseRadius,
                        center = Offset(px, py),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Card for SECTION A: Phase 3 Match Defining Moment Card
 */
@Composable
private fun Phase3DefiningMomentCard(rank: Int, item: TurningPointItem) {
    val isTop = rank == 1
    val rankBadgeColor = if (isTop) PitchBlue else PitchPositive
    val eventColor = getEventTypeColor(item.eventType)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isTop) PitchBlue.copy(alpha = 0.5f) else PitchCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            // Header Row: Rank Badge, Type Badge, Innings & Importance Score Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(rankBadgeColor.copy(alpha = 0.15f))
                            .border(0.8.dp, rankBadgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "#$rank",
                            color = rankBadgeColor,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(eventColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = item.eventType.replace("_", " "),
                            color = eventColor,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Text(
                        text = "Inn ${item.inningsNo}",
                        color = PitchSecondaryText,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }

                // Importance Score & Tier Pill
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PitchBlackBg,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, rankBadgeColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.importanceTier.uppercase(),
                            color = rankBadgeColor,
                            fontSize = 9.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "%.0f".format(item.importanceScore),
                            color = PitchPrimaryText,
                            fontSize = 10.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title,
                color = PitchPrimaryText,
                fontSize = 15.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description Narrative (3-Part Structured Narrative)
            Text(
                text = item.description,
                color = PitchSecondaryText,
                fontSize = 12.5.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 17.5.sp
            )
        }
    }
}
