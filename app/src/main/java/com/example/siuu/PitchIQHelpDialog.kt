package com.example.siuu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val BreeSerifHelpFontFamily = FontFamily(
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

const val MATCH_MOMENTUM_FLOW_HELP_TITLE = "Match Momentum Flow"
val MATCH_MOMENTUM_FLOW_HELP_PARAGRAPHS = listOf(
    "Match Momentum Flow shows how control of a Test match shifts between the two teams over time. It helps identify periods when one team gains an advantage, loses momentum, or comes under pressure.",
    "The flow is based on the match's progression across days and sessions. Changes in the flow help users understand the story behind the score, including important turning points and shifts in control.",
    "The flow is a contextual indicator of match momentum, not a guarantee of the eventual result."
)

const val PRESSURE_FLOW_GRAPH_HELP_TITLE = "Pressure Flow Graph"
val PRESSURE_FLOW_GRAPH_HELP_PARAGRAPHS = listOf(
    "The Pressure Flow Graph illustrates how pressure changes during a Test match across its days and sessions. It helps users understand when a team is under pressure or in a position to apply pressure to its opponent.",
    "Use the graph to explore important phases of the match, such as periods of sustained pressure, a possible comeback, or a change in the balance between the teams.",
    "Pressure is a contextual measure of match conditions. It should be interpreted alongside the score, wickets, overs, and match situation rather than as a prediction of the result.",
    "Also, the pressure flow graph corresponds to the team that is batting at that moment ie at any given point in the graph that pressure is on the team that is batting."
)

/**
 * Small circular question-mark help icon matching the Player Traits reference.
 * Provides a comfortable touch target (28.dp) with a crisp 16.dp grey badge.
 */
@Composable
fun PitchIQHelpIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF9E9E9E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                color = Color(0xFF121212),
                fontSize = 10.5.sp,
                fontFamily = BreeSerifHelpFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

/**
 * Modal help dialog matching the Player Traits popup reference design.
 * Features rounded card container (#222222), bordered close button, and scrollable body text.
 */
@Composable
fun PitchIQHelpDialog(
    title: String,
    paragraphs: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* prevent closing when clicking inside card */ }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Row: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontFamily = BreeSerifHelpFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Close Button (Rounded square with border)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF888888), RoundedCornerShape(6.dp))
                                .background(Color(0xFF252525))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFE0E0E0),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Explanation paragraphs in a scrollable column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        paragraphs.forEachIndexed { index, paragraph ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                            Text(
                                text = paragraph,
                                color = Color(0xFFE4E4E6),
                                fontSize = 14.sp,
                                fontFamily = BreeSerifHelpFontFamily,
                                lineHeight = 21.sp,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
