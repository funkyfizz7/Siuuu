package com.example.siuu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DefaultMatchRepository.initialize(this)
        DefaultMatchHighlightsRepository.initialize(this)
        enableEdgeToEdge()
        val intentMatchId = intent?.getLongExtra("matchId", 0L)?.takeIf { it != 0L }
        val intentPlayerId = intent?.getLongExtra("playerId", 0L)?.takeIf { it != 0L }

        setContent {
            // Navigation Routing:
            // "matches" | "series"
            // "series/{seriesId}"
            // "match/{matchId}"
            // "player/{matchId}/{playerId}"
            var currentHomeTab by remember { mutableStateOf("home") }
            var selectedSeriesId by remember { mutableStateOf<Int?>(null) }
            var selectedMatchId by remember { mutableStateOf<Long?>(intentMatchId) }
            var selectedPlayerRoute by remember {
                mutableStateOf<Pair<Long, Long>?>(
                    if (intentMatchId != null && intentPlayerId != null) Pair(intentMatchId, intentPlayerId) else null
                )
            }

            var isAssistantOpen by remember { mutableStateOf(false) }
            var assistantScreenContext by remember { mutableStateOf(com.example.siuu.assistant.AssistantContext()) }

            // System Back Gesture/Button Handlers (deepest screen handles first)
            BackHandler(enabled = isAssistantOpen) {
                isAssistantOpen = false
            }
            BackHandler(enabled = !isAssistantOpen && selectedPlayerRoute != null) {
                selectedPlayerRoute = null
            }
            BackHandler(enabled = !isAssistantOpen && selectedPlayerRoute == null && selectedMatchId != null) {
                selectedMatchId = null
            }
            BackHandler(enabled = !isAssistantOpen && selectedPlayerRoute == null && selectedMatchId == null && selectedSeriesId != null) {
                selectedSeriesId = null
            }
            BackHandler(enabled = !isAssistantOpen && selectedPlayerRoute == null && selectedMatchId == null && selectedSeriesId == null && currentHomeTab != "home") {
                currentHomeTab = "home"
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF000000) // PitchIQ Pure Black
            ) {
                when {
                    isAssistantOpen -> {
                        com.example.siuu.assistant.AssistantScreen(
                            assistantContext = assistantScreenContext,
                            onBack = { isAssistantOpen = false }
                        )
                    }
                    selectedPlayerRoute != null -> {
                        // Route: player/{matchId}/{playerId}
                        PlayerInspectorScreen(
                            matchId = selectedPlayerRoute!!.first,
                            playerId = selectedPlayerRoute!!.second,
                            onBack = {
                                selectedPlayerRoute = null
                            }
                        )
                    }
                    selectedMatchId != null -> {
                        // Route: match/{matchId}
                        PitchIQMatchScreen(
                            matchId = selectedMatchId!!,
                            onBack = {
                                selectedMatchId = null
                            },
                            onNavigateToPlayer = { pId ->
                                selectedPlayerRoute = Pair(selectedMatchId!!, pId)
                            }
                        )
                    }
                    selectedSeriesId != null -> {
                        // Route: series/{seriesId}
                        SeriesScreen(
                            seriesId = selectedSeriesId!!,
                            onBack = {
                                selectedSeriesId = null
                            },
                            onNavigateToMatch = { matchId ->
                                selectedMatchId = matchId
                            }
                        )
                    }
                    currentHomeTab == "series" -> {
                        // Route: series
                        SeriesBrowserScreen(
                            onSeriesSelected = { seriesId ->
                                selectedSeriesId = seriesId
                            },
                            onNavigateToMatches = {
                                currentHomeTab = "matches"
                            },
                            onBack = {
                                currentHomeTab = "home"
                            }
                        )
                    }
                    currentHomeTab == "matches" -> {
                        // Route: matches
                        MatchBrowserScreen(
                            onMatchSelected = { matchId ->
                                selectedMatchId = matchId
                            },
                            onNavigateToSeries = {
                                currentHomeTab = "series"
                            },
                            onBack = {
                                currentHomeTab = "home"
                            }
                        )
                    }
                    else -> {
                        // Route: home
                        HomeScreen(
                            onMatchSelected = { matchId ->
                                selectedMatchId = matchId
                            },
                            onSeriesSelected = { seriesId ->
                                selectedSeriesId = seriesId
                            },
                            onBrowseAllMatches = {
                                currentHomeTab = "matches"
                            },
                            onBrowseAllSeries = {
                                currentHomeTab = "series"
                            }
                        )
                    }
                }
            }
        }
    }
}
