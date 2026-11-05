package com.android.value.ui.components

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
import com.android.value.ui.theme.InactiveTrack
import com.android.value.ui.theme.PureWhite

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
            }
        }

        content()
    }
}

