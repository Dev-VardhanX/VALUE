#!/usr/bin/env python3
"""
Comprehensive benchmark runner for VALUE app.
Executes scenarios with real input via adb, records dumpsys gfxinfo framestats,
repeats each 3 times and takes the median metrics.
"""

import subprocess
import time
import re
import json
import os
import sys

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"

def adb(*args, timeout=30):
    cmd = [ADB] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    return res.stdout

def adb_shell(*args, timeout=30):
    return adb("shell", *args, timeout=timeout)

def tap(x, y):
    adb_shell("input", "tap", str(x), str(y))

def swipe(x1, y1, x2, y2, duration_ms):
    adb_shell("input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms))

def keyevent(code):
    adb_shell("input", "keyevent", str(code))

def sleep(s):
    time.sleep(s)

def reset_gfxinfo():
    adb_shell("dumpsys", "gfxinfo", PKG, "reset")

def capture_gfxinfo():
    return adb_shell("dumpsys", "gfxinfo", PKG, "framestats")

def parse_framestats(output):
    metrics = {
        "total_frames": 0,
        "janky_frames": 0,
        "janky_pct": 0.0,
        "p50_ms": 0.0,
        "p90_ms": 0.0,
        "p95_ms": 0.0,
        "p99_ms": 0.0,
        "max_ms": 0.0,
        "over_16_6ms": 0,
        "over_11_1ms": 0,
        "over_8_3ms": 0,
        "worst_10": []
    }
    
    # Check summary
    m_total = re.search(r"Total frames rendered:\s*(\d+)", output)
    m_jank = re.search(r"Janky frames:\s*(\d+)\s*\(([^)]+)%\)", output)
    m_p50 = re.search(r"50th percentile:\s*(\d+)ms", output)
    m_p90 = re.search(r"90th percentile:\s*(\d+)ms", output)
    m_p95 = re.search(r"95th percentile:\s*(\d+)ms", output)
    m_p99 = re.search(r"99th percentile:\s*(\d+)ms", output)

    if m_total:
        metrics["total_frames"] = int(m_total.group(1))
    if m_jank:
        metrics["janky_frames"] = int(m_jank.group(1))
        metrics["janky_pct"] = float(m_jank.group(2))
    if m_p50:
        metrics["summary_p50"] = int(m_p50.group(1))
    if m_p90:
        metrics["summary_p90"] = int(m_p90.group(1))
    if m_p95:
        metrics["summary_p95"] = int(m_p95.group(1))
    if m_p99:
        metrics["summary_p99"] = int(m_p99.group(1))
        
    frame_times = []
    in_profile = False
    for line in output.split("\n"):
        line = line.strip()
        if "---PROFILEDATA---" in line:
            in_profile = not in_profile
            continue
        if in_profile and not line.startswith("Flags,"):
            parts = line.split(",")
            if len(parts) >= 18:
                try:
                    # IntendedVsync = index 2, FrameCompleted = index 17
                    intended_vsync = int(parts[2])
                    frame_completed = int(parts[17])
                    duration = (frame_completed - intended_vsync) / 1_000_000.0
                    if 0 < duration < 5000:
                        frame_times.append(duration)
                except (ValueError, IndexError):
                    pass
                    
    if frame_times:
        frame_times.sort()
        n = len(frame_times)
        metrics["framestats_count"] = n
        metrics["p50_ms"] = round(frame_times[int(n * 0.50)], 2)
        metrics["p90_ms"] = round(frame_times[int(n * 0.90)], 2)
        metrics["p95_ms"] = round(frame_times[int(n * 0.95)], 2)
        metrics["p99_ms"] = round(frame_times[min(int(n * 0.99), n - 1)], 2)
        metrics["max_ms"] = round(max(frame_times), 2)
        metrics["over_16_6ms"] = sum(1 for f in frame_times if f > 16.66)
        metrics["over_11_1ms"] = sum(1 for f in frame_times if f > 11.11)
        metrics["over_8_3ms"] = sum(1 for f in frame_times if f > 8.33)
        metrics["worst_10"] = [round(f, 2) for f in frame_times[-10:]]
    elif "summary_p50" in metrics:
        metrics["p50_ms"] = float(metrics["summary_p50"])
        metrics["p90_ms"] = float(metrics["summary_p90"])
        metrics["p95_ms"] = float(metrics["summary_p95"])
        metrics["p99_ms"] = float(metrics["summary_p99"])
        
    return metrics

def median_metric(runs):
    # Sort runs by janky_pct, then p90_ms
    runs_sorted = sorted(runs, key=lambda r: (r.get("janky_pct", 0), r.get("p90_ms", 0)))
    return runs_sorted[len(runs_sorted) // 2]

# Navigation helpers
def go_to_main_set_timer():
    """Ensure we are on Main -> Timer tab, Set Timer screen (not running)."""
    adb_shell("input", "keyevent", "224") # WAKEUP
    focus = adb_shell("dumpsys", "window | grep mCurrentFocus")
    if "com.devvardhan.value" not in focus:
        adb_shell("am", "start", "-n", f"{PKG}/.MainActivity")
        sleep(1.0)
    # Tap Timer tab
    tap(456, 458)
    sleep(0.3)
    # If running, tap reset
    tap(348, 2436)
    sleep(0.3)

def select_timer_theme(theme_index):
    """theme_index: 0=Circle, 1=Hourglass, 2=Concentric Rings, 3=Three Box, 4=Flip Clock, 5=Liquid Fill, 6=Dot Grid"""
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 616)
    sleep(0.5)
    # Swipe back to start
    for _ in range(7):
        swipe(200, 1400, 1000, 1400, 150)
        sleep(0.15)
    sleep(0.3)
    # Swipe to target
    for _ in range(theme_index):
        swipe(1000, 1400, 200, 1400, 250)
        sleep(0.35)
    tap(640, 1915)
    sleep(0.4)
    tap(154, 273)
    sleep(0.4)

def select_stopwatch_theme(theme_index):
    """theme_index: 0=Circle, 1=Concentric Rings, 2=Three Box, 3=Flip Clock"""
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 1752)
    sleep(0.5)
    for _ in range(4):
        swipe(200, 1400, 1000, 1400, 150)
        sleep(0.15)
    sleep(0.3)
    for _ in range(theme_index):
        swipe(1000, 1400, 200, 1400, 250)
        sleep(0.35)
    tap(640, 1915)
    sleep(0.4)
    tap(154, 273)
    sleep(0.4)

