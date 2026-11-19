#!/usr/bin/env python3
"""Refresh SIMAP-APK brand assets from the web project's official images."""
import os
from PIL import Image, ImageDraw

SRC = r"C:/xampp/htdocs/Proyek/assets/images"
RES = r"D:/Devs/SIMAP-APK/app/src/main/res"


def trim(im):
    im = im.convert("RGBA")
    bb = im.getbbox()
    return im.crop(bb) if bb else im


def fit_into(im, box_w, box_h):
    """Scale to fit inside box preserving aspect, high quality."""
    im = im.copy()
    im.thumbnail((box_w, box_h), Image.Resampling.LANCZOS)
    return im


def paste_center(canvas, im):
    x = (canvas.width - im.width) // 2
    y = (canvas.height - im.height) // 2
    canvas.alpha_composite(im, (x, y))


# ---- 1. Drawables (in-app logos) -------------------------------------------
os.makedirs(f"{RES}/drawable", exist_ok=True)

simap = trim(Image.open(f"{SRC}/simap.png"))
simap = fit_into(simap, 1000, 400)                       # ~734x182
simap.save(f"{RES}/drawable/simap.png")
print("drawable/simap.png", simap.size)

logoin = trim(Image.open(f"{SRC}/Logoin.png"))
logoin = fit_into(logoin, 1200, 600)                     # InJourney AIRPORTS wordmark
logoin.save(f"{RES}/drawable/logoin.png")
print("drawable/logoin.png", logoin.size)

# ---- 2. Launcher icons from loader-icon.png --------------------------------
mark = trim(Image.open(f"{SRC}/loader-icon.png"))
print("mark", mark.size)

WHITE = (255, 255, 255)


def rounded_legacy(size):
    """Legacy launcher icon: white rounded square + centered mark."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg = Image.new("RGBA", (size, size), WHITE + (255,))
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, size - 1, size - 1], radius=int(size * 0.22), fill=255
    )
    img.paste(bg, (0, 0), mask)
    m = fit_into(mark, int(size * 0.74), int(size * 0.74))
    paste_center(img, m)
    return img


def adaptive_foreground(size):
    """Adaptive foreground: mark inside the 66% safe zone."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    m = fit_into(mark, int(size * 0.66), int(size * 0.66))
    paste_center(img, m)
    return img


def adaptive_monochrome(size):
    """Monochrome layer: mark silhouette in black."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    m = fit_into(mark, int(size * 0.66), int(size * 0.66))
    alpha = m.split()[3].point(lambda a: 255 if a > 100 else 0)
    sil = Image.new("RGBA", m.size, (0, 0, 0, 255))
    sil.putalpha(alpha)
    paste_center(img, sil)
    return img


legacy = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
adaptive = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}

for den, s in legacy.items():
    d = f"{RES}/mipmap-{den}"
    os.makedirs(d, exist_ok=True)
    icon = rounded_legacy(s)
    icon.save(f"{d}/ic_launcher.png")
    icon.save(f"{d}/ic_launcher_round.png")

for den, s in adaptive.items():
    d = f"{RES}/mipmap-{den}"
    os.makedirs(d, exist_ok=True)
    adaptive_foreground(s).save(f"{d}/ic_launcher_foreground.png")
    adaptive_monochrome(s).save(f"{d}/ic_launcher_monochrome.png")

# adaptive background -> plain white
with open(f"{RES}/values/colors.xml", "w", encoding="utf-8") as f:
    f.write(
        "<resources>\n"
        '    <color name="ic_launcher_background">#FFFFFF</color>\n'
        "</resources>\n"
    )

# Play Store / legacy hi-res icon
rounded_legacy(512).save(r"D:/Devs/SIMAP-APK/ic_launcher-512.png")
print("launcher icons regenerated")
