from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ENTITY = ROOT / "neoforge-26.1.2" / "src" / "main" / "resources" / "assets" / "earth_xuanxue" / "textures" / "entity"

# Head box UV origin, box dimensions, explicit eye columns (measured from the left edge of
# the north face), eye row, and glow colour. Explicit columns keep the pair symmetric even
# when the face width is odd.
SPECS = {
    "spirit_fox": ((0, 0), (8, 7, 7), (2, 5), 2, (126, 246, 234)),
    "wind_wolf": ((0, 0), (7, 7, 7), (1, 5), 2, (112, 228, 250)),
    "stonehorn_goat": ((0, 0), (9, 9, 8), (2, 6), 3, (255, 176, 66)),
    "crystal_turtle": ((0, 0), (6, 5, 6), (1, 4), 2, (140, 255, 244)),
    "ember_crane": ((18, 0), (5, 5, 5), (1, 3), 2, (255, 150, 84)),
}


def main() -> None:
    for name, ((u, v), (w, h, d), (ex, mx), ey, glow) in SPECS.items():
        source = Image.open(ENTITY / f"{name}.png").convert("RGBA")
        ox, oy = u + d, v + d
        out = Image.new("RGBA", source.size, (0, 0, 0, 0))
        core = tuple(min(255, c + 70) for c in glow)
        for x in (ex, mx):
            out.putpixel((ox + x, oy + ey), (*core, 255))
            out.putpixel((ox + x, oy + ey + 1), (*glow, 120))
        out.save(ENTITY / f"{name}_eyes.png")
        print(name, "face origin", (ox, oy), "eyes", (ex, mx), "row", ey)


if __name__ == "__main__":
    main()
