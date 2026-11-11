#!/usr/bin/env python3
"""
Sustained 5-minute smoothness and stability test.
Runs a timer continuously for 5 minutes (300 seconds).
Records:
- dumpsys gfxinfo framestats over the entire 5 minutes
- dumpsys meminfo before and after (PSS, Native Heap, Dalvik Heap)
- dumpsys battery temperature and level before and after
- logcat GC count during the run
"""

import subprocess
import time
import re
import json
import os

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"
OUT_FILE = "qa/after_metrics/sustained_test_metrics.json"

def adb_shell(*args, timeout=30):
    cmd = [ADB, "shell"] + list(args)
    return subprocess.run(cmd, capture_output=True, text=True, timeout=timeout).stdout

def tap(x, y):
    adb_shell("input", "tap", str(x), str(y))

def keyevent(c):
    adb_shell("input", "keyevent", str(c))

def parse_meminfo(output):
    mem = {
        "total_pss_kb": 0,
        "native_heap_kb": 0,
        "dalvik_heap_kb": 0
    }
    m_pss = re.search(r"TOTAL PSS:\s*(\d+)", output)
    if m_pss:
        mem["total_pss_kb"] = int(m_pss.group(1))
    else:
        m_total = re.search(r"TOTAL\s+(\d+)", output)
        if m_total:
            mem["total_pss_kb"] = int(m_total.group(1))
            
    m_native = re.search(r"Native Heap\s+(\d+)", output)
    if m_native:
        mem["native_heap_kb"] = int(m_native.group(1))
        
    m_dalvik = re.search(r"Dalvik Heap\s+(\d+)", output)
    if m_dalvik:
        mem["dalvik_heap_kb"] = int(m_dalvik.group(1))
        
    return mem

def parse_battery(output):
    bat = {
        "level": 0,
        "temperature": 0.0,
        "status": ""
    }
    m_lvl = re.search(r"level:\s*(\d+)", output)
    if m_lvl:
        bat["level"] = int(m_lvl.group(1))
    m_temp = re.search(r"temperature:\s*(\d+)", output)
    if m_temp:
        bat["temperature"] = int(m_temp.group(1)) / 10.0
    m_stat = re.search(r"status:\s*(\d+)", output)
    if m_stat:
        bat["status"] = m_stat.group(1)
    return bat

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
    }
    
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
        metrics["parsed_total_frames"] = n
        metrics["p50_ms"] = round(frame_times[int(n * 0.50)], 2)
        metrics["p90_ms"] = round(frame_times[int(n * 0.90)], 2)
        metrics["p95_ms"] = round(frame_times[int(n * 0.95)], 2)
        metrics["p99_ms"] = round(frame_times[int(n * 0.99)], 2)
        metrics["max_ms"] = round(frame_times[-1], 2)
        metrics["over_16_6ms"] = sum(1 for f in frame_times if f > 16.6)
        metrics["over_11_1ms"] = sum(1 for f in frame_times if f > 11.1)
        metrics["over_8_3ms"] = sum(1 for f in frame_times if f > 8.3)
        
    return metrics

