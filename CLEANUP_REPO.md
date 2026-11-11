# Repository & Codebase Cleanup Report

**Project**: VALUE (`com.devvardhan.value`)  
**Date**: November 11, 2026  
**Branch**: `cleanup/repo-hygiene` (merged to `main`)  
**Safety Tag**: `backup-before-cleanup-2026-11-11`

---

## 1. Executive Summary

| Metric | Before Cleanup | After Cleanup | Change |
| :--- | :--- | :--- | :--- |
| **Tracked Files** | 227 files | 143 files | **-84 files (-37.0%)** |
| **Tracked File Size** | 109.24 MB | 0.88 MB | **-108.36 MB (-99.2%)** |
| **Working Tree Cleanliness** | Unignored QA screenshots & traces | Fully ignored & clean | **100% Clean** |
| **Repository Size (`.git`)** | 36.58 MB | 36.60 MB* | Stable (*history preserved) |
| **Unit Tests Passed** | 27 / 27 (100%) | 27 / 27 (100%) | **100% Passing** |
| **Lint Errors** | 0 errors | 0 errors | **0 Errors** |
| **Dead Resources Removed** | 7 template colors | 0 remaining | **Cleaned** |
| **Obsolete SDK Checks Removed** | 7 warnings | 0 remaining | **Cleaned** |
| **App Behavior / UI Regressions** | None | None | **Zero UI/Logic changes** |

---

## 2. Audit Findings & Removals

### 2.1 Large Files & Junk Removed from Tracking
The repository working tree was 99% bloated by binary artifacts inadvertently committed during benchmark and QA sessions:
* **Perfetto System Traces (3 files, 89.55 MB)**:
  * `qa/traces/baseline_mode_switch.perfetto-trace` (~29.8 MB)
  * `qa/traces/baseline_settings.perfetto-trace` (~29.9 MB)
  * `qa/traces/baseline_wheel.perfetto-trace` (~29.8 MB)
* **QA Screenshots (80 PNG files, 18.79 MB)**:
  * `qa/smooth-before/*.png` (40 files)
  * `qa/smooth-after/*.png` (16 files)
  * `qa/previews/*.png` (24 files)
* **Python Bytecode Cache (1 file, 24 KB)**:
  * `scripts/__pycache__/run_benchmarks.cpython-312.pyc`

### 2.2 Reorganized Project Structure
* **Documentation (`docs/`)**:
  * Moved `SIZE_REPORT.md` -> `docs/SIZE_REPORT.md`
  * Moved `SMOOTHNESS_REPORT.md` -> `docs/SMOOTHNESS_REPORT.md`
  * Moved `SMOOTHNESS_REPORT2.md` -> `docs/SMOOTHNESS_REPORT2.md`
  * Moved `QA_PREVIEWS.md` -> `docs/QA_PREVIEWS.md`
* **Benchmarking & Tooling (`tools/qa/`)**:
  * Moved scripts from `scripts/` -> `tools/qa/scripts/`
  * Moved JSON metrics from `qa/*_metrics/` -> `tools/qa/metrics/`

### 2.3 Unused Resources (Safe Only)
* **`app/src/main/res/values/colors.xml`**:
  * Contained 7 default Android Studio template colors (`purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`, `black`, `white`).
  * Verified via project-wide grep that `R.color.*` and `@color/*` have zero usages across all Kotlin, Java, XML layouts, and tests (the app uses Jetpack Compose theme colors exclusively).
  * Safely removed.
* **Strings in `strings.xml`**:
  * Left 100% intact per safety guidelines to protect accessibility, external references, and localization.

### 2.4 Dead Code & Obsolete SDK Checks (Safe Only)
Since `minSdk = 26` (Android 8.0 Oreo), several legacy fallback branches for `< Android O` and `< Android M` were dead code:
1. **`AlarmSoundManager.kt`**:
   * Removed deprecated `AudioManager.requestAudioFocus` / `abandonAudioFocus` fallback branches.
   * Removed deprecated `Vibrator.vibrate(timings, 0)` fallback branch in favor of standard `VibrationEffect.createWaveform`.
