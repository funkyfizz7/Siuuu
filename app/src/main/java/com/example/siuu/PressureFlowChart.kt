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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
fun PressureFlowChart(
    matchData: MatchIntelligenceData,
    selectedIndex: Int?,
    onPointSelected: (SessionPressurePoint?) -> Unit,
    modifier: Modifier = Modifier,
    breeFont: FontFamily = FontFamily(Font(R.font.bree_serif))
) {
    val textMeasurer = rememberTextMeasurer()
    val points = matchData.pressurePoints

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val idx = findNearestIndex(offset.x, size.width.toFloat(), points.size)
                        onPointSelected(points.getOrNull(idx))
                    }
                }
                .pointerInput(points) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val idx = findNearestIndex(change.position.x, size.width.toFloat(), points.size)
                        onPointSelected(points.getOrNull(idx))
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val usableTop = 22.dp.toPx()
            val usableBottom = height - 16.dp.toPx()
            val usableHeight = usableBottom - usableTop

            val stepX = width / (points.size - 1).coerceAtLeast(1)

            // Y coordinate for pressure (0 at bottom, 100 at top)
            val coordinates = points.mapIndexed { index, p ->
                val x = index * stepX
                val normalizedY = usableBottom - (p.pressureScore / 100f) * usableHeight
                Offset(x, normalizedY)
            }

            // 1. Draw Day Dividers
            drawDayDividers(points, stepX, height, textMeasurer, breeFont)

            // 2. Horizontal Pressure Threshold Lines (70 and 40)
            val y70 = usableBottom - 0.70f * usableHeight
            val y40 = usableBottom - 0.40f * usableHeight

            // 70 Line (Extreme threshold)
            drawLine(
                color = Color(0x33EF4444),
                start = Offset(0f, y70),
                end = Offset(width, y70),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "70 · EXTREME",
                topLeft = Offset(width - 72.dp.toPx(), y70 - 12.dp.toPx()),
                style = TextStyle(
                    fontFamily = breeFont,
                    color = Color(0xFFEF4444).copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            // 40 Line (Moderate threshold)
            drawLine(
                color = Color(0x33FFC72C),
                start = Offset(0f, y40),
                end = Offset(width, y40),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "40 · MODERATE",
                topLeft = Offset(width - 76.dp.toPx(), y40 - 12.dp.toPx()),
                style = TextStyle(
                    fontFamily = breeFont,
                    color = Color(0xFFFFC72C).copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            // 3. Smooth Cubic Spline Path
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
                    lineTo(coordinates.last().x, usableBottom)
                    lineTo(coordinates.first().x, usableBottom)
                    close()
                }
            }

            // 4. Luminous Multi-Zone Area Gradient (Red at top -> Yellow in mid -> Green at bottom)
            drawPath(
                path = filledAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.70f),
                        Color(0xFFFFC72C).copy(alpha = 0.45f),
                        Color(0xFF22C55E).copy(alpha = 0.15f),
                        Color(0xFF22C55E).copy(alpha = 0.02f)
                    ),
                    startY = usableTop,
                    endY = usableBottom
                )
            )

            // 5. White Pressure Stroke Line
            drawPath(
                path = curvePath,
                color = Color(0xFFFFFFFF),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 6. Interactive Scrubber Cursor
            selectedIndex?.let { index ->
                if (index in coordinates.indices) {
                    val selectedCoord = coordinates[index]
                    val selectedPoint = points[index]

                    drawLine(
                        color = Color(0xFFFFFFFF),
                        start = Offset(selectedCoord.x, 0f),
                        end = Offset(selectedCoord.x, height),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
                    )

                    val pointColor = when {
                        selectedPoint.pressureScore >= 70f -> Color(0xFFEF4444)
                        selectedPoint.pressureScore >= 40f -> Color(0xFFFFC72C)
                        else -> Color(0xFF22C55E)
                    }

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
    points: List<SessionPressurePoint>,
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
                    color = Color(0x18FFFFFF),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }
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

private fun findNearestIndex(touchX: Float, totalWidth: Float, count: Int): Int {
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
