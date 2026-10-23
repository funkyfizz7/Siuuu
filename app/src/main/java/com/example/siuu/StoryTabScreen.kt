package com.example.siuu

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
private val PitchMomentumAus = Color(0xFFFFC72C)

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
 * Story UI State Representation (FutureBuilder pattern in Jetpack Compose)
 */
sealed interface StoryUiState {
    object Loading : StoryUiState
    data class Success(val data: StoryTabDisplayData) : StoryUiState
    data class Error(val message: String) : StoryUiState
}

/**
 * Reusable Match Story Tab
 * Accepts matchId, queries MatchRepository, generates dynamic timeline from MatchStoryModel,
 * renders Collapse from fact_match_collapses and Recovery from fact_match_recoveries.
 */
@Composable
fun StoryTabScreen(
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

    val uiState by produceState<StoryUiState>(
        initialValue = StoryUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = StoryUiState.Loading
        try {
            val storyData = repository.getStoryTabData(matchId)
            value = StoryUiState.Success(storyData)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            value = StoryUiState.Error(e.localizedMessage ?: "Failed to load match story. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is StoryUiState.Loading -> {
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
                            text = "Loading Match Story...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is StoryUiState.Error -> {
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
                                text = "Unable to Load Match Story",
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

            is StoryUiState.Success -> {
                StoryContent(data = state.data)
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun StoryTabScreen(
    matchId: Int,
    modifier: Modifier = Modifier
) {
    StoryTabScreen(
        matchId = matchId.toLong(),
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun StoryTabScreen(
    matchData: MatchIntelligenceData,
    modifier: Modifier = Modifier
) {
    StoryTabScreen(
        matchId = matchData.matchId,
        modifier = modifier
    )
}

@Composable
private fun StoryContent(
    data: StoryTabDisplayData,
    modifier: Modifier = Modifier
) {
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
        // Section 1: Match Story Timeline (Dynamically generated from MatchStoryModel)
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
                    Text(
                        text = "MATCH STORY TIMELINE",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    data.timeline.forEachIndexed { index, story ->
                        TimelineStoryRow(
                            story = story,
                            teamColor = data.header.team1Color,
                            isLast = index == data.timeline.size - 1
                        )
                    }
                }
            }
        }

        // Section 2: Biggest Collapse (Dynamically driven by fact_match_collapses / CollapseModel)
        item {
            val collapse = data.primaryCollapse
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchNegative.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
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
                            text = "BIGGEST COLLAPSE",
                            color = PitchNegative,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchNegative)
                        ) {
                            Text(
                                text = collapse.classification,
                                color = PitchNegative,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${collapse.team} ${collapse.score}",
                        color = PitchPrimaryText,
                        fontSize = 20.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                                .height(32.dp)
                                .background(PitchNegative)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = collapse.explanation,
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 3: Best Recovery (Dynamically driven by fact_match_recoveries / RecoveryModel)
        item {
            val recovery = data.primaryRecovery
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchPositive.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
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
                            text = "BEST RECOVERY",
                            color = PitchPositive,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                        ) {
                            Text(
                                text = recovery.classification,
                                color = PitchPositive,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = recovery.transition,
                        color = PitchPrimaryText,
                        fontSize = 20.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                                .height(32.dp)
                                .background(PitchPositive)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = recovery.explanation,
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 4: Match Narrative (Dynamically driven by fact_match_story)
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
                    Text(
                        text = "MATCH NARRATIVE",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(8.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(96.dp)
                                .background(data.header.team1Color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = data.narrative,
                            color = PitchPrimaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimelineStoryRow(
    story: MatchStoryModel,
    teamColor: Color,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Day Node & Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = PitchBlackBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, teamColor)
            ) {
                Text(
                    text = "DAY ${story.dayNumber}",
                    color = teamColor,
                    fontSize = 10.sp,
                    fontFamily = BreeSerifFontFamily,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(PitchCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Headline & Snippet
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Text(
                text = story.headline,
                color = PitchPrimaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = story.summary,
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 15.sp
            )
        }
    }
}
