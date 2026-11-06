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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Dedicated, jitter-free Centiseconds Slot.
 * Measures reference string ".00" ONCE to guarantee strictly immutable layout bounds.
 * Renders in the draw phase via Canvas.drawText() with tabular figures (tnum),
 * ensuring 0ms layout invalidations and zero vibration of adjacent UI components.
 */
@Composable
fun CentisecondsSlot(
    elapsedMillis: Long,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp
) {
    val textMeasurer = rememberTextMeasurer()
    val centis = TimeUtils.formatStopwatchCentiseconds(elapsedMillis)

    val textStyle = TextStyle(
        fontSize = fontSize,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        color = SubtleGray,
        fontFeatureSettings = "tnum",
        textAlign = TextAlign.Center
    )

    // Measure reference ".00" once so width and height are strictly static
    val referenceLayout = remember(textMeasurer, fontSize) {
        textMeasurer.measure(
            text = ".00",
            style = textStyle
        )
    }

    val slotWidthDp = with(LocalDensity.current) { referenceLayout.size.width.toDp() + 6.dp }
    val slotHeightDp = with(LocalDensity.current) { referenceLayout.size.height.toDp() }

    Canvas(
        modifier = modifier.size(width = slotWidthDp, height = slotHeightDp)
    ) {
        val currentLayout = textMeasurer.measure(
            text = ".$centis",
            style = textStyle
        )

        // Center within strictly reserved slot bounds
        val x = (size.width - currentLayout.size.width) / 2f
        val y = (size.height - currentLayout.size.height) / 2f

        drawText(
            textLayoutResult = currentLayout,
            topLeft = Offset(x, y)
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
