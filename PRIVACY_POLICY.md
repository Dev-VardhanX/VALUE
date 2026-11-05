# Privacy Policy for VALUE Timer

**Effective Date:** November 6, 2026

VALUE ("we", "our", or "the app") is a minimalist countdown timer application designed with privacy as a foundational principle.

### 1. Information Collection and Use
VALUE does not collect, transmit, store, or share any personal data, usage metrics, telemetry, or device identifiers. 
- **No Personal Data:** We do not collect names, email addresses, phone numbers, or account credentials.
- **No Analytics or Trackers:** The app contains zero third-party analytics SDKs, trackers, or advertising frameworks.
- **No Internet Access:** The app does not request or require network permissions (`android.permission.INTERNET`). All timer operations, settings, and notifications are processed entirely on your local device.

### 2. Device Permissions
VALUE requests only the minimum device permissions necessary to perform countdown timer functions:
- **Notifications (`POST_NOTIFICATIONS`):** Used strictly to display the ongoing countdown timer in your status bar and alert you when your timer completes.
- **Vibration (`VIBRATE`):** Used to vibrate your device when the timer reaches zero.
- **Foreground Service (`FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE`):** Used solely to keep the countdown active and reliable when you navigate away from the app or turn off your screen.
- **Exact Alarms (`USE_EXACT_ALARM`):** Used to ensure the timer completes accurately at the exact second even when your device enters deep sleep (Doze mode).
- **Wake Lock (`WAKE_LOCK`):** Used to keep the CPU active temporarily while transitioning into the completion alarm.

None of these permissions are used to collect or export data.

### 3. Data Storage
All user preferences (such as selected visual theme and last configured duration) are stored locally on your device using Android Jetpack DataStore. This data never leaves your device and is erased if you uninstall the application or clear app data.

### 4. Children’s Privacy
Because VALUE does not collect any personal information whatsoever, it complies with all international children's privacy regulations, including COPPA and GDPR-K.

### 5. Changes to This Privacy Policy
We may update this Privacy Policy from time to time. Any changes will be posted directly to this document with an updated effective date.

### 6. Contact Us
If you have any questions regarding this Privacy Policy, you may contact the developer at:
- **Email:** dev0vardhan@gmail.com
- **GitHub:** https://github.com/Dev-VardhanX/VALUE

