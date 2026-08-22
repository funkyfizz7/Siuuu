package com.example.siuu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
private val PitchFieldBg = Color(0xFF0A0D14)
private val PitchFieldBorder = Color(0xFF1F2633)
private val PitchFieldLine = Color(0x18FFFFFF)
private val PitchCardInnerBg = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchModerate = Color(0xFFFF9F1C)
private val PitchNegative = Color(0xFFEF4444)
private val PitchMomentumAus = Color(0xFFFFC72C)
private val PitchMomentumEng = Color(0xFF00A3FF)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

enum class LineupTeam { AUSTRALIA, ENGLAND }
enum class LineupViewMode { FIELD, LIST }

@Composable
fun PlayerRatingsTabScreen(
    matchData: MatchIntelligenceData = PrototypeMatchData.ashes2019FirstTest,
    modifier: Modifier = Modifier
) {
    var selectedTeam by remember { mutableStateOf(LineupTeam.AUSTRALIA) }
    var viewMode by remember { mutableStateOf(LineupViewMode.FIELD) }

    val activeTeamXI = if (selectedTeam == LineupTeam.AUSTRALIA) matchData.australiaXI else matchData.englandXI
    val teamColor = if (selectedTeam == LineupTeam.AUSTRALIA) PitchMomentumAus else PitchMomentumEng

    var selectedPlayer by remember(selectedTeam) {
        mutableStateOf(activeTeamXI.firstOrNull { it.isMotm } ?: activeTeamXI.firstOrNull())
    }

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

        // 2. Team Selector & View Mode Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team Switcher Pills
                Row(
                    modifier = Modifier
                        .background(PitchCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    TeamTabPill(
                        label = "Australia (XI)",
                        isSelected = selectedTeam == LineupTeam.AUSTRALIA,
                        activeColor = PitchMomentumAus,
                        onClick = { selectedTeam = LineupTeam.AUSTRALIA }
                    )
                    TeamTabPill(
                        label = "England (XI)",
                        isSelected = selectedTeam == LineupTeam.ENGLAND,
                        activeColor = PitchMomentumEng,
                        onClick = { selectedTeam = LineupTeam.ENGLAND }
                    )
                }

                // Field vs List View Mode Toggle
                Row(
                    modifier = Modifier
                        .background(PitchCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    ViewModeButton(
                        label = "Field",
                        isSelected = viewMode == LineupViewMode.FIELD,
                        onClick = { viewMode = LineupViewMode.FIELD }
                    )
                    ViewModeButton(
                        label = "List",
                        isSelected = viewMode == LineupViewMode.LIST,
                        onClick = { viewMode = LineupViewMode.LIST }
                    )
                }
            }
        }

        // 3. FotMob-Style Lineup Pitch Field OR List View
        item {
            if (viewMode == LineupViewMode.FIELD) {
                FotMobCricketPitchField(
                    players = activeTeamXI,
                    selectedPlayer = selectedPlayer,
                    onPlayerSelected = { selectedPlayer = it }
                )
            } else {
                ListViewLineup(
                    players = activeTeamXI,
                    selectedPlayer = selectedPlayer,
                    onPlayerSelected = { selectedPlayer = it }
                )
            }
        }

        // 4. Interactive Player Breakdown Inspector Card (When Player Tapped)
        item {
            AnimatedVisibility(
                visible = selectedPlayer != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPlayer?.let { player ->
                    PlayerInspectorCard(player = player)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FotMobCricketPitchField(
    players: List<PlayerXI>,
    selectedPlayer: PlayerXI?,
    onPlayerSelected: (PlayerXI) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchFieldBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PitchFieldBorder, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp)
        ) {
            // Field Lines Canvas Overlay (Inner Circle, Pitch Area)
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Center Boundary Ring
                drawCircle(
                    color = PitchFieldLine,
                    radius = w * 0.42f,
                    center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                // Central Cricket Pitch Strip
                val pitchW = 44.dp.toPx()
                val pitchH = h * 0.55f
                drawRect(
                    color = Color(0x0CFFFFFF),
                    topLeft = androidx.compose.ui.geometry.Offset((w - pitchW) / 2f, (h - pitchH) / 2f),
                    size = androidx.compose.ui.geometry.Size(pitchW, pitchH)
                )
                drawRect(
                    color = Color(0x20FFFFFF),
                    topLeft = androidx.compose.ui.geometry.Offset((w - pitchW) / 2f, (h - pitchH) / 2f),
                    size = androidx.compose.ui.geometry.Size(pitchW, pitchH),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }

            // 4 Formation Tiers (Openers, Top Order, Middle/Keeper, Bowlers)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Tier 1: Openers (2 Players)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    players.filter { it.tier == 1 }.forEach { player ->
                        FotMobPlayerNode(
                            player = player,
                            isSelected = selectedPlayer?.id == player.id,
                            onClick = { onPlayerSelected(player) }
                        )
                    }
                }

                // Tier 2: Top Order (3 Players)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    players.filter { it.tier == 2 }.forEach { player ->
                        FotMobPlayerNode(
                            player = player,
                            isSelected = selectedPlayer?.id == player.id,
                            onClick = { onPlayerSelected(player) }
                        )
                    }
                }

                // Tier 3: Middle Order & Keeper (2 Players)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    players.filter { it.tier == 3 }.forEach { player ->
                        FotMobPlayerNode(
                            player = player,
                            isSelected = selectedPlayer?.id == player.id,
                            onClick = { onPlayerSelected(player) }
                        )
                    }
                }

                // Tier 4: Bowlers (4 Players)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    players.filter { it.tier == 4 }.forEach { player ->
                        FotMobPlayerNode(
                            player = player,
                            isSelected = selectedPlayer?.id == player.id,
                            onClick = { onPlayerSelected(player) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FotMobPlayerNode(
    player: PlayerXI,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ratingColor = when {
        player.overallRating >= 8.0f -> PitchPositive
        player.overallRating >= 6.0f -> PitchModerate
        else -> PitchNegative
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Circular Player Avatar Bubble
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF22283A) else Color(0xFF161A24))
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) ratingColor else Color(0xFF2D3546),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.shortName.take(2).uppercase(),
                    color = PitchPrimaryText,
                    fontSize = 13.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            // Floating FotMob Rating Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ratingColor,
                modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%.1f".format(player.overallRating),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    if (player.isMotm) {
                        Spacer(modifier = Modifier.width(1.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Jersey Number & Short Name
        Text(
            text = "${player.jerseyNumber} ${player.shortName}",
            color = if (isSelected) PitchPrimaryText else PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            maxLines = 1
        )
    }
}

@Composable
private fun ListViewLineup(
    players: List<PlayerXI>,
    selectedPlayer: PlayerXI?,
    onPlayerSelected: (PlayerXI) -> Unit
) {
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
                .padding(14.dp)
        ) {
            players.forEach { player ->
                val isSelected = selectedPlayer?.id == player.id
                val ratingColor = when {
                    player.overallRating >= 8.0f -> PitchPositive
                    player.overallRating >= 6.0f -> PitchModerate
                    else -> PitchNegative
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(if (isSelected) PitchCardInnerBg else PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (isSelected) ratingColor.copy(alpha = 0.6f) else PitchCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onPlayerSelected(player) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PitchCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder)
                        ) {
                            Text(
                                text = "${player.jerseyNumber}",
                                color = PitchPrimaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    color = PitchPrimaryText,
                                    fontSize = 13.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                                if (player.isCaptain) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "(c)", color = PitchSecondaryText, fontSize = 10.sp, fontFamily = BreeSerifFontFamily)
                                }
                                if (player.isWicketKeeper) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "(wk)", color = PitchSecondaryText, fontSize = 10.sp, fontFamily = BreeSerifFontFamily)
                                }
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "${player.role} • ${player.primaryHighlight}",
                                color = PitchSecondaryText,
                                fontSize = 10.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ratingColor
                    ) {
                        Text(
                            text = "%.1f".format(player.overallRating),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerInspectorCard(player: PlayerXI) {
    val ratingColor = when {
        player.overallRating >= 8.0f -> PitchPositive
        player.overallRating >= 6.0f -> PitchModerate
        else -> PitchNegative
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ratingColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Player Name + Overall Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            color = PitchPrimaryText,
                            fontSize = 20.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                        if (player.isMotm) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PitchPositive.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PitchPositive)
                            ) {
                                Text(
                                    text = "★ MoM",
                                    color = PitchPositive,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${player.role.uppercase()} • ${player.team.uppercase()}",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PitchBlackBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ratingColor)
                ) {
                    Text(
                        text = "Rating %.1f".format(player.overallRating),
                        color = ratingColor,
                        fontSize = 13.sp,
                        fontFamily = BreeSerifFontFamily,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Advanced PitchIQ Ratings Grid (Overall, Batting, Bowling, Impact)
            Text(
                text = "PITCHIQ ADVANCED RATINGS",
                color = PitchSecondaryText,
                fontSize = 10.sp,
                fontFamily = BreeSerifFontFamily,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RatingMetric("OVERALL", "%.1f".format(player.overallRating), ratingColor)
                RatingMetric("BATTING", "%.1f".format(player.battingRating), if (player.battingRating > 0f) PitchPrimaryText else PitchSecondaryText)
                RatingMetric("BOWLING", "%.1f".format(player.bowlingRating), if (player.bowlingRating > 0f) PitchPrimaryText else PitchSecondaryText)
                RatingMetric("IMPACT", "%.1f".format(player.impactRating), ratingColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Production Stats Grid
            Text(
                text = "MATCH PRODUCTION STATS",
                color = PitchSecondaryText,
                fontSize = 10.sp,
                fontFamily = BreeSerifFontFamily,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (player.role == "Bowler" || player.wickets != "-") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatsMetric("RUNS", player.runs)
                    StatsMetric("WICKETS", player.wickets)
                    StatsMetric("OVERS", player.overs)
                    StatsMetric("ECONOMY", player.economy)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PitchBlackBg, RoundedCornerShape(6.dp))
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatsMetric("RUNS", player.runs)
                    StatsMetric("BALLS", player.balls)
                    StatsMetric("SR", player.strikeRate)
                    StatsMetric("4s / 6s", "${player.fours} / ${player.sixes}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Narrative Story Box
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
                        .background(ratingColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = player.story,
                    color = PitchSecondaryText,
                    fontSize = 12.sp,
                    fontFamily = BreeSerifFontFamily,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun TeamTabPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ViewModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF1E2330) else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) PitchPrimaryText else PitchSecondaryText,
            fontSize = 11.sp,
            fontFamily = BreeSerifFontFamily,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun RatingMetric(title: String, value: String, color: Color) {
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
            color = color,
            fontSize = 15.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}

@Composable
private fun StatsMetric(title: String, value: String) {
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
            fontSize = 13.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}
