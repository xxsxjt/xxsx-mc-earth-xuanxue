"""Redraw the pill icons as round, shaded pills rather than square stickers.

The first attempt drew a 20px circle into a 32px canvas but the seam and rune made it read
as a square tile. This version keeps the silhouette circular, adds a proper specular
highlight and a soft rim, and makes the rune follow the curvature.
"""

from __future__ import annotations

import io
import json
import math
import os
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources')
TEX = ROOT / 'assets' / 'earth_xuanxue' / 'textures' / 'item'

COLORS = {
    'foundation_pill': ((212, 202, 232), (104, 70, 156)),
    'ironbody_pill': ((206, 184, 156), (120, 74, 40)),
    'cloudbreath_pill': ((190, 220, 232), (52, 116, 148)),
    'grainfast_pill': ((214, 206, 164), (124, 112, 52)),
    'nightsight_pill': ((202, 206, 172), (88, 100, 38)),
    'breakthrough_pill': ((238, 210, 248), (136, 62, 172)),
}


def shade(color, delta):
    return tuple(max(0, min(255, c + delta)) for c in color)


def draw_pill(base, accent) -> Image.Image:
    size = 32
    image = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    cx, cy, r = 16.0, 16.5, 11.0

    # Contact shadow, offset down.
    draw.ellipse((cx - r + 1, cy - r + 4, cx + r - 1, cy + r + 3), fill=(0, 0, 0, 64))

    # Body: horizontal scanlines with a spherical light from the upper left.
    for y in range(int(cy - r) - 1, int(cy + r) + 2):
        dy = (y + 0.5 - cy) / r
        if abs(dy) > 1.0:
            continue
        half = math.sqrt(max(0.0, 1.0 - dy * dy)) * r
        for x in range(int(cx - half) - 1, int(cx + half) + 2):
            dx = (x + 0.5 - cx) / r
            d = math.sqrt(dx * dx + dy * dy)
            if d > 1.0:
                continue
            # Light from upper-left: brightness falls off with distance from (-0.45,-0.55).
            lit = max(0.0, 1.0 - math.hypot(dx + 0.45, dy + 0.55) * 0.85)
            delta = int(-52 + lit * 92)
            draw.point((x, y), fill=(*shade(base, delta), 255))

    # Rim darkening.
    draw.ellipse((cx - r, cy - r, cx + r, cy + r),
                 outline=shade(base, -74) + (255,), width=1)

    # Rune: a vertical stroke with a chevron, clipped to the sphere.
    rune = shade(accent, 10)
    draw.line((cx, cy - 5, cx, cy + 4), fill=(*rune, 255), width=1)
    draw.line((cx - 3, cy - 1, cx, cy - 4), fill=(*rune, 255), width=1)
    draw.line((cx + 3, cy - 1, cx, cy - 4), fill=(*rune, 255), width=1)

    # Specular highlight.
    draw.ellipse((cx - 6, cy - 7, cx - 2, cy - 3), fill=(255, 255, 255, 130))
    draw.ellipse((cx - 5, cy - 6, cx - 4, cy - 5), fill=(255, 255, 255, 220))

    # Soften the outer edge so the disc does not look aliased.
    return image.filter(ImageFilter.SMOOTH)


def main() -> None:
    for name, (base, accent) in COLORS.items():
        draw_pill(base, accent).save(TEX / f'{name}.png')
        print('redrew', name)


if __name__ == '__main__':
    main()
