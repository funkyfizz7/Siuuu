package com.example.siuu.assistant

/**
 * Question intent taxonomy for the PitchIQ AI Assistant.
 * Covers Match, Player, Series, Stats, Commentary, and Session queries.
 */
enum class AssistantQuestionType {
    MATCH_SUMMARY,
    MATCH_TURNING_POINT,
    MATCH_MOMENTUM,
    MATCH_PRESSURE,
    MATCH_PARTNERSHIP,
    MATCH_COLLAPSE,
    MATCH_RECOVERY,
    PLAYER_RATING,
    PLAYER_PERFORMANCE,
    PLAYER_SPELL,
    SERIES_SUMMARY,
    SERIES_STATS,
    STAT_QUERY,
    SESSION_SUMMARY,
    COMMENTARY_QUERY,
    COMPARISON_QUERY,
    FALLBACK
}
