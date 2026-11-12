# VALUE

A minimalist timer and stopwatch for Android, in black and white only.

<table>
  <tr>
    <td align="center"><img src="docs/screens/timer-set.png" width="200" alt="Set timer" /><br />Set timer</td>
    <td align="center"><img src="docs/screens/timer-running.png" width="200" alt="Running timer" /><br />Running timer</td>
    <td align="center"><img src="docs/screens/stopwatch-laps.png" width="200" alt="Stopwatch with laps" /><br />Stopwatch with laps</td>
    <td align="center"><img src="docs/screens/fullscreen.png" width="200" alt="Fullscreen view" /><br />Fullscreen view</td>
  </tr>
</table>

I wanted a clock app that shows time and nothing else. VALUE has no accent colors, shadows, or illustrations. Time is read through type and geometry alone.

## What it does

A countdown timer and a stopwatch with lap recording. Swipe or use the pill toggle to switch between them; a running session is not interrupted. Both run in foreground services with an ongoing notification, lockscreen controls, and an alarm when a timer finishes.

The fullscreen view hides its controls after a moment and pads around camera cutouts so the time stays centered in both orientations.

## Design decisions

The timer is set with three looping wheels (hours, minutes, seconds) that snap into place, so there is no keypad to deal with.

There are several themes, including Circle, Flip Clock and Three Box. They change how progress is drawn (arcs, flip cards, split flaps) but share the same timing engine, so a timer behaves the same whichever one you pick.

<table>
  <tr>
    <td align="center"><img src="docs/screens/theme-circle.png" width="110" alt="Circle" /><br />Circle</td>
    <td align="center"><img src="docs/screens/theme-hourglass.png" width="110" alt="Hourglass" /><br />Hourglass</td>
    <td align="center"><img src="docs/screens/theme-three-box.png" width="110" alt="Three Box" /><br />Three Box</td>
    <td align="center"><img src="docs/screens/theme-concentric-rings.png" width="110" alt="Concentric Rings" /><br />Concentric Rings</td>
    <td align="center"><img src="docs/screens/theme-flip-clock.png" width="110" alt="Flip Clock" /><br />Flip Clock</td>
    <td align="center"><img src="docs/screens/theme-liquid-fill.png" width="110" alt="Liquid Fill" /><br />Liquid Fill</td>
    <td align="center"><img src="docs/screens/theme-dot-grid.png" width="110" alt="Dot Grid" /><br />Dot Grid</td>
  </tr>
</table>

## Tech

Kotlin and Jetpack Compose, minSdk 26, targetSdk 35. Preferences are stored in DataStore. Timekeeping runs in foreground services backed by exact alarms, so timers stay accurate when the app is in the background.

## Build

Requires JDK 17 and Android SDK 35.

```bash
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # debug APK
./gradlew assembleRelease     # release APK (needs a signing config)
```

The APK is written to `app/build/outputs/apk/`.