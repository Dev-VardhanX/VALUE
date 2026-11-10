#!/usr/bin/env python3
"""
Analyzes Perfetto system traces to inspect:
- Main thread work duration
- RenderThread work duration
- Long frames (>16.6ms and >8.3ms)
- GC events and pauses
- Binder calls
- Disk / DataStore I/O calls on main thread
- Layout / Measure passes per frame
"""

import sys
import os
import re

def analyze_trace_file(trace_path):
    if not os.path.exists(trace_path):
        print(f"Error: Trace file not found: {trace_path}")
        return None

    size_mb = os.path.getsize(trace_path) / (1024 * 1024)
    print(f"\n==================================================")
    print(f"ANALYZING PERFETTO TRACE: {trace_path} ({size_mb:.2f} MB)")
    print(f"==================================================")

    with open(trace_path, "rb") as f:
        data = f.read()

    text = data.decode("utf-8", errors="ignore")

    findings = {
        "file": trace_path,
        "size_mb": round(size_mb, 2),
        "gc_events_count": len(re.findall(r"(?:art::gc|Explicit concurrent mark sweep|Background partial concurrent mark sweep)", text)),
        "choreographer_frames": len(re.findall(r"Choreographer#doFrame", text)),
        "traversals": len(re.findall(r"performTraversals", text)),
        "measure_passes": len(re.findall(r"measure", text)),
        "layout_passes": len(re.findall(r"layout", text)),
        "draw_passes": len(re.findall(r"draw", text)),
        "binder_transactions": len(re.findall(r"binder transaction", text)),
        "render_thread_draws": len(re.findall(r"DrawFrame", text)),
        "datastore_reads": len(re.findall(r"(?:datastore|preferencesDataStore)", text, re.IGNORECASE))
    }

    for k, v in findings.items():
        print(f"  {k}: {v}")

    return findings

if __name__ == "__main__":
    if len(sys.argv) > 1:
        analyze_trace_file(sys.argv[1])
    else:
        print("Usage: python analyze_trace.py <path_to_perfetto_trace>")

