package com.example.siuu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)

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

enum class MatchTab(val label: String) {
    SUMMARY("Summary"),
    SCORECARD("Scorecard"),
    FACTS("Facts"),
    RATINGS("Ratings")
}

@Composable
fun PitchIQMatchScreen(
    matchId: Long = 1152846L,
    onBack: (() -> Unit)? = null,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(MatchTab.SUMMARY) }

    var isAssistantOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Navigation Bar: Back to Match Browser + Ask PitchIQ Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { onBack() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PitchPrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back",
                            color = PitchPrimaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // "Ask PitchIQ" Pill Button (Top Right Header Placement)
                Surface(
                    onClick = { isAssistantOpen = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1E1E),
                    border = BorderStroke(1.dp, PitchCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✦",
                            color = Color(0xFFFFC72C), // PitchAccentYellow
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask PitchIQ",
                            color = PitchPrimaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // App Bar & Tab Navigation (Strict 6 Tab Production Order: Summary, Momentum, Pressure, Ratings, Scorecard, Turning Points)
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
                            color = PitchPrimaryText
                        )
                    }
                }
            ) {
                MatchTab.values().forEach { tab ->
                    val selected = activeTab == tab
                    Tab(
                        selected = selected,
                        onClick = { activeTab = tab },
                        text = {
                            Text(
                                text = tab.label,
                                color = if (selected) PitchPrimaryText else PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Active Tab Screen (Summary | Scorecard | Facts | Ratings)
            when (activeTab) {
                MatchTab.SUMMARY -> SummaryTabScreen(
                    matchId = matchId,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onNavigateToTurningPoints = { activeTab = MatchTab.FACTS }
                )
                MatchTab.SCORECARD -> ScorecardTabScreen(
                    matchId = matchId,
                    onNavigateToPlayer = onNavigateToPlayer
                )
                MatchTab.FACTS -> FactsTabScreen(
                    matchId = matchId,
                    onNavigateToPlayer = onNavigateToPlayer
                )
                MatchTab.RATINGS -> PlayerRatingsTabScreen(
                    matchId = matchId,
                    onNavigateToPlayer = onNavigateToPlayer
                )
            }
        }

        // Full Screen Assistant Modal when Open
        if (isAssistantOpen) {
            com.example.siuu.assistant.AssistantScreen(
                assistantContext = com.example.siuu.assistant.AssistantContext(
                    currentMatchId = matchId,
                    currentTab = activeTab.label
                ),
                onBack = { isAssistantOpen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
