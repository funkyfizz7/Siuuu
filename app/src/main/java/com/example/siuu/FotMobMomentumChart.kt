package com.example.siuu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
fun FotMobMomentumChart(
    matchData: MatchIntelligenceData,
    selectedIndex: Int?,
    onPointSelected: (SessionMomentumPoint?) -> Unit,
    modifier: Modifier = Modifier,
    breeFont: FontFamily = FontFamily(Font(R.font.bree_serif))
) {
    val textMeasurer = rememberTextMeasurer()
    val points = matchData.points
    val team1 = matchData.team1
    val team2 = matchData.team2

    val maxAbsValue = remember(points) {
        val maxScore = points.maxOfOrNull { abs(it.momentumScore) } ?: 50f
        maxOf(maxScore * 1.15f, 50f)
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val idx = findNearestPointIndex(offset.x, size.width.toFloat(), points.size)
                        onPointSelected(points.getOrNull(idx))
                    }
                }
                .pointerInput(points) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val idx = findNearestPointIndex(change.position.x, size.width.toFloat(), points.size)
                        onPointSelected(points.getOrNull(idx))
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val baselineY = height / 2f
            val usableHeight = height - 42.dp.toPx()

            val stepX = width / (points.size - 1).coerceAtLeast(1)
            val coordinates = points.mapIndexed { index, p ->
                val x = index * stepX
                val normalizedY = baselineY - (p.momentumScore / maxAbsValue) * (usableHeight / 2f)
                Offset(x, normalizedY)
            }

            // 1. Clean Vertical Day Dividers (with Day headers spaced safely)
            drawDayDividers(points, stepX, height, textMeasurer, breeFont)

            // 2. Smooth Sine / Cubic Spline Path
            val curvePath = Path().apply {
                if (coordinates.isNotEmpty()) {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 0 until coordinates.size - 1) {
                        val current = coordinates[i]
                        val next = coordinates[i + 1]
                        val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
                        val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, next.x, next.y)
                    }
                }
            }

            val filledAreaPath = Path().apply {
                addPath(curvePath)
                if (coordinates.isNotEmpty()) {
                    lineTo(coordinates.last().x, baselineY)
                    lineTo(coordinates.first().x, baselineY)
                    close()
                }
            }

            // 3. Bright Luminous Australia Gold Gradient (from Image 2)
            clipRect(left = 0f, top = 0f, right = width, bottom = baselineY) {
                drawPath(
                    path = filledAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            team1.primaryColor.copy(alpha = 0.88f),
                            team1.primaryColor.copy(alpha = 0.45f),
                            team1.primaryColor.copy(alpha = 0.08f)
                        ),
                        startY = 6.dp.toPx(),
                        endY = baselineY
                    )
                )
            }

            // 4. Bright Luminous England Blue Gradient (from Image 2)
            clipRect(left = 0f, top = baselineY, right = width, bottom = height) {
                drawPath(
                    path = filledAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            team2.primaryColor.copy(alpha = 0.08f),
                            team2.primaryColor.copy(alpha = 0.45f),
                            team2.primaryColor.copy(alpha = 0.88f)
                        ),
                        startY = baselineY,
                        endY = height - 6.dp.toPx()
                    )
                )
            }

            // 5. Central Dividing Baseline (y = 0)
            drawLine(
                color = Color(0x55FFFFFF),
                start = Offset(0f, baselineY),
                end = Offset(width, baselineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )

            // 6. Crisp White Momentum Stroke Line
            drawPath(
                path = curvePath,
                color = Color(0xFFFFFFFF),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 7. Team Indicator Labels (AUS & ENG placed with safe spacing)
            drawText(
                textMeasurer = textMeasurer,
                text = "${team1.shortName} ▲",
                topLeft = Offset(14.dp.toPx(), 22.dp.toPx()), // Below Day 1 text so no collision!
                style = TextStyle(
                    fontFamily = breeFont,
                    color = team1.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "${team2.shortName} ▼",
                topLeft = Offset(14.dp.toPx(), height - 24.dp.toPx()),
                style = TextStyle(
                    fontFamily = breeFont,
                    color = team2.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // 8. Interactive Touch Scrubber Indicator
            selectedIndex?.let { index ->
                if (index in coordinates.indices) {
                    val selectedCoord = coordinates[index]
                    val selectedPoint = points[index]

                    drawLine(
                        color = Color(0xFFFFFFFF),
                        start = Offset(selectedCoord.x, 0f),
                        end = Offset(selectedCoord.x, height),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )

                    val pointColor = if (selectedPoint.momentumScore >= 0) team1.primaryColor else team2.primaryColor
                    drawCircle(color = pointColor.copy(alpha = 0.35f), radius = 10.dp.toPx(), center = selectedCoord)
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = selectedCoord)
                    drawCircle(color = pointColor, radius = 3.dp.toPx(), center = selectedCoord)
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDayDividers(
    points: List<SessionMomentumPoint>,
    stepX: Float,
    height: Float,
    textMeasurer: TextMeasurer,
    breeFont: FontFamily
) {
    var lastDay = -1
    points.forEachIndexed { index, point ->
        if (point.dayNumber != lastDay) {
            val x = index * stepX
            if (index > 0) {
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }
            // Day Header placed at the very top (y = 4dp)
            drawText(
                textMeasurer = textMeasurer,
                text = "DAY ${point.dayNumber}",
                topLeft = Offset(x + 5.dp.toPx(), 4.dp.toPx()),
                style = TextStyle(
                    fontFamily = breeFont,
                    color = Color(0xFF888888),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            lastDay = point.dayNumber
        }
    }
}

private fun findNearestPointIndex(touchX: Float, totalWidth: Float, count: Int): Int {
    if (count <= 1) return 0
    val step = totalWidth / (count - 1)
    val rawIndex = (touchX / step).toInt()
    val clamped = rawIndex.coerceIn(0, count - 1)
    return if (clamped < count - 1 && (touchX - clamped * step) > (step / 2f)) {
        clamped + 1
    } else {
        clamped
    }
}
