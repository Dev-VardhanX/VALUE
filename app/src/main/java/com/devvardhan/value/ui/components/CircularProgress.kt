package com.devvardhan.value.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devvardhan.value.ui.theme.InactiveTrack
import com.devvardhan.value.ui.theme.PureWhite

/**
 * Minimalist circular progress ring that cleanly renders remaining time progress.
 *
 * @param progressProvider Lambda provider for progress from 0.0f (completed) to 1.0f (full duration remaining).
 *        Read strictly in the Canvas draw phase to avoid recomposing the outer composable and centered content.
 * @param modifier Layout modifier.
 * @param strokeWidth Thickness of the progress indicator line.
 * @param showLeadingDot Whether to draw a leading circular dot at the progress head.
 * @param content Optional composable to center inside the circular ring.
 */
@Composable
fun MinimalCircularProgress(
    progressProvider: () -> Float,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    strokeWidth: Dp = 3.dp,
    showLeadingDot: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(this.size.width, this.size.height) - strokePx
            if (diameter <= 0f) return@Canvas
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(diameter, diameter)

            // Subtle dark background track (always a true circle)
            drawCircle(
                color = InactiveTrack,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Pure white active progress arc (drawn inside the exact same square rect)
            // Progress provider is read in the draw phase so the parent composable does not recompose
            val sweep = (progressProvider().coerceIn(0f, 1f)) * 360f
            if (sweep > 0f) {
                drawArc(
                    color = PureWhite,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round
                    )
                )

                if (showLeadingDot) {
                    val angleRad = Math.toRadians((-90.0 + sweep)).toFloat()
                    val dotX = center.x + radius * kotlin.math.cos(angleRad)
                    val dotY = center.y + radius * kotlin.math.sin(angleRad)
                    drawCircle(
                        color = PureWhite,
                        radius = strokePx * 1.6f,
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        content()
    }
}

/**
 * Backward-compatible overload accepting a plain Float progress value.
 */
@Composable
fun MinimalCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    strokeWidth: Dp = 3.dp,
    showLeadingDot: Boolean = false,
    content: @Composable () -> Unit = {}
) = MinimalCircularProgress(
    progressProvider = { progress },
    modifier = modifier,
    size = size,
    strokeWidth = strokeWidth,
    showLeadingDot = showLeadingDot,
    content = content
)
