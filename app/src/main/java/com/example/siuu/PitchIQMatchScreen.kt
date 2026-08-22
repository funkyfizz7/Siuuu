package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    Font(R.font.bree_serif, FontWeight.Normal)
)

enum class MatchTab(val label: String) {
    SUMMARY("Summary"),
    MOMENTUM("Momentum"),
    STORY("Story"),
    PRESSURE("Pressure"),
    TURNING_POINTS("Turning Points"),
    RATINGS("Ratings"),
    SCORECARD("Scorecard")
}

@Composable
fun PitchIQMatchScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(MatchTab.SUMMARY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Bar & Tab Navigation
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
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Active Tab Screen (All 7 Core Match Tabs Fully Implemented!)
        when (activeTab) {
            MatchTab.SUMMARY -> SummaryTabScreen(matchData = matchData)
            MatchTab.MOMENTUM -> MomentumTabScreen(matchData = matchData)
            MatchTab.STORY -> StoryTabScreen(matchData = matchData)
            MatchTab.PRESSURE -> PressureTabScreen(matchData = matchData)
            MatchTab.TURNING_POINTS -> TurningPointsTabScreen(matchData = matchData)
            MatchTab.RATINGS -> PlayerRatingsTabScreen(matchData = matchData)
            MatchTab.SCORECARD -> ScorecardTabScreen(matchData = matchData)
        }
    }
}
