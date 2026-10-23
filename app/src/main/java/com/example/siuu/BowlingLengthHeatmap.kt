package com.example.siuu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

private val PitchCardInnerBg = Color(0xFF1C1C1E)
private val PitchCardBorder = Color(0xFF282828)
private val PitchPrimaryText = Color(0xFFFFFFFF)
private val PitchSecondaryText = Color(0xFFA0A0A0)
private val PitchAccentBlue = Color(0xFF00A3FF)
private val PitchModerate = Color(0xFFFF9F1C)

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
 * Commentary-Derived Bowling Length Heatmap Visualisation
 *
 * Renders a broadcast-style top-down cricket pitch with perspective,
 * batter spot & stumps at the top, three partitions (FULL, GOOD, SHORT),
 * and a continuous thermal heatmap overlay showing delivery density.
 */
@Composable
fun BowlingLengthHeatmap(
    data: BowlingHeatmapData?,
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
                    text = "BOWLING LENGTH HEATMAP",
                    color = PitchAccentBlue,
                    fontSize = 11.sp,
                    fontFamily = BreeSerifFontFamily,
                    letterSpacing = 0.5.sp
                )
                if (data != null && data.totalBalls > 0) {
                    Text(
                        text = "${data.totalBalls} BALLS",
                        color = PitchSecondaryText,
                        fontSize = 10.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (data == null || data.totalBalls == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (data == null) "Spatial data not available for this match." else "No deliveries bowled in this innings.",
                        color = PitchSecondaryText,
                        fontSize = 12.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }
                return@Column
            }

            // Pitch & Heatmap Canvas
            PitchHeatmapGraphic(data = data)

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Badges
            val fullCount = data.lengthTotals["FULL"] ?: 0
            val goodCount = data.lengthTotals["GOOD"] ?: 0
            val shortCount = data.lengthTotals["SHORT"] ?: 0
            val totalClassified = data.classifiedLengthCount
            val fullPct = if (totalClassified > 0) (fullCount * 100) / totalClassified else 0
            val goodPct = if (totalClassified > 0) (goodCount * 100) / totalClassified else 0
            val shortPct = if (totalClassified > 0) (shortCount * 100) / totalClassified else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LengthStatBadge(label = "FULL", count = fullCount, pct = fullPct, color = Color(0xFF00E676), modifier = Modifier.weight(1f))
                LengthStatBadge(label = "GOOD", count = goodCount, pct = goodPct, color = Color(0xFFFF9500), modifier = Modifier.weight(1f))
                LengthStatBadge(label = "SHORT", count = shortCount, pct = shortPct, color = Color(0xFFFF3B30), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info: Full tosses & coverage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (data.fullTossCount > 0) {
                    val plural = if (data.fullTossCount > 1) "es" else ""
                    Text(
                        text = "• ${data.fullTossCount} Full Toss$plural (unpitched)",
                        color = PitchModerate,
                        fontSize = 9.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                } else {
                    Text(
                        text = "• All deliveries pitched",
                        color = PitchSecondaryText.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                }

                val covPct = String.format("%.1f%%", data.lengthCoveragePct)
                Text(
                    text = "Length Coverage: $covPct",
                    color = PitchSecondaryText,
                    fontSize = 9.sp,
                    fontFamily = BreeSerifFontFamily
                )
            }
        }
    }
}

