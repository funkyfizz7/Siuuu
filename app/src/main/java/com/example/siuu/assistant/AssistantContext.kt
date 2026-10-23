package com.example.siuu.assistant

/**
 * Screen-aware context model capturing the user's active application location.
 * Enables the assistant to resolve pronouns ("he", "they", "that match", "who dominated")
 * and contextual queries without requiring explicit entities in the prompt.
 */
data class AssistantContext(
    val currentMatchId: Long? = null,
    val currentSeriesId: Long? = null,
    val currentPlayerId: Long? = null,
    val currentTab: String? = null
) {
    // Backwards-compatibility convenience accessors
    val matchId: Long? get() = currentMatchId
    val seriesId: String? get() = currentSeriesId?.toString()
    val playerId: Long? get() = currentPlayerId
}
