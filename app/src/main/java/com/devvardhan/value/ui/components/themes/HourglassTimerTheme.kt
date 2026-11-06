package com.devvardhan.value.ui.components.themes

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import kotlin.math.sqrt

/**
 * Precomputed pseudo-random grain offsets (normalized 0f..1f) to avoid any memory allocations per frame.
 */
private val PRECOMPUTED_GRAIN_X = floatArrayOf(
    0.35f, 0.62f, 0.48f, 0.28f, 0.71f, 0.52f, 0.39f, 0.58f,
    0.44f, 0.66f, 0.31f, 0.74f, 0.50f, 0.42f, 0.59f, 0.36f,
    0.68f, 0.46f, 0.54f, 0.29f, 0.63f, 0.40f, 0.57f, 0.49f
)
private val PRECOMPUTED_GRAIN_Y = floatArrayOf(
    0.15f, 0.22f, 0.45f, 0.68f, 0.75f, 0.32f, 0.82f, 0.18f,
    0.55f, 0.62f, 0.38f, 0.85f, 0.92f, 0.26f, 0.48f, 0.72f,
    0.12f, 0.65f, 0.88f, 0.34f, 0.58f, 0.78f, 0.42f, 0.95f
)

/**
 * Theme 2: Hourglass.
 * Realistic line-drawn hourglass with two funnel/bulb chambers joined by a narrow neck.
 * Sand is strictly clipped to the interior glass path (preventing any spill-over).
 * - Top chamber sand decreases with time, narrowing toward the neck with a subtle center dip.
 * - Bottom chamber sand accumulates into a volume-correct conical mound that widens as it fills.
 * - Falling sand stream and animated grains in the neck run only while active.
 * - Strict monochrome palette with preallocated Paths and zero-allocation grain texture.
 */
