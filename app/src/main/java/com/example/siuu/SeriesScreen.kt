package com.example.siuu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SeriesScreen(
    seriesId: Int,
    onBack: () -> Unit,
    onNavigateToMatch: ((Long) -> Unit)? = null,
    onNavigateToPlayer: ((Long, Long) -> Unit)? = null,
    repository: MatchRepository = DefaultMatchRepository.instance,
    modifier: Modifier = Modifier
) {
    SeriesHubScreen(
        seriesId = seriesId,
        onBack = onBack,
        onNavigateToMatch = onNavigateToMatch,
        onNavigateToPlayer = onNavigateToPlayer,
        repository = repository,
        modifier = modifier
    )
}
