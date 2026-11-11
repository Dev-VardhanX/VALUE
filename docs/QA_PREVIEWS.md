# Style Gallery Previews QA Report

## 1. Root Cause Summary
In `StyleGalleryScreen.kt`, the `LivePreview` composable instantiated the demo time provider as:
```kotlin
val demoMillisProvider: () -> Long = remember(isStopwatchMode) {
    if (!isAnimated) {
        if (isStopwatchMode) { 0L } else { totalDemoDuration }
    } else {
        if (isStopwatchMode) {
            { (phaseNanosState.longValue / 1_000_000L).coerceIn(0L, totalDemoDuration) }
        } else {
            { (totalDemoDuration - (phaseNanosState.longValue / 1_000_000L)).coerceIn(0L, totalDemoDuration) }
        }
    }
}
```
### Why Only Circle Animated
1. `isAnimated` is a plain `Boolean` parameter (`settledPage == page && !pagerState.isScrollInProgress`).
2. When the `HorizontalPager` initialized at page 0 (`Circle`), page 0 was the settled page. Thus, page 0 was composed with `isAnimated = true`. Its closure captured `isAnimated = true` and properly returned the dynamic lambda reading `phaseNanosState`.
3. Due to `beyondViewportPageCount = 1`, off-screen pages (pages 1 through 6) were pre-composed while they were NOT the settled page (`isAnimated = false`).
4. When the user swiped to page 1 (`Hourglass`) or beyond, `isAnimated` became `true`. However, `demoMillisProvider` was remembered only with key `isStopwatchMode`, **not** `isAnimated`.
5. Because the key `isStopwatchMode` did not change, `demoMillisProvider` never recomputed. It held on to the closure captured during pre-composition with `isAnimated = false`.
6. As a result, for all themes other than page 0, `demoMillisProvider()` unconditionally returned static initial values (`totalDemoDuration` or `0L`), freezing sand, rings, odometer digits, wave heights, and dot matrices.

### Additional Improvements Made
- Updated `LivePreview` to use `rememberUpdatedState(isAnimated)` and `rememberUpdatedState(isStopwatchMode)`, preventing any closure capture staleness.
- In `LiquidFillTimerTheme.kt`, removed redundant `LaunchedEffect(isRunning)` with self-driven `withFrameNanos` and derived wave phase deterministically from the remaining time provider in `drawWithContent`.
- In `ThreeBoxTimerTheme.kt` and `FlipClockTimerTheme.kt`, unified digit parsing into single `derivedStateOf` blocks keyed to time providers.

---

## 2. Automated Unit Tests (`ThemePreviewLogicTest.kt`)
All 12 unit tests verifying time-difference variance across $t = 0\text{ s}, 5\text{ s}, 10\text{ s}$ and static initialization when `isAnimated = false` passed.

| Test Case | Description | Result |
| :--- | :--- | :---: |
| `testCircleTimerPreview_StateChangesAcrossDemoTimes` | Progress & remaining text change | **PASS** |
| `testHourglassTimerPreview_SandLevelsChangeAcrossDemoTimes` | Top/bottom sand heights & flow state change | **PASS** |
| `testThreeBoxTimerPreview_ScoreboardDigitsChangeAcrossDemoTimes` | Digits change across demo duration | **PASS** |
| `testConcentricRingsTimerPreview_SecondsRingChangesAcrossDemoTimes` | Inner ring sweep changes across demo times | **PASS** |
| `testFlipClockTimerPreview_TimeStringsChangeAcrossDemoTimes` | HH:MM:SS text changes across demo times | **PASS** |
| `testLiquidFillTimerPreview_LevelAndWavePhaseChangeAcrossDemoTimes` | Liquid fill fraction and wave phase change | **PASS** |
| `testDotGridTimerPreview_DotsCountDecrementsAcrossDemoTimes` | Dot grid matrix active dots change | **PASS** |
| `testCircleStopwatchPreview_ProgressChangesAcrossDemoTimes` | Progress sweep changes across demo times | **PASS** |
| `testConcentricRingsStopwatchPreview_SecondsRingChangesAcrossDemoTimes` | Stopwatch rings advance across demo times | **PASS** |
| `testThreeBoxStopwatchPreview_ScoreboardDigitsChangeAcrossDemoTimes` | Stopwatch scoreboard digits advance | **PASS** |
| `testFlipClockStopwatchPreview_TimeStringsChangeAcrossDemoTimes` | Stopwatch flip digits advance | **PASS** |
| `testPreviewStatusContract_SettledIsRunning_UnsettledIsPaused` | Gating contract verified | **PASS** |

---

## 3. Real Device Verification (`8e9b7868`)
- **Device Model**: Realme RMX5116 (Android 14)
- **Serial**: `8e9b7868`
- **Method**: Automated capture of 4 frames at $t \approx 0.5\text{ s}, 3.0\text{ s}, 6.0\text{ s}, 9.0\text{ s}$ across the 10-second demo ramp. Bounding box pixel diff computed via Pillow (`ImageChops.difference`).
- **Pass Criterion**: Non-zero pixel differences across all consecutive time slices for all themes.

### Results Table

| Gallery Mode | Style Name | Frame 1 $\to$ 2 ($0.5\text{s} \to 3\text{s}$) | Frame 2 $\to$ 3 ($3\text{s} \to 6\text{s}$) | Frame 3 $\to$ 4 ($6\text{s} \to 9\text{s}$) | Loop Wrap ($9\text{s} \to 0.5\text{s}$) | Status |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **Timer** | Circle | 4.53% | 4.60% | 1.32% | 0.15% | **PASS** |
| **Timer** | Hourglass | 2.58% | 1.61% | 3.79% | 0.07% | **PASS** |
| **Timer** | Three Box | 0.74% | 0.56% | 0.46% | 0.68% | **PASS** |
| **Timer** | Concentric Rings | 0.28% | 0.27% | 0.40% | 0.02% | **PASS** |
| **Timer** | Flip Clock | 0.54% | 0.72% | 0.48% | 0.81% | **PASS** |
| **Timer** | Liquid Fill | 18.06% | 22.61% | 40.10% | 0.78% | **PASS** |
| **Timer** | Dot Grid | 1.53% | 1.48% | 2.53% | 0.05% | **PASS** |
| **Stopwatch** | Circle | 0.59% | 1.03% | 0.76% | 0.50% | **PASS** |
| **Stopwatch** | Concentric Rings | 0.38% | 0.55% | 0.72% | 0.24% | **PASS** |
| **Stopwatch** | Three Box | 0.69% | 0.69% | 0.81% | 0.69% | **PASS** |
| **Stopwatch** | Flip Clock | 0.69% | 0.78% | 1.31% | 1.15% | **PASS** |
| **Landscape** | Stopwatch Flip Clock | 0.06% (2,328 px) | - | - | - | **PASS** |

### Additional Interaction Verifications
- **Fast Swipe Restart**: Verified on Hourglass and Liquid Fill. Swiping away and returning quickly restarts the 10-second demo clock cleanly at $t = 0\text{ s}$ without frame flicker or stale frame retention.
- **Orientation Rotation**: Tested device rotation to 90° landscape (`user_rotation = 1`) and back to portrait (`user_rotation = 0`). Layout smoothly adapts without activity recreation; previews continue animating at 60 fps.
- **Artifacts Saved**: All 4-frame sequences per style saved to `qa/previews/`.

