package com.example.siuu.assistant

/**
 * Fast, deterministic, regex- and keyword-based intent classifier for cricket questions.
 * Supports all 100 question forms across Match, Player, Series, Stats, and Commentary domains.
 */
object AssistantIntentClassifier {

    private val COMPARISON_PATTERNS = listOf(
        Regex("""(?i).*(who had\s+(the\s+)?(greater|higher|more|better)\s+impact|who had greater impact).*"""),
        Regex("""(?i).*\b(vs|versus|compared to|better than|or)\b.*"""),
        Regex("""(?i).*(which century mattered more|which century was more important|which 100 was better).*"""),
        Regex("""(?i).*(which partnership was more important|which partnership was better|which stand mattered more).*"""),
        Regex("""(?i).*who was more influential.*""")
    )

    private val STAT_QUERY_PATTERNS = listOf(
        Regex("""(?i).*\b(mvp|most valuable player)\b.*"""),
        Regex("""(?i).*(highest|best|fastest|most|lowest)\s+(control\s*%|control|control percentage|dot balls|dot\s*balls|impact rating|bowling rating|batting rating|individual score|score|economy rate|economy|team total).*"""),
        Regex("""(?i).*who had\s+(the\s+)?(highest|best|lowest)\s+(control percentage|control|bowling rating|batting rating|impact rating|impact|economy rate).*"""),
        Regex("""(?i).*who bowled\s+(the\s+)?most dot balls.*"""),
        Regex("""(?i).*who had\s+(the\s+)?highest impact.*"""),
        Regex("""(?i).*best bowling figures.*"""),
        Regex("""(?i).*most wickets in a (single\s+)?innings.*"""),
        Regex("""(?i).*highest individual score.*"""),
        Regex("""(?i).*highest team total.*"""),
        Regex("""(?i).*lowest economy.*""")
    )

    private val RATING_PATTERNS = listOf(
        Regex("""(?i).*(why|how|what).*(is|was)?\s*.*rated\s*.*"""),
        Regex("""(?i).*why is (he|smith|cummins|root|kohli|siddle|lyon|stokes|\w+)\s+rated.*"""),
        Regex("""(?i).*rating\s*(of|for|breakdown|score|explanation)?\s*.*"""),
        Regex("""(?i).*\brated\s*\d+(\.\d+)?\b.*"""),
        Regex("""(?i).*\b(fotmob rating|overall rating)\b.*""")
    )

    private val PLAYER_SPELL_PATTERNS = listOf(
        Regex("""(?i).*(what was|show me)?\s*.*\b(best|top|greatest)\s+spell.*"""),
        Regex("""(?i).*\bspell\b.*"""),
        Regex("""(?i).*\b(fifer|5-fer)\b.*""")
    )

    private val TURNING_POINT_PATTERNS = listOf(
        Regex("""(?i).*\b(turning point|turning-point|turning points|game changer|game-changer|pivotal moment|decisive moment|match turned|what changed the match|what changed in).*"""),
        Regex("""(?i).*what was the (key|main|defining|crucial|biggest) (moment|point|phase|wicket|event).*""")
    )

    private val MOMENTUM_PATTERNS = listOf(
        Regex("""(?i).*\b(momentum|swing in momentum|who won momentum|session control)\b.*"""),
        Regex("""(?i).*how did (the\s+)?momentum shift.*"""),
        Regex("""(?i).*who controlled day\s*[1-5].*"""),
        Regex("""(?i).*who dominated day\s*[1-5].*""")
    )

    private val PRESSURE_PATTERNS = listOf(
        Regex("""(?i).*\b(pressure|high pressure|peak pressure|pressure index|bowled under pressure|collapsed under pressure)\b.*"""),
        Regex("""(?i).*when was (the\s+)?pressure highest.*"""),
        Regex("""(?i).*who applied (more|most) pressure.*""")
    )

    private val PARTNERSHIP_PATTERNS = listOf(
        Regex("""(?i).*(biggest|highest|best|top)\s+partnership.*"""),
        Regex("""(?i).*\b(partnership|partnerships|stand)\b.*""")
    )

    private val COLLAPSE_PATTERNS = listOf(
        Regex("""(?i).*(biggest|worst|major)\s+collapse.*"""),
        Regex("""(?i).*how did\s+.*\s+collapse.*""")
    )

