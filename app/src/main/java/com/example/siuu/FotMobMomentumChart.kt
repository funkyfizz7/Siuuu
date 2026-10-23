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
import androidx.compose.ui.graphics.drawscope.clipRect
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
 * Dynamic FotMob-Style Momentum Chart
 * Renders warehouse MomentumModel points with luminous dual gradients and ultra-smooth touch scrubber.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun DynamicMomentumChart(
    points: List<MomentumModel>,
    team1ShortName: String,
    team1Color: Color,
    team2ShortName: String,
    team2Color: Color,
    selectedIndex: Int?,
    onPointSelected: (MomentumModel?) -> Unit,
    modifier: Modifier = Modifier,
    breeFont: FontFamily = BreeSerifFontFamily
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No momentum sessions recorded for this match.",
                color = Color(0xFFA0A0A0),
                fontSize = 12.sp,
                fontFamily = breeFont
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()

    val maxAbsValue = remember(points) {
        val maxScore = points.maxOfOrNull { abs(it.momentumScore) }?.toFloat() ?: 50f
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
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val idx = findNearestPointIndex(offset.x, size.width.toFloat(), points.size)
                            onPointSelected(points.getOrNull(idx))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val idx = findNearestPointIndex(change.position.x, size.width.toFloat(), points.size)
                            onPointSelected(points.getOrNull(idx))
                        }
                    )
                }
        ) {
            if (points.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val baselineY = height / 2f
            val usableHeight = height - 42.dp.toPx()

            val stepX = if (points.size > 1) width / (points.size - 1) else width / 2f
            val coordinates = points.mapIndexed { index, p ->
                val x = if (points.size > 1) index * stepX else width / 2f
                val normalizedY = baselineY - (p.momentumScore.toFloat() / maxAbsValue) * (usableHeight / 2f)
                Offset(x, normalizedY)
            }

            if (coordinates.isEmpty()) return@Canvas

            // 1. Clean Vertical Day Dividers
            drawDynamicDayDividers(points, stepX, height, textMeasurer, breeFont)

            // 2. Smooth Sine / Cubic Spline Path
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
                lineTo(coordinates.last().x, baselineY)
                lineTo(coordinates.first().x, baselineY)
                close()
            }

            // 3. Dual Team Gradient Fill (Top = Team 1, Bottom = Team 2)
            clipRect(0f, 0f, width, baselineY) {
                drawPath(
                    path = filledAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            team1Color.copy(alpha = 0.55f),
                            team1Color.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = baselineY
                    )
                )
            }

            clipRect(0f, baselineY, width, height) {
                drawPath(
                    path = filledAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            team2Color.copy(alpha = 0.05f),
                            team2Color.copy(alpha = 0.55f)
                        ),
                        startY = baselineY,
                        endY = height
                    )
                )
            }

            // 4. Luminous Spline Line
            clipRect(0f, 0f, width, baselineY) {
                drawPath(
                    path = curvePath,
                    color = team1Color,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            clipRect(0f, baselineY, width, height) {
                drawPath(
                    path = curvePath,
                    color = team2Color,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 5. Zero-Momentum Dotted Baseline
            drawLine(
                color = Color(0xFF404040),
                start = Offset(0f, baselineY),
                end = Offset(width, baselineY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // 6. Interactive Scrubber Indicator Line & Glowing Node (only shown when user selects/touches a point)
            val activeIdx = selectedIndex
            if (activeIdx != null && activeIdx in coordinates.indices) {
                val activeCoord = coordinates[activeIdx]
                val point = points[activeIdx]
                val isTeam1 = point.momentumScore >= 0
                val activeColor = if (isTeam1) team1Color else team2Color

                // Scrubber dashed vertical line
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(activeCoord.x, 0f),
                    end = Offset(activeCoord.x, height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Glowing outer circle
                drawCircle(
                    color = activeColor.copy(alpha = 0.35f),
                    radius = 9.dp.toPx(),
                    center = activeCoord
                )
                // Center solid node
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = activeCoord
                )
                drawCircle(
                    color = activeColor,
                    radius = 3.dp.toPx(),
                    center = activeCoord
                )
            }

            // 7. Team 1 / Team 2 Labels (Positioned cleanly below Day labels and above Session indicators)
            val t1Result = textMeasurer.measure(
                AnnotatedString(team1ShortName),
                style = TextStyle(
                    color = team1Color.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = breeFont,
                    fontWeight = FontWeight.Normal
                )
            )
            drawText(t1Result, topLeft = Offset(12.dp.toPx(), 22.dp.toPx()))

            val t2Result = textMeasurer.measure(
                AnnotatedString(team2ShortName),
                style = TextStyle(
                    color = team2Color.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = breeFont,
                    fontWeight = FontWeight.Normal
                )
            )
            drawText(t2Result, topLeft = Offset(12.dp.toPx(), height - 36.dp.toPx()))
        }
    }
}

/**
 * Draw vertical Day dividers, Day badges, and Session indicators
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDynamicDayDividers(
    points: List<MomentumModel>,
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
            drawText(dayResult, topLeft = Offset(dayX, 6.dp.toPx()))
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

        // Session Label at Bottom (S1, S2, S3)
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
            topLeft = Offset(x - (sessWidth / 2f), height - 14.dp.toPx())
        )
    }
}

private fun findNearestPointIndex(touchX: Float, totalWidth: Float, count: Int): Int {
    if (count <= 1 || totalWidth <= 0f) return 0
    val stepX = totalWidth / (count - 1).coerceAtLeast(1)
    val index = (touchX / stepX).toInt().coerceIn(0, count - 1)
    val leftDiff = abs(touchX - index * stepX)
    val rightDiff = if (index + 1 < count) abs(touchX - (index + 1) * stepX) else Float.MAX_VALUE
    return if (rightDiff < leftDiff) index + 1 else index
}
