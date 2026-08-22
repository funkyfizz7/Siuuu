package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
private val PitchNegative = Color(0xFFEF4444)
private val PitchMomentumAus = Color(0xFFFFC72C)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

@Composable
fun TurningPointsTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
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

        // Section 1: Top Turning Point (Big Hero Card)
        item {
            val topTP = matchData.topTurningPoint
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PitchCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchMomentumAus.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchMomentumAus)
                        ) {
                            Text(
                                text = "TURNING POINT #1",
                                color = PitchMomentumAus,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                        ) {
                            Text(
                                text = "Importance: ${topTP.importanceScore}",
                                color = PitchPositive,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = topTP.title,
                        color = PitchPrimaryText,
                        fontSize = 24.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = topTP.subtitle,
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2 Key Metric Badges Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchBlackBg, RoundedCornerShape(6.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WIN PROBABILITY SHIFT",
                                color = PitchSecondaryText,
                                fontSize = 9.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = topTP.winProbShift,
                                color = PitchPositive,
                                fontSize = 18.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "KEY IMPACT",
                                color = PitchSecondaryText,
                                fontSize = 9.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = topTP.impactHero,
                                color = PitchPrimaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Narrative Box
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
                                .height(38.dp)
                                .background(PitchMomentumAus)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = topTP.narrative,
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 2: Top 5 Match Events
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
                        text = "TOP 5 MATCH EVENTS",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    matchData.topEvents.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number Box
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PitchCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder)
                            ) {
                                Text(
                                    text = "#${event.rank}",
                                    color = PitchPrimaryText,
                                    fontSize = 12.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    color = PitchPrimaryText,
                                    fontSize = 13.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "${event.daySession} • ${event.explanation}",
                                    color = PitchSecondaryText,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Collapses (from fact_match_collapses.csv)
        item {
            val collapse = matchData.primaryCollapse
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
                            text = "COLLAPSES",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${collapse.team} ${collapse.score}",
                                color = PitchPrimaryText,
                                fontSize = 20.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchNegative.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Severity: ${collapse.severity}",
                                color = PitchNegative,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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

        // Section 4: Recoveries (from fact_match_recoveries.csv)
        item {
            val recovery = matchData.primaryRecovery
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
                            text = "RECOVERIES",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recovery.transition,
                            color = PitchPrimaryText,
                            fontSize = 20.sp,
                            fontFamily = BreeSerifFontFamily
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchBlackBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Recovery Score: ${recovery.recoveryScore.toInt()}",
                                color = PitchPositive,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
