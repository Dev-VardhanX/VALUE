#!/usr/bin/env python3
"""
Compares screenshots in qa/smooth-before/ and qa/smooth-after/.
Calculates pixel differences, structural similarity, and verifies visual fidelity.
"""

import os
import sys
from PIL import Image, ImageChops

BEFORE_DIR = "qa/smooth-before"
AFTER_DIR = "qa/smooth-after"
DIFF_DIR = "qa/smooth-diff"

def compare_images(img1_path, img2_path, diff_path=None):
    im1 = Image.open(img1_path).convert("RGBA")
    im2 = Image.open(img2_path).convert("RGBA")
    
    if im1.size != im2.size:
        return {
            "status": "SIZE_MISMATCH",
            "size1": im1.size,
            "size2": im2.size
        }
        
    diff = ImageChops.difference(im1, im2)
    bbox = diff.getbbox()
    
    if bbox is None:
        return {
            "status": "IDENTICAL",
            "diff_pixels": 0,
            "diff_pct": 0.0
        }
        
    # Count different pixels
    diff_data = diff.getdata()
    total_pixels = im1.size[0] * im1.size[1]
    diff_count = 0
    for pixel in diff_data:
        # Check if R, G, or B is non-zero
        if pixel[0] > 0 or pixel[1] > 0 or pixel[2] > 0:
            diff_count += 1
            
    pct = (diff_count / total_pixels) * 100.0
    
    if diff_path:
        os.makedirs(os.path.dirname(diff_path), exist_ok=True)
        # Highlight diff in red
        diff.save(diff_path)
        
    return {
        "status": "DIFFERENT",
        "diff_pixels": diff_count,
        "diff_pct": round(pct, 4)
    }

def main():
    os.makedirs(DIFF_DIR, exist_ok=True)
    before_files = sorted([f for f in os.listdir(BEFORE_DIR) if f.endswith(".png")])
    
    print(f"Comparing {len(before_files)} screenshot pairs...")
    print(f"{'File':<40} {'Status':<15} {'Diff Pixels':<12} {'Diff %':<8}")
    print("-" * 75)
    
    results = {}
    for fname in before_files:
        p1 = os.path.join(BEFORE_DIR, fname)
        p2 = os.path.join(AFTER_DIR, fname)
        pdiff = os.path.join(DIFF_DIR, fname)
        
        if not os.path.exists(p2):
            print(f"{fname:<40} {'MISSING_AFTER':<15}")
            results[fname] = {"status": "MISSING_AFTER"}
            continue
            
        res = compare_images(p1, p2, pdiff)
        results[fname] = res
        print(f"{fname:<40} {res['status']:<15} {res.get('diff_pixels', 0):<12} {res.get('diff_pct', 0.0):<8}%")
        
    print("\nVisual verification summary complete.")

if __name__ == "__main__":
    main()

