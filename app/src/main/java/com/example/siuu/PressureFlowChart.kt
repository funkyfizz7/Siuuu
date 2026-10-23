package com.example.siuu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlin.math.abs

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
 * Dynamic 0-100 Pressure Flow Spline Chart
 * Renders warehouse PressureModel records with tri-color zone gradient and ultra-smooth touch scrubber.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun DynamicPressureFlowChart(
    points: List<PressureModel>,
    selectedIndex: Int?,
    onPointSelected: (PressureModel?) -> Unit,
    modifier: Modifier = Modifier,
    breeFont: FontFamily = BreeSerifFontFamily
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pressure points recorded for this match.",
                color = Color(0xFFA0A0A0),
                fontSize = 12.sp,
                fontFamily = breeFont
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val idx = findNearestPressureIndex(offset.x, size.width.toFloat(), points.size)
                        onPointSelected(points.getOrNull(idx))
                    }
                }
                .pointerInput(points) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val idx = findNearestPressureIndex(offset.x, size.width.toFloat(), points.size)
                            onPointSelected(points.getOrNull(idx))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val idx = findNearestPressureIndex(change.position.x, size.width.toFloat(), points.size)
                            onPointSelected(points.getOrNull(idx))
                        }
                    )
                }
        ) {
            if (points.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val usableTop = 22.dp.toPx()
            val usableBottom = height - 16.dp.toPx()
            val usableHeight = usableBottom - usableTop

            val stepX = if (points.size > 1) width / (points.size - 1) else width / 2f

            val coordinates = points.mapIndexed { index, p ->
                val x = if (points.size > 1) index * stepX else width / 2f
                val normalizedY = usableBottom - (p.pressureScore.toFloat() / 100f) * usableHeight
                Offset(x, normalizedY)
            }

            if (coordinates.isEmpty()) return@Canvas

            // 1. Draw Day Dividers
            drawDynamicPressureDayDividers(points, stepX, height, textMeasurer, breeFont)

            // 2. Horizontal Pressure Threshold Lines (70 and 40)
            val y70 = usableBottom - 0.70f * usableHeight
            val y40 = usableBottom - 0.40f * usableHeight

            // 70 Line (High threshold)
            drawLine(
                color = Color(0x33EF4444),
                start = Offset(0f, y70),
                end = Offset(width, y70),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "70 · HIGH",
                topLeft = Offset(width - 55.dp.toPx(), y70 - 12.dp.toPx()),
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
                topLeft = Offset(width - 78.dp.toPx(), y40 - 12.dp.toPx()),
                style = TextStyle(
                    fontFamily = breeFont,
                    color = Color(0xFFFFC72C).copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            // 3. Smooth Sine / Cubic Spline Path
            val curvePath = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
                for (i in 0 until coordinates.size - 1) {
                    val current = coordinates[i]
                    val next = coordinates[i + 1]
                    val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
                    val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)
                    cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, next.x, next.y)
                }
            }

            val filledAreaPath = Path().apply {
                addPath(curvePath)
                lineTo(coordinates.last().x, usableBottom)
                lineTo(coordinates.first().x, usableBottom)
                close()
            }

            // 4. Tri-Zone Dynamic Gradient Fill
            drawPath(
                path = filledAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.50f),
                        Color(0xFFFFC72C).copy(alpha = 0.35f),
                        Color(0xFF22C55E).copy(alpha = 0.15f),
                        Color(0xFF22C55E).copy(alpha = 0.02f)
                    ),
                    startY = usableTop,
                    endY = usableBottom
                )
            )

            // 5. White / Glowing Outline Stroke
            drawPath(
                path = curvePath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 6. Interactive Scrubber Indicator Line & Glowing Node (only shown when user selects/touches a point)
            val activeIdx = selectedIndex
            if (activeIdx != null && activeIdx in coordinates.indices) {
                val activeCoord = coordinates[activeIdx]
                val point = points[activeIdx]
                val nodeColor = when {
                    point.pressureScore >= 70 -> Color(0xFFEF4444)
                    point.pressureScore >= 40 -> Color(0xFFFFC72C)
                    else -> Color(0xFF22C55E)
                }

                // Vertical dashed scrubber line
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(activeCoord.x, usableTop),
                    end = Offset(activeCoord.x, usableBottom),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )

                // Glowing outer ring
                drawCircle(
                    color = nodeColor.copy(alpha = 0.35f),
                    radius = 9.dp.toPx(),
                    center = activeCoord
                )
                // Center node
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = activeCoord
                )
                drawCircle(
                    color = nodeColor,
                    radius = 3.dp.toPx(),
                    center = activeCoord
                )
            }
        }
    }
}

/**
 * Draw vertical Day dividers, Day badges, and Session indicators
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDynamicPressureDayDividers(
    points: List<PressureModel>,
    stepX: Float,
    height: Float,
    textMeasurer: TextMeasurer,
    breeFont: FontFamily
) {
    if (points.isEmpty()) return

    var lastDay = -1
    points.forEachIndexed { index, p ->
        val x = if (points.size > 1) index * stepX else size.width / 2f

        // New Day Boundary
        if (p.dayNumber != lastDay) {
            lastDay = p.dayNumber

            // Prominent Day Divider Line
            if (index > 0) {
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Day Badge at Top
            val dayText = "DAY ${p.dayNumber}"
            val dayResult = textMeasurer.measure(
                AnnotatedString(dayText),
                style = TextStyle(
                    color = Color(0xFF888888),
                    fontSize = 9.sp,
                    fontFamily = breeFont,
                    fontWeight = FontWeight.Normal
                )
            )
            val dayWidth = dayResult.size.width.toFloat()
            val dayX = if (x + dayWidth + 6.dp.toPx() > size.width) {
                (size.width - dayWidth - 6.dp.toPx()).coerceAtLeast(0f)
            } else {
                x + 4.dp.toPx()
            }
            drawText(dayResult, topLeft = Offset(dayX, 4.dp.toPx()))
        } else if (index > 0) {
            // Subtle Session Sub-Divider
            drawLine(
                color = Color(0xFF1E1E1E),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 0.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
        }

        // Session Indicator at Bottom (S1, S2, S3)
        val sessText = "S${p.sessionOfDay}"
        val sessResult = textMeasurer.measure(
            AnnotatedString(sessText),
            style = TextStyle(
                color = Color(0xFF555555),
                fontSize = 8.sp,
                fontFamily = breeFont,
                fontWeight = FontWeight.Normal
            )
        )
        val sessWidth = sessResult.size.width.toFloat()
        drawText(
            sessResult,
            topLeft = Offset(x - (sessWidth / 2f), height - 12.dp.toPx())
        )
    }
}

private fun findNearestPressureIndex(touchX: Float, totalWidth: Float, count: Int): Int {
    if (count <= 1 || totalWidth <= 0f) return 0
    val stepX = totalWidth / (count - 1).coerceAtLeast(1)
    val index = (touchX / stepX).toInt().coerceIn(0, count - 1)
    val leftDiff = abs(touchX - index * stepX)
    val rightDiff = if (index + 1 < count) abs(touchX - (index + 1) * stepX) else Float.MAX_VALUE
    return if (rightDiff < leftDiff) index + 1 else index
}