@Composable
private fun PitchHeatmapGraphic(
    data: BowlingHeatmapData,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF14381C), Color(0xFF0C2411))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFF224E2A), RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Grass cut stripes in background
            val stripeW = canvasW / 10f
            for (i in 0 until 10) {
                if (i % 2 == 0) {
                    drawRect(
                        color = Color(0x12FFFFFF),
                        topLeft = Offset(i * stripeW, 0f),
                        size = Size(stripeW, canvasH)
                    )
                }
            }

            // Pitch geometry (trapezoid with perspective, narrower at batter end / top)
            val pitchTop = 26f
            val pitchBottom = canvasH - 18f
            val pitchH = pitchBottom - pitchTop

            // Shift center slightly to the left to leave right margin for partition labels
            val centerX = canvasW * 0.40f
            val topW = canvasW * 0.46f
            val bottomW = canvasW * 0.68f

            val topLeftX = centerX - topW / 2f
            val topRightX = centerX + topW / 2f
            val bottomLeftX = centerX - bottomW / 2f
            val bottomRightX = centerX + bottomW / 2f

            fun widthAtY(y: Float): Float {
                val t = ((y - pitchTop) / pitchH).coerceIn(0f, 1f)
                return topW + t * (bottomW - topW)
            }
            fun leftAtY(y: Float) = centerX - widthAtY(y) / 2f
            fun rightAtY(y: Float) = centerX + widthAtY(y) / 2f

            // 2. Draw Pitch Surface
            val pitchPath = Path().apply {
                moveTo(topLeftX, pitchTop)
                lineTo(topRightX, pitchTop)
                lineTo(bottomRightX, pitchBottom)
                lineTo(bottomLeftX, pitchBottom)
                close()
            }
            drawPath(
                path = pitchPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBAA78C),
                        Color(0xFFA69276),
                        Color(0xFF8F7B60)
                    ),
                    startY = pitchTop,
                    endY = pitchBottom
                )
            )

            // Pitch border
            drawPath(
                path = pitchPath,
                color = Color(0x66FFFFFF),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 3. Central Stump-to-Stump Lane (darker shaded strip down the middle)
            val laneTopW = topW * 0.18f
            val laneBottomW = bottomW * 0.18f
            val lanePath = Path().apply {
                moveTo(centerX - laneTopW / 2f, pitchTop)
                lineTo(centerX + laneTopW / 2f, pitchTop)
                lineTo(centerX + laneBottomW / 2f, pitchBottom)
                lineTo(centerX - laneBottomW / 2f, pitchBottom)
                close()
            }
            drawPath(
                path = lanePath,
                color = Color(0x24000000)
            )

            // 4. Stumps & Creases (Top / Batter End)
            val stumpsY = pitchTop + 14f
            val poppingCreaseY = pitchTop + 36f

            // Bowling crease (dashed line across stumps)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            drawLine(
                color = Color(0x99FFFFFF),
                start = Offset(leftAtY(stumpsY), stumpsY),
                end = Offset(rightAtY(stumpsY), stumpsY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )

            // 3 White Wickets & Bail
            val stumpWidth = 2.dp.toPx()
            val stumpHeight = 14.dp.toPx()
            val stumpSpacing = 6.dp.toPx()
            val stumpTop = stumpsY - stumpHeight + 4f
            val stumpBottom = stumpsY + 4f

            // Wickets
            for (s in -1..1) {
                val sx = centerX + s * stumpSpacing
                drawLine(
                    color = Color.White,
                    start = Offset(sx, stumpTop),
                    end = Offset(sx, stumpBottom),
                    strokeWidth = stumpWidth,
                    cap = StrokeCap.Round
                )
            }
            // Bail connecting wickets at top
            drawLine(
                color = Color.White,
                start = Offset(centerX - stumpSpacing - 2f, stumpTop),
                end = Offset(centerX + stumpSpacing + 2f, stumpTop),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Popping Crease (solid white line)
            drawLine(
                color = Color.White,
                start = Offset(leftAtY(poppingCreaseY) - 8f, poppingCreaseY),
                end = Offset(rightAtY(poppingCreaseY) + 8f, poppingCreaseY),
                strokeWidth = 1.8.dp.toPx()
            )

            // Batter spot (subtle stance dot inside the crease)
            drawCircle(
                color = Color(0xCCFFFFFF),
                radius = 3.dp.toPx(),
                center = Offset(centerX, (stumpsY + poppingCreaseY) / 2f)
            )

            // 5. Three Partitions (FULL, GOOD, SHORT)
            val playAreaH = (pitchBottom - 8f) - poppingCreaseY
            val fullEnd = poppingCreaseY + playAreaH * 0.32f
            val goodEnd = poppingCreaseY + playAreaH * 0.67f
            val shortEnd = pitchBottom - 8f

            val partitionDash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

            // Partition Line 1: Between FULL and GOOD
            drawLine(
                color = Color(0xAAFFFFFF),
                start = Offset(leftAtY(fullEnd), fullEnd),
                end = Offset(canvasW - 14f, fullEnd),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = partitionDash
            )

            // Partition Line 2: Between GOOD and SHORT
            drawLine(
                color = Color(0xAAFFFFFF),
                start = Offset(leftAtY(goodEnd), goodEnd),
                end = Offset(canvasW - 14f, goodEnd),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = partitionDash
            )

            // Partition Line 3: Bottom of SHORT
            drawLine(
                color = Color(0x88FFFFFF),
                start = Offset(leftAtY(shortEnd), shortEnd),
                end = Offset(canvasW - 14f, shortEnd),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = partitionDash
            )

            // Partition Center Y positions
            val fullCenterY = (poppingCreaseY + fullEnd) / 2f
            val goodCenterY = (fullEnd + goodEnd) / 2f
            val shortCenterY = (goodEnd + shortEnd) / 2f

            // 6. Draw Smooth Hawk-Eye Heatmap Overlay
            drawThermalHeatmap(
                data = data,
                centerX = centerX,
                widthAtY = ::widthAtY,
                fullY = fullCenterY,
                goodY = goodCenterY,
                shortY = shortCenterY
            )

            // 7. Draw Partition Labels on Right Margin
            val labelX = canvasW * 0.74f

            fun drawPartitionLabel(label: String, count: Int, total: Int, centerY: Float) {
                val pctStr = if (total > 0) "${(count * 100) / total}%" else "0%"
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(labelX, centerY - 14.dp.toPx()),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BreeSerifFontFamily,
                        letterSpacing = 0.5.sp
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$count balls ($pctStr)",
                    topLeft = Offset(labelX, centerY + 1.dp.toPx()),
                    style = TextStyle(
                        color = PitchSecondaryText,
                        fontSize = 9.sp,
                        fontFamily = BreeSerifFontFamily
                    )
                )
            }

            val totalClassified = max(1, data.classifiedLengthCount)
            drawPartitionLabel("FULL", data.lengthTotals["FULL"] ?: 0, totalClassified, fullCenterY)
            drawPartitionLabel("GOOD", data.lengthTotals["GOOD"] ?: 0, totalClassified, goodCenterY)
            drawPartitionLabel("SHORT", data.lengthTotals["SHORT"] ?: 0, totalClassified, shortCenterY)
        }
    }
}