    private val RECOVERY_PATTERNS = listOf(
        Regex("""(?i).*(best|biggest|greatest)\s+recovery.*"""),
        Regex("""(?i).*how did\s+.*\s+recover.*""")
    )

    private val SESSION_COMMENTARY_PATTERNS = listOf(
        Regex("""(?i).*(summarize|explain|recap|what happened in)\s+(day\s*[1-5]|session\s*[1-3]|morning session|afternoon session|evening session).*"""),
        Regex("""(?i).*(day\s*[1-5]|session\s*[1-3])\s*(summary|recap|breakdown).*"""),
        Regex("""(?i).*what happened in day\s*[1-5]\s*session\s*[1-3].*""")
    )

    private val SERIES_STATS_PATTERNS = listOf(
        Regex("""(?i).*(most|highest|leading|top)\s+(runs|wickets|sixes|fours|centuries|50s|100s|catches|average|batting average|bowling average|strike rate).*"""),
        Regex("""(?i).*(top|leading)\s+(run|wicket)\s*(scorer|taker|getter)s?.*"""),
        Regex("""(?i).*(most runs|most wickets|best batter|best bowler).*"""),
        Regex("""(?i).*who scored the most centuries.*"""),
        Regex("""(?i).*who had the highest batting average.*""")
    )

    private val SERIES_SUMMARY_PATTERNS = listOf(
        Regex("""(?i).*(who won|winner of|result of|summarize|summary of|who dominated)\s+(the\s+)?(ashes\s*2019|ashes|series|tournament|cup|trophy).*"""),
        Regex("""(?i).*(who dominated the ashes|ashes\s*2019\s*summary|series\s*summary|series\s*result).*"""),
        Regex("""(?i).*final scoreline.*"""),
        Regex("""(?i).*retain the ashes.*"""),
        Regex("""(?i).*which tests did\s+.*\s+win.*"""),
        Regex("""(?i).*who won the series.*""")
    )

    private val MATCH_SUMMARY_PATTERNS = listOf(
        Regex("""(?i).*why did (australia|england|india|south africa|pakistan|new zealand|sri lanka|west indies|they|\w+)\s+(win|lose|beat|defeat|dominate).*"""),
        Regex("""(?i).*how did (australia|england|india|south africa|pakistan|new zealand|sri lanka|west indies|they|\w+)\s+(win|lose|beat|defeat).*"""),
        Regex("""(?i).*(match summary|summarize the match|what happened in the match|how did the match end|match result|who won the match|who won the (first|1st|second|2nd|third|3rd|fourth|4th|fifth|5th) test|what was the match story).*"""),
        Regex("""(?i).*(player of the match|victory margin|where was the \w+ test played|what happened in the \d+(st|nd|rd|th) innings).*""")
    )

    private val PLAYER_PERFORMANCE_PATTERNS = listOf(
        Regex("""(?i).*how did\s+.*\s+(perform|play|bowl|bat|do|score\s+his\s+century|score\s+his\s+twin\s+centuries|score\s+century).*"""),
        Regex("""(?i).*(performance|stats|breakdown|scorecard|innings)\s+(of|for)\s+.*"""),
        Regex("""(?i).*(how many runs|how many wickets)\s+did\s+.*\s+(score|take).*""")
    )

    private val FOLLOW_UP_TURNING_POINT_PATTERNS = listOf(
        Regex("""(?i).*(and\s+)?who\s+(caused|triggered|created|did|made|was\s+behind)\s+(it|that|this).*"""),
        Regex("""(?i).*(and\s+)?who\s+was\s+responsible\s+(for\s+it|for\s+that).*""")
    )

    private val FOLLOW_UP_SEQUENCE_PATTERNS = listOf(
        Regex("""(?i).*(and\s+)?what\s+happened\s+(after\s+that|after|next|afterwards|then|subsequently).*"""),
        Regex("""(?i).*(and\s+)?what\s+came\s+next.*""")
    )

