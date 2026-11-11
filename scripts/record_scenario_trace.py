#!/usr/bin/env python3
"""
Records Perfetto traces while executing a specific scenario, then runs analyze_trace.
"""

import subprocess
import time
import os
import sys

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"

def adb_shell(*args, timeout=30):
    return subprocess.run([ADB, "shell"] + list(args), capture_output=True, text=True, timeout=timeout).stdout

def tap(x, y):
    adb_shell("input", "tap", str(x), str(y))

def swipe(x1, y1, x2, y2, d):
    adb_shell("input", "swipe", str(x1), str(y1), str(x2), str(y2), str(d))

def keyevent(c):
    adb_shell("input", "keyevent", str(c))

def sleep(s):
    time.sleep(s)

def record_scenario(name, action_func, duration_s=15):
    os.makedirs("qa/traces", exist_ok=True)
    trace_path_host = f"qa/traces/baseline_{name}.perfetto-trace"
    
    print(f"\n==================================================")
    print(f"RECORDING PERFETTO TRACE: {name} ({duration_s}s)")
    print(f"==================================================")
    
    # Start trace in background on device
    # Using simple atrace / perfetto command
    trace_cmd = (
        f"perfetto -o /data/misc/perfetto-traces/{name}.trace -t {duration_s}s "
        "-b 64mb sched am wm view gfx input res"
    )
    proc = subprocess.Popen([ADB, "shell", trace_cmd])
    sleep(1.0)
    
    # Run the scenario actions
    start_time = time.time()
    action_func(start_time, duration_s - 2)
    
    proc.wait()
    sleep(1.0)
    
    # Pull trace
    subprocess.run([ADB, "pull", f"/data/misc/perfetto-traces/{name}.trace", trace_path_host], capture_output=True)
    if os.path.exists(trace_path_host):
        size = os.path.getsize(trace_path_host)
        print(f"Trace saved: {trace_path_host} ({size} bytes)")
    else:
        print(f"Trace failed: {trace_path_host}")

def action_mode_switch(start_time, max_s):
    adb_shell("input", "keyevent", "224")
    tap(456, 458) # Timer tab
    sleep(0.3)
    tap(348, 2436) # Reset
    sleep(0.3)
    while time.time() - start_time < max_s:
        tap(823, 458) # Stopwatch tab
        sleep(0.4)
        tap(456, 458) # Timer tab
        sleep(0.4)

def action_settings_subpages(start_time, max_s):
    adb_shell("input", "keyevent", "224")
    tap(456, 458)
    sleep(0.3)
    while time.time() - start_time < max_s:
        tap(1126, 273) # Settings
        sleep(0.4)
        tap(600, 616) # Timer style
        sleep(0.5)
        keyevent(4) # Back
        sleep(0.4)
        tap(600, 801) # Sound
        sleep(0.5)
        keyevent(4) # Back
        sleep(0.4)
        keyevent(4) # Back to main
        sleep(0.4)

def action_wheel_flings(start_time, max_s):
    adb_shell("input", "keyevent", "224")
    tap(456, 458)
    sleep(0.3)
    tap(348, 2436)
    sleep(0.3)
    wheels_x = [268, 640, 1012]
    w_idx = 0
    while time.time() - start_time < max_s:
        wx = wheels_x[w_idx % 3]
        swipe(wx, 1750, wx, 1300, 120)
        sleep(0.6)
        swipe(wx, 1300, wx, 1750, 120)
        sleep(0.6)
        w_idx += 1

if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else "all"
    if target in ("all", "mode_switch"):
        record_scenario("mode_switch", action_mode_switch, 12)
    if target in ("all", "settings"):
        record_scenario("settings", action_settings_subpages, 12)
    if target in ("all", "wheel"):
        record_scenario("wheel", action_wheel_flings, 12)

