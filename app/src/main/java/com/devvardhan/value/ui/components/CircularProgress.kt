package com.devvardhan.value.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devvardhan.value.ui.theme.InactiveTrack
import com.devvardhan.value.ui.theme.PureWhite

/**
 * Minimalist circular progress ring that cleanly renders remaining time progress.
 *
 * @param progress Value from 0.0f (completed) to 1.0f (full duration remaining).
 * @param modifier Layout modifier.
 * @param strokeWidth Thickness of the progress indicator line.
 * @param content Optional composable to center inside the circular ring.
 */
@Composable
fun MinimalCircularProgress(
    progress: Float,
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

            // Subtle dark background track
            drawCircle(
                color = InactiveTrack,
                style = Stroke(width = strokePx)
            )

            // Pure white active progress arc
            val sweep = (progress.coerceIn(0f, 1f)) * 360f
            if (sweep > 0f) {
                drawArc(
                    color = PureWhite,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round
                    )
                )

                if (showLeadingDot) {
                    val angleRad = Math.toRadians((-90.0 + sweep)).toFloat()
                    val radius = (this.size.width - strokePx) / 2f
                    val dotX = (this.size.width / 2f) + radius * kotlin.math.cos(angleRad)
                    val dotY = (this.size.height / 2f) + radius * kotlin.math.sin(angleRad)
                    drawCircle(
                        color = PureWhite,
                        radius = strokePx * 1.6f,
                        center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                    )
                }
            }
        }

        content()
    }
}

