package com.example.siuu.assistant

/**
 * Forensic execution trace for internal debugging and validation.
 * Captures the complete reasoning lifecycle without exposing internal logs to the UI.
 */
data class AssistantAnswerTrace(
    val question: String,
    val intent: AssistantQuestionType,
    val entities: AssistantEntities,
    val tablesUsed: List<String>,
    val factsRetrieved: List<String>,
    val evidenceUsed: List<String>,
    val finalAnswer: String,
    val reasoning: AssistantEvidence? = null,
    val executionTimeMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
