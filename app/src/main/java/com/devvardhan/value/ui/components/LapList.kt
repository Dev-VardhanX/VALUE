package com.devvardhan.value.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Scrollable list of recorded laps, ordered newest-on-top.
 * When 3 or more laps exist, highlights fastest with white "BEST" and slowest with gray "WORST".
 * When empty, displays a subtle "No laps yet" placeholder with headers preserved.
 */
@Composable
fun LapList(
    laps: List<LapItem>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Determine best and worst lap numbers once there are 3 or more laps
    val bestLapNumber = remember(laps) {
        if (laps.size >= 3) {
            val minSplit = laps.minOfOrNull { it.lapTimeMillis }
            laps.firstOrNull { it.lapTimeMillis == minSplit }?.lapNumber
        } else null
    }

    val worstLapNumber = remember(laps) {
        if (laps.size >= 3) {
            val maxSplit = laps.maxOfOrNull { it.lapTimeMillis }
            val minSplit = laps.minOfOrNull { it.lapTimeMillis }
            if (maxSplit != minSplit) {
                laps.firstOrNull { it.lapTimeMillis == maxSplit }?.lapNumber
            } else null
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFF070707))
            .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.lap_col_lap),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 1.5.sp,
                modifier = Modifier.width(90.dp)
            )

            Text(
                text = stringResource(R.string.lap_col_split),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 1.5.sp
            )

            Text(
                text = stringResource(R.string.lap_col_total),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 1.5.sp
            )
        }

        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

        if (laps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No laps yet",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    color = SubtleGray,
                    letterSpacing = 1.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = laps,
                    key = { it.lapNumber }
                ) { lap ->
                    val isBest = lap.lapNumber == bestLapNumber
                    val isWorst = lap.lapNumber == worstLapNumber

                    LapRow(
                        lap = lap,
                        isBest = isBest,
                        isWorst = isWorst
                    )

                    HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun LapRow(
    lap: LapItem,
    isBest: Boolean,
    isWorst: Boolean
) {
    val lapSplitText = TimeUtils.formatLapTime(lap.lapTimeMillis)
    val lapTotalText = TimeUtils.formatLapTime(lap.totalTimeMillis)
    val lapNumStr = String.format("%02d", lap.lapNumber)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics {
                contentDescription = "Lap $lapNumStr, split $lapSplitText, total $lapTotalText"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(95.dp)
        ) {
            Text(
                text = "Lap $lapNumStr",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    fontFeatureSettings = "tnum"
                ),
                color = PureWhite
            )

            if (isBest) {
                Spacer(modifier = Modifier.width(6.dp))
                BestBadge()
            } else if (isWorst) {
                Spacer(modifier = Modifier.width(6.dp))
                WorstBadge()
            }
        }

        Text(
            text = lapSplitText,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontFeatureSettings = "tnum"
            ),
            color = if (isBest) PureWhite else SubtleGray
        )

        Text(
            text = lapTotalText,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontFeatureSettings = "tnum"
            ),
            color = PureWhite
        )
    }
}

@Composable
private fun BestBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(PureWhite)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "BEST",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = PureBlack,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun WorstBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF242424))
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "WORST",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SubtleGray,
            letterSpacing = 0.5.sp
        )
    }
}
