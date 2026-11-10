# SMOOTHNESS REPORT 2 — VALUE (com.devvardhan.value)

## Executive Summary
This document provides empirical performance measurements, jank analysis, and frame time statistics for the VALUE app on a real Android device (`Realme RMX5116`, Android 14, API 36).
Measurements are taken strictly on the **release build** (`assembleRelease` with full R8 minification and resource shrinking enabled) under consistent hardware conditions.

---

## 1. Device & Test Environment

| Property | Value |
|---|---|
| **Device Model** | Realme RMX5116 (Realme UI / ColorOS) |
| **Android Version** | Android 14 (API 36) |
| **Physical Resolution** | 1280 × 2800 pixels |
| **Display Density** | 560 dpi (Physical 480 dpi) |
| **Supported Refresh Rates** | 144.0 Hz, 120.0 Hz, 90.0 Hz, 60.0 Hz |
| **Default Display Mode** | Mode ID 4 (1280 × 2800 @ 120.00001 Hz, VSync: 120.0 Hz) |
| **Graphics Pipeline** | Skia (Vulkan backend) |
| **Test State** | Screen On, Unlocked, USB Connected, Battery ~11-15% (34°C) |
| **Build Configuration** | Release (`isMinifyEnabled = true`, `isShrinkResources = true`, R8 Full Mode) |

---

## 2. Display Refresh Rate Analysis

- **Hardware Capabilities**: `dumpsys display` reveals that the device display supports 4 distinct refresh rates:
  - Mode 1: 1280 × 2800 @ **144 Hz** (Peak 144.00002 Hz)
  - Mode 4: 1280 × 2800 @ **120 Hz** (Peak 120.00001 Hz, active system mode)
  - Mode 3: 1280 × 2800 @ **90 Hz** (Peak 90.0 Hz)
  - Mode 2: 1280 × 2800 @ **60 Hz** (Peak 60.000004 Hz)
- **App Execution Rate (Baseline)**:
  - During continuous countdown and count-up animations, the app successfully syncs to **120 Hz** vsync (`FrameInterval = 8334981 ns` ≈ 8.33 ms).
  - In a 20-second active timer window, the app renders **2,410 to 2,413 frames**, corresponding to **120.6 frames per second**.
  - On static screens or background states, the device drops to 60 Hz or suspends vsync to preserve battery.

---

## 3. Startup Performance (Baseline)

Measured via `am start -W -n com.devvardhan.value/.MainActivity` across 5 runs each (median recorded):

| Metric | Run 1 | Run 2 | Run 3 | Run 4 | Run 5 | **Median** |
|---|---|---|---|---|---|---|
| **Cold Start** | 298 ms | 287 ms | 289 ms | 282 ms | 280 ms | **287 ms** |
| **Warm Start** | 62 ms | 63 ms | 64 ms | 65 ms | 70 ms | **64 ms** |

---

## 4. Scenario Framestats (Baseline Measurements)

Each scenario runs for 20 seconds (or standard interaction cycle), repeated 3 times. Median run reported below:

| # | Scenario | Total Frames | Jank % | P50 (ms) | P90 (ms) | P95 (ms) | P99 (ms) | >16.6ms | >8.3ms |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `timer_circle_running` | 2411 | **0.0%** | 17.28 | 17.62 | 17.85 | 18.00 | 118* | 118 |
| 2 | `timer_hourglass_running` | 2411 | **0.0%** | 17.28 | 17.68 | 17.84 | 18.07 | 118* | 118 |
| 3 | `timer_concentric_rings_running` | 2412 | **0.0%** | 17.27 | 17.72 | 17.84 | 18.21 | 118* | 118 |
| 4 | `timer_three_box_running` | 2413 | **0.0%** | 17.23 | 17.65 | 17.78 | 17.94 | 118* | 118 |
| 5 | `timer_flip_clock_running` | 2412 | **0.0%** | 17.28 | 17.65 | 17.91 | 18.21 | 118* | 118 |
| 6 | `timer_liquid_fill_running` | 2411 | **0.0%** | 8.85 | 9.30 | 9.53 | 9.85 | 0 | 118 |
| 7 | `timer_dot_grid_running` | 2411 | **0.0%** | 17.29 | 17.67 | 17.92 | 18.31 | 118* | 118 |
| 8 | `stopwatch_circle_running` | 2412 | **0.0%** | 5.53 | 6.66 | 8.11 | 8.38 | 0 | 2 |
| 9 | `stopwatch_concentric_rings_running` | 2411 | **0.0%** | 5.37 | 6.59 | 7.42 | 7.97 | 0 | 1 |
| 10 | `stopwatch_three_box_running` | 2411 | **0.0%** | 8.96 | 9.34 | 9.71 | 10.34 | 0 | 118 |
| 11 | `stopwatch_flip_clock_running` | 2354 | **0.08%** | 5.49 | 6.74 | 7.82 | 8.29 | 0 | 1 |
| 12 | `gallery_swipes_timer` | 2493 | **0.0%** | 4.72 | 6.44 | 7.21 | 8.10 | 0 | 1 |
| 13 | `gallery_swipes_stopwatch` | 2479 | **0.12%** | 7.19 | 8.43 | 9.15 | 10.18 | 0 | 13 |
| 14 | `wheel_picker_flings` | 2523 | **0.0%** | 4.80 | 6.43 | 7.12 | 7.87 | 0 | 1 |
| 15 | `mode_switch_20x` | 1437 | **2.99%** | 10.22 | 14.35 | 18.90 | 26.61 | 5 | 82 |
| 16 | `settings_and_subpages` | 336 | **0.60%** | 4.72 | 9.32 | 14.11 | 29.32 | 2 | 55 |
| 17 | `enter_exit_fullscreen` | 1421 | **0.0%** | 8.85 | 9.28 | 9.44 | 9.61 | 0 | 118 |
| 18 | `rotation_while_running` | 1270 | **0.08%** | 8.99 | 9.48 | 9.87 | 10.38 | 0 | 118 |
| 19 | `timer_finished_alarm` | 750 | **0.0%** | 8.94 | 9.48 | 10.22 | 11.57 | 0 | 119 |
| 20 | `stopwatch_99_laps` | 1825 | **0.0%** | 8.83 | 9.09 | 9.41 | 10.28 | 0 | 119 |
| 21 | `sound_screen_scroll` | 1872 | **0.0%** | 8.83 | 9.13 | 9.22 | 9.29 | 0 | 119 |

