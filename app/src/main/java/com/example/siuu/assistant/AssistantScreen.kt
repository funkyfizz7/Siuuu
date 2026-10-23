package com.example.siuu.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siuu.R
import kotlinx.coroutines.launch

// PitchIQ Active Production Design Tokens
private val PitchBlackBg = Color(0xFF000000)
private val PitchCardBg = Color(0xFF121212)
private val PitchCardInnerBg = Color(0xFF0A0A0A)
private val PitchCardBorder = Color(0xFF252525)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchUserBubble = Color(0xFF1E1E1E)

private val BreeSerifFontFamily = FontFamily(
    Font(R.font.bree_serif, FontWeight.Thin),
    Font(R.font.bree_serif, FontWeight.ExtraLight),
    Font(R.font.bree_serif, FontWeight.Light),
    Font(R.font.bree_serif, FontWeight.Normal),
    Font(R.font.bree_serif, FontWeight.Medium),
    Font(R.font.bree_serif, FontWeight.SemiBold),
    Font(R.font.bree_serif, FontWeight.Bold),
    Font(R.font.bree_serif, FontWeight.ExtraBold),
    Font(R.font.bree_serif, FontWeight.Black)
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val answer: AssistantAnswer? = null,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, ASSISTANT
}

@Composable
fun AssistantScreen(
    assistantContext: AssistantContext = AssistantContext(),
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var conversationState by remember { mutableStateOf(AssistantConversationState()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val completedMessageIds = remember { mutableStateListOf<String>() }

    val messages = remember { mutableStateListOf<ChatMessage>() }

    val suggestions = remember(assistantContext) {
        AssistantSuggestions.getSuggestions(assistantContext)
    }

    val hasStartedConversation = remember(messages.size) {
        messages.any { it.sender == MessageSender.USER }
    }

    fun submitQuestion(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || isLoading) return

        // Mark any currently streaming message as completed
        for (i in 0 until messages.size) {
            if (messages[i].isStreaming) {
                messages[i] = messages[i].copy(isStreaming = false)
            }
        }

        messages.add(ChatMessage(sender = MessageSender.USER, text = trimmed))
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            val response = AssistantRepository.answerQuestion(
                context = context,
                question = trimmed,
                assistantContext = assistantContext,
                conversationState = conversationState
            )
            conversationState = response.updatedConversationState ?: conversationState

            val msgId = java.util.UUID.randomUUID().toString()
            messages.add(
                ChatMessage(
                    id = msgId,
                    sender = MessageSender.ASSISTANT,
                    text = response.answerText,
                    answer = response,
                    isStreaming = true
                )
            )
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PitchBlackBg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchBlackBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PitchPrimaryText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = "Ask PitchIQ",
                        color = PitchPrimaryText,
                        fontSize = 20.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasStartedConversation) {
                        IconButton(
                            onClick = {
                                messages.clear()
                                completedMessageIds.clear()
                                conversationState = AssistantConversationState()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset Chat",
                                tint = PitchSecondaryText
                            )
                        }
                    }
                }
                HorizontalDivider(color = PitchCardBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
                color = PitchBlackBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(PitchCardBg)
                            .border(1.dp, PitchCardBorder, RoundedCornerShape(24.dp))
                            .padding(start = 18.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                color = PitchPrimaryText,
                                fontSize = 15.sp,
                                fontFamily = BreeSerifFontFamily
                            ),
                            cursorBrush = SolidColor(PitchPrimaryText),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitQuestion(inputText) }),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Ask anything about this match...",
                                        color = PitchSecondaryText,
                                        fontSize = 14.5.sp,
                                        fontFamily = BreeSerifFontFamily
                                    )
                                }
                                innerTextField()
                            }
                        )

                        IconButton(
                            onClick = { submitQuestion(inputText) },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank() && !isLoading) PitchPrimaryText else Color.Transparent
                                )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PitchSecondaryText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank()) PitchBlackBg else PitchSecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (!hasStartedConversation) {
            // Initial Empty State with Suggested Questions
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "Suggested questions",
                    color = PitchSecondaryText,
                    fontSize = 13.5.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                suggestions.take(4).forEach { question ->
                    Surface(
                        onClick = { submitQuestion(question) },
                        shape = RoundedCornerShape(12.dp),
                        color = PitchCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PitchCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                color = PitchPrimaryText,
                                fontSize = 14.5.sp,
                                fontFamily = BreeSerifFontFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            // Active Conversation Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.sender == MessageSender.USER) {
                        UserMessageView(message = message)
                    } else {
                        val isCompleted = message.id in completedMessageIds
                        AssistantMessageView(
                            message = message,
                            isAlreadyCompleted = isCompleted,
                            onStreamingFinished = { id ->
                                if (!completedMessageIds.contains(id)) {
                                    completedMessageIds.add(id)
                                }
                                val idx = messages.indexOfFirst { it.id == id }
                                if (idx != -1 && messages[idx].isStreaming) {
                                    messages[idx] = messages[idx].copy(isStreaming = false)
                                }
                            },
                            onContentUpdated = {
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.scrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMessageView(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .background(PitchUserBubble)
                .border(1.dp, PitchCardBorder, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = PitchPrimaryText,
                fontSize = 15.5.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AssistantMessageView(
    message: ChatMessage,
    isAlreadyCompleted: Boolean = false,
    onStreamingFinished: ((String) -> Unit)? = null,
    onContentUpdated: (() -> Unit)? = null
) {
    val answer = message.answer
    val reasoning = answer?.reasoning

    var isEvidenceExpanded by remember { mutableStateOf(false) }

    val factText = reasoning?.fact ?: cleanPlainText(message.text)
    val evidenceList = reasoning?.evidence ?: emptyList()
    val conclusionText = reasoning?.conclusion ?: ""

    val factLen = factText.length
    val evLens = remember(evidenceList) { evidenceList.map { it.length } }
    val totalEvLen = remember(evLens) { evLens.sum() }
    val conclusionLen = conclusionText.length
    val totalChars = factLen + totalEvLen + conclusionLen

    val shouldStream = message.isStreaming && !isAlreadyCompleted

    var displayedChars by remember(message.id, shouldStream) {
        mutableStateOf(if (shouldStream) 0 else totalChars)
    }

    LaunchedEffect(message.id, shouldStream) {
        if (shouldStream) {
            val totalLength = totalChars
            val step = 3
            var current = 0
            while (current < totalLength) {
                current = (current + step).coerceAtMost(totalLength)
                displayedChars = current
                onContentUpdated?.invoke()
                kotlinx.coroutines.delay(12)
            }
            displayedChars = totalLength
            onStreamingFinished?.invoke(message.id)
            onContentUpdated?.invoke()
        } else {
            displayedChars = totalChars
        }
    }

    val isFinished = displayedChars >= totalChars

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Main Answer / Fact (White, Bree Serif, 16.sp)
        val factChars = displayedChars.coerceIn(0, factLen)
        val isTypingFact = !isFinished && displayedChars < factLen
        val factDisplay = factText.take(factChars) + if (isTypingFact) " ▌" else ""

        Text(
            text = factDisplay,
            color = PitchPrimaryText,
            fontSize = 16.sp,
            fontFamily = BreeSerifFontFamily,
            lineHeight = 24.sp
        )

        // Supporting Evidence (Light Grey, 15.sp)
        if (displayedChars >= factLen && evidenceList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            evidenceList.forEachIndexed { index, ev ->
                val prevSum = evLens.take(index).sum()
                val evStart = factLen + prevSum
                val evEnd = evStart + evLens[index]

                if (displayedChars >= evStart) {
                    val itemChars = (displayedChars - evStart).coerceIn(0, evLens[index])
                    val isTypingItem = !isFinished && displayedChars in evStart until evEnd
                    val itemDisplay = ev.take(itemChars) + if (isTypingItem) " ▌" else ""

                    Text(
                        text = itemDisplay,
                        color = PitchSecondaryText,
                        fontSize = 15.sp,
                        fontFamily = BreeSerifFontFamily,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Conclusion / Wrap-up (White, 15.5.sp)
        val conclusionStart = factLen + totalEvLen
        if (conclusionText.isNotBlank() && displayedChars >= conclusionStart) {
            Spacer(modifier = Modifier.height(10.dp))
            val conclusionChars = (displayedChars - conclusionStart).coerceIn(0, conclusionLen)
            val isTypingConclusion = !isFinished && displayedChars >= conclusionStart && displayedChars < totalChars
            val conclusionDisplay = conclusionText.take(conclusionChars) + if (isTypingConclusion) " ▌" else ""

            Text(
                text = conclusionDisplay,
                color = PitchPrimaryText,
                fontSize = 15.5.sp,
                fontFamily = BreeSerifFontFamily,
                lineHeight = 23.sp
            )
        }

        // Optional "View evidence" dropdown (Step 7)
        if (isFinished && answer != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isEvidenceExpanded = !isEvidenceExpanded }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEvidenceExpanded) "Hide evidence ▴" else "View evidence ▾",
                    color = PitchSecondaryText,
                    fontSize = 12.5.sp,
                    fontFamily = BreeSerifFontFamily
                )
            }

            AnimatedVisibility(visible = isEvidenceExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchCardBg)
                        .border(1.dp, PitchCardBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Sources used",
                        color = PitchPrimaryText,
                        fontSize = 12.5.sp,
                        fontFamily = BreeSerifFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val sources = remember(answer) { formatSources(answer) }
                    sources.forEach { src ->
                        Text(
                            text = "• $src",
                            color = PitchSecondaryText,
                            fontSize = 12.sp,
                            fontFamily = BreeSerifFontFamily,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun cleanPlainText(text: String): String {
    return text
        .replace("FACT\n", "")
        .replace("EVIDENCE\n", "")
        .replace("CONCLUSION\n", "")
        .trim()
}

private fun formatSources(answer: AssistantAnswer?): List<String> {
    val rawTables = answer?.trace?.tablesUsed ?: emptyList()
    if (rawTables.isNotEmpty()) {
        return rawTables.map { table ->
            when (table.lowercase().trim()) {
                "fact_player_ratings.csv", "fact_player_ratings" -> "Match ratings"
                "fact_match_momentum.csv", "fact_momentum.csv", "fact_match_momentum" -> "Momentum"
                "fact_turning_points.csv", "fact_turning_points" -> "Turning points"
                "fact_match_pressure.csv", "fact_pressure.csv", "fact_match_pressure" -> "Pressure flow"
                "fact_scorecard.csv", "fact_partnerships.csv", "fact_scorecard" -> "Scorecard"
                "fact_session_commentary.csv", "fact_match_sessions.csv" -> "Session analysis"
                "fact_player_innings.csv", "innings_commentary.csv" -> "Innings records"
                "fact_series_leaders.csv" -> "Series statistics"
                else -> table.removeSuffix(".csv").replace('_', ' ').replaceFirstChar { it.uppercase() }
            }
        }.distinct()
    }
    return listOf("Match ratings", "Momentum", "Turning points", "Scorecard")
}
