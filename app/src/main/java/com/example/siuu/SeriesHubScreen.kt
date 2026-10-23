package com.example.siuu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
private val PitchCardInner = Color(0xFF1A1A1A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchAccentGold = Color(0xFFFFC72C)
private val PitchAccentBlue = Color(0xFF3B82F6)
private val PitchAccentCyan = Color(0xFF06B6D4)

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

// 4 Top-Level Tabs for Series Hub
enum class SeriesHubTab(val label: String) {
    MATCHES("MATCHES"),
    SERIES_STATS("SERIES STATS"),
    SERIES_XI("SERIES XI"),
    NEWS("NEWS")
}

sealed interface SeriesHubUiState {
    object Loading : SeriesHubUiState
    data class Success(
        val overviewData: SeriesOverviewData,
        val timelineData: SeriesTimelineData,
        val intelligenceData: SeriesIntelligenceData
    ) : SeriesHubUiState
    data class Error(val message: String) : SeriesHubUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesHubScreen(
    seriesId: Int,
    onBack: () -> Unit,
    onNavigateToMatch: ((Long) -> Unit)? = null,
    onNavigateToPlayer: ((Long, Long) -> Unit)? = null,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SeriesHubTab.MATCHES) }
    var selectedStatCategory by remember { mutableStateOf<SeriesLeaderboardCategory?>(null) }

    val uiState by produceState<SeriesHubUiState>(
        initialValue = SeriesHubUiState.Loading,
        key1 = seriesId
    ) {
        value = SeriesHubUiState.Loading
        try {
            val overview = repository.getSeriesOverview(seriesId)
            val timeline = repository.getSeriesTimeline(seriesId)
            val intel = repository.getSeriesIntelligence(seriesId)
            value = SeriesHubUiState.Success(
                overviewData = overview,
                timelineData = timeline,
                intelligenceData = intel
            )
        } catch (e: Exception) {
            value = SeriesHubUiState.Error(e.localizedMessage ?: "Failed to load series hub data.")
        }
    }

    if (selectedStatCategory != null) {
        val seriesTitle = (uiState as? SeriesHubUiState.Success)?.overviewData?.header?.seriesName ?: ""
        SeriesStatDedicatedScreen(
            category = selectedStatCategory!!,
            seriesName = seriesTitle,
            onBack = { selectedStatCategory = null },
            onNavigateToPlayer = onNavigateToPlayer
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SERIES HUB",
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
                is SeriesHubUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PitchAccentBlue,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Series Hub...",
                                color = PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }
                }

                is SeriesHubUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }

                is SeriesHubUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Series Header Card
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            SeriesHubHeaderCard(
                                header = state.overviewData.header,
                                duration = state.intelligenceData.seriesDuration,
                                matchesCount = state.intelligenceData.matchesPlayed
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 4 TOP-LEVEL TABS: [ MATCHES ]  [ SERIES STATS ]  [ SERIES XI ]  [ NEWS ]
                        ScrollableTabRow(
                            selectedTabIndex = activeTab.ordinal,
                            containerColor = PitchBlackBg,
                            contentColor = PitchPrimaryText,
                            edgePadding = 12.dp,
                            divider = {
                                HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
                            },
                            indicator = { tabPositions ->
                                if (activeTab.ordinal in tabPositions.indices) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                                        height = 2.5.dp,
                                        color = PitchAccentBlue
                                    )
                                }
                            }
                        ) {
                            SeriesHubTab.values().forEach { tab ->
                                val selected = activeTab == tab
                                Tab(
                                    selected = selected,
                                    onClick = { activeTab = tab },
                                    text = {
                                        Text(
                                            text = tab.label,
                                            color = if (selected) PitchPrimaryText else PitchSecondaryText,
                                            fontSize = 12.5.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                )
                            }
                        }

                        // Content Body based on Tab
                        when (activeTab) {
                            SeriesHubTab.MATCHES -> {
                                SeriesMatchesListTab(
                                    timelineData = state.timelineData,
                                    onNavigateToMatch = onNavigateToMatch
                                )
                            }

                            SeriesHubTab.SERIES_STATS -> {
                                SeriesIntelligenceStatsSubTab(
                                    intel = state.intelligenceData,
                                    onNavigateToPlayer = onNavigateToPlayer,
                                    onOpenCategory = { category ->
                                        selectedStatCategory = category
                                    }
                                )
                            }

                            SeriesHubTab.SERIES_XI -> {
                                SeriesComingSoonTab(
                                    title = "Series XI",
                                    description = "PitchIQ Combined XI, tactical formations, and player impact ratings for ${state.overviewData.header.seriesName} are coming soon.",
                                    icon = Icons.Default.Shield,
                                    accentColor = PitchAccentGold
                                )
                            }

                            SeriesHubTab.NEWS -> {
                                SeriesComingSoonTab(
                                    title = "Series News & Reports",
                                    description = "Editorial analysis, match breakdowns, and post-series highlights for ${state.overviewData.header.seriesName} are coming soon.",
                                    icon = Icons.Default.Newspaper,
                                    accentColor = PitchAccentBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



/* ==========================================================================================
 * TAB 1: MATCHES (Scrollable vertical list matching Home Screen card design)
 * ========================================================================================== */

@Composable
private fun SeriesMatchesListTab(
    timelineData: SeriesTimelineData,
    onNavigateToMatch: ((Long) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(timelineData.timeline) { index, match ->
            SeriesMatchTimelineCard(
                match = match,
                index = index,
                onClick = {
                    if (match.matchId > 0L) {
                        onNavigateToMatch?.invoke(match.matchId)
                    }
                }
            )
        }
    }
}

/**
 * Individual Match Timeline Card matching Home Screen Card Style
 */
@Composable
private fun SeriesMatchTimelineCard(
    match: SeriesTimelineItem,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDrawn = match.winner.equals("Draw", ignoreCase = true) ||
            match.winner.equals("Drawn", ignoreCase = true) ||
            match.winner.equals("Match Drawn", ignoreCase = true)
    val resultColor = if (isDrawn) PitchAccentGold else PitchPositive

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Header: "1st Test • Venue"
            val headerTitle = if (match.venue.isNotBlank()) {
                "${match.matchNumber.ifBlank { "Test ${index + 1}" }} • ${match.venue}"
            } else {
                match.matchNumber.ifBlank { "Test ${index + 1}" }
            }

            Text(
                text = headerTitle,
                color = PitchPrimaryText,
                fontSize = 12.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Team 1 Line: CountryBadge on left, Score on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryBadge(
                    teamCode = match.team1Short.ifBlank { match.team1 }
                )
                Text(
                    text = match.team1Score.ifBlank { "Yet to bat" },
                    color = PitchPrimaryText,
                    fontSize = 16.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Team 2 Line: CountryBadge on left, Score on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryBadge(
                    teamCode = match.team2Short.ifBlank { match.team2 }
                )
                Text(
                    text = match.team2Score.ifBlank { "Yet to bat" },
                    color = PitchPrimaryText,
                    fontSize = 16.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Below scores: Outcome Result text
            val outcomeText = when {
                isDrawn -> "Match Drawn"
                match.margin.isNotBlank() -> "${match.winner} won by ${match.margin}"
                else -> "${match.winner} won"
            }

            Text(
                text = outcomeText,
                color = resultColor,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ==========================================================================================
 * TAB 2 — SUB-TAB 1: OVERVIEW (30s Series Understanding)
 * ========================================================================================== */

@Composable
private fun SeriesIntelligenceOverviewSubTab(
    intel: SeriesIntelligenceData,
    onNavigateToMatch: ((Long) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Series Header Card
        item {
            OverviewHeaderCard(intel = intel)
        }

        // Section 2: Series Story (4 Bullets)
        item {
            OverviewStoryCard(story = intel.story)
        }

        // Section 3: Series Momentum Flow (Horizontal timeline)
        item {
            OverviewMomentumFlowSection(
                nodes = intel.momentumNodes,
                onNavigateToMatch = onNavigateToMatch
            )
        }
    }
}

@Composable
private fun OverviewHeaderCard(
    intel: SeriesIntelligenceData,
    modifier: Modifier = Modifier
) {
    val header = intel.header
    val isDrawn = header.winner.equals("Drawn", ignoreCase = true) || header.winner.isBlank()
    val isAshes = header.seriesName.contains("Ashes", ignoreCase = true) ||
            (setOf(header.team1, header.team2) == setOf("Australia", "England"))

    val resultSummary = when {
        isDrawn && isAshes && (header.season.contains("2019") || header.seriesName.contains("2019")) -> "Australia retained the Ashes"
        isDrawn -> "Series Drawn"
        isAshes -> "${header.winner} won the Ashes"
        else -> "${header.winner} won the series"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Matchup Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = header.seriesName.ifBlank { "Test Series" },
                    color = PitchPrimaryText,
                    fontSize = 17.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchCardInner)
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (header.totalMatches == 1) "1 Test" else "${header.totalMatches} Tests",
                        color = PitchSecondaryText,
                        fontSize = 11.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${header.team1} vs ${header.team2}",
                color = PitchAccentBlue,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Result & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINAL RESULT",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = resultSummary,
                        color = if (isDrawn) PitchAccentGold else PitchPositive,
                        fontSize = 13.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchCardInner)
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = header.finalSeriesScore.ifBlank { header.resultMargin },
                        color = PitchPrimaryText,
                        fontSize = 13.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aggregates: Matches Played & Series Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PitchCardInner)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MATCHES PLAYED",
                        color = PitchSecondaryText,
                        fontSize = 9.5.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                    Text(
                        text = "${intel.matchesPlayed} Tests",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SERIES DURATION",
                        color = PitchSecondaryText,
                        fontSize = 9.5.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                    Text(
                        text = intel.seriesDuration.ifBlank { header.season },
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewStoryCard(
    story: SeriesStoryData,
    modifier: Modifier = Modifier
) {
    val bullets = listOfNotNull(
        if (story.bulletStart.isNotBlank()) "How series started" to story.bulletStart else null,
        if (story.bulletTurning.isNotBlank()) "Major momentum shift" to story.bulletTurning else null,
        if (story.bulletMomentum.isNotBlank()) "Decisive phase" to story.bulletMomentum else null,
        if (story.bulletConclusion.isNotBlank()) "How series ended" to story.bulletConclusion else null
    )

    if (bullets.isEmpty()) return

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SERIES STORY",
                color = PitchPrimaryText,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            bullets.forEachIndexed { index, (bulletTitle, bulletContent) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PitchAccentBlue)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bulletTitle.uppercase(),
                            color = PitchAccentBlue,
                            fontSize = 10.5.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = bulletContent,
                            color = PitchPrimaryText.copy(alpha = 0.92f),
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
                if (index < bullets.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewMomentumFlowSection(
    nodes: List<SeriesMomentumNode>,
    onNavigateToMatch: ((Long) -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (nodes.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoGraph,
                contentDescription = null,
                tint = PitchAccentBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "SERIES MOMENTUM FLOW",
                color = PitchPrimaryText,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(nodes) { node ->
                OverviewMomentumNodeCard(
                    node = node,
                    onClick = {
                        if (node.matchId > 0L) {
                            onNavigateToMatch?.invoke(node.matchId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OverviewMomentumNodeCard(
    node: SeriesMomentumNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDrawn = node.winner.equals("Draw", ignoreCase = true) || node.winner.equals("Drawn", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .width(260.dp)
            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top: Match Number & Running Score (e.g. Test 1 | AUS 1-0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.matchNumber.ifBlank { "Test Match" },
                    color = PitchPrimaryText,
                    fontSize = 12.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PitchCardInner)
                        .border(1.dp, PitchAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = node.scoreAfterMatch.ifBlank { "-" },
                        color = PitchAccentGold,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Result
            val resultHeadline = when {
                isDrawn -> "Match Drawn"
                node.margin.isNotBlank() -> "${node.winner} won by ${node.margin}"
                else -> "${node.winner} won"
            }
            Text(
                text = resultHeadline,
                color = if (isDrawn) PitchAccentGold else PitchPositive,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )

            // Venue
            if (node.venue.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = node.venue,
                    color = PitchSecondaryText,
                    fontSize = 11.sp,
                    fontFamily = BreeSerifFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Key Turning Point
            if (node.keyTurningPoint.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "TURNING POINT: ${node.keyTurningPoint}",
                    color = PitchSecondaryText,
                    fontSize = 10.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            // MVP
            if (node.mvpPlayer.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MVP: ${node.mvpPlayer}" + if (node.mvpStat.isNotBlank()) " (${node.mvpStat})" else "",
                    color = PitchAccentGold,
                    fontSize = 10.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ==========================================================================================
 * TAB 2 — SUB-TAB 2: STATS (Batting & Bowling Sections with Top 3 & View All)
 * ========================================================================================== */

private fun isBattingCategory(title: String): Boolean {
    val upper = title.uppercase().trim()
    if (upper.contains("BOWL") || upper.contains("WICKET") || upper.contains("ECONOM") || upper.contains("DOT") || upper.contains("ACCURACY") || upper.contains("PRESSURE")) {
        return false
    }
    return upper.contains("RUN") ||
            upper.contains("BAT") ||
            upper.contains("SCORE") ||
            upper.contains("CENTUR") ||
            upper.contains("FIFTI") ||
            upper.contains("SIX") ||
            upper.contains("BOUNDAR") ||
            upper.contains("PARTNER") ||
            upper.contains("CONTROL") ||
            upper.contains("FALSE SHOT") ||
            upper.contains("AVERAGE") ||
            upper.contains("IMPACT")
}

@Composable
private fun SeriesIntelligenceStatsSubTab(
    intel: SeriesIntelligenceData,
    onNavigateToPlayer: ((Long, Long) -> Unit)?,
    onOpenCategory: (SeriesLeaderboardCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val battingCategories = remember(intel.statCategories) {
        intel.statCategories.filter { isBattingCategory(it.categoryTitle) }
    }
    val bowlingCategories = remember(intel.statCategories) {
        intel.statCategories.filter { !isBattingCategory(it.categoryTitle) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BATTING SECTION
        if (battingCategories.isNotEmpty()) {
            item(key = "header_batting") {
                SectionHeaderTitle(
                    title = "BATTING LEADERBOARDS",
                    accentColor = PitchAccentGold
                )
            }

            items(battingCategories, key = { "bat_${it.categoryTitle}" }) { category ->
                LeaderboardCategoryCard(
                    category = category,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onOpenCategory = onOpenCategory
                )
            }
        }

        // BOWLING SECTION
        if (bowlingCategories.isNotEmpty()) {
            item(key = "header_bowling") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeaderTitle(
                    title = "BOWLING LEADERBOARDS",
                    accentColor = PitchAccentCyan
                )
            }

            items(bowlingCategories, key = { "bowl_${it.categoryTitle}" }) { category ->
                LeaderboardCategoryCard(
                    category = category,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onOpenCategory = onOpenCategory
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderTitle(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 15.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Text(
            text = title,
            color = PitchPrimaryText,
            fontSize = 13.5.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun LeaderboardCategoryCard(
    category: SeriesLeaderboardCategory,
    onNavigateToPlayer: ((Long, Long) -> Unit)?,
    onOpenCategory: (SeriesLeaderboardCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedItems = category.leaders.take(3)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)) {
            // Category Header (Clean - title only)
            Text(
                text = category.categoryTitle.uppercase(),
                color = PitchPrimaryText,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Player Rows (Top 3)
            displayedItems.forEachIndexed { index, item ->
                SeriesStatLeaderRowItem(
                    item = item,
                    showSecondaryDetails = false,
                    categoryTitle = category.categoryTitle,
                    onClick = {
                        if (item.playerId > 0L) {
                            onNavigateToPlayer?.invoke(item.matchId, item.playerId)
                        }
                    }
                )
                if (index < displayedItems.size - 1) {
                    HorizontalDivider(
                        color = PitchCardBorder.copy(alpha = 0.5f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            // All Button (opens dedicated screen)
            if (category.leaders.size > 3) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchCardInner)
                            .clickable {
                                onOpenCategory(category)
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            color = PitchAccentBlue,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/* ==========================================================================================
 * DEDICATED FULL-SCREEN STAT LEADERBOARD
 * ========================================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesStatDedicatedScreen(
    category: SeriesLeaderboardCategory,
    seriesName: String,
    onBack: () -> Unit,
    onNavigateToPlayer: ((Long, Long) -> Unit)?,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = category.categoryTitle.uppercase(),
                            color = PitchPrimaryText,
                            fontSize = 15.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        if (seriesName.isNotBlank()) {
                            Text(
                                text = seriesName,
                                color = PitchSecondaryText,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PitchBlackBg),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)) {
                        category.leaders.forEachIndexed { index, item ->
                            SeriesStatLeaderRowItem(
                                item = item,
                                showSecondaryDetails = true,
                                categoryTitle = category.categoryTitle,
                                onClick = {
                                    if (item.playerId > 0L) {
                                        onNavigateToPlayer?.invoke(item.matchId, item.playerId)
                                    }
                                }
                            )
                            if (index < category.leaders.size - 1) {
                                HorizontalDivider(
                                    color = PitchCardBorder.copy(alpha = 0.5f),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesStatLeaderRowItem(
    item: SeriesStatLeaderItem,
    showSecondaryDetails: Boolean = false,
    categoryTitle: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teamPillColor = getTeamColor(item.country)
    val context = LocalContext.current
    val isImageReady = remember(item.playerId, item.playerName) {
        PlayerImageLoader.isPlayerImageReady(context, item.playerId, item.playerName)
    }

    val upperCat = categoryTitle.uppercase().trim()
    val isBowling = upperCat.contains("BOWL") ||
            upperCat.contains("WICKET") ||
            upperCat.contains("ECONOM") ||
            upperCat.contains("DOT") ||
            upperCat.contains("ACCURACY") ||
            upperCat.contains("PRESSURE")

    val isPartnership = upperCat.contains("PARTNER")
    val isControlledRuns = upperCat.contains("CONTROLLED RUNS")
    val isCountLeaderboard = upperCat.contains("CENTUR") ||
            upperCat.contains("FIFTI") ||
            upperCat.contains("SIX") ||
            upperCat.contains("FOUR") ||
            upperCat.contains("BOUNDAR")

    val subHeaderFontSize = when {
        // Bowling: all bowling leaderboards use 10.5.sp (same as Most Wickets)
        isBowling -> 10.5.sp
        // Most Runs: detailed volume stat
        upperCat.equals("MOST RUNS", ignoreCase = true) -> 10.5.sp
        // Most Controlled Runs: slightly increased font size
        isControlledRuns -> 12.sp
        // Most Centuries, Most Fifties, Most Sixes, Most Fours, Most Boundaries: increased font size
        isCountLeaderboard -> 12.5.sp
        // Partnership venue / balls
        isPartnership -> 12.sp
        // Cleaned 2-part stats (e.g. "211 runs • 379 balls" in Best Control, Lowest False Shot, Ratings, etc.):
        else -> 12.sp
    }

    val playerNameFontSize = if (isPartnership) 13.5.sp else 16.sp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Serial No
        Text(
            text = "${item.rank}",
            color = if (item.rank == 1) PitchAccentGold else PitchSecondaryText,
            fontSize = 14.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.widthIn(min = 20.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Player Circular Image Avatar (Enlarged for clear visibility)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PitchCardInner),
            contentAlignment = Alignment.Center
        ) {
            if (isImageReady) {
                PitchIQPlayerAvatar(
                    playerId = item.playerId,
                    playerName = item.playerName,
                    contentDescription = item.playerName,
                    modifier = Modifier.fillMaxSize(),
                    border = BorderStroke(1.2.dp, PitchCardBorder),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = item.playerName.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "PI" }
                Text(
                    text = initials,
                    color = PitchPrimaryText,
                    fontSize = 13.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.2.dp, PitchCardBorder, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Player Name & Subtitle Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.playerName,
                color = PitchPrimaryText,
                fontSize = playerNameFontSize,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = if (isPartnership) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            if (!showSecondaryDetails) {
                // Initial View (Top-3 Cards): Country flag (rounded shape) + country name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(PitchCardInner),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getTeamFlagEmoji(item.country),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = item.country.ifBlank { "Team" },
                        color = PitchSecondaryText,
                        fontSize = subHeaderFontSize,
                        fontFamily = BreeSerifFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Dedicated "All" Screen: Detailed matches, innings, avg / stats info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (item.country.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(PitchCardInner),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getTeamFlagEmoji(item.country),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = item.secondaryStat.ifBlank { item.country },
                        color = PitchSecondaryText,
                        fontSize = subHeaderFontSize,
                        fontFamily = BreeSerifFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Stat value display:
        // Keep colored pill only for 1st player in preview cards (!showSecondaryDetails && item.rank == 1)
        // When clicked all (showSecondaryDetails == true) or rank > 1: remove pills, only white font
        if (!showSecondaryDetails && item.rank == 1) {
            val pillTextColor = getContrastingTextColor(teamPillColor)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(teamPillColor)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.statValue,
                    color = pillTextColor,
                    fontSize = 15.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            Text(
                text = item.statValue,
                color = PitchPrimaryText,
                fontSize = 16.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

/* ==========================================================================================
 * SERIES XI & NEWS: COMING SOON TAB
 * ========================================================================================== */

@Composable
private fun SeriesComingSoonTab(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
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
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    color = PitchPrimaryText,
                    fontSize = 17.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PitchCardInner,
                    border = BorderStroke(1.dp, PitchCardBorder)
                ) {
                    Text(
                        text = "COMING SOON",
                        color = PitchAccentBlue,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    color = PitchSecondaryText,
                    fontSize = 12.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/* ==========================================================================================
 * HEADER CARD COMPONENT
 * ========================================================================================== */

@Composable
private fun SeriesHubHeaderCard(
    header: SeriesHeaderData,
    duration: String,
    matchesCount: Int,
    modifier: Modifier = Modifier
) {
    val isDrawn = header.winner.equals("Drawn", ignoreCase = true) || header.winner.isBlank()
    val isAshes = header.seriesName.contains("Ashes", ignoreCase = true) ||
            (setOf(header.team1, header.team2) == setOf("Australia", "England"))

    val resultOutcome = when {
        isDrawn && isAshes && (header.season.contains("2019") || header.seriesName.contains("2019")) -> "Australia retained Ashes"
        isDrawn -> "Series Drawn"
        isAshes -> "${header.winner} won the Ashes"
        else -> "${header.winner} won"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title + Season Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = header.seriesName,
                    color = PitchPrimaryText,
                    fontSize = 18.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                if (header.season.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PitchCardInner)
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = header.season,
                            color = PitchSecondaryText,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Matchup Subtitle
            Text(
                text = "${header.team1} vs ${header.team2}",
                color = PitchSecondaryText,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Score Pill + Tests Count + Outcome
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDrawn) PitchAccentGold.copy(alpha = 0.15f) else PitchPositive.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (isDrawn) PitchAccentGold.copy(alpha = 0.5f) else PitchPositive.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = header.finalSeriesScore.ifBlank { header.resultMargin },
                            color = if (isDrawn) PitchAccentGold else PitchPositive,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchCardInner)
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (header.totalMatches == 1) "1 Test" else "${header.totalMatches} Tests",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = resultOutcome,
                    color = if (isDrawn) PitchAccentGold else PitchPositive,
                    fontSize = 12.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private fun getContrastingTextColor(backgroundColor: Color): Color {
    val luminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
    return if (luminance > 0.90f) Color(0xFF000000) else Color(0xFFFFFFFF)
}

private fun getTeamColor(teamName: String): Color {
    val t = teamName.lowercase().trim()
    return when {
        t.contains("australia") || t == "aus" -> Color(0xFFFFC72C) // Vibrant Aussie Gold / Yellow
        t.contains("england") || t == "eng" -> Color(0xFF38BDF8)   // Vibrant England Sky Blue
        t.contains("india") || t == "ind" -> Color(0xFF1E88E5)     // Royal Blue
        t.contains("west indies") || t.contains("windies") || t == "wi" -> Color(0xFF9E1B32) // Maroon
        t.contains("pakistan") || t == "pak" -> Color(0xFF16A34A)  // Green
        t.contains("bangladesh") || t == "ban" -> Color(0xFF006A4E)// Dark Green
        t.contains("new zealand") || t == "nz" -> Color(0xFF475569)// Slate / Black
        t.contains("sri lanka") || t == "sl" -> Color(0xFF0F3875)  // Dark Navy Blue
        t.contains("south africa") || t == "sa" || t == "rsa" -> Color(0xFF007A3D) // Protea Green
        t.contains("afghanistan") || t == "afg" -> Color(0xFF0066CC) // Cobalt Blue
        t.contains("ireland") || t == "ire" -> Color(0xFF2ECC71)    // Emerald Green
        t.contains("zimbabwe") || t == "zim" -> Color(0xFFDC2626)   // Red
        else -> Color(0xFF38BDF8)
    }
}