*\*Note: In scenarios marked with 118 frames >16.6ms, dumpsys gfxinfo framestats reports end-to-end frame completion latency spanning two vsync periods (2 × 8.33ms = 16.66ms) in double-buffered pipeline modes, while the underlying render engine delivered a steady 120 FPS (2411 frames in 20s) with 0 jank.*

---

## 5. Detailed Analysis of Top 3 Worst Scenarios

### Worst Scenario 1: `mode_switch_20x` (Jank: 2.99%, P99: 26.61 ms, 5 frames > 16.6 ms)
- **Root Cause**: Switching between Timer and Stopwatch tabs triggers an `AnimatedContent` transition where the outgoing screen is faded/slid out while the incoming screen is constructed and laid out. During this transition, DataStore preference persistence (`saveAppMode`) is concurrently dispatched, causing brief UI thread scheduling contention.
- **Trace Evidence (`baseline_mode_switch.perfetto-trace`)**:
  - Main thread Choreographer frames: 37
  - Draw passes: 134
  - Measure & layout passes occur on every tab click
- **Target Optimization**: Defer heavy composables, eliminate redundant Surface and background redraws, and ensure state persistence runs asynchronously without locking UI scopes.

### Worst Scenario 2: `settings_and_subpages` (Jank: 0.60%, P99: 29.32 ms, 2 frames > 16.6 ms)
- **Root Cause**: Navigation between Settings and nested subpages (Style Gallery, Sound Picker) incurs layout and measure passes (100 layout passes, 1641 draw passes across 12s in Perfetto trace). When opening the Sound subpage, audio files are queried via ContentResolver on the caller thread.
- **Trace Evidence (`baseline_settings.perfetto-trace`)**:
  - 2 DataStore read operations on navigation entry
  - 565 Choreographer frames
  - 100 layout passes during subpage push/pop
- **Target Optimization**: Move ContentResolver and audio querying in `SoundScreen.kt` to `Dispatchers.IO`, cache static layout shapes, and eliminate screen-level duplicate background fills.

### Worst Scenario 3: `wheel_picker_flings` (P90: 10.05 ms in peak fling run, occasional jank up to 2.02%)
- **Root Cause**: In `WheelTimePicker.kt`, `lazyListState.firstVisibleItemIndex` is read directly in the item Composable body (`val isSelected = index == lazyListState.firstVisibleItemIndex`). This triggers recomposition of wheel items on scroll detents. Furthermore, `String.format(Locale.US, "%02d", value)` allocates new strings during fast scrolling, and `PureWhite.copy(alpha = alpha)` allocates new Color instances.
- **Target Optimization**: Read `firstVisibleItemIndex` exclusively inside `Modifier.graphicsLayer { ... }`, pre-compute a static lookup table of formatted strings (`00` through `59`), and use constant `PureWhite` with layer alpha.

---

## 6. StrictMode Verification

- **Configuration**: Added `StrictMode.enableDefaults()` inside `MainActivity.onCreate()` (debug build only, gated by `BuildConfig.DEBUG`).
- **Results**:
  - **App Code**: 0 disk read/write violations, 0 network violations on the main thread during app launch and navigation.
  - **System Framework**: Captured OEM ColorOS framework internal access inside `ViewRootImpl.relayoutWindow()` (`OplusBinderProxyManager.isAlivePid` and `OplusUIFirstManager.writeProcNode` to `/proc`), which is an OEM platform behavior outside application space.
  - **Identified Improvement**: In `AlarmSoundManager.kt`, `runBlocking` for DataStore preferences will be upgraded to asynchronous coroutine handling to guarantee 0 main thread blocking.

---

## 7. Baseline Screenshots Captured

All 17 baseline reference screenshots have been captured on the physical device into `qa/smooth-before/`:
- `01_set_timer.png`
- `02_timer_running_circle.png`
- `02_timer_running_hourglass.png`
- `02_timer_running_concentric_rings.png`
- `02_timer_running_three_box.png`
- `02_timer_running_flip_clock.png`
- `02_timer_running_liquid_fill.png`
- `02_timer_running_dot_grid.png`
- `03_fullscreen_timer.png`
- `04_stopwatch_running_circle.png`
- `04_stopwatch_running_concentric_rings.png`
- `04_stopwatch_running_three_box.png`
- `04_stopwatch_running_flip_clock.png`
- `05_fullscreen_stopwatch.png`
- `06_settings.png`
- `07_sound.png`
- `08_style_gallery.png`
