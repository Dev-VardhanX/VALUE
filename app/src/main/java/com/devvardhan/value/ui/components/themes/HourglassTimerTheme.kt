package com.devvardhan.value.ui.components.themes

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import kotlin.math.pow
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
 *
 * Performance optimizations:
 * - remainingMillisProvider evaluated strictly inside the Canvas draw phase for sand level & grains.
 * - Digits derived once per second via derivedStateOf, eliminating 60fps recomposition.
 * - Grains loop animated using (totalMillis - remainingMillis) without touching SystemClock.elapsedRealtime().
 */
private const val SAND_SAMPLES = 32

/** Cubic bezier point (one axis) */
private fun bez(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
    val u = 1f - t
    return u * u * u * a + 3f * u * u * t * b + 3f * u * t * t * c + t * t * t * d
}

/**
 * Builds a table of (y level, cumulative area) walking along one bulb edge.
 * Index 0 is where the sand starts to accumulate (neck for top, bottom rim for bottom).
 */
private fun fillSandTable(
    w: Float,
    px: FloatArray, py: FloatArray,   // 4 bezier control points of the right edge
    ys: FloatArray, cum: FloatArray
) {
    var prevY = 0f
    var prevW = 0f
    for (i in 0..SAND_SAMPLES) {
        val t = i / SAND_SAMPLES.toFloat()
        val y = bez(py[0], py[1], py[2], py[3], t)
        val width = 2f * (bez(px[0], px[1], px[2], px[3], t) - w / 2f)
        ys[i] = y
        if (i == 0) {
            cum[0] = 0f
        } else {
            cum[i] = cum[i - 1] + (prevW + width) / 2f * kotlin.math.abs(y - prevY)
        }
        prevY = y
        prevW = width
    }
}

/** Given the fraction of total sand (0..1), returns the surface Y inside this bulb. */
private fun levelFromTable(ys: FloatArray, cum: FloatArray, fraction: Float): Float {
    val target = fraction.coerceIn(0f, 1f) * cum[SAND_SAMPLES]
    var i = 1
    while (i < SAND_SAMPLES && cum[i] < target) i++
    val c0 = cum[i - 1]
    val c1 = cum[i]
    val f = if (c1 > c0) ((target - c0) / (c1 - c0)).coerceIn(0f, 1f) else 0f
    return ys[i - 1] + (ys[i] - ys[i - 1]) * f
}

