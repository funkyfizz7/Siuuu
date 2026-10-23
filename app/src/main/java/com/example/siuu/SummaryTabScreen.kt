package com.example.siuu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
private val PitchNegative = Color(0xFFEF4444)
private val PitchAccentYellow = Color(0xFFFFC72C)
private val PitchAccentBlue = Color(0xFF00A3FF)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Normal)
)

/**
 * Summary UI State Representation
 */
sealed interface SummaryUiState {
    object Loading : SummaryUiState
    data class Success(val data: MatchSummaryData) : SummaryUiState
    data class Error(val message: String) : SummaryUiState
}

/**
 * Summary Tab Screen (Match Centre of PitchIQ)
 * FotMob-style Test Match Overview designed to give users a complete understanding in under 30 seconds.
 */
@Composable
fun SummaryTabScreen(
    matchId: Long = 1152846L,
    repository: MatchRepository = DefaultMatchRepository.instance,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    onNavigateToTurningPoints: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var retryTrigger by remember { mutableStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(matchId) {
        NarrativeOverrideManager.triggerBackgroundRefresh(matchId, context)
        NarrativeOverrideManager.overrideUpdates.collect { updatedMatchId ->
            if (updatedMatchId == matchId) {
                retryTrigger++
            }
        }
    }
    val highlights by produceState<MatchHighlights?>(
        initialValue = null,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = try {
            DefaultMatchHighlightsRepository.getInstance(context).getMatchHighlights(matchId)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e("PitchIQHighlights", "Failed to load highlights for $matchId", e)
            null
        }
    }

    val uiState by produceState<SummaryUiState>(
        initialValue = SummaryUiState.Loading,
        key1 = matchId,
        key2 = retryTrigger
    ) {
        value = SummaryUiState.Loading
        try {
            val summary = repository.getMatchSummary(matchId)
            value = SummaryUiState.Success(summary)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            value = SummaryUiState.Error(e.localizedMessage ?: "Failed to load match summary. Please try again.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlackBg)
    ) {
        when (val state = uiState) {
            is SummaryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PitchPositive,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading Match Summary...",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            is SummaryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = PitchNegative,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to Load Match Summary",
                                color = PitchPrimaryText,
                                fontSize = 16.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { retryTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = PitchPositive)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = PitchBlackBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Retry",
                                        color = PitchBlackBg,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is SummaryUiState.Success -> {
                SummaryContent(
                    data = state.data,
                    highlights = highlights,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onNavigateToTurningPoints = onNavigateToTurningPoints
                )
            }
        }
    }
}

/**
 * Overload for Int matchId
 */
@Composable
fun SummaryTabScreen(
    matchId: Int,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    onNavigateToTurningPoints: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    SummaryTabScreen(
        matchId = matchId.toLong(),
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToTurningPoints = onNavigateToTurningPoints,
        modifier = modifier
    )
}

/**
 * Overload for backward compatibility with prototype MatchIntelligenceData
 */
@Composable
fun SummaryTabScreen(
    matchData: MatchIntelligenceData,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    onNavigateToTurningPoints: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    SummaryTabScreen(
        matchId = matchData.matchId,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToTurningPoints = onNavigateToTurningPoints,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryContent(
    data: MatchSummaryData,
    highlights: MatchHighlights? = null,
    onNavigateToPlayer: ((Long) -> Unit)? = null,
    onNavigateToTurningPoints: (() -> Unit)? = null,
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
        // ==========================================
        // SECTION 1 — MATCH HEADER & SCORECARD CARD (Series Hub Style)
        // ==========================================
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
                    // Row 1: Match Number Pill + Venue + Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchAccentBlue.copy(alpha = 0.16f))
                                .border(1.dp, PitchAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = data.header.matchNumber.ifBlank { "Test Match" },
                                color = PitchAccentBlue,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        if (data.header.venue.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${data.header.venue}",
                                color = PitchSecondaryText,
                                fontSize = 12.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2: Team 1 CountryBadge (Flag + Code) + Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryBadge(
                            teamCode = data.header.team1ShortName.ifBlank { data.header.team1Name }
                        )
                        Text(
                            text = formatCricketScore(data.snapshot.team1Scores).ifBlank { "-" },
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Team 2 CountryBadge (Flag + Code) + Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryBadge(
                            teamCode = data.header.team2ShortName.ifBlank { data.header.team2Name }
                        )
                        Text(
                            text = formatCricketScore(data.snapshot.team2Scores).ifBlank { "-" },
                            color = PitchPrimaryText,
                            fontSize = 16.sp,
                            fontFamily = BreeSerifFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = PitchCardBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 4: Match Outcome Headline
                    val isDrawn = data.header.victoryMargin.contains("draw", ignoreCase = true) || data.header.winner.contains("draw", ignoreCase = true)
                    Text(
                        text = data.header.victoryMargin,
                        color = if (isDrawn) PitchAccentYellow else PitchPositive,
                        fontSize = 13.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 5: Player of the Match Card (Clickable -> Redirects to Player Inspector)
                    val pom = data.playerOfMatchSummary
                    val pomRatingColor = getPitchIQRatingColor(pom.overallRating)
                    val isPomClickable = pom.playerId != null && pom.playerId > 0L

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchBlackBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = isPomClickable) {
                                if (pom.playerId != null && pom.playerId > 0L) {
                                    onNavigateToPlayer?.invoke(pom.playerId)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = PitchAccentYellow,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "PLAYER OF THE MATCH",
                                    color = PitchAccentYellow,
                                    fontSize = 10.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val context = LocalContext.current
                                val pomName = pom.name.ifBlank { data.header.playerOfMatch }
                                val pomPid = pom.playerId ?: 0L
                                val isPomImageReady = remember(pomPid, pomName) {
                                    PlayerImageLoader.isPlayerImageReady(context, pomPid, pomName)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PitchCardInnerBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPomImageReady) {
                                        PitchIQPlayerAvatar(
                                            playerId = pomPid,
                                            playerName = pomName,
                                            contentDescription = pomName,
                                            modifier = Modifier.fillMaxSize(),
                                            border = BorderStroke(1.dp, PitchCardBorder),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val pomInitials = pomName.split(" ")
                                            .filter { it.isNotBlank() }
                                            .take(2)
                                            .map { it.first() }
                                            .joinToString("")
                                            .uppercase()
                                        Text(
                                            text = if (pomInitials.isNotBlank()) pomInitials else "★",
                                            color = PitchPrimaryText,
                                            fontSize = 14.sp,
                                            fontFamily = BreeSerifFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.dp, PitchCardBorder, CircleShape)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pomName,
                                        color = PitchPrimaryText,
                                        fontSize = 15.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${pom.team}  •  ${pom.headlinePerformance}",
                                        color = PitchPositive,
                                        fontSize = 11.5.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PitchCardBg,
                                    border = BorderStroke(1.dp, pomRatingColor)
                                ) {
                                    Text(
                                        text = "★ %.1f".format(pom.overallRating),
                                        color = pomRatingColor,
                                        fontSize = 12.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2 — MATCH STORY (Maximum 4 Bullet Points)
        // ==========================================
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
                        text = "MATCH STORY",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val storySentences = if (data.matchStorySentences.isNotEmpty()) {
                        data.matchStorySentences
                    } else if (data.summaryPoints.isNotEmpty()) {
                        data.summaryPoints
                    } else {
                        listOf(data.matchStory)
                    }

                    storySentences.take(4).forEachIndexed { _, sentence ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = PitchPositive,
                                modifier = Modifier
                                    .size(6.dp)
                                    .padding(top = 5.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sentence,
                                color = PitchPrimaryText,
                                fontSize = 13.sp,
                                fontFamily = BreeSerifFontFamily,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 3 — MATCH SESSIONS BREAKDOWN & GROUND VIEW
        // ==========================================
        item {
            val cs = data.controlShare
            val t1Color = if (cs.team1Color != Color.Unspecified) cs.team1Color else data.header.team1Color
            val t2Color = if (cs.team2Color != Color.Unspecified) cs.team2Color else data.header.team2Color

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
                        text = "MATCH SESSIONS BREAKDOWN",
                        color = PitchPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Sessions Won Pills in Team Brand Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Team 1 Sessions Won Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(t1Color)
                                .padding(vertical = 6.5.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${data.controlSummary.team1Name}: ${data.controlSummary.team1SessionsWon}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Team 2 Sessions Won Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(t2Color)
                                .padding(vertical = 6.5.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${data.controlSummary.team2Name}: ${data.controlSummary.team2SessionsWon}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Neutral Sessions Won Pill
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                                .padding(vertical = 6.5.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Neutral: ${data.controlSummary.neutralSessions}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Ground View (Circular Pie Wheel by Days & Sessions)
                    CricketGroundSessionsWheel(
                        momentumPoints = data.momentumPoints,
                        team1Name = data.header.team1Name,
                        team1ShortName = data.header.team1ShortName,
                        team1Color = t1Color,
                        team2Name = data.header.team2Name,
                        team2ShortName = data.header.team2ShortName,
                        team2Color = t2Color
                    )
                }
            }
        }

        // ==========================================
        // SECTION 4 — WATCH HIGHLIGHTS
        // ==========================================
        item {
            var selectedDay by remember(data.matchId) {
                mutableIntStateOf(
                    if (highlights?.isDayAvailable(1) == true) 1
                    else (1..5).firstOrNull { highlights?.isDayAvailable(it) == true } ?: 1
                )
            }

            LaunchedEffect(highlights) {
                if (highlights != null && !highlights.isDayAvailable(selectedDay)) {
                    selectedDay = (1..5).firstOrNull { highlights.isDayAvailable(it) } ?: 1
                }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WATCH HIGHLIGHTS",
                            color = PitchPrimaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            letterSpacing = 0.5.sp
                        )
                        highlights?.sourceName?.takeIf { it.isNotBlank() }?.let { source ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = source,
                                    color = PitchSecondaryText,
                                    fontSize = 9.5.sp,
                                    fontFamily = BreeSerifFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Five-day selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 1..5) {
                            val isAvailable = highlights?.isDayAvailable(day) == true
                            val isSelected = selectedDay == day

                            val bg = when {
                                isSelected -> PitchAccentBlue.copy(alpha = 0.2f)
                                isAvailable -> PitchBlackBg
                                else -> Color(0xFF141414)
                            }
                            val borderColor = when {
                                isSelected -> PitchAccentBlue
                                isAvailable -> PitchCardBorder
                                else -> PitchCardBorder.copy(alpha = 0.3f)
                            }
                            val textColor = when {
                                isSelected -> PitchAccentBlue
                                isAvailable -> PitchPrimaryText
                                else -> Color(0xFF555555)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = isAvailable) {
                                        selectedDay = day
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Day $day",
                                    color = textColor,
                                    fontSize = 11.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val context = LocalContext.current
                    val currentPlayUrl = highlights?.getPlayUrlForDay(selectedDay)
                    val currentThumbUrl = highlights?.getThumbnailUrlForDay(selectedDay)
                    val isPlayable = !currentPlayUrl.isNullOrBlank()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PitchBlackBg)
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = isPlayable) {
                                Log.d("PitchIQHighlights", "Tapped highlights box for match ${data.matchId}, day $selectedDay, opening URL: $currentPlayUrl")
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentPlayUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Log.e("PitchIQHighlights", "No Activity found to handle video URL: $currentPlayUrl", e)
                                    Toast.makeText(context, "No app available to open video link", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("PitchIQHighlights", "Error launching video intent: $currentPlayUrl", e)
                                    Toast.makeText(context, "Could not open video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentThumbUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = currentThumbUrl,
                                contentDescription = "Day $selectedDay Match Highlights Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isPlayable) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.35f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPlayable) "▶" else "—",
                                color = if (isPlayable) Color.White else PitchSecondaryText,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = if (isPlayable) 2.dp else 0.dp)
                            )
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
private fun SubRatingMetric(title: String, value: String) {
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
            fontSize = 12.5.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun KeyStatItem(
    label: String,
    value: String,
    data: MatchSummaryData
) {
    val teamInfo = findTeamForEntity(value, data)
    val teamColor = teamInfo?.second

    Column {
        Text(
            text = label,
            color = PitchSecondaryText,
            fontSize = 10.sp,
            fontFamily = BreeSerifFontFamily
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (teamColor != null && value != "-" && value.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(teamColor)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = value,
                color = PitchPrimaryText,
                fontSize = 13.sp,
                fontFamily = BreeSerifFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// =========================================================================
// Team Color & Entity Resolver for Key Numbers
// =========================================================================

private fun getSummaryTeamColor(teamName: String, data: MatchSummaryData): Color {
    val t = teamName.trim().lowercase()
    val t1 = data.header.team1Name.trim().lowercase()
    val t1Short = data.header.team1ShortName.trim().lowercase()
    val t2 = data.header.team2Name.trim().lowercase()
    val t2Short = data.header.team2ShortName.trim().lowercase()

    return when {
        t == t1 || t == t1Short || (t1.isNotEmpty() && t.contains(t1)) || (t.isNotEmpty() && t1.contains(t)) -> {
            if (data.header.team1Color != Color.Unspecified) data.header.team1Color else getTeamBrandColor(data.header.team1Name)
        }
        t == t2 || t == t2Short || (t2.isNotEmpty() && t.contains(t2)) || (t.isNotEmpty() && t2.contains(t)) -> {
            if (data.header.team2Color != Color.Unspecified) data.header.team2Color else getTeamBrandColor(data.header.team2Name)
        }
        else -> getTeamBrandColor(teamName)
    }
}

private fun getTeamBrandColor(teamName: String): Color {
    val t = teamName.lowercase().trim()
    return when {
        t.contains("west indies") || t.contains("windies") || t == "wi" -> Color(0xFF881337) // Maroon
        t.contains("australia") || t == "aus" -> Color(0xFFD97706) // Aussie Gold / Amber
        t.contains("india") || t == "ind" -> Color(0xFF1D4ED8)     // Royal Blue
        t.contains("england") || t == "eng" -> Color(0xFF0284C7)   // Sky Blue
        t.contains("pakistan") || t == "pak" -> Color(0xFF15803D)  // Green
        t.contains("bangladesh") || t == "ban" -> Color(0xFF047857)// Dark Green
        t.contains("new zealand") || t == "nz" -> Color(0xFF334155)// Slate / Black
        t.contains("sri lanka") || t == "sl" -> Color(0xFF1E3A8A)  // Dark Navy Blue
        t.contains("south africa") || t == "sa" -> Color(0xFF007A3D) // Protea Green
        t.contains("afghanistan") || t == "afg" -> Color(0xFF0369A1) // Cobalt Blue
        t.contains("ireland") || t == "ire" -> Color(0xFF16A34A)    // Emerald Green
        t.contains("zimbabwe") || t == "zim" -> Color(0xFFDC2626)   // Red
        else -> Color(0xFFD97706)
    }
}

private fun findTeamForEntity(text: String, data: MatchSummaryData): Pair<String, Color>? {
    val clean = text.trim().lowercase()
        .replace(Regex("""\([^\)]*\)"""), "")
        .replace("'s", "")
        .replace("'", "")
        .trim()
    if (clean.isBlank()) return null

    // 1. Direct match in playerTeamMap
    val directTeam = data.playerTeamMap[clean]
    if (directTeam != null) {
        return Pair(directTeam, getSummaryTeamColor(directTeam, data))
    }

    // 2. Team names & abbreviations
    val t1 = data.header.team1Name.lowercase()
    val t1Short = data.header.team1ShortName.lowercase()
    val t2 = data.header.team2Name.lowercase()
    val t2Short = data.header.team2ShortName.lowercase()

    if (clean == t1 || clean == t1Short || (t1.isNotEmpty() && clean.contains(t1))) {
        return Pair(data.header.team1Name, getSummaryTeamColor(data.header.team1Name, data))
    }
    if (clean == t2 || clean == t2Short || (t2.isNotEmpty() && clean.contains(t2))) {
        return Pair(data.header.team2Name, getSummaryTeamColor(data.header.team2Name, data))
    }

    // 3. Substring match in playerTeamMap
    val sub = data.playerTeamMap.entries.firstOrNull { (k, _) ->
        k.length > 3 && (clean.contains(k) || k.contains(clean))
    }
    if (sub != null) {
        return Pair(sub.value, getSummaryTeamColor(sub.value, data))
    }

    return null
}

// =========================================================================
// Cricket Match Sessions Pie Chart (5-Day × 3-Session Donut Wheel)
// =========================================================================

@Composable
private fun CricketGroundSessionsWheel(
    momentumPoints: List<MomentumModel>,
    team1Name: String,
    team1ShortName: String,
    team1Color: Color,
    team2Name: String,
    team2ShortName: String,
    team2Color: Color,
    modifier: Modifier = Modifier
) {
    val sessionMap = remember(momentumPoints) {
        val map = mutableMapOf<Pair<Int, Int>, MomentumModel>()
        momentumPoints.forEach { pt ->
            map[Pair(pt.dayNumber, pt.sessionOfDay)] = pt
        }
        map
    }

    var selectedSession by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // 800ms 0° -> 360° smooth rotation/sweep animation on load
    val sweepAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        sweepAnim.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    // Color tokens per specification
    val goldColor = if (team1Color != Color.Unspecified) team1Color else Color(0xFFF5C542)
    val blueColor = if (team2Color != Color.Unspecified) team2Color else Color(0xFF4FC3F7)
    val neutralColor = Color(0xFFD9D9D9)
    val unplayedColor = Color(0xFF08132A)
    val centerBgColor = Color(0xFF030B1C)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlackBg, RoundedCornerShape(8.dp))
            .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 16.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wheel Canvas with Tap Interaction
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val totalRadius = minOf(size.width, size.height) / 2f
                            val rBandOuter = totalRadius - 4.dp.toPx()
                            val rDonutOuter = rBandOuter - 26.dp.toPx()
                            val rDonutInner = rDonutOuter * 0.52f

                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                            if (dist in rDonutInner..rBandOuter) {
                                val angleDeg = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                var norm = (angleDeg - (-90f)) % 360f
                                if (norm < 0) norm += 360f

                                val day = ((norm / 72f).toInt() % 5) + 1
                                val sess = (((norm % 72f) / 24f).toInt() % 3) + 1

                                selectedSession = if (selectedSession == Pair(day, sess)) null else Pair(day, sess)
                            } else {
                                selectedSession = null
                            }
                        }
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val totalRadius = minOf(size.width, size.height) / 2f

                val rBandOuter = totalRadius - 4.dp.toPx()
                val rDonutOuter = rBandOuter - 26.dp.toPx()
                val rDonutInner = rDonutOuter * 0.52f
                val rCenter = rDonutInner - 6.dp.toPx()

                // 1. Draw Outer Ring (Dark Navy Background with Subtle White Border)
                drawCircle(
                    color = Color(0xFF0C1322),
                    radius = rBandOuter,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = rBandOuter,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // 2. Draw 15 Annular Slices (5 Days × 3 Sessions)
                val numDays = 5
                val numSessionsPerDay = 3
                val sessSweep = 360f / (numDays * numSessionsPerDay) // 24.0 deg
                val currentAnimatedSweep = sweepAnim.value

                for (d in 1..numDays) {
                    val dayStartAngle = -90f + ((d - 1) * 72f)

                    for (s in 1..numSessionsPerDay) {
                        val sessStartAngle = dayStartAngle + ((s - 1) * sessSweep)
                        val pt = sessionMap[Pair(d, s)]

                        val fillColor = when {
                            pt != null -> {
                                val w = pt.sessionWinner.lowercase().trim()
                                val t1 = team1Name.lowercase().trim()
                                val t1Short = team1ShortName.lowercase().trim()
                                val t2 = team2Name.lowercase().trim()
                                val t2Short = team2ShortName.lowercase().trim()

                                when {
                                    w == "team1" || w == t1 || (t1Short.isNotEmpty() && w == t1Short) -> goldColor
                                    w == "team2" || w == t2 || (t2Short.isNotEmpty() && w == t2Short) -> blueColor
                                    w == "neutral" || w == "even" || w == "balanced" -> neutralColor
                                    pt.momentumScore >= 12.0 -> goldColor
                                    pt.momentumScore <= -12.0 -> blueColor
                                    else -> neutralColor
                                }
                            }
                            else -> unplayedColor
                        }

                        // Animate sweep on load
                        val sliceProgressAngle = (sessStartAngle - (-90f) + 360f) % 360f
                        if (sliceProgressAngle < currentAnimatedSweep) {
                            val activeSweep = (currentAnimatedSweep - sliceProgressAngle).coerceIn(0f, sessSweep)

                            val isSelected = selectedSession == Pair(d, s)
                            val outerR = if (isSelected) rDonutOuter + 2.dp.toPx() else rDonutOuter
                            val innerR = if (isSelected) rDonutInner - 2.dp.toPx() else rDonutInner

                            // Annular Wedge Path
                            val path = androidx.compose.ui.graphics.Path().apply {
                                arcTo(
                                    rect = androidx.compose.ui.geometry.Rect(
                                        cx - outerR,
                                        cy - outerR,
                                        cx + outerR,
                                        cy + outerR
                                    ),
                                    startAngleDegrees = sessStartAngle,
                                    sweepAngleDegrees = activeSweep,
                                    forceMoveTo = true
                                )
                                arcTo(
                                    rect = androidx.compose.ui.geometry.Rect(
                                        cx - innerR,
                                        cy - innerR,
                                        cx + innerR,
                                        cy + innerR
                                    ),
                                    startAngleDegrees = sessStartAngle + activeSweep,
                                    sweepAngleDegrees = -activeSweep,
                                    forceMoveTo = false
                                )
                                close()
                            }
                            drawPath(path = path, color = fillColor)

                            if (isSelected) {
                                drawPath(
                                    path = path,
                                    color = Color.White,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        // Thin White Separator between sessions within a day
                        if (s > 1) {
                            val rad = Math.toRadians(sessStartAngle.toDouble())
                            val cosVal = Math.cos(rad).toFloat()
                            val sinVal = Math.sin(rad).toFloat()
                            drawLine(
                                color = Color.White.copy(alpha = 0.85f),
                                start = Offset(cx + (rDonutInner * cosVal), cy + (rDonutInner * sinVal)),
                                end = Offset(cx + (rDonutOuter * cosVal), cy + (rDonutOuter * sinVal)),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // Thicker Prominent White Separator between days
                    val dayRad = Math.toRadians(dayStartAngle.toDouble())
                    val dayCos = Math.cos(dayRad).toFloat()
                    val daySin = Math.sin(dayRad).toFloat()
                    drawLine(
                        color = Color.White,
                        start = Offset(cx + (rDonutInner * dayCos), cy + (rDonutInner * daySin)),
                        end = Offset(cx + (rDonutOuter * dayCos), cy + (rDonutOuter * daySin)),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Draw Day Label in Outer Band with precise tangent alignment
                    val rLabel = (rDonutOuter + rBandOuter) / 2f
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                        letterSpacing = 0.08f
                    }
                    val textYOffset = -((textPaint.descent() + textPaint.ascent()) / 2f)

                    when (d) {
                        1 -> {
                            val rad = Math.toRadians(-54.0)
                            val lx = cx + (rLabel * Math.cos(rad)).toFloat()
                            val ly = cy + (rLabel * Math.sin(rad)).toFloat()
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.translate(lx, ly)
                            drawContext.canvas.nativeCanvas.rotate(36f)
                            drawContext.canvas.nativeCanvas.drawText("DAY 1", 0f, textYOffset, textPaint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                        2 -> {
                            val rad = Math.toRadians(18.0)
                            val lx = cx + (rLabel * Math.cos(rad)).toFloat()
                            val ly = cy + (rLabel * Math.sin(rad)).toFloat()
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.translate(lx, ly)
                            drawContext.canvas.nativeCanvas.rotate(108f)
                            drawContext.canvas.nativeCanvas.drawText("DAY 2", 0f, textYOffset, textPaint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                        3 -> {
                            val lx = cx
                            val ly = cy + rLabel
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.translate(lx, ly)
                            drawContext.canvas.nativeCanvas.drawText("DAY 3", 0f, textYOffset, textPaint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                        4 -> {
                            val rad = Math.toRadians(162.0)
                            val lx = cx + (rLabel * Math.cos(rad)).toFloat()
                            val ly = cy + (rLabel * Math.sin(rad)).toFloat()
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.translate(lx, ly)
                            drawContext.canvas.nativeCanvas.rotate(90f)
                            drawContext.canvas.nativeCanvas.drawText("DAY 4", 0f, textYOffset, textPaint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                        5 -> {
                            val rad = Math.toRadians(-126.0)
                            val lx = cx + (rLabel * Math.cos(rad)).toFloat()
                            val ly = cy + (rLabel * Math.sin(rad)).toFloat()
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.translate(lx, ly)
                            drawContext.canvas.nativeCanvas.rotate(-36f)
                            drawContext.canvas.nativeCanvas.drawText("DAY 5", 0f, textYOffset, textPaint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }

                // 3. Draw Concentric Border Rings
                // Outer Donut Ring
                drawCircle(
                    color = Color.White,
                    radius = rDonutOuter,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                // Inner Donut Ring
                drawCircle(
                    color = Color.White,
                    radius = rDonutInner,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // 4. Center Circle (Empty Center with Dark Navy #030B1C Fill)
                drawCircle(
                    color = centerBgColor,
                    radius = rDonutInner,
                    center = Offset(cx, cy)
                )
                // Concentric Inner White Ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = rCenter,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GroundLegendItem(color = goldColor, label = team1ShortName.ifBlank { team1Name })
            Spacer(modifier = Modifier.width(12.dp))
            GroundLegendItem(color = blueColor, label = team2ShortName.ifBlank { team2Name })
            Spacer(modifier = Modifier.width(12.dp))
            GroundLegendItem(color = neutralColor, label = "Neutral")
            Spacer(modifier = Modifier.width(12.dp))
            GroundLegendItem(color = unplayedColor, label = "Unplayed", isBordered = true)
        }

        // Interactive Selected Session Info Card / Tooltip
        AnimatedVisibility(visible = selectedSession != null) {
            val sel = selectedSession
            if (sel != null) {
                val pt = sessionMap[sel]
                val (d, s) = sel

                val isT1 = pt != null && (pt.sessionWinner == "team1" || pt.sessionWinner.equals(team1Name, ignoreCase = true) || pt.sessionWinner.equals(team1ShortName, ignoreCase = true))
                val isT2 = pt != null && (pt.sessionWinner == "team2" || pt.sessionWinner.equals(team2Name, ignoreCase = true) || pt.sessionWinner.equals(team2ShortName, ignoreCase = true))
                val isNeutral = pt != null && (pt.sessionWinner == "neutral" || (!isT1 && !isT2))

                val winnerName = when {
                    pt == null -> "Unplayed Session"
                    isT1 -> team1Name
                    isT2 -> team2Name
                    else -> "Neutral Session"
                }

                val winnerPillColor = when {
                    isT1 -> goldColor
                    isT2 -> blueColor
                    isNeutral -> Color(0xFF64748B)
                    else -> Color(0xFF334155)
                }

                val badgeText = when {
                    pt == null -> "Unplayed Session"
                    isT1 || isT2 -> "Winner: $winnerName"
                    else -> "Neutral Session"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .background(Color(0xFF0B132B), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    // Header Row: Session & Win Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "DAY $d • SESSION $s",
                                color = PitchSecondaryText,
                                fontSize = 11.sp,
                                fontFamily = BreeSerifFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            if (pt != null && (pt.sessionEndScores.isNotEmpty() || pt.sessionEndScore.isNotBlank())) {
                                Spacer(modifier = Modifier.height(3.dp))
                                val scoresToDisplay = if (pt.sessionEndScores.isNotEmpty()) {
                                    pt.sessionEndScores
                                } else {
                                    pt.sessionEndScore.split(" • ").map { it.trim() }
                                }
                                scoresToDisplay.forEach { scoreText ->
                                    Text(
                                        text = scoreText,
                                        color = PitchPrimaryText,
                                        fontSize = 14.sp,
                                        fontFamily = BreeSerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(winnerPillColor)
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    if (pt != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 Metric Tiles (RUNS, WICKETS, OVERS, RUN RATE)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricTile("RUNS", "${pt.runs}")
                            MetricTile("WICKETS", "${pt.wickets}")
                            MetricTile("OVERS", if (pt.formattedOvers.isNotBlank()) pt.formattedOvers else "0.0")
                            MetricTile("RUN RATE", String.format(java.util.Locale.US, "%.2f", pt.runRate))
                        }

                        // Context Analytical Commentary Story
                        val storyText = pt.commentaryText.ifBlank { pt.contextStory.ifBlank { pt.sessionTitle } }
                        if (storyText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchBlackBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(34.dp)
                                        .background(winnerPillColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = storyText,
                                    color = PitchSecondaryText,
                                    fontSize = 11.5.sp,
                                    fontFamily = BreeSerifFontFamily,
                                    lineHeight = 16.5.sp
                                )
                            }
                        }
                    }
                }
            }
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
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun GroundLegendItem(
    color: Color,
    label: String,
    isBordered: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isBordered) Modifier.border(0.8.dp, Color(0xFF475569), CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.width(4.5.dp))
        Text(
            text = label,
            color = PitchSecondaryText,
            fontSize = 10.5.sp,
            fontFamily = BreeSerifFontFamily
        )
    }
}


