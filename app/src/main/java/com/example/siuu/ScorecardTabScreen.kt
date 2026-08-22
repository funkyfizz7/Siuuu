package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val PitchMomentumAus = Color(0xFFFFC72C)
private val PitchMomentumEng = Color(0xFF00A3FF)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

private fun getWicketSuffix(w: Int): String = when (w) {
    1 -> "st"
    2 -> "nd"
    3 -> "rd"
    else -> "th"
}

@Composable
fun ScorecardTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    var selectedInningsIdx by remember { mutableStateOf(0) }
    val currentInnings = matchData.scorecards.getOrNull(selectedInningsIdx) ?: matchData.scorecards.first()
    val teamColor = if (currentInnings.teamName == "Australia") PitchMomentumAus else PitchMomentumEng

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 56.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Match Header
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
                        text = matchData.matchTitle.uppercase(),
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${matchData.team1.name} vs ${matchData.team2.name}",
                        color = PitchPrimaryText,
                        fontSize = 24.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${matchData.winner} ${matchData.victoryMargin}",
                            color = PitchPositive,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        Text(
                            text = matchData.venue.substringBefore(","),
                            color = PitchSecondaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }
        }

        // 2. Innings Selector Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchCardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                matchData.scorecards.forEachIndexed { idx, inn ->
                    val isSelected = selectedInningsIdx == idx
                    val innColor = if (inn.teamName == "Australia") PitchMomentumAus else PitchMomentumEng

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, innColor.copy(alpha = 0.6f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedInningsIdx = idx }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Inn ${inn.inningsNumber}",
                                color = if (isSelected) innColor else PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = inn.teamName.take(3).uppercase(),
                                color = if (isSelected) PitchPrimaryText else PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 3. Innings Total Header Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, teamColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentInnings.teamName.uppercase()} INNINGS ${currentInnings.inningsNumber}",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentInnings.totalScore,
                            color = PitchPrimaryText,
                            fontSize = 26.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PitchBlackBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, teamColor)
                    ) {
                        Text(
                            text = "RR: %.2f".format(currentInnings.runRate),
                            color = teamColor,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. Batting Table (Top line: Name + Stats aligned on same horizontal baseline, Bottom line: Dismissal)
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
                        text = "BATTING",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BATTER",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "R",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = "B",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = "4s",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = "6s",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = "SR",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Batters List
                    currentInnings.batters.forEachIndexed { idx, batter ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 10.dp)
                        ) {
                            // LINE 1: Batter Name + Stats on the exact same horizontal row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = batter.name,
                                    color = PitchPrimaryText,
                                    fontSize = 15.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 6.dp)
                                )
                                Text(
                                    text = "${batter.runs}",
                                    color = if (batter.runs >= 50) PitchPositive else PitchPrimaryText,
                                    fontSize = 15.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = "${batter.balls}",
                                    color = PitchSecondaryText,
                                    fontSize = 14.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = "${batter.fours}",
                                    color = PitchSecondaryText,
                                    fontSize = 14.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(30.dp)
                                )
                                Text(
                                    text = "${batter.sixes}",
                                    color = PitchSecondaryText,
                                    fontSize = 14.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(30.dp)
                                )
                                Text(
                                    text = "%.1f".format(batter.strikeRate),
                                    color = PitchSecondaryText,
                                    fontSize = 14.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(50.dp)
                                )
                            }

                            // LINE 2: Dismissal details right below Batter Name
                            if (batter.dismissal.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = batter.dismissal,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }
                        }

                        if (idx < currentInnings.batters.size - 1) {
                            HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Extras & Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extras: ${currentInnings.extras}",
                            color = PitchSecondaryText,
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        Text(
                            text = "Total: ${currentInnings.totalScore}",
                            color = PitchPrimaryText,
                            fontSize = 14.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Bowling Table
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
                        text = "BOWLING",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bowling Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BOWLER",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "O",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(40.dp)
                        )
                        Text(
                            text = "M",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = "R",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(38.dp)
                        )
                        Text(
                            text = "W",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = "ECON",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    currentInnings.bowlers.forEachIndexed { idx, bowler ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bowler.name,
                                color = PitchPrimaryText,
                                fontSize = 15.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = "%.1f".format(bowler.overs),
                                color = PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = "${bowler.maidens}",
                                color = PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(30.dp)
                            )
                            Text(
                                text = "${bowler.runs}",
                                color = PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(38.dp)
                            )
                            Text(
                                text = "${bowler.wickets}",
                                color = if (bowler.wickets >= 3) PitchPositive else PitchPrimaryText,
                                fontSize = 15.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(30.dp)
                            )
                            Text(
                                text = "%.2f".format(bowler.economy),
                                color = PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(50.dp)
                            )
                        }

                        if (idx < currentInnings.bowlers.size - 1) {
                            HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // 6. Fall of Wickets (Line by Line Format!)
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
                        text = "FALL OF WICKETS",
                        color = PitchPrimaryText,
                        fontSize = 16.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Header Row for Line-by-Line FOW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCORE",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.width(65.dp)
                        )
                        Text(
                            text = "BATTER",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "OVER",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Line by Line FOW Items
                    currentInnings.fallOfWickets.forEachIndexed { idx, fow ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 7.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fow.score,
                                color = PitchPrimaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(65.dp)
                            )
                            Text(
                                text = fow.batterName,
                                color = PitchSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${fow.over} ov",
                                color = PitchSecondaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(60.dp)
                            )
                        }

                        if (idx < currentInnings.fallOfWickets.size - 1) {
                            HorizontalDivider(color = PitchCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // 7. All Partnerships (with individual contributions) + Key Partnerships
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
                        Text(
                            text = "PARTNERSHIPS",
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Individual Breakdown",
                            color = PitchSecondaryText,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    currentInnings.allPartnerships.forEach { part ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .background(if (part.isKey) PitchCardInnerBg else PitchBlackBg, RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (part.isKey) PitchPositive.copy(alpha = 0.5f) else PitchCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            // Top Row: Wicket Number + Total Runs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${part.wicketNumber}${getWicketSuffix(part.wicketNumber)} Wkt Stand",
                                        color = PitchPrimaryText,
                                        fontSize = 14.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (part.isKey && part.intelligenceLabel.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PitchPositive.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                                        ) {
                                            Text(
                                                text = part.intelligenceLabel,
                                                color = PitchPositive,
                                                fontSize = 10.sp,
                                                fontFamily = BreeSerifFontFamily,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${part.totalRuns} runs (${part.totalBalls}b)",
                                    color = if (part.isKey) PitchPositive else PitchPrimaryText,
                                    fontSize = 14.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Middle Row: Individual Contributions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${part.batter1Name}  ${part.batter1Runs} (${part.batter1Balls})",
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                                Text(
                                    text = "${part.batter2Runs} (${part.batter2Balls})  ${part.batter2Name}",
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Bottom Row: Proportional Horizontal Split Bar
                            val b1Pct = if (part.totalRuns > 0) part.batter1Runs.toFloat() / part.totalRuns.toFloat() else 0.5f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF1E2330))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(b1Pct.coerceIn(0.05f, 0.95f))
                                        .background(teamColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - b1Pct).coerceIn(0.05f, 0.95f))
                                        .background(teamColor.copy(alpha = 0.35f))
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
