package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardInner = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchAccentGold = Color(0xFFFFC72C)
private val PitchAccentBlue = Color(0xFF00A3FF)
private val PitchAccentRed = Color(0xFFEF4444)

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

data class TrendingNewsItem(
    val id: String,
    val tag: String,
    val title: String,
    val excerpt: String,
    val readTime: String,
    val date: String
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val featuredMatches: List<HomeFeaturedMatchItem>,
        val featuredSeries: List<SeriesIndexItem>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * PitchIQ Home Screen
 * Landing page containing:
 * 1. HEADER (PitchIQ, Test Match Intelligence, Search Icon)
 * 2. MATCH CAROUSEL (7 swipe cards from fact_home_featured_matches.csv)
 * 3. BROWSE SERIES (horizontal chips)
 * 4. TRENDING NEWS (placeholder)
 */
@Composable
fun HomeScreen(
    onMatchSelected: (Long) -> Unit,
    onSeriesSelected: (Int) -> Unit,
    onBrowseAllMatches: () -> Unit = {},
    onBrowseAllSeries: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }

    val uiState by produceState<HomeUiState>(initialValue = HomeUiState.Loading) {
        try {
            val featuredMatches = repository.getHomeFeaturedMatches()
            val allSeries = repository.getSeriesList()
            
            value = HomeUiState.Success(
                featuredMatches = featuredMatches,
                featuredSeries = allSeries
            )
        } catch (e: Exception) {
            value = HomeUiState.Error(e.localizedMessage ?: "Failed to load home data.")
        }
    }

    val trendingStories = listOf(
        TrendingNewsItem(
            id = "story_1",
            tag = "TACTICAL ANALYSIS",
            title = "The Anatomy of 4th-Innings Chases in Modern Test Cricket",
            excerpt = "How scoreboard pressure, pitch deterioration, and ball age dictate historic Day 5 run chases.",
            readTime = "4 min read",
            date = "Sep 15, 2026"
        ),
        TrendingNewsItem(
            id = "story_2",
            tag = "PITCHIQ DEEP DIVE",
            title = "Why Context-Aware Pressure Beats Traditional Run-Rate Metrics",
            excerpt = "Why a wicket at 18/2 with the new ball carries 5x more leverage than a wicket at 450/7.",
            readTime = "6 min read",
            date = "Sep 14, 2026"
        )
    )

    val latestNews = listOf(
        TrendingNewsItem(
            id = "news_1",
            tag = "HISTORIC RIVALRIES",
            title = "Border-Gavaskar & The Ashes: The Data Behind Decisive Sessions",
            excerpt = "Analyzing 69 Test series across 166 warehouse matches through PitchIQ intelligence.",
            readTime = "5 min read",
            date = "Sep 12, 2026"
        ),
        TrendingNewsItem(
            id = "news_2",
            tag = "SERIES REPORT",
            title = "Headingley 2019 to Gabba 2021: Turning Points That Defined Eras",
            excerpt = "Statistical deep dive into match-defining sessions and individual masterclasses under extreme pressure.",
            readTime = "7 min read",
            date = "Sep 10, 2026"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PitchAccentBlue, strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Loading PitchIQ...",
                            color = PitchSecondaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = PitchAccentRed,
                        fontSize = 14.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
                    // 1. App Header (STEP 2)
                    item {
                        HomeHeader(
                            onSearchClicked = {
                                showSearchDialog = true
                                onSearchClicked()
                            }
                        )
                    }

                    // 2. Featured Matches Carousel (7 swipe cards)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        FeaturedMatchesSection(
                            matches = state.featuredMatches,
                            onMatchSelected = onMatchSelected,
                            onSeriesSelected = onSeriesSelected,
                            onViewAll = onBrowseAllMatches
                        )
                    }

                    // 3. Browse Series Section (Horizontal chips)
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        BrowseSeriesSection(
                            seriesList = state.featuredSeries,
                            onSeriesSelected = onSeriesSelected,
                            onViewAll = onBrowseAllSeries
                        )
                    }

                    // 4. Trending Stories Section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        TrendingStoriesSection(storiesList = trendingStories)
                    }

                    // 5. Latest News Section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        LatestNewsSection(newsList = latestNews)
                    }
                }
            }
        }
    }

    if (showSearchDialog) {
        HomeSearchDialog(
            onDismiss = { showSearchDialog = false },
            onNavigateToMatches = {
                showSearchDialog = false
                onBrowseAllMatches()
            },
            onNavigateToSeries = {
                showSearchDialog = false
                onBrowseAllSeries()
            }
        )
    }
}