# Scenario implementations
def run_timer_theme_scenario(theme_name, theme_index):
    select_timer_theme(theme_index)
    tap(640, 2464) # Start timer
    sleep(0.6)
    reset_gfxinfo()
    sleep(20.0)
    raw = capture_gfxinfo()
    tap(348, 2436) # Reset timer
    sleep(0.5)
    return parse_framestats(raw)

def run_stopwatch_theme_scenario(theme_name, theme_index):
    select_stopwatch_theme(theme_index)
    tap(823, 458) # Stopwatch tab
    sleep(0.4)
    tap(1126, 1718) # Reset
    sleep(0.3)
    tap(640, 1718) # Start
    sleep(0.6)
    reset_gfxinfo()
    sleep(20.0)
    raw = capture_gfxinfo()
    tap(640, 1718) # Pause
    sleep(0.3)
    tap(1126, 1718) # Reset
    sleep(0.4)
    tap(456, 458) # Return to Timer
    sleep(0.4)
    return parse_framestats(raw)

def run_gallery_swipes_timer():
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 616)
    sleep(0.5)
    reset_gfxinfo()
    start_time = time.time()
    direction = 1
    while time.time() - start_time < 20.0:
        if direction == 1:
            swipe(1000, 1400, 250, 1400, 280)
        else:
            swipe(250, 1400, 1000, 1400, 280)
        sleep(0.4)
        if time.time() - start_time > 10.0 and direction == 1:
            direction = -1
    raw = capture_gfxinfo()
    keyevent(4)
    sleep(0.4)
    keyevent(4)
    sleep(0.4)
    return parse_framestats(raw)

def run_gallery_swipes_stopwatch():
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 1752)
    sleep(0.5)
    reset_gfxinfo()
    start_time = time.time()
    direction = 1
    while time.time() - start_time < 20.0:
        if direction == 1:
            swipe(1000, 1400, 250, 1400, 280)
        else:
            swipe(250, 1400, 1000, 1400, 280)
        sleep(0.4)
        if time.time() - start_time > 10.0 and direction == 1:
            direction = -1
    raw = capture_gfxinfo()
    keyevent(4)
    sleep(0.4)
    keyevent(4)
    sleep(0.4)
    return parse_framestats(raw)

def run_wheel_picker_flings():
    go_to_main_set_timer()
    reset_gfxinfo()
    start_time = time.time()
    wheels_x = [268, 640, 1012]
    w_idx = 0
    while time.time() - start_time < 20.0:
        wx = wheels_x[w_idx % 3]
        swipe(wx, 1750, wx, 1300, 120)
        sleep(0.6)
        swipe(wx, 1300, wx, 1750, 120)
        sleep(0.6)
        w_idx += 1
    raw = capture_gfxinfo()
    return parse_framestats(raw)