@Composable
fun HourglassTimerTheme(
    remainingMillisProvider: () -> Long = { 0L },
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val isPaused = status == TimerStatus.Paused

    // Digits derived once per second to prevent unnecessary recomposition
    val displayedSeconds by remember(remainingMillisProvider) {
        derivedStateOf {
            val rem = remainingMillisProvider()
            if (rem <= 0L) 0L else (rem + 999L) / 1000L
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
        val isCompact = maxHeight < 240.dp
        val dims = remember(maxWidth.value, maxHeight.value, layoutMode) {
            AdaptiveLayoutHelper.calculateHourglassDimensions(maxWidth.value, maxHeight.value, layoutMode)
        }
        // add this import: import kotlin.math.pow
        val hourglassCanvas = @Composable {
            // Preallocated tables: no allocations while drawing
            val topYs = remember { FloatArray(SAND_SAMPLES + 1) }
            val topCum = remember { FloatArray(SAND_SAMPLES + 1) }
            val botYs = remember { FloatArray(SAND_SAMPLES + 1) }
            val botCum = remember { FloatArray(SAND_SAMPLES + 1) }
            val topPx = remember { FloatArray(4) }
            val topPy = remember { FloatArray(4) }
            val botPx = remember { FloatArray(4) }
            val botPy = remember { FloatArray(4) }

            Canvas(
                modifier = Modifier.size(width = dims.width.dp, height = dims.height.dp)
            ) {
                val rem = remainingMillisProvider()
                val progress = ThemeFractionCalculator
                    .calculateOverallProgress(rem, totalMillis)
                    .coerceIn(0f, 1f)
                val isRunning = status == TimerStatus.Running && rem > 0L

                val w = size.width
                val h = size.height

                // Hourglass geometric proportions
                val neckWidth = (w * 0.09f).coerceIn(6f, 6f)
                val neckHeight = (h * 0.05f).coerceIn(6f, 18f)
                val neckTopY = (h - neckHeight) / 2f
                val neckBottomY = neckTopY + neckHeight

                val strokeThickness = 1.5.dp.toPx()

                // THINNER HOURGLASS: bigger inset = narrower bulbs (was 0.06f)
                val rimInset = w * 0.06f
                // Plates stick out slightly past the glass on each side
                val plateOverhang = w * 0.05f

                // STREAM WIDTH: fraction of the neck width (0.8f = almost fills the neck)
                val streamWidth = neckWidth * 0.8f

                // 1. Glass path
                glassPath.reset()
                glassPath.moveTo(rimInset, 0f)
                glassPath.lineTo(w - rimInset, 0f)
                glassPath.cubicTo(
                    w - rimInset, neckTopY * 0.35f,
                    (w + neckWidth) / 2f + 4f, neckTopY * 0.85f,
                    (w + neckWidth) / 2f, neckTopY
                )
                glassPath.lineTo((w + neckWidth) / 2f, neckBottomY)
                glassPath.cubicTo(
                    (w + neckWidth) / 2f + 4f, neckBottomY + (h - neckBottomY) * 0.15f,
                    w - rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
                    w - rimInset, h
                )
                glassPath.lineTo(rimInset, h)
                glassPath.cubicTo(
                    rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
                    (w - neckWidth) / 2f - 4f, neckBottomY + (h - neckBottomY) * 0.15f,
                    (w - neckWidth) / 2f, neckBottomY
                )
                glassPath.lineTo((w - neckWidth) / 2f, neckTopY)
                glassPath.cubicTo(
                    (w - neckWidth) / 2f - 4f, neckTopY * 0.85f,
                    rimInset, neckTopY * 0.35f,
                    rimInset, 0f
                )
                glassPath.close()

                // 2. End plates (follow the glass width)
                val plateThickness = 2.5.dp.toPx()
                drawLine(
                    color = PureWhite,
                    start = Offset(rimInset - plateOverhang, 0f),
                    end = Offset(w - rimInset + plateOverhang, 0f),
                    strokeWidth = plateThickness,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = PureWhite,
                    start = Offset(rimInset - plateOverhang, h),
                    end = Offset(w - rimInset + plateOverhang, h),
                    strokeWidth = plateThickness,
                    cap = StrokeCap.Round
                )

                // ---- Volume-based sand levels (top and bottom always match) ----
                val elapsedFraction = 1f - progress
                val botH = h - neckBottomY

                // Top bulb edge, starting at the neck going up to the rim
                topPx[0] = (w + neckWidth) / 2f;       topPy[0] = neckTopY
                topPx[1] = (w + neckWidth) / 2f + 4f;  topPy[1] = neckTopY * 0.85f
                topPx[2] = w - rimInset;               topPy[2] = neckTopY * 0.35f
                topPx[3] = w - rimInset;               topPy[3] = 0f
                fillSandTable(w, topPx, topPy, topYs, topCum)

                // Bottom bulb edge, starting at the bottom rim going up to the neck
                botPx[0] = w - rimInset;               botPy[0] = h
                botPx[1] = w - rimInset;               botPy[1] = neckBottomY + botH * 0.65f
                botPx[2] = (w + neckWidth) / 2f + 4f;  botPy[2] = neckBottomY + botH * 0.15f
                botPx[3] = (w + neckWidth) / 2f;       botPy[3] = neckBottomY
                fillSandTable(w, botPx, botPy, botYs, botCum)

                val topSandLevelY = levelFromTable(topYs, topCum, progress)
                val bottomLevelY = levelFromTable(botYs, botCum, elapsedFraction)

                // Bottom pile: peaked early, flat when full
                val depth = h - bottomLevelY
                val moundAmp = depth * 0.35f * (1f - elapsedFraction * elapsedFraction * elapsedFraction)
                val moundPeakY = bottomLevelY - moundAmp * 0.3f
                val moundEdgeY = bottomLevelY + moundAmp * 0.7f

                // 3. Sand, clipped to the glass
                clipPath(glassPath) {
                    // --- TOP SAND ---
                    if (progress > 0f) {
                        // Fade the crater in at the start and out near the end, so it never snaps
                        val fadeIn = ((1f - progress) / 0.08f).coerceIn(0f, 1f)   // 0 at the very start -> 1 after 8%
                        val fadeOut = (progress / 0.15f).coerceIn(0f, 1f)         // 1 until the last 15% -> 0 at empty
                        val dip = 3.dp.toPx() * fadeIn * fadeOut

                        topSandPath.reset()
                        topSandPath.moveTo(0f, topSandLevelY)
                        topSandPath.quadraticTo(w / 2f, topSandLevelY + dip * 2f, w, topSandLevelY)
                        topSandPath.lineTo(w, neckBottomY)
                        topSandPath.lineTo(0f, neckBottomY)
                        topSandPath.close()

                        drawPath(topSandPath, color = PureWhite)
                    }

                    // --- BOTTOM SAND ---
                    if (elapsedFraction > 0f) {
                        // quadratic midpoint = (edge + control) / 2, so compensate to hit the peak
                        val controlY = 2f * moundPeakY - moundEdgeY

                        bottomSandPath.reset()
                        bottomSandPath.moveTo(0f, h)
                        bottomSandPath.lineTo(w, h)
                        bottomSandPath.lineTo(w, moundEdgeY)
                        bottomSandPath.quadraticTo(w / 2f, controlY, 0f, moundEdgeY)
                        bottomSandPath.close()

                        drawPath(bottomSandPath, color = PureWhite)
                    }

                    // --- GRAIN TEXTURE (only where sand exists) ---
                    val grainColor = Color(0x28000000)
                    val grainRadius = 0.9.dp.toPx()
                    for (i in PRECOMPUTED_GRAIN_X.indices) {
                        val gx = PRECOMPUTED_GRAIN_X[i] * w
                        val gyTop = neckTopY * PRECOMPUTED_GRAIN_Y[i]
                        val gyBottom = neckBottomY + botH * PRECOMPUTED_GRAIN_Y[i]

                        if (progress > 0f && gyTop >= topSandLevelY) {
                            drawCircle(grainColor, radius = grainRadius, center = Offset(gx, gyTop))
                        }
                        if (elapsedFraction > 0f && gyBottom >= bottomLevelY) {
                            drawCircle(grainColor, radius = grainRadius, center = Offset(gx, gyBottom))
                        }
                    }

                    // --- FALLING STREAM (wider, Butt cap so the width is exact) ---
                    if (isRunning) {
                        val streamEndY = moundPeakY.coerceIn(neckBottomY, h)

                        drawLine(
                            color = PureWhite,
                            start = Offset(w / 2f, neckTopY - 2f),
                            end = Offset(w / 2f, streamEndY),
                            strokeWidth = streamWidth,
                            cap = StrokeCap.Butt
                        )

                        val elapsed = (totalMillis - rem).coerceAtLeast(0L)
                        val grainPeriodMs = 950L

                        val loopProgress1 = (elapsed % grainPeriodMs) / grainPeriodMs.toFloat()
                        val loopProgress2 = ((elapsed + grainPeriodMs / 2) % grainPeriodMs) / grainPeriodMs.toFloat()

                        val grain1Y = neckTopY + (streamEndY - neckTopY) * loopProgress1
                        val grain2Y = neckTopY + (streamEndY - neckTopY) * loopProgress2

                        // Grains scale with the stream so they stay visible
                        val grainR = streamWidth * 0.6f
                        drawCircle(PureWhite, radius = grainR, center = Offset(w / 2f - grainR, grain1Y))
                        drawCircle(PureWhite, radius = grainR, center = Offset(w / 2f + grainR, grain2Y))
                    }
                }

                // 4. Glass outline
                drawPath(
                    path = glassPath,
                    color = BorderSubtle,
                    style = Stroke(width = strokeThickness)
                )
            }
        }

//        val hourglassCanvas = @Composable {
//            // Preallocated tables: no allocations while drawing
//            val topYs = remember { FloatArray(SAND_SAMPLES + 1) }
//            val topCum = remember { FloatArray(SAND_SAMPLES + 1) }
//            val botYs = remember { FloatArray(SAND_SAMPLES + 1) }
//            val botCum = remember { FloatArray(SAND_SAMPLES + 1) }
//            val topPx = remember { FloatArray(4) }
//            val topPy = remember { FloatArray(4) }
//            val botPx = remember { FloatArray(4) }
//            val botPy = remember { FloatArray(4) }
//
//            Canvas(
//                modifier = Modifier.size(width = dims.width.dp, height = dims.height.dp)
//            ) {
//                val rem = remainingMillisProvider()
//                val progress = ThemeFractionCalculator
//                    .calculateOverallProgress(rem, totalMillis)
//                    .coerceIn(0f, 1f)
//                val isRunning = status == TimerStatus.Running && rem > 0L
//
//                val w = size.width
//                val h = size.height
//
//                // Hourglass geometric proportions
//                val neckWidth = (w * 0.09f).coerceIn(6f, 16f)
//                val neckHeight = (h * 0.05f).coerceIn(6f, 18f)
//                val neckTopY = (h - neckHeight) / 2f
//                val neckBottomY = neckTopY + neckHeight
//
//                val plateExtra = w * 0.08f
//                val strokeThickness = 1.5.dp.toPx()
//                val rimInset = w * 0.06f
//
//                // 1. Glass path
//                glassPath.reset()
//                glassPath.moveTo(rimInset, 0f)
//                glassPath.lineTo(w - rimInset, 0f)
//                glassPath.cubicTo(
//                    w - rimInset, neckTopY * 0.35f,
//                    (w + neckWidth) / 2f + 4f, neckTopY * 0.85f,
//                    (w + neckWidth) / 2f, neckTopY
//                )
//                glassPath.lineTo((w + neckWidth) / 2f, neckBottomY)
//                glassPath.cubicTo(
//                    (w + neckWidth) / 2f + 4f, neckBottomY + (h - neckBottomY) * 0.15f,
//                    w - rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
//                    w - rimInset, h
//                )
//                glassPath.lineTo(rimInset, h)
//                glassPath.cubicTo(
//                    rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
//                    (w - neckWidth) / 2f - 4f, neckBottomY + (h - neckBottomY) * 0.15f,
//                    (w - neckWidth) / 2f, neckBottomY
//                )
//                glassPath.lineTo((w - neckWidth) / 2f, neckTopY)
//                glassPath.cubicTo(
//                    (w - neckWidth) / 2f - 4f, neckTopY * 0.85f,
//                    rimInset, neckTopY * 0.35f,
//                    rimInset, 0f
//                )
//                glassPath.close()
//
//                // 2. End plates
//                val plateThickness = 2.5.dp.toPx()
//                drawLine(
//                    color = PureWhite,
//                    start = Offset(-plateExtra, 0f),
//                    end = Offset(w + plateExtra, 0f),
//                    strokeWidth = plateThickness,
//                    cap = StrokeCap.Round
//                )
//                drawLine(
//                    color = PureWhite,
//                    start = Offset(-plateExtra, h),
//                    end = Offset(w + plateExtra, h),
//                    strokeWidth = plateThickness,
//                    cap = StrokeCap.Round
//                )
//
//                // ---- Volume-based sand levels (top and bottom always match) ----
//                val elapsedFraction = 1f - progress
//                val botH = h - neckBottomY
//
//                // Top bulb edge, starting at the neck going up to the rim
//                topPx[0] = (w + neckWidth) / 2f;       topPy[0] = neckTopY
//                topPx[1] = (w + neckWidth) / 2f + 4f;  topPy[1] = neckTopY * 0.85f
//                topPx[2] = w - rimInset;               topPy[2] = neckTopY * 0.35f
//                topPx[3] = w - rimInset;               topPy[3] = 0f
//                fillSandTable(w, topPx, topPy, topYs, topCum)
//
//                // Bottom bulb edge, starting at the bottom rim going up to the neck
//                botPx[0] = w - rimInset;               botPy[0] = h
//                botPx[1] = w - rimInset;               botPy[1] = neckBottomY + botH * 0.65f
//                botPx[2] = (w + neckWidth) / 2f + 4f;  botPy[2] = neckBottomY + botH * 0.15f
//                botPx[3] = (w + neckWidth) / 2f;       botPy[3] = neckBottomY
//                fillSandTable(w, botPx, botPy, botYs, botCum)
//
//                val topSandLevelY = levelFromTable(topYs, topCum, progress)
//                val bottomLevelY = levelFromTable(botYs, botCum, elapsedFraction)
//
//                // Bottom pile: peaked early, flat when full
//                val depth = h - bottomLevelY
//                val moundAmp = depth * 0.35f * (1f - elapsedFraction * elapsedFraction * elapsedFraction)
//                val moundPeakY = bottomLevelY - moundAmp * 0.3f
//                val moundEdgeY = bottomLevelY + moundAmp * 0.7f
//
//                // 3. Sand, clipped to the glass
//                clipPath(glassPath) {
//                    // --- TOP SAND ---
//                    if (progress > 0f) {
//                        val dip = if (isRunning && progress in 0.05f..0.95f) 3.dp.toPx() else 0f
//
//                        topSandPath.reset()
//                        topSandPath.moveTo(0f, topSandLevelY)
//                        topSandPath.quadraticTo(w / 2f, topSandLevelY + dip * 2f, w, topSandLevelY)
//                        topSandPath.lineTo(w, neckBottomY)
//                        topSandPath.lineTo(0f, neckBottomY)
//                        topSandPath.close()
//
//                        drawPath(topSandPath, color = PureWhite)
//                    }
//
//                    // --- BOTTOM SAND ---
//                    if (elapsedFraction > 0f) {
//                        // quadratic midpoint = (edge + control) / 2, so compensate to hit the peak
//                        val controlY = 2f * moundPeakY - moundEdgeY
//
//                        bottomSandPath.reset()
//                        bottomSandPath.moveTo(0f, h)
//                        bottomSandPath.lineTo(w, h)
//                        bottomSandPath.lineTo(w, moundEdgeY)
//                        bottomSandPath.quadraticTo(w / 2f, controlY, 0f, moundEdgeY)
//                        bottomSandPath.close()
//
//                        drawPath(bottomSandPath, color = PureWhite)
//                    }
//
//                    // --- GRAIN TEXTURE (only where sand exists) ---
//                    val grainColor = Color(0x28000000)
//                    val grainRadius = 0.9.dp.toPx()
//                    for (i in PRECOMPUTED_GRAIN_X.indices) {
//                        val gx = PRECOMPUTED_GRAIN_X[i] * w
//                        val gyTop = neckTopY * PRECOMPUTED_GRAIN_Y[i]
//                        val gyBottom = neckBottomY + botH * PRECOMPUTED_GRAIN_Y[i]
//
//                        if (progress > 0f && gyTop >= topSandLevelY) {
//                            drawCircle(grainColor, radius = grainRadius, center = Offset(gx, gyTop))
//                        }
//                        if (elapsedFraction > 0f && gyBottom >= bottomLevelY) {
//                            drawCircle(grainColor, radius = grainRadius, center = Offset(gx, gyBottom))
//                        }
//                    }
//
//                    // --- FALLING STREAM ---
//                    if (isRunning) {
//                        val streamEndY = moundPeakY.coerceIn(neckBottomY, h)
//
//                        drawLine(
//                            color = PureWhite,
//                            start = Offset(w / 2f, neckTopY - 2f),
//                            end = Offset(w / 2f, streamEndY),
//                            strokeWidth = 1.8.dp.toPx(),
//                            cap = StrokeCap.Round
//                        )
//
//                        val elapsed = (totalMillis - rem).coerceAtLeast(0L)
//                        val loopProgress1 = (elapsed % 300L) / 300f
//                        val loopProgress2 = ((elapsed + 150L) % 300L) / 300f
//
//                        val grain1Y = neckTopY + (streamEndY - neckTopY) * loopProgress1
//                        val grain2Y = neckTopY + (streamEndY - neckTopY) * loopProgress2
//
//                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f - 1.2f, grain1Y))
//                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f + 1.2f, grain2Y))
//                    }
//                }
//
//                // 4. Glass outline
//                drawPath(
//                    path = glassPath,
//                    color = BorderSubtle,
//                    style = Stroke(width = strokeThickness)
//                )
//            }
//        }
//        val hourglassCanvas = @Composable {
//            Canvas(
//                modifier = Modifier.size(width = dims.width.dp, height = dims.height.dp)
//            ) {
//                // Optimization: Read remainingMillisProvider in draw phase so the parent composable does not recompose
//                val rem = remainingMillisProvider()
//                val progress = ThemeFractionCalculator.calculateOverallProgress(rem, totalMillis)
//                val isRunning = status == TimerStatus.Running && rem > 0L
//
//                val w = size.width
//                val h = size.height
//
//                // Hourglass geometric proportions
//                val neckWidth = (w * 0.09f).coerceIn(6f, 16f)
//                val neckHeight = (h * 0.05f).coerceIn(6f, 18f)
//                val neckTopY = (h - neckHeight) / 2f
//                val neckBottomY = neckTopY + neckHeight
//
//                val plateExtra = w * 0.08f
//                val strokeThickness = 1.5.dp.toPx()
//                val rimInset = w * 0.06f
//
//                // 1. Build the complete closed Glass Path (two rounded funnels meeting at the neck)
//                glassPath.reset()
//                // Top rim
//                glassPath.moveTo(rimInset, 0f)
//                glassPath.lineTo(w - rimInset, 0f)
//                // Right bulb (top chamber) curving down to neck
//                glassPath.cubicTo(
//                    w - rimInset, neckTopY * 0.35f,
//                    (w + neckWidth) / 2f + 4f, neckTopY * 0.85f,
//                    (w + neckWidth) / 2f, neckTopY
//                )
//                // Neck right edge
//                glassPath.lineTo((w + neckWidth) / 2f, neckBottomY)
//                // Right bulb (bottom chamber) curving down to bottom rim
//                glassPath.cubicTo(
//                    (w + neckWidth) / 2f + 4f, neckBottomY + (h - neckBottomY) * 0.15f,
//                    w - rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
//                    w - rimInset, h
//                )
//                // Bottom rim
//                glassPath.lineTo(rimInset, h)
//                // Left bulb (bottom chamber) curving up to neck
//                glassPath.cubicTo(
//                    rimInset, neckBottomY + (h - neckBottomY) * 0.65f,
//                    (w - neckWidth) / 2f - 4f, neckBottomY + (h - neckBottomY) * 0.15f,
//                    (w - neckWidth) / 2f, neckBottomY
//                )
//                // Neck left edge
//                glassPath.lineTo((w - neckWidth) / 2f, neckTopY)
//                // Left bulb (top chamber) curving up to top rim
//                glassPath.cubicTo(
//                    (w - neckWidth) / 2f - 4f, neckTopY * 0.85f,
//                    rimInset, neckTopY * 0.35f,
//                    rimInset, 0f
//                )
//                glassPath.close()
//
//                // 2. Draw Top and Bottom End Plates (flat horizontal bars extending slightly past bulbs)
//                val plateThickness = 2.5.dp.toPx()
//                drawLine(
//                    color = PureWhite,
//                    start = Offset(-plateExtra, 0f),
//                    end = Offset(w + plateExtra, 0f),
//                    strokeWidth = plateThickness,
//                    cap = StrokeCap.Round
//                )
//                drawLine(
//                    color = PureWhite,
//                    start = Offset(-plateExtra, h),
//                    end = Offset(w + plateExtra, h),
//                    strokeWidth = plateThickness,
//                    cap = StrokeCap.Round
//                )
//
//                // 3. Draw sand strictly clipped to the interior glass path
//                clipPath(glassPath) {
//                    // --- TOP CHAMBER SAND ---
//                    if (progress > 0.005f) {
//                        val availableTopHeight = neckTopY - 2f
//                        val topSandLevelY = (1f - progress) * availableTopHeight
//
//                        val t = (topSandLevelY / neckTopY).coerceIn(0f, 1f)
//                        val bulbHalfWidthAtLevel = ((w / 2f - rimInset) * (1f - t) + (neckWidth / 2f) * t)
//
//                        topSandPath.reset()
//                        val dip = if (isRunning && progress in 0.05f..0.95f) 3.dp.toPx() else 0f
//                        topSandPath.moveTo(w / 2f - bulbHalfWidthAtLevel, topSandLevelY)
//                        topSandPath.quadraticTo(
//                            w / 2f, topSandLevelY + dip,
//                            w / 2f + bulbHalfWidthAtLevel, topSandLevelY
//                        )
//                        topSandPath.lineTo((w + neckWidth) / 2f, neckTopY)
//                        topSandPath.lineTo((w - neckWidth) / 2f, neckTopY)
//                        topSandPath.close()
//
//                        drawPath(topSandPath, color = PureWhite)
//                    }
//
//                    // --- BOTTOM CHAMBER SAND (Volume-correct conical mound) ---
//                    val elapsedFraction = (1f - progress).coerceIn(0f, 1f)
//                    if (elapsedFraction > 0.005f) {
//                        val availableBottomHeight = h - neckBottomY
//                        val moundHeight = (availableBottomHeight * sqrt(elapsedFraction)).coerceAtMost(availableBottomHeight - 4f)
//                        val moundPeakY = h - moundHeight
//
//                        bottomSandPath.reset()
//                        bottomSandPath.moveTo(0f, h)
//                        bottomSandPath.lineTo(w, h)
//                        bottomSandPath.lineTo(w, h - (moundHeight * 0.35f))
//                        bottomSandPath.quadraticTo(w / 2f, moundPeakY, 0f, h - (moundHeight * 0.35f))
//                        bottomSandPath.close()
//
//                        drawPath(bottomSandPath, color = PureWhite)
//                    }
//
//                    // --- FINE SAND GRAIN TEXTURE (Precomputed static noise) ---
//                    val grainColor = Color(0x28000000)
//                    for (i in PRECOMPUTED_GRAIN_X.indices) {
//                        val gx = PRECOMPUTED_GRAIN_X[i] * w
//                        val gyTop = neckTopY * PRECOMPUTED_GRAIN_Y[i]
//                        val gyBottom = neckBottomY + (h - neckBottomY) * PRECOMPUTED_GRAIN_Y[i]
//
//                        if (progress > 0.05f) {
//                            drawCircle(grainColor, radius = 0.9.dp.toPx(), center = Offset(gx, gyTop))
//                        }
//                        if (elapsedFraction > 0.05f) {
//                            drawCircle(grainColor, radius = 0.9.dp.toPx(), center = Offset(gx, gyBottom))
//                        }
//                    }
//
//                    // --- FALLING SAND STREAM & GRAINS IN NECK ---
//                    if (isRunning && progress > 0.01f) {
//                        val elapsedBottomHeight = (h - neckBottomY) * sqrt(elapsedFraction)
//                        val streamEndY = (h - elapsedBottomHeight).coerceIn(neckBottomY, h)
//
//                        // Central falling stream
//                        drawLine(
//                            color = PureWhite,
//                            start = Offset(w / 2f, neckTopY - 2f),
//                            end = Offset(w / 2f, streamEndY),
//                            strokeWidth = 1.8.dp.toPx(),
//                            cap = StrokeCap.Round
//                        )
//
//                        // Looping falling grains derived from elapsed time: zero allocations, zero SystemClock dependency
//                        val elapsed = (totalMillis - rem).coerceAtLeast(0L)
//                        val loopProgress1 = (elapsed % 300L) / 300f
//                        val loopProgress2 = ((elapsed + 150L) % 300L) / 300f
//
//                        val grain1Y = neckTopY + (streamEndY - neckTopY) * loopProgress1
//                        val grain2Y = neckTopY + (streamEndY - neckTopY) * loopProgress2
//
//                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f - 1.2f, grain1Y))
//                        drawCircle(PureWhite, radius = 1.2.dp.toPx(), center = Offset(w / 2f + 1.2f, grain2Y))
//                    }
//                }
//
//                // 4. Draw outer glass contour outline in subtle gray
//                drawPath(
//                    path = glassPath,
//                    color = BorderSubtle,
//                    style = Stroke(width = strokeThickness)
//                )
//            }
//        }

        val textMeasurer = rememberTextMeasurer()
        val fontScale = LocalDensity.current.fontScale

        val digitsContent = @Composable {
            val baseFontSize = if (fontScale > 1.0f) {
                (dims.fontSize / fontScale).sp
            } else {
                dims.fontSize.sp
            }

            // Defensively ensure digits never exceed available width
            val style = remember(baseFontSize) {
                TextStyle(
                    fontSize = baseFontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    fontFeatureSettings = "tnum"
                )
            }
            val measuredWidthDp = with(LocalDensity.current) {
                textMeasurer.measure("00:00:00", style).size.width.toDp()
            }
            val maxAvailableDigitsWidthDp = if (dims.isRowLayout) {
                val available = maxWidth - dims.width.dp - 36.dp
                if (available < 60.dp) 60.dp else available
            } else {
                val available = maxWidth - 24.dp
                if (available < 60.dp) 60.dp else available
            }
            val fittedFontSize = if (measuredWidthDp > maxAvailableDigitsWidthDp) {
                val scale = (maxAvailableDigitsWidthDp.value / measuredWidthDp.value) * 0.95f
                (baseFontSize.value * scale).sp
            } else {
                baseFontSize
            }

            val subFontSize = if (fontScale > 1.0f) {
                ((dims.fontSize * 0.20f).coerceIn(11f, 16f) / fontScale).sp
            } else {
                (dims.fontSize * 0.20f).coerceIn(11f, 16f).sp
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = fittedFontSize,
                        letterSpacing = 2.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 6.dp))

                Text(
                    text = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted",
                    fontSize = subFontSize,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (dims.isRowLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                hourglassCanvas()
                digitsContent()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = if (isCompact) 4.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                hourglassCanvas()
                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 24.dp))
                digitsContent()
            }
        }
    }
}

/**
 * Backward-compatible overload accepting a direct Long value.
 */
@Composable
fun HourglassTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = HourglassTimerTheme(
    remainingMillisProvider = { remainingMillis },
    totalMillis = totalMillis,
    status = status,
    modifier = modifier,
    layoutMode = layoutMode
)

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Hourglass Portrait 50%", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun HourglassTimerThemePortraitPreview() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 60000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Hourglass Paused", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun HourglassTimerThemePausedPreview() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 45000L,
            totalMillis = 120000L,
            status = TimerStatus.Paused
        )
    }
}

@Preview(name = "Hourglass Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun HourglassTimerThemeLandscapePreview() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 80000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Hourglass Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun HourglassTimerThemeTabletPreview() {
    ValueTimerTheme {
        HourglassTimerTheme(
            remainingMillis = 90000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}