    /**
     * Classifies a user query string into an [AssistantQuestionType].
     */
    fun classify(question: String, conversationState: AssistantConversationState? = null): AssistantQuestionType {
        val clean = question.trim()
        if (clean.isBlank()) return AssistantQuestionType.FALLBACK

        // Conversational Follow-Up Checks
        if (FOLLOW_UP_TURNING_POINT_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_TURNING_POINT
        }

        if (FOLLOW_UP_SEQUENCE_PATTERNS.any { it.matches(clean) }) {
            return if (conversationState?.lastDayNumber != null || conversationState?.lastSessionNumber != null) {
                AssistantQuestionType.SESSION_SUMMARY
            } else {
                AssistantQuestionType.MATCH_SUMMARY
            }
        }

        // 0. Comparison Queries ("Who had greater impact?", "Smith or Labuschagne?", "Cummins vs Lyon?", "Which century mattered more?")
        if (COMPARISON_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.COMPARISON_QUERY
        }

        // 1. Stat Queries ("Highest control %?", "Best bowling rating?", "Most dot balls?", "Highest impact rating?", "Who was the MVP?")
        if (STAT_QUERY_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.STAT_QUERY
        }


        // 2. Player Rating ("Why is Smith rated 9.7?", "Why is he rated so highly?")
        if (RATING_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.PLAYER_RATING
        }

        // 3. Player Spell ("What was Lyon's best spell?")
        if (PLAYER_SPELL_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.PLAYER_SPELL
        }

        // 4. Turning Point ("What changed the match?", "What was the turning point?")
        if (TURNING_POINT_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_TURNING_POINT
        }

        // 5. Match Collapses & Recoveries
        if (COLLAPSE_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_COLLAPSE
        }
        if (RECOVERY_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_RECOVERY
        }

        // 6. Match Partnerships ("What was the biggest partnership?")
        if (PARTNERSHIP_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_PARTNERSHIP
        }

        // 7. Commentary / Day / Session Summaries ("Summarize Day 4.", "Explain Session 2.")
        if (SESSION_COMMENTARY_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.SESSION_SUMMARY
        }

        // 8. Series Stats ("Most runs in Ashes 2019?", "Most wickets?", "Best batter?", "Best bowler?")
        if (SERIES_STATS_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.SERIES_STATS
        }

        // 9. Series Summary ("Who dominated the Ashes?", "Who won the series?", "Summarize Ashes 2019")
        if (SERIES_SUMMARY_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.SERIES_SUMMARY
        }

        // 10. Momentum ("Who won momentum?", "Who controlled Day 3?", "Who dominated Day 1?")
        if (MOMENTUM_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_MOMENTUM
        }

        // 11. Pressure ("Who applied more pressure?", "When was pressure highest?")
        if (PRESSURE_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_PRESSURE
        }

        // 12. Match Summary ("Why did Australia win?", "Why did they win?", "Who won the first Test?", "Player of the Match")
        if (MATCH_SUMMARY_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.MATCH_SUMMARY
        }

        // 13. Player Performance ("How did Root score his century?", "How did Warner perform?")
        if (PLAYER_PERFORMANCE_PATTERNS.any { it.matches(clean) }) {
            return AssistantQuestionType.PLAYER_PERFORMANCE
        }

        // Secondary keyword pass
        val lower = clean.lowercase()
        return when {
            lower.contains("rated") || lower.contains("rating") -> AssistantQuestionType.PLAYER_RATING
            lower.contains("spell") -> AssistantQuestionType.PLAYER_SPELL
            lower.contains("turning point") || lower.contains("changed the match") || lower.contains("changed in") -> AssistantQuestionType.MATCH_TURNING_POINT
            lower.contains("partnership") -> AssistantQuestionType.MATCH_PARTNERSHIP
            lower.contains("collapse") -> AssistantQuestionType.MATCH_COLLAPSE
            lower.contains("recover") -> AssistantQuestionType.MATCH_RECOVERY
            lower.contains("day ") || lower.contains("session ") -> AssistantQuestionType.SESSION_SUMMARY
            lower.contains("momentum") || lower.contains("controlled") -> AssistantQuestionType.MATCH_MOMENTUM
            lower.contains("pressure") -> AssistantQuestionType.MATCH_PRESSURE
            lower.contains("ashes") || lower.contains("series") -> AssistantQuestionType.SERIES_SUMMARY
            lower.contains("most runs") || lower.contains("most wickets") || lower.contains("best batter") || lower.contains("best bowler") -> AssistantQuestionType.SERIES_STATS
            lower.contains("win") || lower.contains("won") || lower.contains("dominate") -> AssistantQuestionType.MATCH_SUMMARY
            lower.contains("control") || lower.contains("dot balls") || lower.contains("impact") || lower.contains("mvp") -> AssistantQuestionType.STAT_QUERY
            else -> AssistantQuestionType.FALLBACK
        }
    }
}