2. **`MainActivity.kt`**:
   * Removed redundant `Build.VERSION.SDK_INT >= Build.VERSION_CODES.M` checks in `applyPreferredDisplayMode()` and `findOptimalHighRefreshMode()`.
3. **`StopwatchService.kt`**:
   * Removed redundant `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)` check wrapping `createNotificationChannel()`.
4. **`TimerService.kt`**:
   * Removed redundant `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)` check wrapping `createNotificationChannels()`.

### 2.5 Secret Audit Results
An exhaustive audit was run across the entire Git history (`git log -p -S...`) checking for:
* `storePassword`, `keyPassword`, `keystore.properties`, `*.jks`, `*.keystore`, `*.pepk`
* `local.properties`, `google-services.json`, `.env`, private keys (`BEGIN PRIVATE KEY`, `BEGIN RSA PRIVATE KEY`)

**Result: 0 secrets leaked.**  
No signing credentials, private keys, or passwords were ever tracked or committed to version control. The only occurrence of `storePassword` in Git history was standard Gradle build logic loading properties dynamically.

---

## 3. `.gitignore` Configuration

A comprehensive `.gitignore` was configured covering:
* **Android & Gradle**: `.gradle/`, `build/`, `*/build/`, `.idea/`, `*.iml`, `local.properties`, `captures/`, `.externalNativeBuild/`, `.cxx/`
* **Release Artifacts**: `*.apk`, `*.aab`, `*.ap_`, `*.dex`, `*.class`, `*.idsig`, `*.dm`
* **Keystore & Secrets**: `keystore.properties`, `*.jks`, `*.keystore`, `*.pepk`, `google-services.json`
* **QA & Traces**: `qa/`, `screenshots/`, `burst_*.png`, `*.perfetto-trace`, `*.pftrace`, `gfxinfo*`, `*.hprof`, `*.log`, `*.dump`
* **Python Scripts**: `__pycache__/`, `*.pyc`, `*.pyo`, `.pytest_cache/`, `.venv/`, `venv/`, `env/`
* **Operating System**: `.DS_Store`, `Thumbs.db`, `*.swp`, `*~`, `*.tmp`, `*.bak`
* **Explicit Exceptions**:
  * `!gradle-wrapper.jar`
  * `!keystore.properties.example`
  * `!app/src/main/res/**`

---

## 4. Fresh Clone Verification

A clean checkout of branch `cleanup/repo-hygiene` was tested from scratch in an isolated temporary directory (`%TEMP%\value-clean-test`):
* `./gradlew assembleDebug` -> **BUILD SUCCESSFUL**
* `./gradlew assembleRelease` -> **BUILD SUCCESSFUL** (falls back to debug signing when `keystore.properties` is absent)
* `./gradlew bundleRelease` -> **BUILD SUCCESSFUL** (produces unsigned/debug-signed `.aab` cleanly)
* `./gradlew testDebugUnitTest` -> **BUILD SUCCESSFUL** (27/27 unit tests pass)
* `./gradlew lintDebug` -> **BUILD SUCCESSFUL** (0 errors)
* `git status` after full build -> **100% Clean**, no untracked or unignored files generated.

---

## 5. Keystore Setup Guide

For local release builds and CI/CD pipelines:
1. Copy `keystore.properties.example` to `keystore.properties`:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
2. Populate the four required properties:
   ```properties
   storePassword=your_actual_store_password
   keyPassword=your_actual_key_password
   keyAlias=your_actual_key_alias
   storeFile=path/to/your_release.keystore
   ```
3. `keystore.properties` is strictly ignored by Git and will never be committed.
4. If `keystore.properties` is omitted, `app/build.gradle.kts` automatically falls back to debug signing so that local builds and CI validation can run seamlessly.

---

## 6. Rollback Instructions

If you ever need to restore the repository to the exact state before this cleanup was performed:

```bash
# 1. Fetch tags from remote
git fetch origin --tags

# 2. Checkout the pre-cleanup backup tag
git checkout backup-before-cleanup-2026-11-11

# 3. If you want to reset 'main' back to this tag:
git checkout main
git reset --hard backup-before-cleanup-2026-11-11
```
The tag `backup-before-cleanup-2026-11-11` is pushed and permanently available on GitHub.
