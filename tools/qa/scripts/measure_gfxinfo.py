#!/usr/bin/env python3
"""
Captures gfxinfo framestats for the VALUE app.
Usage: python measure_gfxinfo.py <scenario_name>
  - Resets gfxinfo, waits for user input, then captures.
"""
import subprocess
import sys
import re
import json
import os
from datetime import datetime

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"

def run_adb(*args):
    result = subprocess.run([ADB] + list(args), capture_output=True, text=True, timeout=10)
    return result.stdout + result.stderr

def reset_gfxinfo():
    run_adb("shell", "dumpsys", "gfxinfo", PKG, "reset")

def capture_gfxinfo():
    return run_adb("shell", "dumpsys", "gfxinfo", PKG, "framestats")

def parse_gfxinfo(output):
    """Parse gfxinfo output for key metrics."""
    metrics = {}
    
    # Parse summary section
    total_match = re.search(r"Total frames rendered:\s*(\d+)", output)
    janky_match = re.search(r"Janky frames:\s*(\d+)\s*\(([^)]+)%\)", output)
    p50_match = re.search(r"50th percentile:\s*(\d+)ms", output)
    p90_match = re.search(r"90th percentile:\s*(\d+)ms", output)
    p95_match = re.search(r"95th percentile:\s*(\d+)ms", output)
    p99_match = re.search(r"99th percentile:\s*(\d+)ms", output)
    missed_vsync = re.search(r"Number Missed Vsync:\s*(\d+)", output)
    high_input = re.search(r"Number High input latency:\s*(\d+)", output)
    slow_ui = re.search(r"Number Slow UI thread:\s*(\d+)", output)
    slow_bitmap = re.search(r"Number Slow bitmap uploads:\s*(\d+)", output)
    slow_issue = re.search(r"Number Slow issue draw commands:\s*(\d+)", output)
    gpu_match = re.search(r"Number GPU completion deadline missed:\s*(\d+)", output)
    
    if total_match: metrics["total_frames"] = int(total_match.group(1))
    if janky_match:
        metrics["janky_frames"] = int(janky_match.group(1))
        metrics["janky_pct"] = float(janky_match.group(2))
    if p50_match: metrics["p50_ms"] = int(p50_match.group(1))
    if p90_match: metrics["p90_ms"] = int(p90_match.group(1))
    if p95_match: metrics["p95_ms"] = int(p95_match.group(1))
    if p99_match: metrics["p99_ms"] = int(p99_match.group(1))
    if missed_vsync: metrics["missed_vsync"] = int(missed_vsync.group(1))
    if high_input: metrics["high_input_latency"] = int(high_input.group(1))
    if slow_ui: metrics["slow_ui_thread"] = int(slow_ui.group(1))
    if slow_bitmap: metrics["slow_bitmap_uploads"] = int(slow_bitmap.group(1))
    if slow_issue: metrics["slow_draw_commands"] = int(slow_issue.group(1))
    if gpu_match: metrics["gpu_deadline_missed"] = int(gpu_match.group(1))
    
    # Parse framestats lines
    frame_times = []
    in_framestats = False
    for line in output.split("\n"):
        if "---PROFILEDATA---" in line:
            in_framestats = not in_framestats
            continue
        if in_framestats and not line.startswith("Flags,"):
            parts = line.strip().split(",")
            if len(parts) >= 14:
                try:
                    # IntendedVsync to FrameCompleted (columns 1 and 13, in ns)
                    intended_vsync = int(parts[1])
                    frame_completed = int(parts[13])
                    frame_time_ms = (frame_completed - intended_vsync) / 1_000_000
                    if frame_time_ms > 0 and frame_time_ms < 10000:  # sanity
                        frame_times.append(frame_time_ms)
                except (ValueError, IndexError):
                    pass
    
    if frame_times:
        frame_times.sort()
        metrics["framestats_count"] = len(frame_times)
        metrics["over_8.3ms"] = sum(1 for f in frame_times if f > 8.3)
        metrics["over_11.1ms"] = sum(1 for f in frame_times if f > 11.1)
        metrics["over_16.6ms"] = sum(1 for f in frame_times if f > 16.6)
        metrics["over_33.3ms"] = sum(1 for f in frame_times if f > 33.3)
        n = len(frame_times)
        metrics["fs_p50"] = round(frame_times[int(n * 0.5)], 2)
        metrics["fs_p90"] = round(frame_times[int(n * 0.9)], 2)
        metrics["fs_p95"] = round(frame_times[int(n * 0.95)], 2)
        metrics["fs_p99"] = round(frame_times[min(int(n * 0.99), n - 1)], 2)
        metrics["fs_max"] = round(max(frame_times), 2)
        metrics["worst_10"] = [round(f, 2) for f in frame_times[-10:]]
    
    return metrics

def main():
    scenario = sys.argv[1] if len(sys.argv) > 1 else "unnamed"
    
    print(f"\n=== Scenario: {scenario} ===")
    print("Resetting gfxinfo...")
    reset_gfxinfo()
    
    input(">>> Perform the scenario on the device, then press ENTER to capture... ")
    
    print("Capturing gfxinfo...")
    raw = capture_gfxinfo()
    
    # Save raw output
    os.makedirs("qa/gfxinfo", exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    raw_file = f"qa/gfxinfo/{scenario}_{timestamp}_raw.txt"
    with open(raw_file, "w") as f:
        f.write(raw)
    
    metrics = parse_gfxinfo(raw)
    metrics["scenario"] = scenario
    metrics["timestamp"] = timestamp
    
    # Save parsed metrics
    json_file = f"qa/gfxinfo/{scenario}_{timestamp}.json"
    with open(json_file, "w") as f:
        json.dump(metrics, f, indent=2)
    
    # Print summary
    print(f"\n--- Results: {scenario} ---")
    for k, v in metrics.items():
        if k not in ("worst_10", "scenario", "timestamp"):
            print(f"  {k}: {v}")
    if "worst_10" in metrics:
        print(f"  worst_10: {metrics['worst_10']}")
    print(f"\nSaved: {raw_file}")
    print(f"Saved: {json_file}")

if __name__ == "__main__":
    main()