def run_mode_switch_20x():
    go_to_main_set_timer()
    reset_gfxinfo()
    for _ in range(20):
        tap(823, 458)
        sleep(0.4)
        tap(456, 458)
        sleep(0.4)
    raw = capture_gfxinfo()
    return parse_framestats(raw)

def run_settings_and_subpages():
    go_to_main_set_timer()
    reset_gfxinfo()
    for _ in range(3):
        tap(1126, 273)
        sleep(0.4)
        tap(600, 616)
        sleep(0.5)
        keyevent(4)
        sleep(0.4)
        tap(600, 801)
        sleep(0.5)
        keyevent(4)
        sleep(0.4)
        tap(600, 1752)
        sleep(0.5)
        keyevent(4)
        sleep(0.4)
        keyevent(4)
        sleep(0.5)
    raw = capture_gfxinfo()
    return parse_framestats(raw)

def run_enter_exit_fullscreen():
    go_to_main_set_timer()
    tap(640, 2464)
    sleep(0.5)
    reset_gfxinfo()
    for _ in range(10):
        tap(1126, 273)
        sleep(0.5)
        tap(640, 1400)
        sleep(0.5)
    raw = capture_gfxinfo()
    tap(348, 2436)
    sleep(0.4)
    return parse_framestats(raw)

def run_rotation_while_running():
    go_to_main_set_timer()
    tap(640, 2464)
    sleep(0.5)
    reset_gfxinfo()
    adb_shell("settings", "put", "system", "user_rotation", "1")
    adb_shell("settings", "put", "system", "accelerometer_rotation", "0")
    sleep(2.5)
    adb_shell("settings", "put", "system", "user_rotation", "0")
    sleep(2.5)
    adb_shell("settings", "put", "system", "user_rotation", "1")
    sleep(2.5)
    adb_shell("settings", "put", "system", "user_rotation", "0")
    sleep(2.5)
    raw = capture_gfxinfo()
    tap(348, 2436)
    sleep(0.4)
    return parse_framestats(raw)

def run_timer_finished_alarm():
    go_to_main_set_timer()
    for _ in range(5):
        tap(640, 1940)
        sleep(0.1)
    for _ in range(3):
        tap(1012, 1225)
        sleep(0.1)
    sleep(0.3)
    reset_gfxinfo()
    tap(640, 2464)
    sleep(5.0)
    tap(640, 2400)
    sleep(1.0)
    raw = capture_gfxinfo()
    for _ in range(5):
        tap(640, 1225)
        sleep(0.1)
    for _ in range(3):
        tap(1012, 1940)
        sleep(0.1)
    sleep(0.3)
    return parse_framestats(raw)

def run_stopwatch_99_laps():
    go_to_main_set_timer()
    tap(823, 458)
    sleep(0.4)
    tap(1126, 1718)
    sleep(0.3)
    tap(640, 1718)
    sleep(0.3)
    for _ in range(99):
        tap(154, 1718)
        sleep(0.02)
    sleep(0.5)
    reset_gfxinfo()
    start_time = time.time()
    while time.time() - start_time < 15.0:
        swipe(640, 2500, 640, 2000, 150)
        sleep(0.4)
        swipe(640, 2000, 640, 2500, 150)
        sleep(0.4)
    raw = capture_gfxinfo()
    tap(640, 1718)
    sleep(0.3)
    tap(1126, 1718)
    sleep(0.3)
    tap(456, 458)
    sleep(0.4)
    return parse_framestats(raw)

def run_sound_screen_scroll():
    go_to_main_set_timer()
    tap(1126, 273)
    sleep(0.4)
    tap(600, 801)
    sleep(0.5)
    reset_gfxinfo()
    start_time = time.time()
    while time.time() - start_time < 15.0:
        swipe(640, 2200, 640, 600, 200)
        sleep(0.5)
        swipe(640, 600, 640, 2200, 200)
        sleep(0.5)
    raw = capture_gfxinfo()
    keyevent(4)
    sleep(0.4)
    keyevent(4)
    sleep(0.4)
    return parse_framestats(raw)

def run_cold_start():
    adb_shell("am", "force-stop", PKG)
    sleep(1.0)
    out = adb_shell("am", "start", "-W", "-n", f"{PKG}/.MainActivity")
    total_time = 0
    m = re.search(r"TotalTime:\s*(\d+)", out)
    if m:
        total_time = int(m.group(1))
    else:
        m2 = re.search(r"WaitTime:\s*(\d+)", out)
        if m2:
            total_time = int(m2.group(1))
    sleep(1.0)
    return total_time

