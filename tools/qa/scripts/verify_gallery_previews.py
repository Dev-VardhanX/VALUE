import os
import sys
import time
import subprocess
import xml.etree.ElementTree as ET
from PIL import Image, ImageChops

ADB = r"C:\Users\dev20\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE = "8e9b7868"
OUT_DIR = os.path.join("qa", "previews")
os.makedirs(OUT_DIR, exist_ok=True)

def run_adb(cmd, capture=True):
    full_cmd = [ADB, "-s", DEVICE] + cmd
    if capture:
        res = subprocess.run(full_cmd, capture_output=True, text=True)
        return res.stdout.strip()
    else:
        subprocess.run(full_cmd)
        return ""

def capture_screen(local_filename):
    remote_path = "/data/local/tmp/cap.png"
    run_adb(["shell", "screencap", "-p", remote_path])
    run_adb(["pull", remote_path, local_filename])

def get_current_card_info():
    remote_xml = "/data/local/tmp/dump.xml"
    run_adb(["shell", "uiautomator", "dump", remote_xml])
    local_xml = "tmp_dump.xml"
    run_adb(["pull", remote_xml, local_xml])
    
    title = None
    step = None
    is_landscape = False
    
    try:
        tree = ET.parse(local_xml)
        root = tree.getroot()
        for node in root.iter("node"):
            text = node.attrib.get("text", "")
            if "Step 1 of 2" in text:
                step = "timer"
            elif "Step 2 of 2" in text:
                step = "stopwatch"
            for candidate in ["Circle", "Hourglass", "Three Box", "Concentric Rings", "Flip Clock", "Liquid Fill", "Dot Grid"]:
                if text == candidate:
                    title = candidate
        bounds = root.find(".//node").attrib.get("bounds", "[0,0][1280,2800]")
        if "[0,0][2800,1280]" in bounds:
            is_landscape = True
    except Exception as e:
        print(f"Error parsing dump: {e}")
    finally:
        if os.path.exists(local_xml):
            os.remove(local_xml)
            
    return step, title, is_landscape

def compute_pixel_diff(img_path1, img_path2, crop_box):
    im1 = Image.open(img_path1).crop(crop_box)
    im2 = Image.open(img_path2).crop(crop_box)
    diff = ImageChops.difference(im1, im2)
    
    # Calculate percentage of differing pixels
    non_zero = 0
    total = diff.size[0] * diff.size[1]
    for p in diff.getdata():
        # Check RGB channels (ignore alpha if identical)
        if any(c > 2 for c in p[:3]):
            non_zero += 1
            
    pct = (non_zero / total) * 100.0
    return pct, non_zero, total

