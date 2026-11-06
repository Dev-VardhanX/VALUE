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
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Dedicated, jitter-free Centiseconds Slot.
 * - Sits directly to the right of seconds on the same baseline at ~40% main font size.
 * - Measures reference string ".00" ONCE to guarantee strictly immutable layout bounds.
 * - Renders in the draw phase via Canvas.drawText() with tabular figures (tnum),
 *   ensuring 0ms layout invalidations and zero vibration of adjacent UI components.
 */
@Composable
fun CentisecondsSlot(
    elapsedMillis: Long,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = SubtleGray,
    fixedWidthDp: Dp? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val centis = TimeUtils.formatStopwatchCentiseconds(elapsedMillis)

    val textStyle = TextStyle(
        fontSize = fontSize,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        color = color,
        fontFeatureSettings = "tnum",
        textAlign = TextAlign.Start
    )

    // Measure reference ".00" once so width and height are strictly static
    val referenceLayout = remember(textMeasurer, fontSize) {
        textMeasurer.measure(
            text = ".00",
            style = textStyle
        )
    }

    val measuredWidthDp = with(LocalDensity.current) { referenceLayout.size.width.toDp() }
    val slotWidth = fixedWidthDp ?: measuredWidthDp
    val slotHeight = with(LocalDensity.current) { referenceLayout.size.height.toDp() }

    Canvas(
        modifier = modifier.size(width = slotWidth, height = slotHeight)
    ) {
        val currentLayout = textMeasurer.measure(
            text = ".$centis",
            style = textStyle
        )

        // Align baseline to bottom of slot
        val y = size.height - currentLayout.size.height

        drawText(
            textLayoutResult = currentLayout,
            topLeft = Offset(0f, y)
        )
    }
}

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
