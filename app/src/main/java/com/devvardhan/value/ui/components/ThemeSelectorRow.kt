package com.devvardhan.value.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Horizontally scrollable row of theme preview tiles with miniaturized icons.
 */
@Composable
fun ThemeSelectorRow(
    selectedTheme: TimerTheme,
    onThemeSelected: (TimerTheme) -> Unit,
    availableThemes: List<TimerTheme> = TimerTheme.entries,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(availableThemes) { theme ->
            ThemeTile(
                theme = theme,
                isSelected = theme == selectedTheme,
                onClick = { onThemeSelected(theme) }
            )
        }
    }
}

@Composable
fun ThemeTile(
    theme: TimerTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (isSelected) PureWhite else BorderSubtle
    val bgColor = if (isSelected) Color(0xFF161616) else Color(0xFF0A0A0A)
    val textColor = if (isSelected) PureWhite else SubtleGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(76.dp)
            .clip(shape)
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeMiniIcon(theme = theme, isSelected = isSelected)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = theme.displayName,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Clean vector miniature of each theme.
 */
@Composable
fun ThemeMiniIcon(theme: TimerTheme, isSelected: Boolean) {
    val primaryColor = if (isSelected) PureWhite else SubtleGray

    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        when (theme) {
            TimerTheme.CIRCLE -> {
                drawCircle(
                    color = BorderSubtle,
                    radius = w * 0.42f,
                    style = Stroke(width = 1.8f)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }

            TimerTheme.HOURGLASS -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.1f)
                    lineTo(w * 0.85f, h * 0.1f)
                    lineTo(w * 0.5f, h * 0.5f)
                    lineTo(w * 0.85f, h * 0.9f)
                    lineTo(w * 0.15f, h * 0.9f)
                    lineTo(w * 0.5f, h * 0.5f)
                    close()
                }
                drawPath(path, color = primaryColor, style = Stroke(width = 1.5f))
            }

            TimerTheme.THREE_BOX -> {
                val panelW = w * 0.28f
                val gap = w * 0.05f
                for (i in 0..2) {
                    val x = i * (panelW + gap) + w * 0.02f
                    drawRect(
                        color = BorderSubtle,
                        topLeft = Offset(x, h * 0.25f),
                        size = Size(panelW, h * 0.5f),
                        style = Stroke(width = 1.2f)
                    )
                    drawLine(
                        color = primaryColor,
                        start = Offset(x + 2f, h * 0.5f),
                        end = Offset(x + panelW - 2f, h * 0.5f),
                        strokeWidth = 1f
                    )
                }
            }

            TimerTheme.CONCENTRIC_RINGS -> {
                for (r in listOf(0.42f, 0.30f, 0.18f)) {
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 220f + (r * 100f),
                        useCenter = false,
                        topLeft = Offset(w * (0.5f - r), h * (0.5f - r)),
                        size = Size(w * r * 2, h * r * 2),
                        style = Stroke(width = 1.4f)
                    )
                }
            }

            TimerTheme.FLIP_CLOCK -> {
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(w * 0.12f, h * 0.2f),
                    size = Size(w * 0.76f, h * 0.6f),
                    style = Stroke(width = 1.5f)
                )
                drawLine(
                    color = BorderSubtle,
                    start = Offset(w * 0.12f, h * 0.5f),
                    end = Offset(w * 0.88f, h * 0.5f),
                    strokeWidth = 1.5f
                )
            }

            TimerTheme.LIQUID_FILL -> {
                drawRect(
                    color = BorderSubtle,
                    topLeft = Offset(w * 0.18f, h * 0.18f),
                    size = Size(w * 0.64f, h * 0.64f),
                    style = Stroke(width = 1.4f)
                )
                val liquidPath = Path().apply {
                    moveTo(w * 0.18f, h * 0.82f)
                    lineTo(w * 0.18f, h * 0.52f)
                    quadraticTo(w * 0.5f, h * 0.42f, w * 0.82f, h * 0.52f)
                    lineTo(w * 0.82f, h * 0.82f)
                    close()
                }
                drawPath(liquidPath, color = primaryColor)
            }

            TimerTheme.DOT_GRID -> {
                val dots = 3
                val step = w * 0.24f
                val start = w * 0.26f
                for (r in 0 until dots) {
                    for (c in 0 until dots) {
                        drawCircle(
                            color = primaryColor,
                            radius = 1.8f,
                            center = Offset(start + c * step, start + r * step)
                        )
                    }
                }
            }
        }
    }
}

