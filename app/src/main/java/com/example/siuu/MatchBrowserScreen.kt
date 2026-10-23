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
private val PitchCardInner = Color(0xFF0A0A0A)
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

sealed interface MatchBrowserUiState {
    object Loading : MatchBrowserUiState
    data class Success(val matches: List<MatchIndexItem>) : MatchBrowserUiState
    data class Error(val message: String) : MatchBrowserUiState
}

@Composable
fun MatchBrowserScreen(
    onMatchSelected: (Long) -> Unit,
    onNavigateToSeries: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSeason by remember { mutableStateOf("All") }

    val uiState by produceState<MatchBrowserUiState>(initialValue = MatchBrowserUiState.Loading) {
        try {
            val list = repository.getMatchList()
            value = MatchBrowserUiState.Success(list)
        } catch (e: Exception) {
            value = MatchBrowserUiState.Error(e.localizedMessage ?: "Failed to load matches.")
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
                        text = "Test Cricket Match Intelligence",
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
                        .background(PitchAccentBlue)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Matches",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToSeries() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Series",
                        color = PitchSecondaryText,
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily
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
                    text = "Search by team, venue or season...",
                    color = PitchSecondaryText,
                    fontSize = 13.sp,
                    fontFamily = BreeSerifFontFamily
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PitchCardInner,
                unfocusedContainerColor = PitchCardInner,
                focusedBorderColor = PitchPrimaryText,
                unfocusedBorderColor = PitchCardBorder,
                focusedTextColor = PitchPrimaryText,
                unfocusedTextColor = PitchPrimaryText
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState) {
            is MatchBrowserUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PitchPositive,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            is MatchBrowserUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color(0xFFEF4444),
                        fontFamily = BreeSerifFontFamily
                    )
                }
            }

            is MatchBrowserUiState.Success -> {
                val allMatches = state.matches

                // Distinct seasons
                val seasons = remember(allMatches) {
                    listOf("All") + allMatches.map { it.season }.distinct().sortedDescending()
                }

                // Season Filter Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    seasons.forEach { season ->
                        val isSelected = selectedSeason == season
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PitchPrimaryText else PitchCardBg)
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedSeason = season }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = season,
                                color = if (isSelected) PitchBlackBg else PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter matches
                val filteredMatches = remember(allMatches, searchQuery, selectedSeason) {
                    allMatches.filter { match ->
                        val matchSeason = (selectedSeason == "All" || match.season == selectedSeason)
                        val matchSearch = searchQuery.isBlank() ||
                                match.matchTitle.contains(searchQuery, ignoreCase = true) ||
                                match.team1.contains(searchQuery, ignoreCase = true) ||
                                match.team2.contains(searchQuery, ignoreCase = true) ||
                                match.venue.contains(searchQuery, ignoreCase = true) ||
                                match.winner.contains(searchQuery, ignoreCase = true)
                        matchSeason && matchSearch
                    }
                }

                if (filteredMatches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "No matches found matching \"$searchQuery\"",
                            color = PitchSecondaryText,
                            fontFamily = BreeSerifFontFamily,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredMatches, key = { it.matchId }) { match ->
                            MatchCardItem(
                                match = match,
                                onClick = { onMatchSelected(match.matchId) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCardItem(
    match: MatchIndexItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Series Name / Trophy + Season
            Text(
                text = match.seriesHeader.ifBlank { match.matchTitle }.cleanYearBrackets(),
                color = PitchPrimaryText,
                fontSize = 13.5.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Sub-Header: "1st Test, Galle"
            Text(
                text = match.testSubHeader.ifBlank { match.venue },
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Team 1 Line: "IND  600 & 240/3"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.team1Short.ifBlank { match.team1.take(3).uppercase() },
                    color = PitchPrimaryText,
                    fontSize = 16.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = match.team1Score.ifBlank { "Yet to bat" },
                    color = PitchPrimaryText,
                    fontSize = 16.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Team 2 Line: "SL   291 & 245"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.team2Short.ifBlank { match.team2.take(3).uppercase() },
                    color = PitchPrimaryText,
                    fontSize = 16.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = match.team2Score.ifBlank { "Yet to bat" },
                    color = PitchPrimaryText,
                    fontSize = 16.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PitchCardBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Result ("India won by 304 runs" / "Match Drawn")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDrawn = match.winner.contains("Drawn", ignoreCase = true) || match.victoryMargin.contains("Drawn", ignoreCase = true)
                Text(
                    text = if (isDrawn) "Match Drawn" else match.victoryMargin.ifBlank { "${match.winner} won" },
                    color = if (isDrawn) PitchSecondaryText else PitchPositive,
                    fontSize = 12.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = "View Intelligence →",
                    color = PitchPrimaryText,
                    fontSize = 12.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
