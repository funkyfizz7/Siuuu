package com.example.siuu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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

// FotMob / PitchIQ Design Tokens
private val FotMobDarkBg = Color(0xFF141414)
private val FotMobCardBg = Color(0xFF1E1E1E)
private val FotMobPrimaryText = Color(0xFFFFFFFF)
private val FotMobSecondaryText = Color(0xFF9E9E9E)
private val FotMobGreenAccent = Color(0xFF22C55E)
private val FotMobBlueAccent = Color(0xFF00A3FF)
private val FotMobOrangeAccent = Color(0xFFFF9F1C)
private val FotMobRedAccent = Color(0xFFEF4444)

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
 * FotMob-style Player Match & Innings Breakdown Screen
 *
 * Header:
 * - Back button & Done button
 * - Circular avatar with attached rating badge
 * - Chevrons to navigate tabs
 * - Player Name
 * - 3-Column Meta: Position/Role | Country/Team | Overall Match / Innings Scorecard
 *
 * Tabs:
 * [ OVERALL ] [ INN 1 ] [ INN 2 ] [ INN 3 ] [ INN 4 ]
 *
 * - OVERALL tab: Full match summary stats, match production, batting & bowling intelligence, impact & narrative.
 * - INN 1..4 tabs: Direct binding from player_innings_details.json with zero runtime calculations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerInningsBreakdownScreen(
    player: PlayerMatchRatingModel,
    inningsProfile: PlayerInningsDetailsProfile?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tab indices: 0 = OVERALL, 1 = INN 1, 2 = INN 2, 3 = INN 3, 4 = INN 4
    val totalInnings = maxOf(inningsProfile?.innings?.size ?: 4, 4)
    var selectedTab by remember { mutableStateOf(0) } // 0 = OVERALL, 1..totalInnings

    val isOverall = selectedTab == 0
    val currentInningsDetail = if (!isOverall) {
        inningsProfile?.innings?.firstOrNull { it.inningsNumber == selectedTab }
            ?: PlayerInningsDetailModel(inningsNumber = selectedTab, participationType = "DNB", inningsScorecard = "DNB")
    } else null

    // Determine current display rating
    val displayRating = if (isOverall) {
        player.overallRating
    } else {
        val innRating = when (currentInningsDetail?.participationType) {
            "BAT" -> currentInningsDetail.battingRating
            "BOWL" -> currentInningsDetail.bowlingRating
            "BAT_AND_BOWL" -> maxOf(currentInningsDetail.battingRating, currentInningsDetail.bowlingRating)
            else -> player.overallRating
        }
        if (innRating > 0.0) innRating else player.overallRating
    }
    val ratingBadgeColor = getPitchIQRatingColor(displayRating)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PLAYER BREAKDOWN",
                        color = FotMobPrimaryText,
                        fontSize = 15.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FotMobPrimaryText
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = "Done",
                            color = FotMobGreenAccent,
                            fontSize = 15.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FotMobDarkBg
                )
            )
        },
        containerColor = FotMobDarkBg,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FotMobDarkBg),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── 1. FotMob Top Hero Card ──────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = FotMobCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Row with Navigation Chevrons & Rating Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedTab > 0) selectedTab--
                                },
                                enabled = selectedTab > 0,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2A2A))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Tab",
                                    tint = if (selectedTab > 0) FotMobPrimaryText else FotMobSecondaryText.copy(alpha = 0.3f)
                                )
                            }

                            // Center Avatar with Rating Pill attached
                            Box(
                                contentAlignment = Alignment.TopEnd,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                val context = LocalContext.current
                                val pid = player.playerId?.toLongOrNull() ?: 0L
                                val isImageReady = remember(pid, player.playerName) {
                                    PlayerImageLoader.isPlayerImageReady(context, pid, player.playerName)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isImageReady) {
                                        PitchIQPlayerAvatar(
                                            playerId = pid,
                                            playerName = player.playerName,
                                            contentDescription = player.playerName,
                                            modifier = Modifier.fillMaxSize(),
                                            border = BorderStroke(2.dp, Color(0xFF444444)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val initials = player.playerName.split(" ")
                                            .filter { it.isNotBlank() }
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                            .ifBlank { "PIQ" }
                                        Text(
                                            text = initials,
                                            color = FotMobPrimaryText,
                                            fontSize = 22.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(2.dp, Color(0xFF444444), CircleShape)
                                        )
                                    }
                                }

                                // Rating Badge attached to top-right of avatar
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ratingBadgeColor,
                                    modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = "%.1f".format(displayRating),
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (selectedTab < totalInnings) selectedTab++
                                },
                                enabled = selectedTab < totalInnings,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2A2A))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Tab",
                                    tint = if (selectedTab < totalInnings) FotMobPrimaryText else FotMobSecondaryText.copy(alpha = 0.3f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Player Name
                        Text(
                            text = player.playerName,
                            color = FotMobPrimaryText,
                            fontSize = 22.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3-Column FotMob Metadata Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            FotMobMetaColumn(
                                label = "Position",
                                value = player.role.replace("Batter", "Bat").replace("Wicketkeeper", "WK")
                            )
                            FotMobMetaColumn(
                                label = "Country",
                                value = player.team
                            )
                            FotMobMetaColumn(
                                label = if (isOverall) "Match Impact" else "Inn $selectedTab",
                                value = if (isOverall) {
                                    if (player.impactRank != "-") "Rank ${player.impactRank}" else "%.1f".format(player.impactRating)
                                } else {
                                    currentInningsDetail?.inningsScorecard ?: "DNB"
                                }
                            )
                        }
                    }
                }
            }

            // ── 2. FotMob-Style Tabs [ OVERALL ] [ INN 1 ] [ INN 2 ] [ INN 3 ] [ INN 4 ] ──
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(FotMobCardBg, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // OVERALL Tab
                    val isOverallSelected = selectedTab == 0
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isOverallSelected) Color(0xFF333333) else Color.Transparent,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "OVERALL",
                                color = if (isOverallSelected) FotMobPrimaryText else FotMobSecondaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = if (isOverallSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    // INN 1..4 Tabs
                    (1..totalInnings).forEach { innNum ->
                        val isSelected = selectedTab == innNum
                        val innData = inningsProfile?.innings?.firstOrNull { it.inningsNumber == innNum }
                        val isDnb = innData == null || innData.participationType == "DNB"

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Color(0xFF333333) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = innNum }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "INN $innNum",
                                    color = if (isSelected) FotMobPrimaryText else (if (isDnb) FotMobSecondaryText.copy(alpha = 0.4f) else FotMobSecondaryText),
                                    fontSize = 11.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. OVERALL TAB VIEW (Match Summary Stats) ────────────────────
            if (isOverall) {
                // Section: Top stats (Overall)
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Top stats",
                        color = FotMobPrimaryText,
                        fontSize = 17.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        FotMobStatRow("Overall rating", "%.1f".format(player.overallRating))
                        if (player.battingRating > 0.0) {
                            FotMobStatRow("Batting rating", "%.1f".format(player.battingRating))
                        }
                        if (player.bowlingRating > 0.0) {
                            FotMobStatRow("Bowling rating", "%.1f".format(player.bowlingRating))
                        }
                        if (player.impactRating > 0.0) {
                            val rankSuffix = if (player.impactRank != "-") " (${player.impactRank})" else ""
                            FotMobStatRow("Impact rating", "%.1f$rankSuffix".format(player.impactRating))
                        }
                        if (player.runs != "-") {
                            FotMobStatRow("Runs scored", player.runs)
                        }
                        if (player.balls != "-") {
                            FotMobStatRow("Balls faced", player.balls)
                        }
                        if (player.strikeRate != "-") {
                            FotMobStatRow("Strike rate", player.strikeRate)
                        }
                        if (player.wickets != "-") {
                            FotMobStatRow("Wickets taken", player.wickets)
                        }
                        if (player.overs != "-") {
                            FotMobStatRow("Overs bowled", player.overs)
                        }
                        if (player.economy != "-") {
                            FotMobStatRow("Economy rate", player.economy)
                        }
                    }
                }

                // Section: Batting Intelligence (if available)
                val hasBattingIntel = player.controlPct != "-" && player.controlPct.isNotBlank()
                if (hasBattingIntel) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Batting execution & control",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Shot control %", player.controlPct)
                            FotMobStatRow("False shot %", player.falseShotPct)
                            FotMobStatRow("Attacking shots %", player.attackingPct)
                            FotMobStatRow("Leaves %", player.leavePct)
                            FotMobStatRow("Dot balls faced %", player.battingDotPct)
                            FotMobStatRow("Boundary runs %", player.boundaryRunPct)
                            if (player.fours != "-" || player.sixes != "-") {
                                FotMobStatRow("Boundaries", "${player.fours}x4 • ${player.sixes}x6")
                            }
                            if (player.battingImpactRating > 0.0) {
                                FotMobStatRow("Batting impact rating", "%.1f".format(player.battingImpactRating))
                            }
                        }
                    }
                }

                // Section: Bowling Intelligence (if available)
                val hasBowlingIntel = player.bowlingDotPct != "-" && player.bowlingDotPct.isNotBlank()
                if (hasBowlingIntel) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bowling execution & control",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Dot ball %", player.bowlingDotPct)
                            FotMobStatRow("Line accuracy %", player.lineAcc)
                            FotMobStatRow("Length accuracy %", player.lengthAcc)
                            FotMobStatRow("Boundaries conceded", player.boundariesConceded)
                            if (player.bowlingImpactRating > 0.0) {
                                FotMobStatRow("Bowling impact rating", "%.1f".format(player.bowlingImpactRating))
                            }
                        }
                    }
                }

                // Section: Match Story Narrative
                if (player.story.isNotBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Match intelligence narrative",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FotMobCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = player.story,
                                    color = FotMobSecondaryText,
                                    fontSize = 13.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. INNINGS 1..4 TABS VIEW ────────────────────────────────────
            if (!isOverall && currentInningsDetail != null) {
                if (currentInningsDetail.participationType in listOf("BAT", "BAT_AND_BOWL")) {
                    // Section: Top stats (Batting Innings)
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Top stats",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Scorecard", currentInningsDetail.inningsScorecard)
                            FotMobStatRow("Runs scored", "${currentInningsDetail.runs}")
                            FotMobStatRow("Balls faced", "${currentInningsDetail.balls}")
                            FotMobStatRow("Strike rate", "%.2f".format(currentInningsDetail.strikeRate))
                            FotMobStatRow("Shot control %", "%.1f%%".format(currentInningsDetail.controlPct))
                            FotMobStatRow("False shot %", "%.1f%%".format(currentInningsDetail.falseShotPct))
                            FotMobStatRow("Batting rating", "%.1f".format(currentInningsDetail.battingRating))
                            FotMobStatRow("Arrived at", currentInningsDetail.arrivalScore)
                        }
                    }

                    // Section: Batting execution & control
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Batting execution & control",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Attacking shots %", "%.1f%%".format(currentInningsDetail.attackingPct))
                            FotMobStatRow("Boundary runs %", "%.1f%%".format(currentInningsDetail.boundaryRunPct))
                            FotMobStatRow("Fours (4s)", "${currentInningsDetail.fours}")
                            FotMobStatRow("Sixes (6s)", "${currentInningsDetail.sixes}")
                            FotMobStatRow("Leaves %", "%.1f%%".format(currentInningsDetail.leavePct))
                            FotMobStatRow("Dot balls faced %", "%.1f%%".format(currentInningsDetail.dotBallPct))
                            if (currentInningsDetail.dismissed) {
                                FotMobStatRow("Dismissal", "${currentInningsDetail.dismissalType} ${if (currentInningsDetail.dismissedBy.isNotBlank()) "b ${currentInningsDetail.dismissedBy}" else ""}")
                            } else {
                                FotMobStatRow("Status", "Not Out *")
                            }
                        }
                    }
                }

                // Bowling Innings
                if (currentInningsDetail.participationType in listOf("BOWL", "BAT_AND_BOWL")) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (currentInningsDetail.participationType == "BAT_AND_BOWL") "Bowling" else "Top stats",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Bowling figures", currentInningsDetail.inningsScorecard)
                            FotMobStatRow("Overs bowled", currentInningsDetail.overs)
                            FotMobStatRow("Wickets taken", "${currentInningsDetail.wickets}")
                            FotMobStatRow("Runs conceded", "${currentInningsDetail.runsConceded}")
                            FotMobStatRow("Economy rate", "%.2f".format(currentInningsDetail.economy))
                            FotMobStatRow("Bowling rating", "%.1f".format(currentInningsDetail.bowlingRating))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bowling execution & control",
                            color = FotMobPrimaryText,
                            fontSize = 17.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            FotMobStatRow("Dot ball %", "%.1f%%".format(currentInningsDetail.bowlingDotPct))
                            FotMobStatRow("Line accuracy %", "%.1f%%".format(currentInningsDetail.lineAccuracyPct))
                            FotMobStatRow("Length accuracy %", "%.1f%%".format(currentInningsDetail.lengthAccuracyPct))
                            FotMobStatRow("Boundaries conceded", "${currentInningsDetail.boundariesConceded}")
                            FotMobStatRow("Maidens bowled", "${currentInningsDetail.maidens}")
                        }
                    }
                }

                // DNB Innings
                if (currentInningsDetail.participationType == "DNB") {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Did not participate",
                                color = FotMobPrimaryText,
                                fontSize = 19.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Player did not bat or bowl during Innings $selectedTab.",
                                color = FotMobSecondaryText,
                                fontSize = 14.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * FotMob 3-Column Metadata component
 */
@Composable
private fun FotMobMetaColumn(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = FotMobPrimaryText,
            fontSize = 16.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = FotMobSecondaryText,
            fontSize = 13.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}

/**
 * FotMob Clean Flat Stat Row: Left Label <----------------> Right Value
 */
@Composable
private fun FotMobStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = FotMobSecondaryText,
            fontSize = 15.5.sp,
            fontFamily = BreeSerifFontFamily
        )
        Text(
            text = value,
            color = FotMobPrimaryText,
            fontSize = 15.5.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}
