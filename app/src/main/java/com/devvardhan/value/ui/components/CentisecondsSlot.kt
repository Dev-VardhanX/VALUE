package com.devvardhan.value.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Dedicated, jitter-free Centiseconds Slot.
 * - Sits directly to the right of seconds on the same baseline at ~40% main font size.
 * - Pre-measures all 100 hundredth layouts (.00 .. .99) ONCE to eliminate all allocations and
 *   text measurement passes inside Canvas.drawText() during 60/120fps animations.
 * - Reads elapsedMillisProvider strictly in the Canvas draw phase, resulting in 0ms layout
 *   invalidations and zero parent recompositions.
 */
@Composable
fun CentisecondsSlot(
    elapsedMillisProvider: () -> Long,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = SubtleGray,
    fixedWidthDp: Dp? = null
) {
    val textMeasurer = rememberTextMeasurer()

    // Cache text style based on size and color
    val textStyle = remember(fontSize, color) {
        TextStyle(
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = color,
            fontFeatureSettings = "tnum",
            textAlign = TextAlign.Start
        )
    }

    // Measure reference ".00" once so width and height are strictly static
    val referenceLayout = remember(textMeasurer, textStyle) {
        textMeasurer.measure(
            text = ".00",
            style = textStyle
        )
    }

    val measuredWidthDp = with(LocalDensity.current) { referenceLayout.size.width.toDp() }
    val slotWidth = fixedWidthDp ?: measuredWidthDp
    val slotHeight = with(LocalDensity.current) { referenceLayout.size.height.toDp() }

    // Optimization: Pre-cache all 100 possible centisecond layouts (.00 to .99) to completely
    // avoid calling textMeasurer.measure() inside the Canvas draw scope every frame.
    val cachedLayouts = remember(textMeasurer, textStyle) {
        Array(100) { i ->
            val centisStr = if (i < 10) ".0$i" else ".$i"
            textMeasurer.measure(
                text = centisStr,
                style = textStyle
            )
        }
    }

    Canvas(
        modifier = modifier.size(width = slotWidth, height = slotHeight)
    ) {
        // Read provider in the draw phase: does not cause composable recomposition
        val ms = elapsedMillisProvider()
        val hundredths = ((ms % 1000L) / 10L).toInt().coerceIn(0, 99)
        val currentLayout = cachedLayouts[hundredths]

        // Align baseline to bottom of slot
        val y = size.height - currentLayout.size.height

        drawText(
            textLayoutResult = currentLayout,
            topLeft = Offset(0f, y)
        )
    }
}

/**
 * Backward-compatible overload accepting a direct Long value.
 */
@Composable
fun CentisecondsSlot(
    elapsedMillis: Long,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = SubtleGray,
    fixedWidthDp: Dp? = null
) = CentisecondsSlot(
    elapsedMillisProvider = { elapsedMillis },
    fontSize = fontSize,
    modifier = modifier,
    color = color,
    fixedWidthDp = fixedWidthDp
)

@Preview(name = "Tabular Digits 0-9 Alignment Preview", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TabularDigitsUniformityPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(0, 110, 220, 330, 440, 550, 660, 770, 880, 990).forEach { ms ->
            CentisecondsSlot(elapsedMillis = ms.toLong(), fontSize = 24.sp)
        }
    }
}
