import os
import sys
import time
import subprocess
import xml.etree.ElementTree as ET

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE = "8e9b7868"
OUT_DIR = os.path.join("qa", "cleanup-before")
os.makedirs(OUT_DIR, exist_ok=True)

def run_adb(args):
    cmd = [ADB, "-s", DEVICE] + args
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

def capture(name):
    remote = "/data/local/tmp/screen.png"
    local = os.path.join(OUT_DIR, f"{name}.png")
    run_adb(["shell", "screencap", "-p", remote])
    run_adb(["pull", remote, local])
    print(f"Captured: {local}")

def rotate(orientation): # 0 = portrait, 1 = landscape
    run_adb(["shell", "settings", "put", "system", "accelerometer_rotation", "0"])
    run_adb(["shell", "settings", "put", "system", "user_rotation", str(orientation)])
    time.sleep(1.2)

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(0.5)

def keyevent(key):
    run_adb(["shell", "input", "keyevent", str(key)])
    time.sleep(0.5)

def swipe(x1, y1, x2, y2, duration=250):
    run_adb(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])
    time.sleep(0.6)

def dump_ui():
    remote = "/data/local/tmp/dump.xml"
    local = "temp_dump.xml"
    run_adb(["shell", "uiautomator", "dump", remote])
    run_adb(["pull", remote, local])
    try:
        tree = ET.parse(local)
        return tree.getroot()
    except Exception as e:
        print(f"Dump parse error: {e}")
        return None
    finally:
        if os.path.exists(local):
            os.remove(local)

def find_node(root, text=None, content_desc=None):
    if root is None:
        return None
    for n in root.iter("node"):
        if text and text in n.attrib.get("text", ""):
            return n
        if content_desc and content_desc in n.attrib.get("content-desc", ""):
            return n
    return None

def tap_node(node):
    if node is None:
        return False
    bounds = node.attrib.get("bounds", "") # [l,t][r,b]
    if not bounds:
        return False
    parts = bounds.replace("][", ",").replace("[", "").replace("]", "").split(",")
    l, t, r, b = [int(p) for p in parts]
    cx = (l + r) // 2
    cy = (t + b) // 2
    tap(cx, cy)
    return True

def tap_text(text):
    root = dump_ui()
    node = find_node(root, text=text)
    if node is not None:
        return tap_node(node)
    print(f"Could not find text: {text}")
    return False

def tap_desc(desc):
    root = dump_ui()
    node = find_node(root, content_desc=desc)
    if node is not None:
        return tap_node(node)
    print(f"Could not find desc: {desc}")
    return False

