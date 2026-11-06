# Google Play Store Release Checklist for VALUE

This guide provides everything needed to submit and release **VALUE** to the Google Play Store.

---

## 1. Store Listing Copy

### App Title
`VALUE - Timer & Stopwatch` (or `VALUE`)

### Short Description (79 characters, max 80)
`A minimal, pure monochrome countdown timer and precision centisecond stopwatch.`

### Full Description
```
VALUE is a minimal, pure black-and-white countdown timer and precision stopwatch crafted with uncompromising attention to detail, typography, and fluid motion.

No advertisements. No accounts. No tracking. No clutter.

TIMER FEATURES:
• Strictly Monochrome: Built on a deep pure black canvas (#000000) with crisp white numerals and subtle gray accents.
• 7 Architectural Themes:
  1. Circle: Minimalist high-precision progress ring.
  2. Hourglass: Canvas line-drawn hourglass with flowing sand and falling neck stream.
  3. Scoreboard (Three Box): Stadium-style split panels (HRS, MIN, SEC) with mechanical vertical odometer rolls.
  4. Concentric Rings: Triple monochrome activity rings for hours, minutes, and seconds.
  5. Flip Clock: Tactile split-flap digit cards with smooth 3D rotation animations.
  6. Liquid Fill: Fluid vessel container with animated sinusoidal waves and automatic text color inversion.
  7. Dot Grid: 60-dot matrix that transitions dot-by-dot as time elapses.
• Style Gallery with Live Previews: Browse all architectural themes in an elegant horizontal pager with live demo clocks before choosing your style.
• Immersive Fullscreen Mode: Hides status and navigation bars completely for distraction-free focus, with auto-scaling typography for landscape and portrait.

STOPWATCH FEATURES:
• Centisecond Accuracy: Counts up from 00:00.00 with hundredths-of-a-second resolution. Adaptive layout cleanly displays MM:SS.cc under 1 hour and H:MM:SS.cc at 1 hour and above without digit jitter.
• Lap Split Management: Record up to 99 laps with split and total times, newest on top. Automatic highlights identify the fastest lap ("BEST") in pure white and the slowest ("WORST") in subtle gray.
• Dual Mode Switch: Effortlessly toggle between Timer and Stopwatch via a tactile top pill switch. Switching modes never resets or stops the active session in either mode.
• Background Chronometer: Ongoing lock screen and notification shade display powered by Android's native chronometer for zero battery waste.

ENGINE & ARCHITECTURE:
• True 60 FPS Decoupled Clock: Logic clock powered directly by SystemClock.elapsedRealtime guarantees zero drift and zero jitter.
• Background & Lockscreen Reliability: Lightweight foreground services with ongoing notification controls ensure uninterrupted execution even during Doze mode and across process death.
• Pure Privacy: Zero data collected. Zero network permissions requested. Everything stays on your device.
```

---

## 2. Graphic Assets Specifications

| Asset | Dimensions | Format | Notes |
| :--- | :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | 32-bit PNG | Pure black background with white ring (already generated in project) |
| **Feature Graphic** | 1024 x 500 px | PNG or JPEG | Pure black banner with clean white "VALUE" typography and countdown/stopwatch digits |
| **Phone Screenshots** | 1080 x 2400 px (or 1080 x 1920 px) | PNG or JPEG | Minimum 2 screenshots (recommended: 1. Style Gallery Screen with live theme previews, 2. Timer Set Screen, 3. Timer Scoreboard theme, 4. Stopwatch running with Lap list, 5. Fullscreen Landscape Stopwatch) |
| **7-inch Tablet** | 1200 x 1920 px | PNG or JPEG | Minimum 1 screenshot |
| **10-inch Tablet** | 1600 x 2560 px | PNG or JPEG | Minimum 1 screenshot |

---

## 3. Keystore Generation & Signing Setup

### Step 3.1: Generate Your Release Keystore with `keytool`
Open PowerShell or your terminal and run:

```powershell
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias value_key
```

When prompted, choose a secure password and enter your developer details.

### Step 3.2: Configure `keystore.properties`
In the root directory of the project (`d:\AndroidDev\Projects\VALUE\`), create a file named `keystore.properties` (this file is already git-ignored):

```properties
storeFile=../release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=value_key
keyPassword=YOUR_KEY_PASSWORD
```

*(If `keystore.properties` is omitted, the build script automatically uses the standard debug keystore as a fallback so builds never fail).*

---

## 4. Building the Production Release Bundle (AAB)

To compile and package the release Android App Bundle (AAB):

```powershell
.\gradlew.bat bundleRelease
```

* **Output Artifact Path:**
  `app\build\outputs\bundle\release\app-release.aab`
* **Artifact Size:** ~3.78 MB (optimized with R8 minification and resource shrinking).

---

## 5. Play Console Declarations

### A. Foreground Service Declaration Form
When prompted by Google Play Console regarding `FOREGROUND_SERVICE_SPECIAL_USE`:

* **Foreground Service Type:** `Special Use` (`specialUse`)
* **Subtype Specified in Manifest:**
  `User-initiated countdown timer and precision stopwatch with ongoing progress notification and lock screen controls`
* **User-Facing Feature Explanation (copy and paste):**
  > "VALUE is a dedicated user-initiated productivity utility providing both a countdown timer and a precision stopwatch. When a user starts either the timer or the stopwatch, the application launches a lightweight foreground service with an ongoing notification to guarantee uninterrupted timekeeping, live chronometer/countdown display, and quick pause/lap/reset controls directly from the lock screen and notification shade even when the device enters deep sleep (Doze mode)."

### B. Permissions Declarations
* `POST_NOTIFICATIONS`: Required for Android 13+ (API 33+) to show ongoing timer and stopwatch notifications.
* `VIBRATE`: Required for haptic feedback and timer completion alerts.
* `FOREGROUND_SERVICE`: Required for background timekeeping.
* `FOREGROUND_SERVICE_SPECIAL_USE`: Required for dedicated timer and stopwatch services.

### C. Data Safety Form
* **Does your app collect or share any user data?** No.
* **Are all user data encrypted in transit?** N/A (no network requests).
* **Do you provide a way for users to request data deletion?** N/A (no user data stored on servers).
