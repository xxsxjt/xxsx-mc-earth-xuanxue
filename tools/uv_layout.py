"""Lay out box UVs for a model spec and paint its texture with directional material shaders.

Every face is painted with smooth volumetric gradients, directional fur/feather strokes,
hexagonal tortoise scutes, or faceted crystal glows — never 1x1 checkerboards or harsh
black box borders.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ENTITY = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "xuanhuan_earth" / "textures" / "entity"
OUT = ROOT / "tmp" / "uv-layout"


@dataclass
class BoxSpec:
    name: str
    x: float
    y: float
    z: float
    w: float
    h: float
    d: float
    style: str = "fur"
    grow: float = 0.0
    uv: tuple[int, int] = (0, 0)


@dataclass
class PartSpec:
    name: str
    pivot: tuple[float, float, float]
    rot: tuple[float, float, float] = (0.0, 0.0, 0.0)
    boxes: list[BoxSpec] = field(default_factory=list)
    children: list["PartSpec"] = field(default_factory=list)
    parent: str | None = None


def box_extent(w: int, h: int, d: int) -> tuple[int, int]:
    return (2 * (w + d), d + h)


class Allocator:
    def __init__(self, size: int) -> None:
        self.size = size
        self.cursor_x = 0
        self.cursor_y = 0
        self.row_height = 0
        self.assigned: dict[str, tuple[int, int]] = {}

    def allocate(self, key: str, w: int, h: int, d: int) -> tuple[int, int]:
        bw, bh = box_extent(w, h, d)
        bw += 1
        bh += 1
        if self.cursor_x + bw > self.size:
            self.cursor_x = 0
            self.cursor_y += self.row_height
            self.row_height = 0
        if self.cursor_y + bh > self.size:
            raise RuntimeError(f"texture {self.size} too small at {key}")
        uv = (self.cursor_x, self.cursor_y)
        self.assigned[key] = uv
        self.cursor_x += bw
        self.row_height = max(self.row_height, bh)
        return uv


def face_rects(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    return {
        "up": (u + d, v, u + d + w, v + d),
        "down": (u + d + w, v, u + d + w + w, v + d),
        "east": (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "west": (u + d + w, v + d, u + d + w + d, v + d + h),
        "south": (u + d + w + d, v + d, u + d + w + d + w, v + d + h),
    }


def shade(color: tuple[int, int, int], delta: float) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, int(round(c + delta)))) for c in color) + (255,)


def lerp_rgb(c0: tuple[int, int, int], c1: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    return (
        int(round(c0[0] + (c1[0] - c0[0]) * t)),
        int(round(c0[1] + (c1[1] - c0[1]) * t)),
        int(round(c0[2] + (c1[2] - c0[2]) * t)),
    )


FACE_LIGHT = {"up": 18, "down": -22, "west": -4, "north": 8, "east": 6, "south": -10}


def paint_box(image: Image.Image, u: int, v: int, box: BoxSpec,
              base: tuple[int, int, int], accent: tuple[int, int, int]) -> None:
    draw = ImageDraw.Draw(image)
    w, h, d = int(round(box.w)), int(round(box.h)), int(round(box.d))
    dorsal = tuple(max(0, c - 26) for c in base)
    ventral = tuple(min(255, c + 36) for c in base)

    for face, rect in face_rects(u, v, w, h, d).items():
        x0, y0, x1, y1 = rect
        fw = x1 - x0
        fh = y1 - y0
        if fw <= 0 or fh <= 0:
            continue
        fl = FACE_LIGHT[face]

        if box.style == "fur":
            # Smooth dorsal-to-ventral gradient + soft directional hair streaks (3-4px strokes)
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                if face == "up":
                    row_rgb = dorsal
                elif face == "down":
                    row_rgb = ventral
                else:
                    row_rgb = lerp_rgb(dorsal, ventral, ty ** 0.9)
                for px in range(x0, x1):
                    tx = (px - x0) / max(1, fw - 1) if fw > 1 else 0.5
                    # Cylindrical volume falloff near edges
                    edge_V = -10.0 * ((2.0 * tx - 1.0) ** 2)
                    # Smooth directional fur wave along length
                    wave = math.sin((px - x0) * 0.9 + (py - y0) * 0.45) * 7.0
                    draw.point((px, py), fill=shade(row_rgb, fl + edge_V + wave))

        elif box.style == "fur_glow":
            # Tail tip / ear tip transitioning from fur base to glowing spirit flame accent
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    tx = (px - x0) / max(1, fw - 1) if fw > 1 else 0.5
                    t_glow = (ty * 0.65 + tx * 0.35) if face in ("east", "west") else ty
                    rgb = lerp_rgb(base, accent, min(1.0, t_glow * 1.15))
                    glow_boost = 28.0 * (t_glow ** 1.5)
                    draw.point((px, py), fill=shade(rgb, fl * 0.5 + glow_boost))

        elif box.style == "feather":
            # Layered overlapping plumage scallops with warm golden-ember tips
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    rel_x = px - x0
                    rel_y = py - y0
                    # Scallop feather barb pattern (3px wide, 3px tall staggered)
                    barb_phase = (rel_y + (rel_x // 2)) % 3
                    delta = fl + (12 if barb_phase == 0 else (2 if barb_phase == 1 else -10))
                    # Tip of wing/tail feathers gets rich ember-gold & dark ink contrast
                    if "vane" in box.name or "feather" in box.name:
                        if ty > 0.68:
                            rgb = lerp_rgb(accent, (36, 28, 32), (ty - 0.68) / 0.32)
                            delta += 10
                        elif ty > 0.42:
                            rgb = lerp_rgb(base, accent, (ty - 0.42) / 0.26)
                        else:
                            rgb = base
                    else:
                        rgb = lerp_rgb(base, shade(base, -14)[:3], ty * 0.5)
                    draw.point((px, py), fill=shade(rgb, delta))

        elif box.style == "feather_dark":
            # Obsidian-black secondary/tail tips with gold-crimson fire veins
            dark_feather = (34, 28, 36)
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    vein = abs((px - x0) - fw // 2) == 0 and (py - y0) % 2 == 0
                    rgb = lerp_rgb(accent, dark_feather, min(1.0, ty * 1.2))
                    if vein:
                        rgb = lerp_rgb(rgb, (255, 196, 84), 0.65)
                    draw.point((px, py), fill=shade(rgb, fl * 0.6))

        elif box.style == "crest_red":
            # Cinnabar-crimson crane crown with bright ruby center
            crimson = (212, 42, 38)
            bright_ruby = (255, 118, 72)
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    rgb = lerp_rgb(bright_ruby, crimson, ty)
                    draw.point((px, py), fill=shade(rgb, fl * 0.5 + 10))

        elif box.style == "shell":
            # Hexagonal tortoise scute plates with bevel highlights & glowing sutures
            for py in range(y0, y1):
                for px in range(x0, x1):
                    rx = px - x0
                    ry = py - y0
                    # Hex-like staggered scute grid (5x4 cells)
                    row_idx = ry // 4
                    col_shift = 2 if (row_idx & 1) else 0
                    cx = (rx + col_shift) % 5
                    cy = ry % 4
                    is_seam = (cx == 0 or cy == 0)
                    is_center = (cx in (2, 3) and cy == 2)
                    if is_seam:
                        # Subtle glowing cyan-jade suture between scutes
                        rgb = lerp_rgb(base, accent, 0.28)
                        draw.point((px, py), fill=shade(rgb, fl - 22))
                    elif is_center:
                        draw.point((px, py), fill=shade(base, fl + 22))
                    else:
                        bevel = 10 if (cy == 1 or cx == 1) else -6
                        draw.point((px, py), fill=shade(base, fl + bevel))

        elif box.style == "horn":
            # Ridged spiral mountain-ram horn with glowing golden ley-line cracks
            horn_base = (74, 66, 58)
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    ry = py - y0
                    rx = px - x0
                    # Annular growth rings every 2 rows
                    ring = 14 if (ry % 2 == 0) else -12
                    # Diagonal golden ley-line vein
                    is_vein = ((rx + ry) % 5 == 0) and (0 < rx < fw - 1)
                    if is_vein:
                        rgb = lerp_rgb(horn_base, accent, 0.78)
                        draw.point((px, py), fill=shade(rgb, 24))
                    else:
                        rgb = lerp_rgb(horn_base, (46, 40, 36), ty * 0.5)
                        draw.point((px, py), fill=shade(rgb, fl + ring))

        elif box.style == "stone":
            # Layered craggy mountain armor plate with warm amber veins
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    rx = px - x0
                    ry = py - y0
                    strata = int(math.sin(rx * 0.7 + ry * 1.3) * 12)
                    if (rx + ry * 2) % 9 == 0 and fw >= 4:
                        rgb = lerp_rgb(base, accent, 0.55)
                        draw.point((px, py), fill=shade(rgb, fl + 16))
                    else:
                        draw.point((px, py), fill=shade(base, fl + strata - int(ty * 10)))

        elif box.style == "crystal":
            # Faceted spirit crystal with bright core, longitudinal bevels, and violet-cyan gradient
            violet_tint = (max(40, accent[0] - 20), max(80, int(accent[1] * 0.72)), min(255, accent[2] + 25))
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    tx = (px - x0) / max(1, fw - 1) if fw > 1 else 0.5
                    base_c = lerp_rgb(accent, violet_tint, ty * 0.65)
                    # Facet highlight down the center-left ridge
                    ridge = 1.0 - min(1.0, abs(tx - 0.35) * 2.5)
                    tip_glow = (1.0 - ty) * 26.0
                    delta = fl * 0.4 + ridge * 34.0 + tip_glow - 8.0
                    draw.point((px, py), fill=shade(base_c, delta))

        elif box.style == "metal":
            # Brushed bronze/spirit-iron sheen with specular horizon band
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                spec_band = math.exp(-((ty - 0.38) ** 2) / 0.06) * 28.0
                for px in range(x0, x1):
                    draw.point((px, py), fill=shade(base, fl + spec_band - 8.0))

        else:
            for py in range(y0, y1):
                for px in range(x0, x1):
                    draw.point((px, py), fill=shade(base, fl))

        # Subtle 1px soft bevel on large faces only (never dark cartoon frames!)
        if fw >= 5 and fh >= 5 and box.style not in ("crystal", "fur_glow"):
            for px in range(x0, x1):
                top_c = image.getpixel((px, y0))[:3]
                bot_c = image.getpixel((px, y1 - 1))[:3]
                draw.point((px, y0), fill=shade(top_c, 10))
                draw.point((px, y1 - 1), fill=shade(bot_c, -12))

        # Paint expressive facial features on skull north face
        if box.name == "skull" and face == "north" and fw >= 4 and fh >= 4:
            ey = y0 + max(1, fh // 3)
            lx = x0 + max(0, fw // 4)
            rx = x1 - 1 - max(0, fw // 4)
            eye_pupil = (18, 22, 28, 255)
            eye_glow = shade(accent, 52)
            eye_halo = shade(accent, 18)
            for ex in (lx, rx):
                draw.point((ex, ey), fill=eye_glow)
                if ey + 1 < y1:
                    draw.point((ex, ey + 1), fill=eye_pupil)
                if ex - 1 >= x0:
                    draw.point((ex - 1, ey), fill=eye_halo)
            # Forehead spirit mark
            cx = x0 + fw // 2
            if fh >= 5 and fw >= 5:
                draw.point((cx, y0 + 1), fill=eye_glow)
                draw.point((cx, max(y0 + 1, ey - 1)), fill=eye_halo)

        # Flowing spirit runes on torso / shell / armor flanks
        elif box.name in ("torso", "dome", "plate") and face in ("west", "east") and fw >= 7 and fh >= 4:
            my = y0 + fh // 2
            mx = x0 + fw // 2
            rune_core = shade(accent, 38)
            rune_soft = shade(lerp_rgb(base, accent, 0.55), 16)
            pts = [
                (x0 + 2, my),
                (x0 + max(2, fw // 3), max(y0 + 1, my - 1)),
                (mx, my),
                (x0 + min(fw - 2, (fw * 2) // 3), min(y1 - 2, my + 1)),
                (x1 - 3, max(y0 + 1, my - 1)),
            ]
            draw.line(pts, fill=rune_soft, width=1)
            draw.point((mx, my), fill=rune_core)
            if mx - 1 >= x0:
                draw.point((mx - 1, max(y0 + 1, my - 1)), fill=rune_core)


def emit_java_offsets(specs: list[PartSpec]) -> str:
    lines = []

    def walk(part: PartSpec, depth: int):
        pad = "    " * depth
        for box in part.boxes:
            lines.append(f'{pad}{part.name}.{box.name} uv=({box.uv[0]}, {box.uv[1]}) '
                         f'dims=({box.w:g}, {box.h:g}, {box.d:g})')
        for child in part.children:
            walk(child, depth + 1)

    for part in specs:
        walk(part, 0)
    return "\n".join(lines)


def build(name: str, size: int, specs: list[PartSpec],
          base: tuple[int, int, int], accent: tuple[int, int, int]) -> dict:
    allocator = Allocator(size)
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    def walk(part: PartSpec):
        for box in part.boxes:
            uv = allocator.allocate(f"{part.name}.{box.name}",
                                    int(round(box.w)), int(round(box.h)), int(round(box.d)))
            box.uv = uv
            paint_box(image, uv[0], uv[1], box, base, accent)
        for child in part.children:
            walk(child)

    for spec in specs:
        walk(spec)

    OUT.mkdir(parents=True, exist_ok=True)
    image.save(OUT / f"{name}.png")
    ENTITY.mkdir(parents=True, exist_ok=True)
    image.save(ENTITY / f"{name}.png")
    print("===", name, size)
    return {"size": size, "texture": name}