def main():
    os.makedirs(os.path.dirname(OUT_FILE), exist_ok=True)
    print("==================================================")
    print("STARTING 5-MINUTE SUSTAINED SMOOTHNESS BENCHMARK")
    print("==================================================")

    # Keep device awake & dismiss keyguard
    adb_shell("svc", "power", "stayon", "true")
    adb_shell("wm", "dismiss-keyguard")
    adb_shell("am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(1.0)

    # 1. Reset state and start a 10-minute timer
    tap(456, 458) # Timer tab
    time.sleep(0.3)
    tap(348, 2436) # Reset
    time.sleep(0.3)
    # Fling minute wheel up to ensure plenty of time (10+ min)
    adb_shell("input", "swipe", "640", "1500", "640", "1100", "200")
    time.sleep(0.5)
    
    # 2. Reset gfxinfo and clear logcat
    adb_shell("dumpsys", "gfxinfo", PKG, "reset")
    adb_shell("logcat", "-c")
    
    # 3. Record baseline memory & battery
    mem_before_raw = adb_shell("dumpsys", "meminfo", PKG)
    bat_before_raw = adb_shell("dumpsys", "battery")
    mem_before = parse_meminfo(mem_before_raw)
    bat_before = parse_battery(bat_before_raw)
    print(f"Initial Memory: PSS = {mem_before['total_pss_kb']} KB, Native = {mem_before['native_heap_kb']} KB, Dalvik = {mem_before['dalvik_heap_kb']} KB")
    print(f"Initial Battery: Level = {bat_before['level']}%, Temp = {bat_before['temperature']} C")

    # 4. Start timer!
    print("Starting timer...")
    tap(640, 2464) # Start
    time.sleep(1.0)
    
    # Verify running
    mode_raw = adb_shell("dumpsys", "window", "windows")
    m_mode = re.search(r"preferredDisplayMode=(\d+)", mode_raw)
    display_mode = m_mode.group(1) if m_mode else "unknown"
    print(f"Active Display Mode during run: {display_mode}")

    # 5. Run for 300 seconds (5 minutes)
    duration_s = 300
    start_time = time.time()
    print(f"Monitoring for {duration_s} seconds (5 minutes)...")
    while time.time() - start_time < duration_s:
        elapsed = int(time.time() - start_time)
        print(f"  Elapsed: {elapsed}/{duration_s} s...", end="\r", flush=True)
        # Keep screen on and awake
        keyevent(224)
        time.sleep(10)
    print("\n5 minutes completed!")

    # 6. Capture final metrics WHILE STILL RUNNING
    framestats_raw = adb_shell("dumpsys", "gfxinfo", PKG, "framestats")
    mem_after_raw = adb_shell("dumpsys", "meminfo", PKG)
    bat_after_raw = adb_shell("dumpsys", "battery")
    logcat_gc = adb_shell("logcat", "-d")
    
    # Now stop timer
    tap(348, 2436) # Reset
    
    framestats = parse_framestats(framestats_raw)
    mem_after = parse_meminfo(mem_after_raw)
    bat_after = parse_battery(bat_after_raw)
    
    # Count GC pauses in logcat
    gc_lines = [l for l in logcat_gc.split("\n") if "GC" in l and PKG in l]
    gc_count = len(gc_lines)
    
    results = {
        "duration_seconds": duration_s,
        "display_mode": display_mode,
        "framestats": framestats,
        "memory_before_kb": mem_before,
        "memory_after_kb": mem_after,
        "memory_delta_pss_kb": mem_after["total_pss_kb"] - mem_before["total_pss_kb"],
        "battery_before": bat_before,
        "battery_after": bat_after,
        "battery_temp_delta_c": round(bat_after["temperature"] - bat_before["temperature"], 1),
        "battery_level_delta_pct": bat_before["level"] - bat_after["level"],
        "gc_count_during_run": gc_count,
        "gc_sample_lines": gc_lines[:5]
    }
    
    with open(OUT_FILE, "w") as f:
        json.dump(results, f, indent=2)
        
    print("\n--- 5-Minute Sustained Test Results ---")
    print(f"Total Frames Rendered: {framestats.get('total_frames')}")
    print(f"Janky Frames: {framestats.get('janky_frames')} ({framestats.get('janky_pct')}%)")
    print(f"P50: {framestats.get('p50_ms')} ms, P90: {framestats.get('p90_ms')} ms, P99: {framestats.get('p99_ms')} ms")
    print(f"Memory PSS: {mem_before['total_pss_kb']} KB -> {mem_after['total_pss_kb']} KB (Delta: {results['memory_delta_pss_kb']} KB)")
    print(f"Battery Temp: {bat_before['temperature']} C -> {bat_after['temperature']} C (Delta: {results['battery_temp_delta_c']} C)")
    print(f"GC Events: {gc_count}")
    print(f"Results saved to {OUT_FILE}")

if __name__ == "__main__":
    main()