def main():
    print("=== CAPTURING BASELINE SCREENSHOTS (STEP 0) ===")
    rotate(0)
    run_adb(["shell", "am", "start", "-n", "com.devvardhan.value/.MainActivity"])
    time.sleep(1.5)

    # 1. SET TIMER SCREEN
    print("\n--- 1. Set Timer Screen ---")
    rotate(0)
    capture("set_timer_portrait")
    rotate(1)
    capture("set_timer_landscape")
    rotate(0)

    # 2. SETTINGS SCREEN
    print("\n--- 2. Settings Screen ---")
    tap(1126, 273) # Settings icon
    time.sleep(1.0)
    rotate(0)
    capture("settings_portrait")
    rotate(1)
    capture("settings_landscape")
    rotate(0)

    # 3. SOUND SCREEN
    print("\n--- 3. Sound Screen ---")
    tap_text("Alarm sound")
    time.sleep(1.0)
    rotate(0)
    capture("sound_portrait")
    rotate(1)
    capture("sound_landscape")
    rotate(0)
    keyevent(4) # Back to Settings
    time.sleep(0.5)

    # 4. STYLE GALLERIES
    print("\n--- 4. Style Galleries ---")
    tap_text("Timer style")
    time.sleep(1.0)
    rotate(0)
    capture("gallery_timer_portrait")
    rotate(1)
    capture("gallery_timer_landscape")
    rotate(0)
    keyevent(4) # Back to Settings
    time.sleep(0.5)

    tap_text("Stopwatch style")
    time.sleep(1.0)
    rotate(0)
    capture("gallery_stopwatch_portrait")
    rotate(1)
    capture("gallery_stopwatch_landscape")
    rotate(0)
    keyevent(4) # Back to Settings
    time.sleep(0.5)
    keyevent(4) # Back to Main
    time.sleep(0.5)

    # 5. TIMER 7 STYLES RUNNING (PAUSED AT SAME FIXED VALUE)
    timer_styles = ["Circle", "Hourglass", "Three Box", "Concentric Rings", "Flip Clock", "Liquid Fill", "Dot Grid"]
    for s_idx, style_name in enumerate(timer_styles):
        print(f"\n--- Timer Style: {style_name} ---")
        # Go to Settings -> Timer style
        tap(1126, 273) # Settings
        time.sleep(0.8)
        tap_text("Timer style")
        time.sleep(0.8)
        # Find style card by swiping
        for _ in range(7):
            root = dump_ui()
            n = find_node(root, text=style_name)
            if n is not None:
                # Select it
                tap_text("Select") or tap_text("Selected")
                time.sleep(0.8)
                break
            swipe(1000, 1500, 200, 1500, 250)
            time.sleep(0.5)
        # Now back at Settings, back to Main
        keyevent(4)
        time.sleep(0.8)
        
        # Start timer (default 00:00:23 from wheel, or let's start and immediately pause)
        tap_text("Start") or tap(640, 2450)
        time.sleep(1.2)
        # Pause timer (Pause button is center)
        tap(640, 2450)
        time.sleep(0.5)
        
        # Capture Running Timer (Paused)
        prefix = f"timer_{style_name.lower().replace(' ', '_')}"
        rotate(0)
        capture(f"{prefix}_portrait")
        rotate(1)
        capture(f"{prefix}_landscape")
        rotate(0)
        
        # Fullscreen (tap fullscreen icon next to gear: ~960, 273)
        tap(960, 273)
        time.sleep(1.0)
        capture(f"{prefix}_fullscreen_portrait")
        rotate(1)
        capture(f"{prefix}_fullscreen_landscape")
        rotate(0)
        # Tap screen to exit fullscreen
        tap(640, 1400)
        time.sleep(0.8)
        
        # Reset timer
        # Reset button is left pill button (~280, 2450)
        tap(280, 2450)
        time.sleep(0.8)

    # 6. STOPWATCH 4 STYLES RUNNING (0 LAPS & 5 LAPS)
    print("\n--- Switching to Stopwatch Mode ---")
    tap(820, 450) # Stopwatch tab
    time.sleep(1.0)
    
    stopwatch_styles = ["Circle", "Concentric Rings", "Three Box", "Flip Clock"]
    for s_idx, style_name in enumerate(stopwatch_styles):
        print(f"\n--- Stopwatch Style: {style_name} ---")
        tap(1126, 273) # Settings
        time.sleep(0.8)
        tap_text("Stopwatch style")
        time.sleep(0.8)
        for _ in range(5):
            root = dump_ui()
            n = find_node(root, text=style_name)
            if n is not None:
                tap_text("Select") or tap_text("Selected")
                time.sleep(0.8)
                break
            swipe(1000, 1500, 200, 1500, 250)
            time.sleep(0.5)
        keyevent(4) # back to Main
        time.sleep(0.8)

        # 0 Laps: Start, wait 1s, pause
        tap(640, 2450) # Start
        time.sleep(1.2)
        tap(640, 2450) # Pause
        time.sleep(0.5)
        
        prefix = f"stopwatch_{style_name.lower().replace(' ', '_')}"
        rotate(0)
        capture(f"{prefix}_0laps_portrait")
        rotate(1)
        capture(f"{prefix}_0laps_landscape")
        rotate(0)
        
        # Add 5 Laps (Resume, tap Lap 5 times, Pause)
        tap(640, 2450) # Resume
        time.sleep(0.3)
        for _ in range(5):
            tap(1000, 2450) # Flag / Lap button on right
            time.sleep(0.3)
        tap(640, 2450) # Pause
        time.sleep(0.5)
        
        capture(f"{prefix}_5laps_portrait")
        rotate(1)
        capture(f"{prefix}_5laps_landscape")
        rotate(0)
        
        # Reset stopwatch
        tap(280, 2450) # Reset button on left
        time.sleep(0.8)

    # 7. FINISHED TIMER SCREEN
    print("\n--- 7. Finished Timer Screen ---")
    tap(450, 450) # Timer tab
    time.sleep(0.8)
    # Set wheel to 00:00:02
    # Increment seconds 2 times
    tap(981, 1225) # Increment seconds
    time.sleep(0.2)
    tap(981, 1225)
    time.sleep(0.2)
    tap(640, 2450) # Start
    print("Waiting 4s for timer to finish...")
    time.sleep(4.0)
    rotate(0)
    capture("timer_finished_portrait")
    rotate(1)
    capture("timer_finished_landscape")
    rotate(0)
    # Dismiss alarm
    tap(640, 2450) # Dismiss
    time.sleep(1.0)

    rotate(0)
    print("\n=== STEP 0 BASELINE CAPTURE COMPLETED SUCCESSFULLY ===")

if __name__ == "__main__":
    main()

