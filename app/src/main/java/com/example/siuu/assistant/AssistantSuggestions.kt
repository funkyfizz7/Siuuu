package com.example.siuu.assistant

/**
 * Context-aware question suggestion provider for the PitchIQ Assistant (Task 10).
 * Dynamically provides tailored queries based on current screen context (Match, Player, Series, Tab).
 */
object AssistantSuggestions {

    fun getSuggestions(context: AssistantContext): List<String> {
        return when {
            context.currentPlayerId != null -> getPlayerSuggestions(context)
            context.currentMatchId != null -> getMatchSuggestions(context)
            context.currentSeriesId != null -> getSeriesSuggestions(context)
            else -> getGlobalSuggestions()
        }
    }

    private fun getMatchSuggestions(context: AssistantContext): List<String> {
        return when (context.currentTab?.lowercase()?.trim()) {
            "pressure" -> listOf(
                "When was pressure highest?",
                "Who applied more pressure?",
                "Which session changed the match?",
                "Explain the momentum swings"
            )
            "momentum" -> listOf(
                "Explain the momentum swings",
                "Who won momentum?",
                "Who controlled Day 3?",
                "Most dominant session?"
            )
            "ratings" -> listOf(
                "Who had the highest impact?",
                "Why is he rated highly?",
                "Who was the MVP?",
                "Best bowling rating?"
            )
            "turningpoints", "turning points" -> listOf(
                "What was the turning point?",
                "Who caused it?",
                "Which century mattered more?",
                "Why did they win?"
            )
            else -> listOf(
                "Why did they win?",
                "What was the turning point?",
                "Who had the highest impact?",
                "Explain the momentum swings"
            )
        }
    }

    private fun getPlayerSuggestions(context: AssistantContext): List<String> {
        return listOf(
            "Why is he rated highly?",
            "What was his best innings?",
            "How did he impact this match?"
        )
    }

    private fun getSeriesSuggestions(context: AssistantContext): List<String> {
        return listOf(
            "Who dominated the series?",
            "Who scored the most runs?",
            "Best bowler?",
            "What decided the series?"
        )
    }

    private fun getGlobalSuggestions(): List<String> {
        return listOf(
            "Why did Australia win?",
            "What was the turning point?",
            "Who had the highest impact?",
            "Explain the momentum swings"
        )
    }
}