@Composable
fun HourglassTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val progress = ThemeFractionCalculator.calculateOverallProgress(remainingMillis, totalMillis)
    val isRunning = status == TimerStatus.Running && remainingMillis > 0L
    val isPaused = status == TimerStatus.Paused

    // Digits derived once per second to prevent unnecessary recomposition
    val displayedSeconds by remember(remainingMillis) {
        derivedStateOf {
            if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
        }
    }
    val formattedTime by remember(displayedSeconds) {
        derivedStateOf {
            TimeUtils.formatHmsFromSeconds(displayedSeconds)
        }
    }
    val totalTimeFormatted = remember(totalMillis) {
        TimeUtils.formatHms(totalMillis)
    }

    // Cached Path objects reused across draw passes
    val glassPath = remember { Path() }
    val topSandPath = remember { Path() }
    val bottomSandPath = remember { Path() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value, layoutMode) {
            AdaptiveLayoutHelper.calculateHourglassDimensions(maxWidth.value, maxHeight.value, layoutMode)
        }

        val hourglassCanvas = @Composable {
            Canvas(
                modifier = Modifier.size(width = dims.width.dp, height = dims.height.dp)
            ) {
                val w = size.width
                val h = size.height

                // Hourglass geometric proportions
                val neckWidth = (w * 0.09f).coerceIn(6f, 16f)
                val neckHeight = (h * 0.05f).coerceIn(6f, 18f)
                val neckTopY = (h - neckHeight) / 2f
                val neckBottomY = neckTopY + neckHeight

                val plateExtra = w * 0.08f
                val strokeThickness = 1.5.dp.toPx()
                val rimInset = w * 0.06f

                // 1. Build the complete closed Glass Path (two rounded funnels meeting at the neck)
                glassPath.reset()
                // Top rim
                glassPath.moveTo(rimInset, 0f)
                glassPath.lineTo(w - rimInset, 0f)
                // Right bulb (top chamber) curving down to neck
                glassPath.cubicTo(
                    w - rimInset, neckTopY * 0.35f,
                    (w + neckWidth) / 2f + 4f, neckTopY * 0.85f,
                    (w + neckWidth) / 2f, neckTopY
                )
                // Neck right edge
                glassPath.lineTo((w + neckWidth) / 2f, neckBottomY)
                // Right bulb (bottom chamber) curving down to bottom rim
                glassPath.cubicTo(
                    (w + neckWidth) / 2f + 4f, neckBottomY + (h - neckBottomY) * 0.15f,
                    w - rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
                    w - rimInset, h
                )
                // Bottom rim
                glassPath.lineTo(rimInset, h)
                // Left bulb (bottom chamber) curving up to neck
                glassPath.cubicTo(
                    rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
                    (w - neckWidth) / 2f - 4f, neckBottomY + (h - neckBottomY) * 0.15f,
                    (w - neckWidth) / 2f, neckBottomY
                )
                // Neck left edge
                glassPath.lineTo((w - neckWidth) / 2f, neckTopY)
                // Left bulb (top chamber) curving up to top rim
                glassPath.cubicTo(
                    (w - neckWidth) / 2f - 4f, neckTopY * 0.85f,
                    rimInset, neckTopY * 0.35f,
                    rimInset, 0f
                )
                glassPath.close()

                // 2. Draw Top and Bottom End Plates
                drawLine(
                    color = SubtleGray,
                    start = Offset(-plateExtra, 0f),
                    end = Offset(w + plateExtra, 0f),
                    strokeWidth = strokeThickness * 1.8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = SubtleGray,
                    start = Offset(-plateExtra, h),
                    end = Offset(w + plateExtra, h),
                    strokeWidth = strokeThickness * 1.8f,
                    cap = StrokeCap.Round
                )

                // 3. Clip sand strictly to the inside of the glass so sand never spills outside
                clipPath(glassPath) {
                    // --- TOP CHAMBER SAND ---
                    if (progress > 0.005f) {
                        val availableTopHeight = neckTopY - strokeThickness
                        // Surface level lowers as progress decreases (from 0 near rim to neckTopY)
                        val topSurfaceY = neckTopY - (availableTopHeight * progress)

                        topSandPath.reset()
                        topSandPath.moveTo(0f, topSurfaceY)

                        // Subtle funnel dip in center while running
                        if (isRunning && progress in 0.03f..0.98f) {
                            topSandPath.quadraticTo(w / 2f, topSurfaceY + 4.dp.toPx(), w, topSurfaceY)
                        } else {
                            topSandPath.lineTo(w, topSurfaceY)
                        }
                        topSandPath.lineTo(w, neckTopY)
                        topSandPath.lineTo(0f, neckTopY)
                        topSandPath.close()

                        drawPath(topSandPath, color = PureWhite)
                    }

                    // --- BOTTOM CHAMBER SAND (Volume-correct conical mound) ---
                    val elapsedFraction = (1f - progress).coerceIn(0f, 1f)
                    if (elapsedFraction > 0.005f) {
                        val availableBottomHeight = h - neckBottomY
                        // Mound height grows non-linearly with elapsed volume: sqrt gives natural conical accumulation
                        val moundHeight = (availableBottomHeight * sqrt(elapsedFraction)).coerceAtMost(availableBottomHeight - 4f)
                        val moundPeakY = h - moundHeight
                        val baseSpread = (w * 0.48f * elapsedFraction.coerceAtLeast(0.35f)).coerceAtMost(w / 2f)

                        bottomSandPath.reset()
                        bottomSandPath.moveTo(0f, h)
                        bottomSandPath.lineTo(w, h)
                        bottomSandPath.lineTo(w, h - (moundHeight * 0.35f))
                        // Curved conical mound peak
                        bottomSandPath.quadraticTo(w / 2f, moundPeakY, 0f, h - (moundHeight * 0.35f))
                        bottomSandPath.close()

                        drawPath(bottomSandPath, color = PureWhite)
                    }

                    // --- FINE SAND GRAIN TEXTURE (Precomputed static noise) ---
                    val grainColor = Color(0x28000000)
                    for (i in PRECOMPUTED_GRAIN_X.indices) {
                        val gx = PRECOMPUTED_GRAIN_X[i] * w
                        val gyTop = neckTopY * PRECOMPUTED_GRAIN_Y[i]
                        val gyBottom = neckBottomY + (h - neckBottomY) * PRECOMPUTED_GRAIN_Y[i]

                        // Draw tiny grains within top sand
                        if (progress > 0.05f) {
                            drawCircle(grainColor, radius = 0.9.dp.toPx(), center = Offset(gx, gyTop))
                        }
                        // Draw tiny grains within bottom sand
                        if (elapsedFraction > 0.05f) {
                            drawCircle(grainColor, radius = 0.9.dp.toPx(), center = Offset(gx, gyBottom))
                        }
                    }

                    // --- FALLING SAND STREAM & GRAINS IN NECK ---
                    if (isRunning && progress > 0.01f) {
                        val elapsedBottomHeight = (h - neckBottomY) * sqrt(elapsedFraction)
                        val streamEndY = (h - elapsedBottomHeight).coerceIn(neckBottomY, h)

                        // Central falling stream
                        drawLine(
                            color = PureWhite,
                            start = Offset(w / 2f, neckTopY - 2f),
                            end = Offset(w / 2f, streamEndY),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Fast looping falling grains (draw-phase clock, zero allocations)
                        val now = SystemClock.elapsedRealtime()
                        val loopProgress1 = (now % 300L) / 300f
                        val loopProgress2 = ((now + 150L) % 300L) / 300f

                        val grain1Y = neckTopY + (streamEndY - neckTopY) * loopProgress1
                        val grain2Y = neckTopY + (streamEndY - neckTopY) * loopProgress2

                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f - 1.2f, grain1Y))
                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f + 1.2f, grain2Y))
                    }
                }

                // 4. Draw outer glass contour outline in subtle gray
                drawPath(
                    path = glassPath,
                    color = BorderSubtle,
                    style = Stroke(width = strokeThickness)
                )
            }
        }

        val digitsContent = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    fontSize = dims.fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted",
                    fontSize = (dims.fontSize * 0.20f).coerceIn(11f, 16f).sp,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        if (dims.isRowLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                hourglassCanvas()
                digitsContent()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                hourglassCanvas()
                Spacer(modifier = Modifier.height(24.dp))
                digitsContent()
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS (100%, 60%, 25%, 0% remaining)
// -------------------------------------------------------------------------------------------------

@Preview(name = "Hourglass 100% Remaining (Full)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewHourglass100() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 60_000L,
            totalMillis = 60_000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Hourglass 60% Remaining", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewHourglass60() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 36_000L,
            totalMillis = 60_000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Hourglass 25% Remaining", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewHourglass25() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 15_000L,
            totalMillis = 60_000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Hourglass 0% Remaining (Drained)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewHourglass0() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 0L,
            totalMillis = 60_000L,
            status = TimerStatus.Finished
        )
    }
}

@Preview(name = "Hourglass Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewHourglassLandscape() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 25_000L,
            totalMillis = 60_000L,
            status = TimerStatus.Running
        )
    }
}
