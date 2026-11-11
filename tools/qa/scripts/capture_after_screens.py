#!/usr/bin/env python3
"""
Captures AFTER screenshots of all main screens and styles in VALUE app,
saving them into qa/smooth-after/ for pixel diff verification against qa/smooth-before/.
"""

import subprocess
import time
import os

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"
OUT_DIR = "qa/smooth-after"

def adb_shell(*args):
    return subprocess.run([ADB, "shell"] + list(args), capture_output=True, text=True, timeout=15).stdout

def tap(x, y):
    adb_shell("input", "tap", str(x), str(y))

def swipe(x1, y1, x2, y2, d):
    adb_shell("input", "swipe", str(x1), str(y1), str(x2), str(y2), str(d))

def keyevent(c):
    adb_shell("input", "keyevent", str(c))

def sleep(s):
    time.sleep(s)

def capture_screen(name):
    adb_shell("screencap", "-p", "/sdcard/screen.png")
    out_path = os.path.join(OUT_DIR, f"{name}.png")
    subprocess.run([ADB, "pull", "/sdcard/screen.png", out_path], capture_output=True)
    print(f"Captured: {out_path}")

def go_to_main_set_timer():
    adb_shell("input", "keyevent", "224") # Wakeup
    tap(456, 458) # Timer tab
    sleep(0.3)
    tap(348, 2436) # Reset
    sleep(0.3)

def select_timer_theme(theme_index):
    go_to_main_set_timer()
    tap(1126, 273) # Settings
    sleep(0.4)
    tap(600, 616) # Timer style
    sleep(0.5)
    for _ in range(7):
        swipe(200, 1400, 1000, 1400, 150)
        sleep(0.1)
    sleep(0.3)
    for _ in range(theme_index):
        swipe(1000, 1400, 200, 1400, 250)
        sleep(0.3)
    tap(640, 1915) # Select
    sleep(0.4)
    tap(154, 273) # Back
    sleep(0.4)

def select_stopwatch_theme(theme_index):
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 1752)
    sleep(0.5)
    for _ in range(4):
        swipe(200, 1400, 1000, 1400, 150)
        sleep(0.1)
    sleep(0.3)
    for _ in range(theme_index):
        swipe(1000, 1400, 200, 1400, 250)
        sleep(0.3)
    tap(640, 1915)
    sleep(0.4)
    tap(154, 273)
    sleep(0.4)

def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Capturing AFTER screenshots for quality verification...")

    # 1. Set Timer screen
    go_to_main_set_timer()
    sleep(0.5)
    capture_screen("01_set_timer")

    # 2. Timer running themes (7)
    timer_themes = [
        ("circle", 0),
        ("hourglass", 1),
        ("concentric_rings", 2),
        ("three_box", 3),
        ("flip_clock", 4),
        ("liquid_fill", 5),
        ("dot_grid", 6)
    ]
    for name, idx in timer_themes:
        select_timer_theme(idx)
        tap(640, 2464) # Start
        sleep(0.8)
        capture_screen(f"02_timer_running_{name}")
        tap(348, 2436) # Reset
        sleep(0.4)

    # 3. Fullscreen timer
    select_timer_theme(0)
    tap(640, 2464)
    sleep(0.5)
    tap(1126, 273) # Fullscreen
    sleep(0.6)
    capture_screen("03_fullscreen_timer")
    tap(640, 1400) # Exit fullscreen
    sleep(0.4)
    tap(348, 2436) # Reset
    sleep(0.4)

    # 4. Stopwatch running themes (4)
    sw_themes = [
        ("circle", 0),
        ("concentric_rings", 1),
        ("three_box", 2),
        ("flip_clock", 3)
    ]
    for name, idx in sw_themes:
        select_stopwatch_theme(idx)
        tap(823, 458) # Stopwatch tab
        sleep(0.4)
        tap(1126, 1718) # Reset
        sleep(0.2)
        tap(640, 1718) # Start
        sleep(0.5)
        # Add 2 laps
        tap(154, 1718)
        sleep(0.3)
        tap(154, 1718)
        sleep(0.4)
        capture_screen(f"04_stopwatch_running_{name}")
        tap(640, 1718) # Pause
        sleep(0.3)
        tap(1126, 1718) # Reset
        sleep(0.3)
        tap(456, 458) # Back to Timer
        sleep(0.4)

    # 5. Fullscreen stopwatch
    tap(823, 458)
    sleep(0.3)
    tap(1126, 273) # Fullscreen
    sleep(0.5)
    capture_screen("05_fullscreen_stopwatch")
    tap(640, 1400) # Exit
    sleep(0.3)
    tap(456, 458)
    sleep(0.3)

    # 6. Settings Screen
    tap(1126, 273)
    sleep(0.5)
    capture_screen("06_settings")

    # 7. Sound Screen
    tap(600, 801)
    sleep(0.5)
    capture_screen("07_sound")
    keyevent(4) # Back to Settings
    sleep(0.3)
    keyevent(4) # Back to Main
    sleep(0.4)

    # 8. Style Gallery
    tap(1126, 273)
    sleep(0.3)
    tap(600, 616)
    sleep(0.5)
    capture_screen("08_style_gallery")
    keyevent(4)
    sleep(0.3)
    keyevent(4)
    sleep(0.4)

    print("All AFTER screenshots captured successfully!")

if __name__ == "__main__":
    main()

