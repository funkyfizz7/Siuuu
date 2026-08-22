package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
private val PitchModerate = Color(0xFFFFC72C)
private val PitchNegative = Color(0xFFEF4444)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

@Composable
fun PressureTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    // Default selected point: Day 1 Sess 2 (84.8 Extreme Pressure)
    var selectedPoint by remember { mutableStateOf<SessionPressurePoint?>(matchData.pressurePoints.getOrNull(1)) }

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
        // Match Header
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
                        fontSize = 11.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${matchData.team1.name} vs ${matchData.team2.name}",
                        color = PitchPrimaryText,
                        fontSize = 22.sp,
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
                            fontSize = 13.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        Text(
                            text = matchData.venue.substringBefore(","),
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }
        }

        // Section 1: Pressure Flow Graph Card
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
                            text = "PRESSURE FLOW GRAPH",
                            color = PitchPrimaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Touch to inspect",
                            color = PitchSecondaryText,
                            fontSize = 10.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // The 0-100 Pressure Chart
                    PressureFlowChart(
                        matchData = matchData,
                        selectedIndex = selectedPoint?.let { sp -> matchData.pressurePoints.indexOfFirst { it.sessionIndex == sp.sessionIndex } },
                        onPointSelected = { selectedPoint = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        breeFont = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Pressure Zone Badges Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchCardInnerBg, RoundedCornerShape(6.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZoneBadge("Low (0-40)", PitchPositive)
                        ZoneBadge("Moderate (40-70)", PitchModerate)
                        ZoneBadge("Extreme (70-100)", PitchNegative)
                    }
                }
            }
        }

        // Section 2 & 3: Session Inspector & Pressure Explanation
        item {
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { point ->
                    val zoneColor = when {
                        point.pressureScore >= 70f -> PitchNegative
                        point.pressureScore >= 40f -> PitchModerate
                        else -> PitchPositive
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, zoneColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row: Session & Pressure Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "DAY ${point.dayNumber} • SESSION ${point.sessionOfDay} (INN ${point.innings})",
                                        color = PitchSecondaryText,
                                        fontSize = 10.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${point.battingTeam} Batting",
                                        color = PitchPrimaryText,
                                        fontSize = 16.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PitchBlackBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, zoneColor)
                                ) {
                                    Text(
                                        text = "Pressure: %.1f".format(point.pressureScore),
                                        color = zoneColor,
                                        fontSize = 12.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 Flat Metric Tiles
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MetricTile("RUNS", "${point.runs}")
                                MetricTile("WICKETS", "${point.wickets}")
                                MetricTile("OVERS", "${point.overs}")
                                MetricTile("RUN RATE", "%.2f".format(point.runRate))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Section 3: Pressure Explanation Narrative Box
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
                                        .height(34.dp)
                                        .background(zoneColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point.explanation,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Zone Label Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(PitchBlackBg, RoundedCornerShape(4.dp))
                                    .border(1.dp, zoneColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(zoneColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = point.pressureZone,
                                    color = zoneColor,
                                    fontSize = 11.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Highest Pressure Moments
        item {
            val highestMoments = remember(matchData) {
                matchData.pressurePoints
                    .sortedByDescending { it.pressureScore }
                    .take(3)
            }

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
                        text = "HIGHEST PRESSURE MOMENTS",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    highestMoments.forEach { moment ->
                        val zoneColor = when {
                            moment.pressureScore >= 70f -> PitchNegative
                            moment.pressureScore >= 40f -> PitchModerate
                            else -> PitchPositive
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Day ${moment.dayNumber} • Session ${moment.sessionOfDay} (${moment.battingTeam})",
                                    color = PitchPrimaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = moment.explanation,
                                    color = PitchSecondaryText,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PitchBlackBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, zoneColor)
                            ) {
                                Text(
                                    text = "%.1f".format(moment.pressureScore),
                                    color = zoneColor,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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

@Composable
private fun ZoneBadge(label: String, color: Color) {
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

@Composable
private fun MetricTile(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = PitchSecondaryText,
            fontSize = 9.sp,
            fontFamily = BreeSerifFontFamily,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = PitchPrimaryText,
            fontSize = 15.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}
