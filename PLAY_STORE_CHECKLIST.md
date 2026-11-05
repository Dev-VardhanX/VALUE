# Google Play Store Release Checklist for VALUE

This guide provides everything needed to submit and release **VALUE** to the Google Play Store.

---

## 1. Store Listing Copy

### App Title
`VALUE`

### Short Description (77 characters, max 80)
`A minimal, pure monochrome countdown timer built with premium craftsmanship.`

### Full Description
```
VALUE is a minimal, pure black-and-white countdown timer crafted with uncompromising attention to detail, typography, and fluid motion.

No advertisements. No accounts. No tracking. No clutter.

FEATURES:
• Strictly Monochrome: Built on a deep pure black canvas (#000000) with crisp white numerals and subtle gray accents.
• 7 Architectural Themes:
  1. Circle: Minimalist high-precision progress ring.
  2. Hourglass: Canvas line-drawn hourglass with flowing sand and falling neck stream.
  3. Scoreboard (Three Box): Stadium-style split panels (HRS, MIN, SEC) with mechanical vertical odometer rolls.
  4. Concentric Rings: Triple monochrome activity rings for hours, minutes, and seconds.
  5. Flip Clock: Tactile split-flap digit cards with smooth 3D rotation animations.
  6. Liquid Fill: Fluid vessel container with animated sinusoidal waves and automatic text color inversion.
  7. Dot Grid: 60-dot matrix that transitions dot-by-dot as time elapses.
• Live Theme Switcher: Preview and switch themes on the fly during an active timer without interrupting your countdown.
• Immersive Fullscreen Mode: Hides status and navigation bars completely for distraction-free focus, with auto-scaling typography for landscape and portrait.
• True 60 FPS Engine: Decoupled logic clock powered by SystemClock.elapsedRealtime ensures zero drift and zero jitter.
• Background & Lockscreen Reliability: Runs as a lightweight foreground service with ongoing status bar controls and exact alarm integration for deep sleep (Doze mode).
• Pure Privacy: Zero data collected. Zero network permissions requested. Everything stays on your device.
```

---

## 2. Graphic Assets Specifications

| Asset | Dimensions | Format | Notes |
| :--- | :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | 32-bit PNG | Pure black background with white ring (already generated in project) |
| **Feature Graphic** | 1024 x 500 px | PNG or JPEG | Pure black banner with clean white "VALUE" typography and countdown digits |
| **Phone Screenshots** | 1080 x 2400 px (or 1080 x 1920 px) | PNG or JPEG | Minimum 2 screenshots (recommended: Set Screen, Scoreboard theme, Hourglass theme, Fullscreen landscape) |
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
* **Artifact Size:** ~3.7 MB (optimized with R8 minification and resource shrinking).

---

## 5. Play Console Declarations

### A. Foreground Service Declaration Form
When prompted by Google Play Console regarding `FOREGROUND_SERVICE_SPECIAL_USE`:

* **Foreground Service Type:** `Special Use` (`specialUse`)
* **Subtype Specified in Manifest:**
  `User-initiated countdown timer with ongoing progress notification and completion alarm`
* **User-Facing Feature Explanation (copy and paste):**
  > "VALUE is a dedicated user-initiated countdown timer application. When a user configures a timer and taps Start, the application starts a foreground service with an ongoing notification to maintain accurate timekeeping, display remaining time, and allow pause/resume/reset actions from the lock screen. The foreground service is critical to ensure the timer alarm sounds accurately on schedule even if the user turns off the screen or switches to other applications."
* **Video Demonstration:** Record a brief 15–30 second screen recording showing:
  1. Starting a 10-second timer.
  2. Pressing the Home button (showing ongoing notification).
  3. Timer hitting zero with vibration, alarm sound, and notification.

### B. Data Safety Form Answers
* **Does your app collect or share any user data?** Select **No**.
* **Is all user data collected encrypted in transit?** N/A (no data collected).
* **Does the app request network access?** No network permission (`android.permission.INTERNET` is not in the manifest).

### C. Content Rating
* Category: **Utility / Productivity**
* Does the app contain violence, adult content, or gambling? **No**
* Does the app share user location or facilitate communication? **No**
* Rating result: **PEGI 3 / ESRB Everyone / IARC 3+**

---

## 6. Step-by-Step Upload to Internal Testing Track

1. Log in to [Google Play Console](https://play.google.com/console).
2. Select your app: **VALUE** (or click **Create app** if not yet created).
3. On the left navigation drawer, go to **Testing > Internal testing**.
4. Click **Create new release**.
5. Upload `app\build\outputs\bundle\release\app-release.aab`.
6. Enter release name (e.g., `1.0.0 (1)`).
7. Enter release notes:
   `Initial production release of VALUE Timer: minimalist design, 7 architectural themes, 60fps animations, and zero tracking.`
8. Click **Save** and then **Review release**.
9. Add your email address under the **Testers** tab and send the invite link to your physical device.
10. Install from Google Play Internal Testing to verify download, notifications, and alarm performance on a physical device.
