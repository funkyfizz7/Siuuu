package com.example.siuu.assistant

/**
 * Historical record of a single conversational turn.
 */
data class AssistantConversationTurn(
    val question: String,
    val intent: AssistantQuestionType,
    val entities: AssistantEntities,
    val headline: String,
    val answerText: String,
    val reasoning: AssistantEvidence? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Stateful conversation memory for multi-turn conversational follow-ups.
 * Enables natural follow-ups such as "What was the turning point?", "And who caused it?", "What happened after that?".
 */
data class AssistantConversationState(
    val lastIntent: AssistantQuestionType? = null,
    val lastPlayerId: Long? = null,
    val lastPlayerName: String? = null,
    val lastMatchId: Long? = null,
    val lastSeriesId: String? = null,
    val lastTurningPointId: String? = null,
    val lastTurningPointTitle: String? = null,
    val lastSessionId: String? = null,
    val lastDayNumber: Int? = null,
    val lastSessionNumber: Int? = null,
    val lastInningsNumber: Int? = null,
    val lastMentionedTeam: String? = null,
    val lastMentionedMetric: String? = null,
    val history: List<AssistantConversationTurn> = emptyList()
) {

    /**
     * Updates the conversation state with the latest turn outcome.
     */
    fun updateWithTurn(
        question: String,
        intent: AssistantQuestionType,
        entities: AssistantEntities,
        answer: AssistantAnswer
    ): AssistantConversationState {
        val newTurn = AssistantConversationTurn(
            question = question,
            intent = intent,
            entities = entities,
            headline = answer.headline,
            answerText = answer.answerText,
            reasoning = answer.reasoning
        )

        val turningPointTitle = answer.dataPoints["turning_point"]
            ?: if (intent == AssistantQuestionType.MATCH_TURNING_POINT) answer.headline.removePrefix("Defining Turning Point: ") else lastTurningPointTitle

        return copy(
            lastIntent = intent,
            lastPlayerId = entities.playerId ?: lastPlayerId,
            lastPlayerName = entities.playerName ?: lastPlayerName,
            lastMatchId = entities.matchId ?: lastMatchId,
            lastSeriesId = entities.seriesId ?: lastSeriesId,
            lastTurningPointId = lastTurningPointId,
            lastTurningPointTitle = turningPointTitle ?: lastTurningPointTitle,
            lastDayNumber = entities.dayNumber ?: lastDayNumber,
            lastSessionNumber = entities.sessionNumber ?: lastSessionNumber,
            lastInningsNumber = entities.inningsNumber ?: lastInningsNumber,
            lastMentionedTeam = entities.teamName ?: lastMentionedTeam,
            lastMentionedMetric = entities.metricName ?: lastMentionedMetric,
            history = history + newTurn
        )
    }

    /**
     * Resets conversation state.
     */
    fun clear(): AssistantConversationState = AssistantConversationState()
}
