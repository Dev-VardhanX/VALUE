package com.devvardhan.value.model

import androidx.compose.runtime.Immutable

/**
 * Immutable representation of a recorded stopwatch lap.
 * @param lapNumber 1-based sequential lap index (up to 99).
 * @param lapTimeMillis Duration of this individual lap (split).
 * @param totalTimeMillis Total accumulated elapsed time when lap was recorded.
 */
@Immutable
data class LapItem(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long
)

