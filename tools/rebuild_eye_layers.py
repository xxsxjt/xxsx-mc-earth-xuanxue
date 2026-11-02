"""Regenerate the emissive eye overlays against the rebuilt model UVs.

The eye layer must sample the exact same face rectangle the body texture uses, so the eye
pixels are read back from the newly painted body texture instead of hard-coded offsets.
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import rebuild_beast_models as spec  # noqa: E402
from uv_layout import face_rects  # noqa: E402
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ENTITY = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "xuanhuan_earth" / "textures" / "entity"

# model name -> (factory, head part path, skull box name, glow colour)
TARGETS = {
    "spirit_fox": (spec.fox, "head", "skull", (120, 246, 234)),
    "wind_wolf": (spec.wolf, "head", "skull", (112, 228, 250)),
    "stonehorn_goat": (spec.goat, "head", "skull", (255, 176, 66)),
    "crystal_turtle": (spec.turtle, "head", "skull", (140, 255, 244)),
    "ember_crane": (spec.crane, "head", "skull", (255, 150, 84)),
}


def find_box(parts, part_name: str, box_name: str):
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
    for name, (factory, part_name, box_name, glow) in TARGETS.items():
        size, parts, base, accent = factory()
        # The UVs are assigned during build(); run it against a throwaway copy first so the
        # spec objects in this call carry the same offsets the model file was generated with.
        from uv_layout import build
        build(name, size, parts, base, accent)

        box = find_box(parts, part_name, box_name)
        if box is None:
            print(name, 'box not found')
            continue

        rect = face_rects(box.uv[0], box.uv[1], int(box.w), int(box.h), int(box.d))['north']
        x0, y0, x1, y1 = rect
        width = x1 - x0
        height = y1 - y0

        out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        core = tuple(min(255, c + 60) for c in glow)
        # Two eyes, symmetric about the face centre, one third down the face.
        eye_y = y0 + max(1, height // 3)
        left_x = x0 + max(0, width // 4)
        right_x = x1 - 1 - max(0, width // 4)
        for x in (left_x, right_x):
            out.putpixel((x, eye_y), (*core, 255))
            if eye_y + 1 < y1:
                out.putpixel((x, eye_y + 1), (*glow, 120))
        out.save(ENTITY / f"{name}_eyes.png")
        print(name, 'face', rect, 'eyes at', (left_x, right_x), 'row', eye_y)


if __name__ == '__main__':
    main()