def run_warm_start():
    keyevent(3)
    sleep(1.0)
    out = adb_shell("am", "start", "-W", "-n", f"{PKG}/.MainActivity")
    total_time = 0
    m = re.search(r"TotalTime:\s*(\d+)", out)
    if m:
        total_time = int(m.group(1))
    else:
        m2 = re.search(r"WaitTime:\s*(\d+)", out)
        if m2:
            total_time = int(m2.group(1))
    sleep(1.0)
    return total_time

def main():
    os.makedirs("qa/baseline_metrics", exist_ok=True)
    
    print("==================================================")
    print("STARTING COMPREHENSIVE BASELINE MEASUREMENTS")
    print("==================================================")
    
    results = {}
    
    print("\n[1/23] Measuring Cold Start (5 runs)...")
    cold_runs = []
    for r in range(5):
        t = run_cold_start()
        print(f"  Run {r+1}: {t} ms")
        cold_runs.append(t)
    cold_runs.sort()
    results["cold_start_ms"] = cold_runs[len(cold_runs) // 2]
    print(f"  Median Cold Start: {results['cold_start_ms']} ms")
    
    print("\n[2/23] Measuring Warm Start (5 runs)...")
    warm_runs = []
    for r in range(5):
        t = run_warm_start()
        print(f"  Run {r+1}: {t} ms")
        warm_runs.append(t)
    warm_runs.sort()
    results["warm_start_ms"] = warm_runs[len(warm_runs) // 2]
    print(f"  Median Warm Start: {results['warm_start_ms']} ms")
    
    scenarios = [
        ("timer_circle_running", run_timer_theme_scenario, ("Circle", 0)),
        ("timer_hourglass_running", run_timer_theme_scenario, ("Hourglass", 1)),
        ("timer_concentric_rings_running", run_timer_theme_scenario, ("Concentric Rings", 2)),
        ("timer_three_box_running", run_timer_theme_scenario, ("Three Box", 3)),
        ("timer_flip_clock_running", run_timer_theme_scenario, ("Flip Clock", 4)),
        ("timer_liquid_fill_running", run_timer_theme_scenario, ("Liquid Fill", 5)),
        ("timer_dot_grid_running", run_timer_theme_scenario, ("Dot Grid", 6)),
        ("stopwatch_circle_running", run_stopwatch_theme_scenario, ("Circle", 0)),
        ("stopwatch_concentric_rings_running", run_stopwatch_theme_scenario, ("Concentric Rings", 1)),
        ("stopwatch_three_box_running", run_stopwatch_theme_scenario, ("Three Box", 2)),
        ("stopwatch_flip_clock_running", run_stopwatch_theme_scenario, ("Flip Clock", 3)),
        ("gallery_swipes_timer", run_gallery_swipes_timer, ()),
        ("gallery_swipes_stopwatch", run_gallery_swipes_stopwatch, ()),
        ("wheel_picker_flings", run_wheel_picker_flings, ()),
        ("mode_switch_20x", run_mode_switch_20x, ()),
        ("settings_and_subpages", run_settings_and_subpages, ()),
        ("enter_exit_fullscreen", run_enter_exit_fullscreen, ()),
        ("rotation_while_running", run_rotation_while_running, ()),
        ("timer_finished_alarm", run_timer_finished_alarm, ()),
        ("stopwatch_99_laps", run_stopwatch_99_laps, ()),
        ("sound_screen_scroll", run_sound_screen_scroll, ())
    ]
    
    for i, (sname, sfunc, sargs) in enumerate(scenarios, start=3):
        print(f"\n[{i}/{len(scenarios)+2}] Scenario: {sname} (3 runs)...")
        runs = []
        for r in range(3):
            print(f"  Run {r+1}...", end="", flush=True)
            metrics = sfunc(*sargs)
            print(f" {metrics.get('total_frames', 0)} frames, jank: {metrics.get('janky_pct', 0)}%, p50: {metrics.get('p50_ms', 0)}ms, p90: {metrics.get('p90_ms', 0)}ms")
            runs.append(metrics)
        med = median_metric(runs)
        results[sname] = med
        print(f"  --> Median {sname}: Jank {med['janky_pct']}%, P50: {med['p50_ms']}ms, P90: {med['p90_ms']}ms, P99: {med['p99_ms']}ms, >16.6ms: {med['over_16_6ms']}, >8.3ms: {med['over_8_3ms']}")
        
        with open("qa/baseline_metrics/baseline_summary.json", "w") as f:
            json.dump(results, f, indent=2)
            
    print("\n==================================================")
    print("ALL BASELINE BENCHMARKS COMPLETED!")
    print("Results saved to qa/baseline_metrics/baseline_summary.json")
    print("==================================================")

if __name__ == "__main__":
    main()
