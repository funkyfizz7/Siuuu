package com.example.siuu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF141414)
private val PitchCardInnerBg = Color(0xFF1C1C1E)
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

enum class PlayerInspectorTab(val label: String) {
    SUMMARY("MATCH SUMMARY"),
    TIMELINE("INNINGS TIMELINE"),
    BOWLING("BOWLING"),
    IMPACT("MATCH IMPACT")
}

sealed interface PlayerInspectorUiState {
    object Loading : PlayerInspectorUiState
    data class Success(val data: PlayerInspectorData) : PlayerInspectorUiState
    data class Error(val message: String) : PlayerInspectorUiState
}

/**
 * Player Inspector Screen
 * Route: player/{matchId}/{playerId}
 *
 * Tabs:
 * 1. MATCH SUMMARY (Phase 6 Summary + Phase 6D Player Match Story)
 * 2. INNINGS TIMELINE (Phase 6A Innings Breakdown, Milestones & Partnerships)
 * 3. BOWLING (Phase 6B Workload, Continuous Spells & Dismissals)
 * 4. MATCH IMPACT (Phase 6C Rating/Impact explanation & Key Contributions)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerInspectorScreen(
    matchId: Long,
    playerId: Long,
    onBack: () -> Unit,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }
    var activeTab by remember { mutableStateOf(PlayerInspectorTab.SUMMARY) }

    val uiState by produceState<PlayerInspectorUiState>(
        initialValue = PlayerInspectorUiState.Loading,
        key1 = matchId,
        key2 = playerId,
        key3 = retryTrigger
    ) {
        value = PlayerInspectorUiState.Loading
        try {
            val data = repository.getPlayerInspectorData(matchId, playerId)
            value = PlayerInspectorUiState.Success(data)
        } catch (e: Exception) {
            value = PlayerInspectorUiState.Error(e.localizedMessage ?: "Failed to load player inspector data.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PLAYER INSPECTOR",
                        color = PitchPrimaryText,
                        fontSize = 15.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PitchPrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PitchBlackBg,
                    titleContentColor = PitchPrimaryText
                )
            )
        },
        containerColor = PitchBlackBg,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PitchBlackBg)
        ) {
            when (val state = uiState) {
                is PlayerInspectorUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PitchPositive,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Player Inspector...",
                                color = PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }
                }

                is PlayerInspectorUiState.Error -> {
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
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error Loading Player",
                                    color = PitchNegative,
                                    fontSize = 16.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { retryTrigger++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = PitchCardInnerBg)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = PitchPrimaryText)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry", color = PitchPrimaryText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                is PlayerInspectorUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header Profile Card
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            PlayerInspectorHeaderCard(data = state.data)
                        }

                        // Inspector Tabs Row
                        ScrollableTabRow(
                            selectedTabIndex = activeTab.ordinal,
                            containerColor = PitchBlackBg,
                            contentColor = PitchPrimaryText,
                            edgePadding = 16.dp,
                            divider = {
                                HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
                            },
                            indicator = { tabPositions ->
                                if (activeTab.ordinal in tabPositions.indices) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                                        height = 2.5.dp,
                                        color = PitchPositive
                                    )
                                }
                            }
                        ) {
                            PlayerInspectorTab.values().forEach { tab ->
                                val selected = activeTab == tab
                                Tab(
                                    selected = selected,
                                    onClick = { activeTab = tab },
                                    text = {
                                        Text(
                                            text = tab.label,
                                            color = if (selected) PitchPrimaryText else PitchSecondaryText,
                                            fontSize = 12.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        // Tab Content Body
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            when (activeTab) {
                                PlayerInspectorTab.SUMMARY -> PlayerInspectorMatchSummaryTab(data = state.data)
                                PlayerInspectorTab.TIMELINE -> PlayerInspectorTimelineTab(data = state.data)
                                PlayerInspectorTab.BOWLING -> PlayerInspectorBowlingTab(data = state.data)
                                PlayerInspectorTab.IMPACT -> PlayerInspectorImpactTab(data = state.data)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Header Profile Card
 */
