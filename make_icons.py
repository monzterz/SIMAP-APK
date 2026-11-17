#!/usr/bin/env python3
"""Generate SIMAP launcher icons (adaptive + legacy + monochrome)."""
import os
from PIL import Image, ImageDraw

RES = "/home/lokotre/simap-android/app/src/main/res"

TOP = (13, 148, 136)    # teal #0d9488
BOT = (14, 165, 233)    # sky  #0ea5e9
WHITE = (255, 255, 255)

def gradient(w, h):
    img = Image.new("RGB", (1, h))
    for y in range(h):
        t = y / max(1, h - 1)
        img.putpixel((0, y), tuple(int(TOP[i] + (BOT[i] - TOP[i]) * t) for i in range(3)))
    return img.resize((w, h), Image.Resampling.LANCZOS)

def rounded_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m

def shield(draw, cx, cy, s, color):
    top = cy - s
    w = s * 0.72
    pts = [
        (cx, top),
        (cx + w, top + s * 0.30),
        (cx + w, cy + s * 0.28),
        (cx, cy + s),
        (cx - w, cy + s * 0.28),
        (cx - w, top + s * 0.30),
    ]
    draw.polygon(pts, fill=color)

def check(draw, cx, cy, s, color):
    p1 = (cx - s * 0.40, cy + s * 0.05)
    p2 = (cx - s * 0.12, cy - s * 0.22)
    p3 = (cx + s * 0.42, cy - s * 0.38)
    draw.line([p1, p2, p3], fill=color, width=max(2, int(s * 0.18)), joint="curve")

def make_background(size):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    img.paste(gradient(size, size), (0, 0))
    img.putalpha(rounded_mask(size, int(size * 0.25)))
    return img

def make_foreground(size):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = size * 0.30
    cx = cy = size // 2
    shield(d, cx, cy - size * 0.02, s, WHITE)
    check(d, cx, cy - size * 0.02, s, TOP)
    return img

def make_mono(size):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = size * 0.30
    shield(d, size // 2, size // 2, s, (0, 0, 0, 255))
    return img

# density qualifier -> pixel size
legacy = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
adaptive = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}

def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

for den, s in legacy.items():
    save(make_background(s), f"{RES}/mipmap-{den}/ic_launcher.png")
    save(make_background(s), f"{RES}/mipmap-{den}/ic_launcher_round.png")

for den, s in adaptive.items():
    save(make_foreground(s), f"{RES}/mipmap-{den}/ic_launcher_foreground.png")
    save(make_mono(s), f"{RES}/mipmap-{den}/ic_launcher_monochrome.png")

colors_xml = (
    '<resources>\n'
    '    <color name="ic_launcher_background">#0d9488</color>\n'
    '</resources>\n'
)
os.makedirs(f"{RES}/values", exist_ok=True)
with open(f"{RES}/values/colors.xml", "w") as f:
    f.write(colors_xml)

anydpi = f"{RES}/mipmap-anydpi-v26"
os.makedirs(anydpi, exist_ok=True)
for name in ("ic_launcher", "ic_launcher_round"):
    xml = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background"/>\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>\n'
        '    <monochrome android:drawable="@mipmap/ic_launcher_monochrome"/>\n'
        '</adaptive-icon>\n'
    )
    with open(f"{anydpi}/{name}.xml", "w") as f:
        f.write(xml)

print("icons generated")