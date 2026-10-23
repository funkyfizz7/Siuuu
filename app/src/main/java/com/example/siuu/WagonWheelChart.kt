package com.example.siuu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val PitchCardInnerBg = Color(0xFF1C1C1E)
private val PitchCardBorder = Color(0xFF282828)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchPositive = Color(0xFF22C55E)
private val PitchAccentBlue = Color(0xFF00A3FF)
private val PitchRunsMuted = Color(0xFFD4D4D8)

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
 * Top-down Wagon Wheel Visualisation
 * Displays commentary-derived scoring rays on a cricket ground oval.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun WagonWheelChart(
    data: PlayerWagonWheelData?,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchCardInnerBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PitchCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WAGON WHEEL",
                    color = PitchPositive,
                    fontSize = 11.sp,
                    fontFamily = BreeSerifFontFamily,
                    letterSpacing = 0.5.sp
                )
                if (data != null && data.isLeftHanded) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A2A2E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LHB STANCE",
                            color = PitchSecondaryText,
                            fontSize = 9.sp,
                            fontFamily = BreeSerifFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (data == null || data.scoringBalls == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (data == null) "Spatial data not loaded for this match." else "No scoring shots in this innings.",
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
                return@Column
            }

            // Wheel Canvas
            val textMeasurer = rememberTextMeasurer()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val groundRadius = (minOf(size.width, size.height) / 2f) * 0.72f
                    val infieldRadius = groundRadius * 0.52f

                    // 1. Outfield boundary rope
                    drawCircle(
                        color = PitchCardBorder,
                        radius = groundRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 2. Infield 30-yard circle
                    drawCircle(
                        color = Color(0xFF2C2C30),
                        radius = infieldRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    )

                    // 3. Central Pitch strip
                    val pitchWidth = 8.dp.toPx()
                    val pitchHeight = 28.dp.toPx()
                    drawRect(
                        color = Color(0xFF2F2F35),
                        topLeft = Offset(centerX - pitchWidth / 2f, centerY - pitchHeight / 2f),
                        size = Size(pitchWidth, pitchHeight)
                    )
                    // Batting crease line (batter standing at top facing down towards 180°)
                    drawLine(
                        color = PitchSecondaryText.copy(alpha = 0.5f),
                        start = Offset(centerX - pitchWidth / 2f - 4f, centerY - pitchHeight * 0.25f),
                        end = Offset(centerX + pitchWidth / 2f + 4f, centerY - pitchHeight * 0.25f),
                        strokeWidth = 1.5f
                    )
                    // Bowling crease line at bottom of pitch
                    drawLine(
                        color = PitchSecondaryText.copy(alpha = 0.3f),
                        start = Offset(centerX - pitchWidth / 2f - 3f, centerY + pitchHeight * 0.35f),
                        end = Offset(centerX + pitchWidth / 2f + 3f, centerY + pitchHeight * 0.35f),
                        strokeWidth = 1.0f
                    )

                    // 4. Draw Scoring Rays
                    val isLhb = data.isLeftHanded
                    for (del in data.deliveries) {
                        if (del.runs <= 0 || del.angle == null) continue

                        val angleDeg = if (isLhb) {
                            // Mirror across vertical axis (0 deg is top, mirror X)
                            (360f - del.angle) % 360f
                        } else {
                            del.angle
                        }

                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        // Convention: 0° is straight down ground (UP in screen coords, -Y)
                        // Clockwise: 90° is right (+X), 180° is down (+Y), 270° is left (-X)
                        val rayLength = when {
                            del.runs >= 6 -> groundRadius * 1.12f
                            del.runs >= 4 -> groundRadius * 0.98f
                            del.runs == 3 -> groundRadius * 0.75f
                            del.runs == 2 -> groundRadius * 0.62f
                            else -> groundRadius * 0.48f // 1 run
                        }

                        val endX = (centerX + rayLength * sin(angleRad)).toFloat()
                        val endY = (centerY - rayLength * cos(angleRad)).toFloat()

                        val rayColor = when {
                            del.runs >= 6 -> PitchAccentBlue
                            del.runs >= 4 -> PitchPositive
                            else -> PitchRunsMuted
                        }

                        val rayStroke = when {
                            del.runs >= 6 -> 2.2.dp.toPx()
                            del.runs >= 4 -> 1.7.dp.toPx()
                            else -> 1.0.dp.toPx()
                        }

                        drawLine(
                            color = rayColor,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = rayStroke,
                            cap = StrokeCap.Round
                        )
                    }

                    // 5. Central batter dot
                    drawCircle(
                        color = PitchPrimaryText,
                        radius = 3.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )

                    // 6. Sector Text Labels around boundary
                    val sectorDisplay = listOf(
                        Triple("STRAIGHT", 180.0, "Straight"),
                        Triple("MID_OFF", 202.5, "Mid-Off"),
                        Triple("COVER", 247.5, "Cover"),
                        Triple("POINT", 292.5, "Point"),
                        Triple("THIRD_MAN", 332.5, "3rd Man"),
                        Triple("FINE_LEG", 27.5, "Fine Leg"),
                        Triple("SQUARE_LEG", 67.5, "Sq Leg"),
                        Triple("MID_WICKET", 112.5, "Mid-Wkt"),
                        Triple("MID_ON", 157.5, "Mid-On")
                    )

                    val labelRadius = groundRadius * 1.22f
                    for ((secId, baseAngle, label) in sectorDisplay) {
                        val secData = data.sectors[secId]
                        val runsInSec = secData?.runsTotal ?: 0
                        val shotsInSec = secData?.shotsCount ?: 0
                        if (shotsInSec == 0 && runsInSec == 0) continue

                        val dispAngle = if (isLhb) (360.0 - baseAngle) % 360.0 else baseAngle
                        val rad = Math.toRadians(dispAngle)
                        val lx = (centerX + labelRadius * sin(rad)).toFloat()
                        val ly = (centerY - labelRadius * cos(rad)).toFloat()

                        val textLayout = textMeasurer.measure(
                            text = "$label\n${runsInSec}r",
                            style = TextStyle(
                                color = PitchSecondaryText,
                                fontSize = 8.sp,
                                fontFamily = BreeSerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )

                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(lx - textLayout.size.width / 2f, ly - textLayout.size.height / 2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Six
                Box(modifier = Modifier.size(7.dp).background(PitchAccentBlue, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                val sixesCount = data.sectors.values.sumOf { it.sixes }
                Text(text = "Six ($sixesCount)", color = PitchSecondaryText, fontSize = 10.sp, fontFamily = BreeSerifFontFamily)

                Spacer(modifier = Modifier.width(16.dp))

                // Four
                Box(modifier = Modifier.size(7.dp).background(PitchPositive, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                val foursCount = data.sectors.values.sumOf { it.fours }
                Text(text = "Four ($foursCount)", color = PitchSecondaryText, fontSize = 10.sp, fontFamily = BreeSerifFontFamily)

                Spacer(modifier = Modifier.width(16.dp))

                // 1-3 Runs
                Box(modifier = Modifier.size(7.dp).background(PitchRunsMuted, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                val singlesCount = data.sectors.values.sumOf { it.singles + it.doubles + it.triples }
                Text(text = "1-3 Runs ($singlesCount)", color = PitchSecondaryText, fontSize = 10.sp, fontFamily = BreeSerifFontFamily)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coverage / Provenance Footer
            val covPct = String.format("%.1f", data.coveragePct)
            val bndPct = String.format("%.1f", data.boundaryCoveragePct)
            Text(
                text = "Classified scoring: ${data.directlyClassifiedBalls}/${data.scoringBalls} ($covPct%) • Boundaries: ${data.directlyClassifiedBoundaries}/${data.totalBoundaries} ($bndPct%)",
                color = PitchSecondaryText,
                fontSize = 9.sp,
                fontFamily = BreeSerifFontFamily,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
