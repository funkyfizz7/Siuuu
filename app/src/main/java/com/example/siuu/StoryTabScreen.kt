package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class TimelineItem(
    val dayNumber: Int,
    val headline: String,
    val snippet: String
)

@Composable
fun StoryTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    val timeline = listOf(
        TimelineItem(1, "Australia collapse to 122/8", "Broad and Woakes tear through top order before Smith begins epic recovery."),
        TimelineItem(2, "Steve Smith scores 144", "Tail-end partnership with Siddle lifts Australia from disaster to 284."),
        TimelineItem(3, "England secure 90-run lead", "Rory Burns hits gritty 133 to steer England to 374 in reply."),
        TimelineItem(4, "Smith scores another century", "Smith (142) and Wade (110) push lead to 397 before declaration."),
        TimelineItem(5, "Nathan Lyon takes six wickets", "Lyon (6/49) destroys England on crumbling pitch to seal 251-run win.")
    )

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

        // Section 1: Match Story Timeline
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

                    timeline.forEachIndexed { index, item ->
                        TimelineRow(
                            item = item,
                            isLast = index == timeline.size - 1
                        )
                    }
                }
            }
        }

        // Section 2: Biggest Collapse
        item {
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
                                text = "Major Collapse",
                                color = PitchNegative,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Australia 122/8",
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
                            text = "Australia lost control before Smith rescued them.",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 3: Best Recovery
        item {
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
                                text = "Major Recovery",
                                color = PitchPositive,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "122/8 → 284",
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
                            text = "Smith and the tail transformed the innings.",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 4: Match Narrative
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
                                .background(PitchMomentumAus)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Australia's decision to bat first at Edgbaston seemed fatal after slumping to 122/8 on Day 1. However, Steve Smith's heroic 144 resurrected the innings to 284. England replied with 374 on the back of Rory Burns' 133 to gain a 90-run lead. The Test turned completely on Day 4 when Smith scored his second century (142) alongside Matthew Wade (110), setting England a daunting 398-run chase. On Day 5, Nathan Lyon tore through England's batting with 6/49 to wrap up one of the greatest comeback victories in Ashes history.",
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
private fun TimelineRow(
    item: TimelineItem,
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
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMomentumAus)
            ) {
                Text(
                    text = "DAY ${item.dayNumber}",
                    color = PitchMomentumAus,
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
                text = item.headline,
                color = PitchPrimaryText,
                fontSize = 14.sp,
                fontFamily = BreeSerifFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.snippet,
                color = PitchSecondaryText,
                fontSize = 11.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 15.sp
            )
        }
    }
}
