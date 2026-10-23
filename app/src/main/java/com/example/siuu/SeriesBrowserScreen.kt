package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardInner = Color(0xFF1A1A1A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchAccentGold = Color(0xFFFFC72C)
private val PitchAccentBlue = Color(0xFF00A3FF)

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

sealed interface SeriesBrowserUiState {
    object Loading : SeriesBrowserUiState
    data class Success(val seriesList: List<SeriesIndexItem>) : SeriesBrowserUiState
    data class Error(val message: String) : SeriesBrowserUiState
}

@Composable
fun SeriesBrowserScreen(
    onSeriesSelected: (Int) -> Unit,
    onNavigateToMatches: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortDescending by remember { mutableStateOf(true) } // Default: Most Recent First

    val uiState by produceState<SeriesBrowserUiState>(initialValue = SeriesBrowserUiState.Loading) {
        try {
            val list = repository.getSeriesList()
            value = SeriesBrowserUiState.Success(list)
        } catch (e: Exception) {
            value = SeriesBrowserUiState.Error(e.localizedMessage ?: "Failed to load series.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Brand Header & Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E1E))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PitchPrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Text(
                        text = "PitchIQ",
                        color = PitchPrimaryText,
                        fontSize = 22.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Series Intelligence Layer",
                        color = PitchSecondaryText,
                        fontSize = 11.5.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
            }

            // Navigation Toggle (Matches / Series)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF181818))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(20.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToMatches() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Matches",
                        color = PitchSecondaryText,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PitchAccentBlue)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Series",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search Ashes, BGT, India, Australia, England...",
                    color = PitchSecondaryText,
                    fontSize = 13.sp,
                    fontFamily = BreeSerifFontFamily
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PitchSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = PitchSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PitchCardInner,
                unfocusedContainerColor = PitchCardInner,
                focusedTextColor = PitchPrimaryText,
                unfocusedTextColor = PitchPrimaryText,
                focusedBorderColor = PitchAccentBlue,
                unfocusedBorderColor = PitchCardBorder
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Category Filter Chips
        val quickFilters = listOf("All", "Ashes", "BGT", "India", "Australia", "England", "South Africa", "New Zealand", "Pakistan", "West Indies")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickFilters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) PitchAccentBlue else PitchCardInner)
                        .border(
                            1.dp,
                            if (isSelected) PitchAccentBlue else PitchCardBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.Black else PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Body
        when (val state = uiState) {
            is SeriesBrowserUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
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
                            text = "Loading Series Archive...",
                            color = PitchSecondaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is SeriesBrowserUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
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

            is SeriesBrowserUiState.Success -> {
                val query = searchQuery.trim().lowercase()

                val filteredList = state.seriesList.filter { item ->
                    val matchesQuery = when {
                        query.isBlank() -> true
                        query == "bgt" || query.contains("border") || query.contains("gavaskar") -> {
                            val teams = setOf(item.team1.lowercase(), item.team2.lowercase())
                            teams.contains("india") && teams.contains("australia")
                        }
                        query == "ashes" -> {
                            item.seriesName.lowercase().contains("ashes") ||
                                    (setOf(item.team1.lowercase(), item.team2.lowercase()) == setOf("australia", "england"))
                        }
                        else -> {
                            item.seriesName.lowercase().contains(query) ||
                                    item.team1.lowercase().contains(query) ||
                                    item.team2.lowercase().contains(query) ||
                                    item.season.lowercase().contains(query) ||
                                    item.winner.lowercase().contains(query) ||
                                    item.venueCountries.lowercase().contains(query)
                        }
                    }

                    val matchesFilter = when (selectedFilter) {
                        "All" -> true
                        "Ashes" -> item.seriesName.contains("Ashes", ignoreCase = true) ||
                                (setOf(item.team1, item.team2) == setOf("Australia", "England"))
                        "BGT" -> setOf(item.team1, item.team2) == setOf("India", "Australia")
                        else -> item.team1.equals(selectedFilter, ignoreCase = true) ||
                                item.team2.equals(selectedFilter, ignoreCase = true)
                    }

                    matchesQuery && matchesFilter
                }

                // Default Sorting: Most Recent First (by startDate descending)
                val sortedList = if (sortDescending) {
                    filteredList.sortedByDescending { it.startDate }
                } else {
                    filteredList.sortedBy { it.startDate }
                }

                // Subheader: count + sort toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sortedList.size} of ${state.seriesList.size} Series",
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { sortDescending = !sortDescending }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (sortDescending) "Recent First ↓" else "Oldest First ↑",
                            color = PitchAccentGold,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (sortedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No series matching criteria.",
                            color = PitchSecondaryText,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedList, key = { it.seriesId }) { seriesItem ->
                            SeriesCard(
                                series = seriesItem,
                                onClick = { onSeriesSelected(seriesItem.seriesId) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Series Card Presentation Component
 */
@Composable
private fun SeriesCard(
    series: SeriesIndexItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Series Name + Season Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = series.seriesName,
                    color = PitchPrimaryText,
                    fontSize = 17.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF222733))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = series.season,
                        color = PitchAccentBlue,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Teams Row & Test Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TeamCircleIndicator(series.team1)
                    Text(
                        text = "${series.team1} vs ${series.team2}",
                        color = PitchPrimaryText.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontFamily = BreeSerifFontFamily
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
                        text = if (series.totalMatches == 1) "1 Test" else "${series.totalMatches} Tests",
                        color = PitchSecondaryText,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Outcome / Result Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formattedResult = formatSeriesOutcome(series)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (series.winner.equals("Drawn", ignoreCase = true) || series.winner.isBlank())
                                    PitchAccentGold
                                else
                                    PitchPositive
                            )
                    )
                    Text(
                        text = formattedResult,
                        color = if (series.winner.equals("Drawn", ignoreCase = true) || series.winner.isBlank())
                            PitchAccentGold
                        else
                            PitchPositive,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                if (series.venueCountries.isNotBlank()) {
                    Text(
                        text = series.venueCountries,
                        color = PitchSecondaryText.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
            }
        }
    }
}

/**
 * Helper to display team color circle indicator
 */
@Composable
private fun TeamCircleIndicator(teamName: String) {
    val color = when {
        teamName.contains("Australia", ignoreCase = true) -> Color(0xFFFFC72C)
        teamName.contains("England", ignoreCase = true) -> Color(0xFF38BDF8)
        teamName.contains("India", ignoreCase = true) -> Color(0xFF1E88E5)
        teamName.contains("South Africa", ignoreCase = true) -> Color(0xFF007A3D)
        teamName.contains("New Zealand", ignoreCase = true) -> Color(0xFF64748B)
        teamName.contains("Pakistan", ignoreCase = true) -> Color(0xFF16A34A)
        teamName.contains("Sri Lanka", ignoreCase = true) -> Color(0xFF0F3875)
        teamName.contains("West Indies", ignoreCase = true) -> Color(0xFF9E1B32)
        teamName.contains("Bangladesh", ignoreCase = true) -> Color(0xFF006A4E)
        else -> Color(0xFFA0A0A0)
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Format standard series winner summary string
 */
private fun formatSeriesOutcome(series: SeriesIndexItem): String {
    val isAshes = series.seriesName.contains("Ashes", ignoreCase = true) ||
            (setOf(series.team1, series.team2) == setOf("Australia", "England"))

    return when {
        series.winner.equals("Drawn", ignoreCase = true) || series.winner.isBlank() -> {
            if (isAshes && series.season.contains("2019")) {
                "Australia retained Ashes (${series.resultMargin})"
            } else if (series.resultMargin.isNotBlank() && series.resultMargin != "0-0") {
                "Series Drawn (${series.resultMargin})"
            } else {
                "Series Drawn"
            }
        }
        isAshes -> {
            "${series.winner} won the Ashes (${series.resultMargin})"
        }
        series.resultMargin.isNotBlank() -> {
            "${series.winner} won (${series.resultMargin})"
        }
        else -> {
            "${series.winner} won"
        }
    }
}
