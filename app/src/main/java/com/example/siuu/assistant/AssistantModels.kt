package com.example.siuu.assistant

/**
 * Extracted and context-resolved entities from user query.
 */
data class AssistantEntities(
    val playerName: String? = null,
    val playerId: Long? = null,
    val player2Name: String? = null,
    val player2Id: Long? = null,
    val teamName: String? = null,
    val seriesName: String? = null,
    val seriesId: String? = null,
    val matchId: Long? = null,
    val metricName: String? = null,
    val targetRating: Double? = null,
    val dayNumber: Int? = null,
    val sessionNumber: Int? = null,
    val inningsNumber: Int? = null,
    val resolvedPronoun: String? = null,
    val comparisonTarget: String? = null
)

/**
 * Structured reasoning model providing FACT + EVIDENCE + CONCLUSION.
 */
data class AssistantEvidence(
    val fact: String,
    val evidence: List<String>,
    val conclusion: String
)

/**
 * Structured, deterministic response returned by the PitchIQ Assistant.
 */
data class AssistantAnswer(
    val questionType: AssistantQuestionType,
    val headline: String,
    val answerText: String,
    val reasoning: AssistantEvidence? = null,
    val keyFacts: List<String> = emptyList(),
    val dataPoints: Map<String, String> = emptyMap(),
    val confidence: Double = 1.0,
    val sources: List<String> = emptyList(),
    val trace: AssistantAnswerTrace? = null,
    val updatedConversationState: AssistantConversationState? = null
)


