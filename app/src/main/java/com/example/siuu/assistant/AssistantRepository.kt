package com.example.siuu.assistant

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main coordinator repository for the PitchIQ AI Assistant.
 *
 * Execution Pipeline:
 * Question + Context
 *   -> Context Awareness & Pronoun Resolution (AssistantContextResolver)
 *   -> Deterministic Intent Classification (AssistantIntentClassifier)
 *   -> Entity Extraction & Normalization (AssistantEntityExtractor)
 *   -> Multi-Table Evidence Retrieval (AssistantEvidenceRetriever)
 *   -> Reasoning & Synthesis Layer (AssistantReasoningEngine)
 *   -> Structured Answer (AssistantAnswer)
 *
 * Core Guarantees:
 * - 100% Deterministic and Repository-Driven (Zero LLM / Zero External API calls).
 * - Instant performance across Match, Player, Series, Stats, and Commentary domains.
 */
object AssistantRepository {

    /**
     * Answers any user question using screen context awareness, conversation state tracking, and deterministic warehouse retrieval.
     */
    suspend fun answerQuestion(
        context: Context,
        question: String,
        assistantContext: AssistantContext = AssistantContext(),
        conversationState: AssistantConversationState = AssistantConversationState()
    ): AssistantAnswer = withContext(Dispatchers.IO) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank()) {
            return@withContext AssistantAnswer(
                questionType = AssistantQuestionType.FALLBACK,
                headline = "Ask PitchIQ Assistant",
                answerText = "Please ask a question about match results, player ratings, turning points, momentum, or series statistics.",
                keyFacts = listOf(
                    "Try: 'Why did they win?'",
                    "Try: 'What was the turning point?'",
                    "Try: 'Why is he rated so highly?'",
                    "Try: 'Most runs in Ashes 2019?'",
                    "Try: 'Summarize Day 4.'"
                ),
                confidence = 1.0,
                sources = emptyList(),
                updatedConversationState = conversationState
            )
        }

        // 1. Initial Entity Extraction
        val rawEntities = AssistantEntityExtractor.extract(context, cleanQuestion, assistantContext)

        // 2. Context & Pronoun Resolution Layer ("he", "they", "that spell", "who caused it?", conversational history)
        val resolvedEntities = AssistantContextResolver.resolve(context, cleanQuestion, rawEntities, assistantContext, conversationState)

        // 3. Classify intent with conversational follow-up awareness
        val intent = AssistantIntentClassifier.classify(cleanQuestion, conversationState)

        // 4. Evidence Retrieval from Warehouse Tables
        val facts = AssistantEvidenceRetriever.retrieveEvidence(context, intent, resolvedEntities)

        // 5. Reasoning Layer Synthesis
        val answer = AssistantReasoningEngine.synthesizeAnswer(intent, resolvedEntities, facts, cleanQuestion)

        // 6. Update and attach conversational state
        val updatedState = conversationState.updateWithTurn(cleanQuestion, intent, resolvedEntities, answer)
        return@withContext answer.copy(updatedConversationState = updatedState)
    }
}


