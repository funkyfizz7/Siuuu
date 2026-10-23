package com.example.siuu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Returns the international flag emoji for a given team code or team name.
 */
fun getTeamFlagEmoji(teamCodeOrName: String): String {
    val normalized = teamCodeOrName.trim().uppercase()
    return when {
        normalized.contains("AUS") || normalized.contains("AUSTRALIA") -> "🇦🇺"
        normalized.contains("IND") || normalized.contains("INDIA") -> "🇮🇳"
        normalized.contains("ENG") || normalized.contains("ENGLAND") -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
        normalized.contains("PAK") || normalized.contains("PAKISTAN") -> "🇵🇰"
        normalized.contains("NZ") || normalized.contains("NEW ZEALAND") -> "🇳🇿"
        normalized.contains("SA") || normalized.contains("RSA") || normalized.contains("SOUTH AFRICA") -> "🇿🇦"
        normalized.contains("WI") || normalized.contains("WIN") || normalized.contains("WEST INDIES") -> "🌴"
        normalized.contains("SL") || normalized.contains("SRI LANKA") -> "🇱🇰"
        normalized.contains("BAN") || normalized.contains("BANGLADESH") -> "🇧🇩"
        normalized.contains("AFG") || normalized.contains("AFGHANISTAN") -> "🇦🇫"
        normalized.contains("IRE") || normalized.contains("IRELAND") -> "🇮🇪"
        normalized.contains("ZIM") || normalized.contains("ZIMBABWE") -> "🇿🇼"
        else -> "🏏"
    }
}

/**
 * Reusable Country Badge Component
 *
 * Displays the country flag in a rounded container followed by the team code.
 */
@Composable
fun CountryBadge(
    flagUrl: String? = null,
    teamCode: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        // Flag Container: Slightly larger rounded container for flag
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 25.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF222226))
                .border(0.8.dp, Color(0xFF38383E), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getTeamFlagEmoji(teamCode),
                fontSize = 17.5.sp,
                textAlign = TextAlign.Center
            )
        }

        // Team Code (e.g. AUS, IND, ENG, PAK)
        Text(
            text = teamCode.trim().uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = BreeSerifFontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}