private fun DrawScope.drawThermalHeatmap(
    data: BowlingHeatmapData,
    centerX: Float,
    widthAtY: (Float) -> Float,
    fullY: Float,
    goodY: Float,
    shortY: Float
) {
    val fullCount = data.lengthTotals["FULL"] ?: 0
    val goodCount = data.lengthTotals["GOOD"] ?: 0
    val shortCount = data.lengthTotals["SHORT"] ?: 0

    val maxZoneCount = max(max(fullCount, goodCount), max(shortCount, 1)).toFloat()

    val zones = listOf(
        Triple("FULL", fullY, fullCount),
        Triple("GOOD", goodY, goodCount),
        Triple("SHORT", shortY, shortCount)
    )

    data class EmissionPoint(val x: Float, val y: Float, val weight: Float)
    val points = mutableListOf<EmissionPoint>()

    for ((lenKey, yPos, zoneCount) in zones) {
        if (zoneCount <= 0) continue

        val offCount = data.getCellCount(lenKey, "OUTSIDE_OFF")
        val stumpCount = data.getCellCount(lenKey, "OFF_MIDDLE_STUMPS")
        val legCount = data.getCellCount(lenKey, "LEG_SIDE")
        val unkLineCount = zoneCount - (offCount + stumpCount + legCount)

        val pitchW = widthAtY(yPos)
        val offX = centerX - pitchW * 0.18f
        val stumpX = centerX
        val legX = centerX + pitchW * 0.18f

        if (offCount > 0) {
            points.add(EmissionPoint(offX, yPos, offCount / maxZoneCount))
        }
        if (stumpCount > 0) {
            points.add(EmissionPoint(stumpX, yPos, stumpCount / maxZoneCount))
        }
        if (legCount > 0) {
            points.add(EmissionPoint(legX, yPos, legCount / maxZoneCount))
        }
        // If lateral line was mostly unclassified, center on stump line
        if (unkLineCount > 0 || (offCount == 0 && stumpCount == 0 && legCount == 0)) {
            val weight = (if (unkLineCount > 0) unkLineCount else zoneCount) / maxZoneCount
            points.add(EmissionPoint(stumpX, yPos, weight))
        }
    }

    if (points.isEmpty()) return

    // Add bridging points between adjacent zones to produce a continuous Hawk-Eye column
    if (fullCount > 0 && goodCount > 0) {
        val bridgeY = (fullY + goodY) / 2f
        val bridgeWeight = ((fullCount + goodCount) / (2f * maxZoneCount)) * 0.75f
        points.add(EmissionPoint(centerX, bridgeY, bridgeWeight))
    }
    if (goodCount > 0 && shortCount > 0) {
        val bridgeY = (goodY + shortY) / 2f
        val bridgeWeight = ((goodCount + shortCount) / (2f * maxZoneCount)) * 0.65f
        points.add(EmissionPoint(centerX, bridgeY, bridgeWeight))
    }

    // Layer 1: Wide Cool Cyan/Blue Aura (outer edge)
    for (pt in points) {
        val outerRadius = 46.dp.toPx() * (0.8f + 0.4f * pt.weight)
        val blueAlpha = (0.28f * pt.weight).coerceIn(0.08f, 0.45f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00B0FF).copy(alpha = blueAlpha),
                    Color(0x0000B0FF)
                ),
                center = Offset(pt.x, pt.y),
                radius = outerRadius
            ),
            center = Offset(pt.x, pt.y),
            radius = outerRadius
        )
    }

    // Layer 2: Mid Vibrant Green Aura
    for (pt in points) {
        val greenRadius = 32.dp.toPx() * (0.8f + 0.4f * pt.weight)
        val greenAlpha = (0.45f * pt.weight).coerceIn(0.12f, 0.60f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00E676).copy(alpha = greenAlpha),
                    Color(0x0000E676)
                ),
                center = Offset(pt.x, pt.y),
                radius = greenRadius
            ),
            center = Offset(pt.x, pt.y),
            radius = greenRadius
        )
    }

    // Layer 3: Warm Yellow Mantle
    for (pt in points) {
        if (pt.weight < 0.15f) continue
        val yellowRadius = 22.dp.toPx() * (0.8f + 0.35f * pt.weight)
        val yellowAlpha = (0.65f * pt.weight).coerceIn(0.20f, 0.78f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD600).copy(alpha = yellowAlpha),
                    Color(0x00FFD600)
                ),
                center = Offset(pt.x, pt.y),
                radius = yellowRadius
            ),
            center = Offset(pt.x, pt.y),
            radius = yellowRadius
        )
    }

    // Layer 4: Fiery Orange Core
    for (pt in points) {
        if (pt.weight < 0.25f) continue
        val orangeRadius = 15.dp.toPx() * (0.8f + 0.3f * pt.weight)
        val orangeAlpha = (0.80f * pt.weight).coerceIn(0.35f, 0.88f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF8500).copy(alpha = orangeAlpha),
                    Color(0x00FF8500)
                ),
                center = Offset(pt.x, pt.y),
                radius = orangeRadius
            ),
            center = Offset(pt.x, pt.y),
            radius = orangeRadius
        )
    }

    // Layer 5: Concentrated Red Core (Hotspot for dominant zone)
    for (pt in points) {
        if (pt.weight < 0.45f) continue
        val redRadius = 10.dp.toPx() * (0.8f + 0.3f * pt.weight)
        val redAlpha = (0.92f * pt.weight).coerceIn(0.40f, 0.95f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF2200).copy(alpha = redAlpha),
                    Color(0x00FF2200)
                ),
                center = Offset(pt.x, pt.y),
                radius = redRadius
            ),
            center = Offset(pt.x, pt.y),
            radius = redRadius
        )
    }
}

@Composable
private fun LengthStatBadge(
    label: String,
    count: Int,
    pct: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF242426), RoundedCornerShape(6.dp))
            .border(1.dp, PitchCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Column {
                Text(
                    text = label,
                    color = PitchPrimaryText,
                    fontSize = 10.sp,
                    fontFamily = BreeSerifFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count ($pct%)",
                    color = PitchSecondaryText,
                    fontSize = 9.sp,
                    fontFamily = BreeSerifFontFamily
                )
            }
        }
    }
}
