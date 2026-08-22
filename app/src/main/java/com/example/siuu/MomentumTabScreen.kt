package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// PitchIQ Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchNegative = Color(0xFFEF4444)
private val PitchMomentumAus = Color(0xFFFFC72C)
private val PitchMomentumEng = Color(0xFF00A3FF)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

@Composable
fun MomentumTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    // Default selected point: Session 3 (Smith rescue)
    var selectedPoint by remember { mutableStateOf<SessionMomentumPoint?>(matchData.points.getOrNull(2)) }

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

        // 2. Match Momentum Flow Card (Control section removed from Momentum tab as requested!)
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
                            text = "MATCH MOMENTUM FLOW",
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

                    // The Chart (with luminous colors from image 2 and no lower legend)
                    FotMobMomentumChart(
                        matchData = matchData,
                        selectedIndex = selectedPoint?.let { sp -> matchData.points.indexOfFirst { it.sessionIndex == sp.sessionIndex } },
                        onPointSelected = { selectedPoint = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        breeFont = BreeSerifFontFamily
                    )
                }
            }
        }

        // 3. Session Intelligence Inspector Card
        item {
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { point ->
                    val isTeam1Leading = point.momentumScore >= 0
                    val accentColor = if (isTeam1Leading) PitchMomentumAus else PitchMomentumEng

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                Column {
                                    Text(
                                        text = "DAY ${point.dayNumber} · SESS ${point.sessionOfDay} (INN ${point.innings})",
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
                                    color = Color(0xFF000000),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                                ) {
                                    Text(
                                        text = "Score: %.1f".format(point.momentumScore),
                                        color = accentColor,
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
                                    .background(Color(0xFF000000), RoundedCornerShape(6.dp))
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

                            // Narrative Story Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(34.dp)
                                        .background(accentColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point.contextStory,
                                    color = PitchSecondaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 17.sp
                                )
                            }

                            // Swing Tag
                            if (point.sessionIndex > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val isPositiveSwing = point.momentumSwing >= 0
                                val swingColor = if (isPositiveSwing) PitchPositive else PitchNegative

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFF000000), RoundedCornerShape(4.dp))
                                        .border(1.dp, swingColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPositiveSwing) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = swingColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Swing: " + (if (point.momentumSwing > 0) "+" else "") + "%.1f".format(point.momentumSwing) + " (${point.momentumLabel})",
                                        color = swingColor,
                                        fontSize = 11.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Biggest Momentum Shifts
        item {
            val topSwings = remember(matchData) {
                matchData.points
                    .filter { it.sessionIndex > 1 }
                    .sortedByDescending { abs(it.momentumSwing) }
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
                        text = "BIGGEST MOMENTUM SHIFTS",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    topSwings.forEachIndexed { idx, swing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Day ${swing.dayNumber} · Sess ${swing.sessionOfDay} (${swing.battingTeam})",
                                    color = PitchPrimaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = swing.contextStory,
                                    color = PitchSecondaryText,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    maxLines = 1
                                )
                            }

                            val isPositive = swing.momentumSwing > 0
                            val badgeColor = if (isPositive) PitchPositive else PitchNegative

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF000000),
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                            ) {
                                Text(
                                    text = (if (isPositive) "+" else "") + "%.1f".format(swing.momentumSwing),
                                    color = badgeColor,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