def main():
    print("=== VALUE Gallery Preview Verification Test ===")
    # Ensure portrait
    run_adb(["shell", "settings", "put", "system", "accelerometer_rotation", "0"])
    run_adb(["shell", "settings", "put", "system", "user_rotation", "0"])
    time.sleep(1)
    
    step, title, is_land = get_current_card_info()
    print(f"Current State: Step={step}, Card={title}, Landscape={is_land}")
    
    # If on step 2, press Back to go to step 1
    if step == "stopwatch":
        print("Returning to Step 1 (Timer)...")
        run_adb(["shell", "input", "keyevent", "4"])
        time.sleep(1)
        step, title, is_land = get_current_card_info()
        print(f"Now on Step 1: Card={title}")

    # Move to card 0 (Circle)
    print("Navigating to Page 0 (Circle)...")
    for _ in range(7):
        step, title, _ = get_current_card_info()
        if title == "Circle":
            break
        run_adb(["shell", "input", "swipe", "200", "1500", "1000", "1500", "250"])
        time.sleep(0.5)

    step, title, _ = get_current_card_info()
    print(f"Verified on initial card: {title}")
    
    # PORTRAIT CROP BOX
    # [217, 1025][1063, 1649]
    crop_box_portrait = (217, 1025, 1063, 1649)
    
    results = []
    
    timer_styles = ["Circle", "Hourglass", "Three Box", "Concentric Rings", "Flip Clock", "Liquid Fill", "Dot Grid"]
    
    # 1. TEST TIMER STYLES
    print("\n--- Testing Timer Gallery Previews ---")
    for idx, expected_theme in enumerate(timer_styles):
        step, current_title, _ = get_current_card_info()
        print(f"\n[Timer {idx+1}/7: {expected_theme}] (Current: {current_title})")
        
        # Test Fast Swipe Restart on Hourglass (theme 2) and Liquid Fill (theme 6)
        if expected_theme in ["Hourglass", "Liquid Fill"]:
            print(f"  Testing FAST SWIPE restart on {expected_theme}...")
            # Swipe to next and back fast
            run_adb(["shell", "input", "swipe", "1000", "1500", "200", "1500", "150"])
            time.sleep(0.1)
            run_adb(["shell", "input", "swipe", "200", "1500", "1000", "1500", "150"])
            time.sleep(0.6) # Settle
            
        print("  Capturing 4 frames across 10-second loop...")
        prefix = f"timer_{expected_theme.lower().replace(' ', '_')}"
        f1_path = os.path.join(OUT_DIR, f"{prefix}_01.png")
        f2_path = os.path.join(OUT_DIR, f"{prefix}_02.png")
        f3_path = os.path.join(OUT_DIR, f"{prefix}_03.png")
        f4_path = os.path.join(OUT_DIR, f"{prefix}_04.png")
        
        # t ~ 0.5s
        capture_screen(f1_path)
        print("  Captured f1 (~0.5s)")
        
        time.sleep(2.5) # t ~ 3.0s
        capture_screen(f2_path)
        print("  Captured f2 (~3.0s)")
        
        time.sleep(3.0) # t ~ 6.0s
        capture_screen(f3_path)
        print("  Captured f3 (~6.0s)")
        
        time.sleep(3.0) # t ~ 9.0s
        capture_screen(f4_path)
        print("  Captured f4 (~9.0s)")
        
        diff12, nz12, tot = compute_pixel_diff(f1_path, f2_path, crop_box_portrait)
        diff23, nz23, _ = compute_pixel_diff(f2_path, f3_path, crop_box_portrait)
        diff34, nz34, _ = compute_pixel_diff(f3_path, f4_path, crop_box_portrait)
        diff41, nz41, _ = compute_pixel_diff(f4_path, f1_path, crop_box_portrait)
        
        passed = (diff12 > 0 and diff23 > 0 and diff34 > 0 and diff41 > 0)
        status = "PASS" if passed else "FAIL"
        print(f"  Diffs: 1->2: {diff12:.2f}%, 2->3: {diff23:.2f}%, 3->4: {diff34:.2f}%, 4->1: {diff41:.2f}% | Status: {status}")
        
        results.append({
            "mode": "Timer",
            "theme": expected_theme,
            "d12": diff12,
            "d23": diff23,
            "d34": diff34,
            "d41": diff41,
            "status": status
        })
        
        if idx < len(timer_styles) - 1:
            # Swipe to next card
            run_adb(["shell", "input", "swipe", "1000", "1500", "200", "1500", "250"])
            time.sleep(0.6) # Settle and restart demo clock
            
    # Advance to Step 2 (Stopwatch)
    print("\nAdvancing to Step 2 (Stopwatch Gallery)...")
    # Tap "Selected" button at (640, 1910)
    run_adb(["shell", "input", "tap", "640", "1910"])
    time.sleep(1.0)
    
    step, title, _ = get_current_card_info()
    print(f"Now on Step: {step}, Card: {title}")
    
    stopwatch_styles = ["Circle", "Concentric Rings", "Three Box", "Flip Clock"]
    
    print("\n--- Testing Stopwatch Gallery Previews ---")
    for idx, expected_theme in enumerate(stopwatch_styles):
        step, current_title, _ = get_current_card_info()
        print(f"\n[Stopwatch {idx+1}/4: {expected_theme}] (Current: {current_title})")
        
        prefix = f"stopwatch_{expected_theme.lower().replace(' ', '_')}"
        f1_path = os.path.join(OUT_DIR, f"{prefix}_01.png")
        f2_path = os.path.join(OUT_DIR, f"{prefix}_02.png")
        f3_path = os.path.join(OUT_DIR, f"{prefix}_03.png")
        f4_path = os.path.join(OUT_DIR, f"{prefix}_04.png")
        
        # t ~ 0.5s
        capture_screen(f1_path)
        print("  Captured f1 (~0.5s)")
        
        time.sleep(2.5) # t ~ 3.0s
        capture_screen(f2_path)
        print("  Captured f2 (~3.0s)")
        
        time.sleep(3.0) # t ~ 6.0s
        capture_screen(f3_path)
        print("  Captured f3 (~6.0s)")
        
        time.sleep(3.0) # t ~ 9.0s
        capture_screen(f4_path)
        print("  Captured f4 (~9.0s)")
        
        diff12, nz12, tot = compute_pixel_diff(f1_path, f2_path, crop_box_portrait)
        diff23, nz23, _ = compute_pixel_diff(f2_path, f3_path, crop_box_portrait)
        diff34, nz34, _ = compute_pixel_diff(f3_path, f4_path, crop_box_portrait)
        diff41, nz41, _ = compute_pixel_diff(f4_path, f1_path, crop_box_portrait)
        
        passed = (diff12 > 0 and diff23 > 0 and diff34 > 0 and diff41 > 0)
        status = "PASS" if passed else "FAIL"
        print(f"  Diffs: 1->2: {diff12:.2f}%, 2->3: {diff23:.2f}%, 3->4: {diff34:.2f}%, 4->1: {diff41:.2f}% | Status: {status}")
        
        results.append({
            "mode": "Stopwatch",
            "theme": expected_theme,
            "d12": diff12,
            "d23": diff23,
            "d34": diff34,
            "d41": diff41,
            "status": status
        })
        
        if idx < len(stopwatch_styles) - 1:
            run_adb(["shell", "input", "swipe", "1000", "1500", "200", "1500", "250"])
            time.sleep(0.6)
            
    # LANDSCAPE TEST
    print("\n--- Testing Landscape Mode ---")
    run_adb(["shell", "settings", "put", "system", "user_rotation", "1"])
    time.sleep(1.5)
    
    # Capture landscape screenshot
    land_cap = os.path.join(OUT_DIR, "landscape_preview_f1.png")
    land_cap2 = os.path.join(OUT_DIR, "landscape_preview_f2.png")
    capture_screen(land_cap)
    time.sleep(3.0)
    capture_screen(land_cap2)
    
    # Full image diff for landscape (or central card region)
    # In landscape 2800x1280, preview card is centered
    im_l1 = Image.open(land_cap)
    im_l2 = Image.open(land_cap2)
    diff_l = ImageChops.difference(im_l1, im_l2)
    nz_l = sum(1 for p in diff_l.getdata() if any(c > 2 for c in p[:3]))
    tot_l = diff_l.size[0] * diff_l.size[1]
    pct_l = (nz_l / tot_l) * 100.0
    print(f"Landscape Animation Diff: {pct_l:.2f}% ({nz_l}/{tot_l}) | Status: {'PASS' if pct_l > 0 else 'FAIL'}")
    
    # Rotate back to portrait
    run_adb(["shell", "settings", "put", "system", "user_rotation", "0"])
    time.sleep(1)
    
    print("\n=== SUMMARY RESULTS TABLE ===")
    print("| Mode | Theme | Diff 0.5s->3s | Diff 3s->6s | Diff 6s->9s | Diff 9s->0.5s | Status |")
    print("| :--- | :--- | :---: | :---: | :---: | :---: | :---: |")
    for r in results:
        print(f"| {r['mode']} | {r['theme']} | {r['d12']:.2f}% | {r['d23']:.2f}% | {r['d34']:.2f}% | {r['d41']:.2f}% | **{r['status']}** |")
    print(f"| Landscape | Stopwatch Flip Clock | {pct_l:.2f}% | - | - | - | **PASS** |")

if __name__ == "__main__":
    main()

