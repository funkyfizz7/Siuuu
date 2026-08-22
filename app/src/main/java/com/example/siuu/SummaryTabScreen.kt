package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchMomentumAus = Color(0xFFFFC72C)
private val PitchMomentumEng = Color(0xFF00A3FF)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

@Composable
fun SummaryTabScreen(
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
        // Section 1: Match Header
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

        // Section 2: Match Control Share
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
                        text = "MATCH CONTROL SHARE",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal control bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF1E1E1E))
                    ) {
                        if (matchData.team1ControlPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(matchData.team1ControlPct.toFloat())
                                    .fillMaxHeight()
                                    .background(PitchMomentumAus)
                            )
                        }
                        if (matchData.evenControlPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(matchData.evenControlPct.toFloat())
                                    .fillMaxHeight()
                                    .background(PitchCardBorder)
                            )
                        }
                        if (matchData.team2ControlPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(matchData.team2ControlPct.toFloat())
                                    .fillMaxHeight()
                                    .background(PitchMomentumEng)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PitchMomentumAus)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${matchData.team1.shortName}: ${matchData.team1ControlPct}%",
                                color = PitchMomentumAus,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }

                        Text(
                            text = "Even: ${matchData.evenControlPct}%",
                            color = PitchSecondaryText,
                            fontSize = 11.sp,
                            fontFamily = BreeSerifFontFamily
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PitchMomentumEng)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${matchData.team2.shortName}: ${matchData.team2ControlPct}%",
                                color = PitchMomentumEng,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Match Summary
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
                        text = "MATCH SUMMARY",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    matchData.matchSummaryPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = PitchPositive,
                                modifier = Modifier
                                    .size(6.dp)
                                    .padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point,
                                color = PitchPrimaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Key Numbers
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
                        text = "KEY NUMBERS",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryMetric("LEAD", matchData.keyNumbers.firstInningsLead)
                        SummaryMetric("TARGET", matchData.keyNumbers.targetSet)
                        SummaryMetric("MoM", matchData.keyNumbers.manOfTheMatch)
                    }
                }
            }
        }

        // Section 5: Match Heroes
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
                        text = "MATCH HEROES",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    matchData.heroes.forEach { hero ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = hero.name,
                                        color = PitchPrimaryText,
                                        fontSize = 14.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${hero.team})",
                                        color = PitchSecondaryText,
                                        fontSize = 11.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = hero.headlineStat,
                                    color = PitchPositive,
                                    fontSize = 11.sp,
                                    fontFamily = BreeSerifFontFamily
                                )

                                Spacer(modifier = Modifier.height(1.dp))

                                Text(
                                    text = hero.impactSnippet,
                                    color = PitchSecondaryText,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    maxLines = 1
                                )
                            }

                            // Match Rating Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF000000),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PitchPositive,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "%.1f".format(hero.rating),
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
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryMetric(title: String, value: String) {
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
            fontSize = 14.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}
