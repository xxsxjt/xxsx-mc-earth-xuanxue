"""Lay out box UVs for a model spec and paint its texture.

Hand-packing UV offsets is how box models end up with smeared faces. This allocator takes
a part/box spec, reserves a rectangle for every box using vanilla's unwrap layout, emits
the Java ``texOffs`` values, and paints each face from a palette so the result is legible
in the offline preview.
"""

from __future__ import annotations

import json
import random
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


def shade(color: tuple[int, int, int], delta: int) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, c + delta)) for c in color) + (255,)


FACE_LIGHT = {"up": 20, "down": -30, "west": -8, "north": 6, "east": 10, "south": -14}


def paint_box(image: Image.Image, u: int, v: int, box: BoxSpec,
              base: tuple[int, int, int], accent: tuple[int, int, int]) -> None:
    draw = ImageDraw.Draw(image)
    w, h, d = int(box.w), int(box.h), int(box.d)
    rng = random.Random(sum(ord(c) for c in box.name))
    # Two-tone shading: dorsal darker, ventral lighter. A single flat fur colour made every
    # creature read as a white blob in the four-view sheets.
    dorsal = tuple(max(0, c - 46) for c in base)
    ventral = tuple(min(255, c + 34) for c in base)
    for face, rect in face_rects(u, v, w, h, d).items():
        x0, y0, x1, y1 = rect
        if x1 <= x0 or y1 <= y0:
            continue
        tone = shade(base, FACE_LIGHT[face])
        draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=tone)

        if box.style == "fur" and y1 - y0 >= 4:
            split = y0 + max(1, (y1 - y0) // 2)
            draw.rectangle((x0, y0, x1 - 1, split - 1),
                           fill=shade(dorsal, FACE_LIGHT[face] // 2))
            draw.rectangle((x0, split, x1 - 1, y1 - 1),
                           fill=shade(ventral, FACE_LIGHT[face] // 3))
            for y in range(y0 + 1, split, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(dorsal, FACE_LIGHT[face] // 2 + rng.choice((-16, 10))))
            for y in range(split + 1, y1 - 1, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(ventral, FACE_LIGHT[face] // 3 + rng.choice((-14, 12))))
        elif box.style == "fur":
            for y in range(y0 + 1, y1 - 1, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(base, FACE_LIGHT[face] + rng.choice((-18, 14))))

        if box.style == "crystal":
            for offset in range(-(y1 - y0), x1 - x0, 3):
                draw.line((x0 + offset, y1 - 1, x0 + offset + (y1 - y0), y0),
                          fill=shade(accent, 40))
            for offset in range(-(y1 - y0), x1 - x0, 6):
                draw.line((x0 + offset + 1, y1 - 1, x0 + offset + 1 + (y1 - y0), y0),
                          fill=shade(accent, -40))
        elif box.style == "metal":
            if y1 - y0 >= 4:
                mid = y0 + (y1 - y0) // 2
                draw.line((x0, mid, x1 - 1, mid), fill=shade(base, FACE_LIGHT[face] - 34))
                draw.line((x0, mid - 1, x1 - 1, mid - 1), fill=shade(base, FACE_LIGHT[face] + 26))
        elif box.style == "feather":
            for y in range(y0 + 1, y1 - 1, 2):
                draw.line((x0 + 1, y, x1 - 2, y - 1), fill=shade(accent, 8))
            for y in range(y0 + 2, y1 - 1, 4):
                draw.line((x0 + 2, y, x1 - 3, y - 1), fill=shade(base, FACE_LIGHT[face] - 26))

        # Paint facial features on skull north face so the face is unmistakable in Blockbench and in-game.
        if box.name == "skull" and face == "north" and (x1 - x0) >= 3 and (y1 - y0) >= 3:
            fw = x1 - x0
            fh = y1 - y0
            ey = y0 + max(1, fh // 3)
            lx = x0 + max(0, fw // 4)
            rx = x1 - 1 - max(0, fw // 4)
            eye_dark = (22, 26, 32, 255)
            eye_glow = shade(accent, 45)
            for ex in (lx, rx):
                draw.point((ex, ey), fill=eye_glow)
                if ey + 1 < y1:
                    draw.point((ex, ey + 1), fill=eye_dark)
            cx = x0 + fw // 2
            if fh >= 4 and fw >= 5:
                draw.point((cx, y0 + 1), fill=eye_glow)
                draw.point((cx, max(y0 + 1, ey - 1)), fill=shade(accent, 15))
        elif box.name in ("torso", "dome", "plate") and face in ("west", "east") and (x1 - x0) >= 6 and (y1 - y0) >= 4:
            fw = x1 - x0
            fh = y1 - y0
            my = y0 + fh // 2
            mx = x0 + fw // 2
            rune = shade(accent, 25)
            draw.line((x0 + 2, my, mx, max(y0 + 1, my - 1), x1 - 3, my), fill=rune)
            if fh >= 6:
                draw.point((mx, min(y1 - 2, my + 1)), fill=shade(accent, 45))

        # Dark bottom/right border plus light top/left: reads as an outline in game and
        # keeps adjacent parts visually separated.
        if x1 - x0 > 2 and y1 - y0 > 2:
            draw.line((x0, y0, x1 - 1, y0), fill=shade(base, FACE_LIGHT[face] + 30))
            draw.line((x0, y0, x0, y1 - 1), fill=shade(base, FACE_LIGHT[face] + 16))
            draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=shade(base, FACE_LIGHT[face] - 52))
            draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=shade(base, FACE_LIGHT[face] - 34))


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
    result = {}

    def walk(part: PartSpec):
        for box in part.boxes:
            uv = allocator.allocate(f"{part.name}.{box.name}",
                                    int(box.w), int(box.h), int(box.d))
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
    print(emit_java_offsets(specs))
    return {"size": size, "texture": name}