/**
 * 1. Home Header (STEP 2)
 * Top section:
 * - PitchIQ
 * - Test Match Intelligence
 * Right side:
 * - Search Icon
 */
@Composable
private fun HomeHeader(
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "PitchIQ",
                color = PitchPrimaryText,
                fontSize = 24.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Test Match Intelligence",
                color = PitchSecondaryText,
                fontSize = 12.5.sp,
                fontFamily = BreeSerifFontFamily
            )
        }

        // Right side: Search Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(PitchCardBg)
                .border(1.dp, PitchCardBorder, CircleShape)
                .clickable { onSearchClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PitchPrimaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Search Dialog highlighting future capabilities:
 * - AI Assistant
 * - Player Search
 * - Series Search
 * - Match Search
 */
@Composable
private fun HomeSearchDialog(
    onDismiss: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToSeries: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .clickable(enabled = false) {}, // Prevent dismiss when tapping card
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search & Intelligence",
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PitchSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Input Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search matches, series, players or ask AI...",
                                color = PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = PitchSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchAccentBlue,
                            unfocusedBorderColor = PitchCardBorder,
                            focusedTextColor = PitchPrimaryText,
                            unfocusedTextColor = PitchPrimaryText,
                            cursorColor = PitchAccentBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "INTELLIGENCE HUBS",
                        color = PitchSecondaryText,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Future Feature 1: AI Assistant
                    SearchFeatureTile(
                        icon = Icons.Default.AutoAwesome,
                        iconTint = PitchAccentGold,
                        title = "AI Assistant",
                        subtitle = "Ask natural questions about pressure shifts, collapses & spell impact",
                        badge = "COMING SOON",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Future Feature 2: Player Search
                    SearchFeatureTile(
                        icon = Icons.Default.Person,
                        iconTint = PitchAccentBlue,
                        title = "Player Search",
                        subtitle = "Search 1,200+ Test cricketers, spell breakdown and milestone stats",
                        badge = "INDEXED",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Future Feature 3: Series Search
                    SearchFeatureTile(
                        icon = Icons.Default.EmojiEvents,
                        iconTint = PitchAccentGold,
                        title = "Series Search",
                        subtitle = "Browse 69 bilateral Test series and trophy histories",
                        badge = "ACTIVE",
                        onClick = onNavigateToSeries
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Feature 4: Match Search
                    SearchFeatureTile(
                        icon = Icons.Default.SportsCricket,
                        iconTint = PitchPositive,
                        title = "Match Search",
                        subtitle = "Diagnose 166 warehouse Test matches and scorecards",
                        badge = "166 MATCHES",
                        onClick = onNavigateToMatches
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFeatureTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PitchCardInner)
            .border(1.dp, PitchCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = PitchPrimaryText,
                    fontSize = 13.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF252525))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = if (badge == "COMING SOON") PitchAccentGold else PitchSecondaryText,
                        fontSize = 9.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 2. Featured Matches Carousel Section (7 swipe cards)
 */
@Composable
private fun FeaturedMatchesSection(
    matches: List<HomeFeaturedMatchItem>,
    onMatchSelected: (Long) -> Unit,
    onSeriesSelected: (Int) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .background(PitchAccentGold, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FEATURED MATCHES",
                    color = PitchPrimaryText,
                    fontSize = 14.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "See all →",
                color = PitchAccentGold,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel (7 swipe cards)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(matches, key = { it.matchId }) { match ->
                FeaturedMatchCard(
                    match = match,
                    onMatchIntelligenceClicked = { onMatchSelected(match.matchId) },
                    onSeriesClicked = { onSeriesSelected(match.seriesId) }
                )
            }
        }
    }
}

/**
 * Single Featured Match Card for Carousel
 *
 * Top:
 * 1st Test • Ashes 2019
 * Small text:
 * Edgbaston
 *
 * Middle:
 * ENG      374 & 146
 * AUS      487 & 401/8d
 *
 * Below scores:
 * Australia won by 251 runs
 *
 * Bottom Row:
 * MATCH INTELLIGENCE           SERIES →
 * Exactly two actions.
 */
@Composable
private fun FeaturedMatchCard(
    match: HomeFeaturedMatchItem,
    onMatchIntelligenceClicked: () -> Unit,
    onSeriesClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .width(295.dp)
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            .clickable { onMatchIntelligenceClicked() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top: 1st Test • Ashes 2019
            Text(
                text = "${match.matchNumber} • ${match.seriesName.cleanYearBrackets()}",
                color = PitchPrimaryText,
                fontSize = 12.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Small text: Edgbaston
            Text(
                text = match.venue,
                color = PitchSecondaryText,
                fontSize = 10.5.sp,
                fontFamily = BreeSerifFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Middle: Scoreboard block with CountryBadge
            // Team 1 Line (e.g. [ 🇦🇺 AUS ]          450 & 200/5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryBadge(
                    flagUrl = null,
                    teamCode = match.team1Short
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

            // Team 2 Line (e.g. [ 🇮🇳 IND ]          410 & 95/2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryBadge(
                    flagUrl = null,
                    teamCode = match.team2Short
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
            val isDrawn = match.winner.equals("Draw", ignoreCase = true) || match.winner.equals("Match Drawn", ignoreCase = true)
            Text(
                text = match.victoryMargin.ifBlank { if (isDrawn) "Match Drawn" else "${match.winner} won" },
                color = if (isDrawn) PitchAccentGold else PitchPositive,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = PitchCardBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: SCHEDULE →
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onSeriesClicked() }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SCHEDULE",
                        color = Color(0xFF3B82F6),
                        fontSize = 13.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "→",
                        color = Color(0xFF3B82F6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 3. Browse Series Section (Horizontal Chips)
 */
@Composable
private fun BrowseSeriesSection(
    seriesList: List<SeriesIndexItem>,
    onSeriesSelected: (Int) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .background(PitchAccentBlue, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BROWSE SERIES",
                    color = PitchPrimaryText,
                    fontSize = 14.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "View all →",
                color = PitchAccentBlue,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Chips Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(seriesList, key = { it.seriesId }) { series ->
                SeriesHorizontalChip(
                    series = series,
                    onClick = { onSeriesSelected(series.seriesId) }
                )
            }
        }
    }
}

/**
 * Single Series Horizontal Chip
 */
@Composable
private fun SeriesHorizontalChip(
    series: SeriesIndexItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PitchCardBg)
            .border(1.dp, PitchCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = series.seriesName.cleanYearBrackets(),
                color = PitchPrimaryText,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF202020))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = series.season,
                    color = PitchSecondaryText,
                    fontSize = 10.sp,
                    fontFamily = BreeSerifFontFamily
                )
            }
        }
    }
}

/**
 * 4. Trending Stories Section (Placeholder)
 */
@Composable
private fun TrendingStoriesSection(
    storiesList: List<TrendingNewsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .background(PitchPositive, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TRENDING STORIES",
                color = PitchPrimaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            storiesList.forEach { story ->
                NewsStoryCard(item = story)
            }
        }
    }
}

/**
 * 5. Latest News Section (Placeholder)
 */
@Composable
private fun LatestNewsSection(
    newsList: List<TrendingNewsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .background(PitchAccentGold, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LATEST NEWS",
                color = PitchPrimaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            newsList.forEach { news ->
                NewsStoryCard(item = news)
            }
        }
    }
}

@Composable
private fun NewsStoryCard(
    item: TrendingNewsItem
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E2430))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.tag,
                        color = PitchAccentBlue,
                        fontSize = 9.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${item.date} • ${item.readTime}",
                    color = PitchSecondaryText,
                    fontSize = 10.5.sp,
                    fontFamily = BreeSerifFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                color = PitchPrimaryText,
                fontSize = 14.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.excerpt,
                color = PitchSecondaryText,
                fontSize = 12.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 16.sp
            )
        }
    }
}
