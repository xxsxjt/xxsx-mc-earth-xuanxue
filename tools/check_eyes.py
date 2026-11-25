"""Verify each eye overlay's painted pixels fall inside the head's front-face rectangle."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'tools'))

import rebuild_beast_models as spec  # noqa: E402
from uv_layout import build, face_rects  # noqa: E402
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ENTITY = ROOT / 'neoforge-26.1.2' / 'src' / 'main' / 'resources' / 'assets' / 'earth_xuanxue' / 'textures' / 'entity'

TARGETS = {
    'spirit_fox': (spec.fox, 'head', 'skull'),
    'wind_wolf': (spec.wolf, 'head', 'skull'),
    'stonehorn_goat': (spec.goat, 'head', 'skull'),
    'crystal_turtle': (spec.turtle, 'head', 'skull'),
    'ember_crane': (spec.crane, 'head', 'skull'),
}


def find_box(parts, part_name, box_name):
    def walk(part):
        if part.name == part_name:
            for box in part.boxes:
                if box.name == box_name:
                    return box
        for child in part.children:
            found = walk(child)
            if found is not None:
                return found
        return None

    for part in parts:
        found = walk(part)
        if found is not None:
            return found
    return None


def main() -> None:
    for name, (factory, part_name, box_name) in TARGETS.items():
        size, parts, base, accent = factory()
        build(name, size, parts, base, accent)
        box = find_box(parts, part_name, box_name)
        if box is None:
            print(name, 'no head box')
            continue
        x0, y0, x1, y1 = face_rects(box.uv[0], box.uv[1], int(box.w), int(box.h), int(box.d))['north']

        eyes = Image.open(ENTITY / f'{name}_eyes.png').convert('RGBA')
        px = eyes.load()
        pts = [(x, y) for y in range(eyes.height) for x in range(eyes.width) if px[x, y][3] > 0]
        inside = [p for p in pts if x0 <= p[0] < x1 and y0 <= p[1] < y1]
        status = 'OK' if pts and len(inside) == len(pts) else 'FAIL'
        print(f'{name:<16} {status}  face=({x0},{y0})-({x1},{y1}) '
              f'eyes={len(pts)} inside={len(inside)}')


if __name__ == '__main__':
    main()