@Composable
private fun PlayerInspectorHeaderCard(
    data: PlayerInspectorData,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with Attached Rating Pill
                Box(contentAlignment = Alignment.BottomEnd) {
                    val initials = data.playerName.split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it.first() }
                        .joinToString("")
                        .uppercase()

                    val context = LocalContext.current
                    val isImageReady = remember(data.playerId, data.playerName) {
                        PlayerImageLoader.isPlayerImageReady(context, data.playerId, data.playerName)
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PitchCardInnerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isImageReady) {
                            PitchIQPlayerAvatar(
                                playerId = data.playerId,
                                playerName = data.playerName,
                                contentDescription = data.playerName,
                                modifier = Modifier.fillMaxSize(),
                                border = BorderStroke(1.5.dp, PitchCardBorder),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = if (initials.isNotBlank()) initials else "PI",
                                color = PitchPrimaryText,
                                fontSize = 18.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(1.5.dp, PitchCardBorder, CircleShape)
                            )
                        }
                    }

                    val ratingColor = getPitchIQRatingColor(data.matchRating)
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ratingColor)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", data.matchRating),
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.playerName,
                        color = PitchPrimaryText,
                        fontSize = 18.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (data.teamName.isNotBlank()) {
                            Text(
                                text = data.teamName,
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                            Text(
                                text = "•",
                                color = PitchSecondaryText.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF222733))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = data.role.ifBlank { "Player" },
                                color = PitchAccentBlue,
                                fontSize = 11.sp,
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

// =========================================================================
// TAB 1: MATCH SUMMARY (Phase 6 Summary + Phase 6D Player Match Story)
// =========================================================================
@Composable
private fun PlayerInspectorMatchSummaryTab(
    data: PlayerInspectorData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Phase 6D: Player Match Story
        if (data.matchStoryNarrative.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MATCH STORY & NARRATIVE",
                                color = PitchAccentBlue,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = data.matchStoryNarrative,
                            color = PitchPrimaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        // Quick Highlights Banner
        item {
            val sm = data.matchSummary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricHighlightChip(
                    label = "RUNS",
                    value = "${sm.runs}",
                    modifier = Modifier.weight(1f)
                )
                MetricHighlightChip(
                    label = "BEST",
                    value = "${sm.highestScore}",
                    modifier = Modifier.weight(1f)
                )
                MetricHighlightChip(
                    label = "WICKETS",
                    value = if (sm.oversBowled > 0.0) "${sm.wickets}" else "0",
                    modifier = Modifier.weight(1f)
                )
                MetricHighlightChip(
                    label = "CATCHES",
                    value = "${sm.catches}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Batting Summary Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BATTING PRODUCTION",
                        color = PitchAccentBlue,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val sm = data.matchSummary
                    val avgStr = if (sm.outs > 0) String.format("%.2f", sm.battingAverage) else if (sm.runs > 0) "${sm.runs}*" else "-"

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatItem(label = "Runs", value = "${sm.runs}", modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Balls", value = "${sm.balls}", modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Average", value = avgStr, modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Strike Rate", value = String.format("%.1f", sm.battingStrikeRate), modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatItem(label = "Boundaries", value = "${sm.fours}x4, ${sm.sixes}x6", modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Boundary %", value = String.format("%.1f%%", sm.boundaryPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Control %", value = String.format("%.1f%%", sm.controlPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Dot Ball %", value = String.format("%.1f%%", sm.dotPct), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Bowling Summary Card
        if (data.matchSummary.oversBowled > 0.0 || data.bowlingSpells.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "BOWLING WORKLOAD & FIGURES",
                            color = PitchPositive,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val sm = data.matchSummary
                        val bAvgStr = if (sm.wickets > 0) String.format("%.2f", sm.bowlingAverage) else "-"
                        val bSrStr = if (sm.wickets > 0) String.format("%.1f", sm.bowlingStrikeRate) else "-"

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(label = "Overs", value = String.format("%.1f", sm.oversBowled), modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Maidens", value = "${sm.maidens}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Runs", value = "${sm.runsConceded}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Wickets", value = "${sm.wickets}", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(label = "BBI", value = sm.bestBowlingInnings, modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Economy", value = String.format("%.2f", sm.bowlingEconomy), modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Bowling Avg", value = bAvgStr, modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Bowling SR", value = bSrStr, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Fielding Card
        val sm = data.matchSummary
        if (sm.catches > 0 || sm.runOuts > 0 || sm.stumpings > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "FIELDING CONTRIBUTIONS",
                            color = PitchModerate,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(label = "Catches", value = "${sm.catches}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Run Outs", value = "${sm.runOuts}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Stumpings", value = "${sm.stumpings}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Total Dismissals", value = "${sm.catches + sm.runOuts + sm.stumpings}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: INNINGS TIMELINE (Phase 6A)
// =========================================================================
@Composable
private fun PlayerInspectorTimelineTab(
    data: PlayerInspectorData,
    modifier: Modifier = Modifier
) {
    if (data.battingInnings.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Did Not Bat in this match",
                color = PitchSecondaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(data.battingInnings) { inn ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Innings Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchAccentBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "INNINGS ${inn.innings}",
                                color = PitchAccentBlue,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Text(
                            text = "${inn.runs} (${inn.balls})",
                            color = PitchPrimaryText,
                            fontSize = 18.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arrival / Departure Context
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = PitchCardInnerBg),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "ARRIVED", color = PitchSecondaryText, fontSize = 9.sp, fontFamily = BreeSerifFontFamily)
                                Text(
                                    text = "${inn.arrivalScore} (${String.format("%.1f", inn.arrivalOvers)} ov)",
                                    color = PitchPrimaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = PitchCardInnerBg),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "DEPARTED", color = PitchSecondaryText, fontSize = 9.sp, fontFamily = BreeSerifFontFamily)
                                val depText = if (inn.isOut) "${inn.departureScore} (${String.format("%.1f", inn.departureOvers)} ov)" else "${inn.departureScore} (Not Out)"
                                Text(
                                    text = depText,
                                    color = if (inn.isOut) PitchNegative else PitchPositive,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Dismissal text if out
                    if (inn.isOut && (inn.scorecardDismissal.isNotBlank() || inn.dismissalType.isNotBlank())) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val dismText = if (inn.scorecardDismissal.isNotBlank()) {
                            inn.scorecardDismissal
                        } else {
                            formatCricketDismissalText(inn.dismissalType, inn.dismissedBy, inn.fielder)
                        }
                        Text(
                            text = dismText,
                            color = PitchSecondaryText,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    // Milestones (50 / 100)
                    if (inn.ballsTo50 > 0 || inn.ballsTo100 > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (inn.ballsTo50 > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E281E))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "50: ${inn.ballsTo50} balls",
                                        color = PitchPositive,
                                        fontSize = 11.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                            if (inn.ballsTo100 > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF28231E))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "100: ${inn.ballsTo100} balls",
                                        color = PitchModerate,
                                        fontSize = 11.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Telemetry & Control %
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatItem(label = "Control %", value = String.format("%.1f%%", inn.controlPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "False Shot %", value = String.format("%.1f%%", inn.falseShotPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Boundary %", value = String.format("%.1f%%", inn.boundaryPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Dot %", value = String.format("%.1f%%", inn.dotPct), modifier = Modifier.weight(1f))
                    }

                    // Innings Partnerships
                    if (inn.partnerships.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PARTNERSHIPS",
                            color = PitchSecondaryText,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        inn.partnerships.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PitchCardInnerBg)
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = p.partnerName,
                                        color = PitchPrimaryText,
                                        fontSize = 12.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = "${p.startScore} → ${p.endScore} (Player: ${p.playerRuns}r, Partner: ${p.partnerRuns}r)",
                                        color = PitchSecondaryText,
                                        fontSize = 10.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${p.runs} (${p.balls}b)",
                                        color = PitchPositive,
                                        fontSize = 13.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = "Imp: ${String.format("%.0f", p.importanceScore)}",
                                        color = PitchSecondaryText,
                                        fontSize = 9.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // Commentary-Derived Wagon Wheel
                    Spacer(modifier = Modifier.height(14.dp))
                    WagonWheelChart(data = inn.wagonWheel)
                }
            }
        }
    }
}

// =========================================================================
// TAB 3: BOWLING TAB (Phase 6B)
// =========================================================================
@Composable
private fun PlayerInspectorBowlingTab(
    data: PlayerInspectorData,
    modifier: Modifier = Modifier
) {
    if (data.bowlingSpells.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Did Not Bowl in this match",
                color = PitchSecondaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Overall Bowling Figures Overview
        item {
            val sm = data.matchSummary
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MATCH BOWLING FIGURES",
                        color = PitchPositive,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatItem(label = "Overs", value = String.format("%.1f", sm.oversBowled), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Runs", value = "${sm.runsConceded}", modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Wickets", value = "${sm.wickets}", modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Economy", value = String.format("%.2f", sm.bowlingEconomy), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatItem(label = "BBI", value = sm.bestBowlingInnings, modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Line Acc", value = String.format("%.1f%%", sm.lineAccuracyPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Length Acc", value = String.format("%.1f%%", sm.lengthAccuracyPct), modifier = Modifier.weight(1f))
                        SummaryStatItem(label = "Dot Ball %", value = String.format("%.1f%%", sm.bowlingDotPct), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Innings Bowling Figures Overview (for each bowled innings)
        if (data.bowlingInnings.isNotEmpty()) {
            items(data.bowlingInnings) { innBowl ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INNINGS ${innBowl.innings} BOWLING FIGURES",
                                color = PitchAccentBlue,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${innBowl.wickets}/${innBowl.runsConceded} (${String.format("%.1f", innBowl.overs)} ov)",
                                color = if (innBowl.wickets > 0) PitchPositive else PitchPrimaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(label = "Overs", value = String.format("%.1f", innBowl.overs), modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Runs", value = "${innBowl.runsConceded}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Wickets", value = "${innBowl.wickets}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Economy", value = String.format("%.2f", innBowl.economy), modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(label = "Spells", value = "${innBowl.spellsCount}", modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Line Acc", value = String.format("%.1f%%", innBowl.lineAccuracyPct), modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Length Acc", value = String.format("%.1f%%", innBowl.lengthAccuracyPct), modifier = Modifier.weight(1f))
                            SummaryStatItem(label = "Dot Ball %", value = String.format("%.1f%%", innBowl.dotPct), modifier = Modifier.weight(1f))
                        }

                        // Combined Commentary-Derived Bowling Heatmap
                        Spacer(modifier = Modifier.height(14.dp))
                        BowlingLengthHeatmap(data = innBowl.heatmap)
                    }
                }
            }
        }

        // Section Title: Continuous Bowling Spells
        item {
            Text(
                text = "CONTINUOUS BOWLING SPELLS",
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }

        // Spells List
        items(data.bowlingSpells) { spell ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INN ${spell.innings} • SPELL ${spell.spellNumber}",
                                color = PitchAccentBlue,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Overs ${spell.startOver}–${spell.endOver} (${spell.oversCount} ov)",
                                color = PitchSecondaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${spell.oversCount}-${spell.maidens}-${spell.runsConceded}-${spell.wickets}",
                                color = if (spell.wickets > 0) PitchPositive else PitchPrimaryText,
                                fontSize = 16.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Econ ${String.format("%.2f", spell.economy)}",
                                color = PitchSecondaryText,
                                fontSize = 10.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PitchCardInnerBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Dots: ${spell.dotPct}%",
                                    color = PitchSecondaryText,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }

                            val (impactLabel, impactColor) = when {
                                spell.wickets >= 3 -> "Match Defining" to PitchPositive
                                spell.wickets >= 2 -> "Key Breakthrough" to PitchPositive
                                spell.wickets == 1 && spell.economy <= 3.0 -> "High Pressure" to PitchAccentBlue
                                spell.wickets == 1 -> "Wicket Taken" to PitchAccentBlue
                                spell.oversCount >= 4 && spell.economy <= 2.50 -> "Tight & Controlled" to PitchModerate
                                spell.oversCount <= 2 && spell.economy == 0.0 -> "Tidy Over" to PitchAccentBlue
                                spell.economy > 5.0 && spell.oversCount >= 2 -> "Expensive" to PitchNegative
                                spell.economy <= 3.5 -> "Building Pressure" to PitchModerate
                                else -> "Contained Spell" to PitchSecondaryText
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(impactColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = impactLabel,
                                    color = impactColor,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = PitchSecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Expanded Details: Wickets list
                    if (expanded && spell.wicketsTakenList.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "DISMISSALS IN THIS SPELL:",
                            color = PitchPositive,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        spell.wicketsTakenList.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { wkt ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PitchPositive)
                                )
                                Text(
                                    text = wkt,
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

// =========================================================================
// TAB 4: MATCH IMPACT (Phase 6C)
// =========================================================================
@Composable
private fun PlayerInspectorImpactTab(
    data: PlayerInspectorData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Impact Score Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MATCH IMPACT RATING",
                        color = PitchPositive,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = String.format("%.1f", data.impactRating),
                                color = PitchPositive,
                                fontSize = 32.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = if (data.impactRank == 1) "Match MVP Performer" else "Impact Ranking #${data.impactRank}",
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%.1f", data.matchRating)} ★",
                                color = getPitchIQRatingColor(data.matchRating),
                                fontSize = 24.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Match Rating",
                                color = PitchSecondaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    if (data.battingImpact > 0.0 || data.bowlingImpact > 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = PitchCardBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryStatItem(
                                label = "Batting Impact",
                                value = String.format("%.1f", data.battingImpact),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatItem(
                                label = "Bowling Impact",
                                value = String.format("%.1f", data.bowlingImpact),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Key Contributions List (Phase 6C)
        if (data.contributions.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "KEY CONTRIBUTIONS",
                            color = PitchAccentBlue,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        data.contributions.forEach { itemText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PitchPositive,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = itemText,
                                    color = PitchPrimaryText,
                                    fontSize = 13.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // Decisive Turning Points
        if (data.turningPoints.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DECISIVE MATCH TURNING POINTS",
                            color = PitchModerate,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        data.turningPoints.forEach { tp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PitchCardInnerBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tp.title,
                                        color = PitchPrimaryText,
                                        fontSize = 12.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = tp.description,
                                        color = PitchSecondaryText,
                                        fontSize = 11.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PitchModerate.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Imp: ${String.format("%.1f", tp.impactScore)}",
                                        color = PitchModerate,
                                        fontSize = 10.sp,
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
}

// Reusable Stat Item Composables
@Composable
private fun MetricHighlightChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardInnerBg),
        modifier = modifier.border(1.dp, PitchCardBorder, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = PitchSecondaryText,
                fontSize = 10.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = PitchPrimaryText,
                fontSize = 16.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private fun formatCricketDismissalText(dismissalType: String, dismissedBy: String, fielder: String): String {
    val dType = dismissalType.trim().lowercase()
    val bowler = dismissedBy.trim()
    val fld = fielder.trim()

    return when {
        dType.contains("not out") -> "not out"
        dType == "bowled" || dType == "b" -> if (bowler.isNotBlank()) "b $bowler" else "bowled"
        dType == "caught" || dType == "c" -> {
            when {
                bowler.isNotBlank() && fld.isNotBlank() -> "c $fld b $bowler"
                bowler.isNotBlank() -> "c b $bowler"
                fld.isNotBlank() -> "c $fld"
                else -> "caught"
            }
        }
        dType == "caught and bowled" || dType == "c & b" -> if (bowler.isNotBlank()) "c & b $bowler" else "c & b"
        dType == "lbw" -> if (bowler.isNotBlank()) "lbw b $bowler" else "lbw"
        dType == "stumped" || dType == "st" -> {
            when {
                bowler.isNotBlank() && fld.isNotBlank() -> "st $fld b $bowler"
                fld.isNotBlank() -> "st $fld"
                bowler.isNotBlank() -> "st b $bowler"
                else -> "stumped"
            }
        }
        dType == "run out" || dType == "run_out" -> if (fld.isNotBlank()) "run out ($fld)" else "run out"
        dType == "hit wicket" -> if (bowler.isNotBlank()) "hit wicket b $bowler" else "hit wicket"
        else -> {
            buildString {
                append(dismissalType.replaceFirstChar { it.uppercase() })
                if (bowler.isNotBlank()) append(" b $bowler")
                if (fld.isNotBlank()) append(" c $fld")
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = PitchSecondaryText,
            fontSize = 10.sp,
            fontFamily = BreeSerifFontFamily
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = PitchPrimaryText,
            fontSize = 13.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}
