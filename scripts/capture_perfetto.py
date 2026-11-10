#!/usr/bin/env python3
"""
Captures a Perfetto system trace on Android for VALUE app.
"""

import subprocess
import time
import sys
import os

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PKG = "com.devvardhan.value"

PERFETTO_CONFIG = """
buffers: {
    size_kb: 65536
    fill_policy: RING_BUFFER
}
data_sources: {
    config {
        name: "linux.ftrace"
        ftrace_config {
            ftrace_events: "sched/sched_switch"
            ftrace_events: "sched/sched_wakeup"
            ftrace_events: "power/cpu_frequency"
            ftrace_events: "power/gpu_frequency"
            atrace_categories: "am"
            atrace_categories: "wm"
            atrace_categories: "view"
            atrace_categories: "input"
            atrace_categories: "gfx"
            atrace_categories: "sched"
            atrace_apps: "com.devvardhan.value"
        }
    }
}
data_sources: {
    config {
        name: "android.surfaceflinger.frame"
    }
}
duration_ms: 15000
"""

def capture_trace(output_path):
    print("Writing perfetto config to device...")
    subprocess.run([ADB, "shell", "echo", f"'{PERFETTO_CONFIG}'", ">", "/sdcard/perfetto.pbtx"])
    print("Starting 15s Perfetto trace...")
    res = subprocess.run([
        ADB, "shell",
        "perfetto", "--txt", "-c", "/sdcard/perfetto.pbtx", "-o", "/data/misc/perfetto-traces/trace.perfetto-trace"
    ], capture_output=True, text=True, timeout=30)
    print(res.stdout)
    if res.stderr:
        print("Stderr:", res.stderr)
        
    print("Pulling trace to host...")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    subprocess.run([ADB, "pull", "/data/misc/perfetto-traces/trace.perfetto-trace", output_path])
    if os.path.exists(output_path):
        size = os.path.getsize(output_path)
        print(f"Trace saved successfully: {output_path} ({size} bytes)")
    else:
        print("Failed to save trace.")

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "qa/traces/baseline_trace.perfetto-trace"
    capture_trace(out)

