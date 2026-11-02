from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "xuanhuan_earth"


def color(value: str) -> tuple[int, int, int]:
    value = value.removeprefix("#")
    return tuple(int(value[index:index + 2], 16) for index in (0, 2, 4))


def shade(value: tuple[int, int, int], delta: float) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, int(round(channel + delta)))) for channel in value) + (255,)


def lerp_rgb(c0: tuple[int, int, int], c1: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    return (
        int(round(c0[0] + (c1[0] - c0[0]) * t)),
        int(round(c0[1] + (c1[1] - c0[1]) * t)),
        int(round(c0[2] + (c1[2] - c0[2]) * t)),
    )


class Atlas:
    FACE_LIGHT = {"up": 16, "down": -22, "west": -5, "north": 8, "east": 6, "south": -10}

    def __init__(self, size: tuple[int, int]) -> None:
        self.image = Image.new("RGBA", size, (0, 0, 0, 0))
        self.draw = ImageDraw.Draw(self.image)

    def surface(self, rect: tuple[int, int, int, int], base: tuple[int, int, int], face: str,
                style: str, accent: tuple[int, int, int] | None, seed: int) -> None:
        x0, y0, x1, y1 = rect
        fw = x1 - x0
        fh = y1 - y0
        if fw <= 0 or fh <= 0:
            return
        fl = self.FACE_LIGHT[face]
        acc = accent or base

        if style == "cloth":
            # Smooth vertical silk drapery folds + subtle golden hem trim
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    rx = px - x0
                    fold = math.sin(rx * 1.15 + ty * 1.6) * 9.0
                    grad = (0.5 - ty) * 14.0
                    self.draw.point((px, py), fill=shade(base, fl + fold + grad))
            if fh >= 4 and face in ("north", "south", "east", "west"):
                for px in range(x0, x1):
                    self.draw.point((px, y1 - 1), fill=shade(acc, fl + 12))

        elif style == "skin":
            # Soft warm skin shading with gentle cheek blush
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    tx = (px - x0) / max(1, fw - 1) if fw > 1 else 0.5
                    cyl = -8.0 * ((2.0 * tx - 1.0) ** 2) + (0.4 - ty) * 10.0
                    self.draw.point((px, py), fill=shade(base, fl + cyl))

        elif style == "hair":
            # Lustrous dark hair strands with sheen band
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                sheen = math.exp(-((ty - 0.35) ** 2) / 0.06) * 20.0
                for px in range(x0, x1):
                    strand = ((px - x0) % 2) * 5.0 - 2.5
                    self.draw.point((px, py), fill=shade(base, fl + sheen + strand))

        elif style == "crystal":
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                for px in range(x0, x1):
                    tx = (px - x0) / max(1, fw - 1) if fw > 1 else 0.5
                    ridge = (1.0 - abs(tx - 0.4) * 2.0) * 28.0
                    rgb = lerp_rgb(acc, base, ty * 0.6)
                    self.draw.point((px, py), fill=shade(rgb, fl * 0.5 + ridge))

        elif style == "metal":
            for py in range(y0, y1):
                ty = (py - y0) / max(1, fh - 1) if fh > 1 else 0.5
                spec = math.exp(-((ty - 0.4) ** 2) / 0.08) * 24.0
                for px in range(x0, x1):
                    self.draw.point((px, py), fill=shade(base, fl + spec - 6.0))

        else:
            for py in range(y0, y1):
                for px in range(x0, x1):
                    self.draw.point((px, py), fill=shade(base, fl))

    def box(self, uv: tuple[int, int], dims: tuple[float, float, float], base: str,
            style: str = "flat", accent: str | None = None, seed: int = 0) -> dict[str, tuple[int, int, int, int]]:
        u, v = uv
        width, height, depth = (max(1, int(round(value))) for value in dims)
        faces = {
            "up": (u + depth, v, u + depth + width, v + depth),
            "down": (u + depth + width, v, u + depth + width * 2, v + depth),
            "east": (u, v + depth, u + depth, v + depth + height),
            "north": (u + depth, v + depth, u + depth + width, v + depth + height),
            "west": (u + depth + width, v + depth, u + depth + width + depth, v + depth + height),
            "south": (u + depth + width + depth, v + depth,
                      u + depth + width + depth + width, v + depth + height),
        }
        base_color = color(base)
        accent_color = color(accent) if accent else None
        for index, (face, rect) in enumerate(faces.items()):
            self.surface(rect, base_color, face, style, accent_color, seed * 11 + index)
        return faces

    def save(self, name: str) -> None:
        out = ASSETS / "textures" / "entity" / f"{name}.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        self.image.save(out)


def point(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], x: int, y: int,
          fill: tuple[int, int, int, int]) -> None:
    x0, y0, x1, y1 = rect
    if 0 <= x < x1 - x0 and 0 <= y < y1 - y0:
        draw.point((x0 + x, y0 + y), fill=fill)


def line(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], points: list[tuple[int, int]],
         fill: tuple[int, int, int, int]) -> None:
    x0, y0, _, _ = rect
    draw.line([(x0 + x, y0 + y) for x, y in points], fill=fill, width=1)


def human_face(atlas: Atlas, rect: tuple[int, int, int, int], hair: str, eye: str,
               trim: str, beard: bool) -> None:
    dark_hair = shade(color(hair), -10)
    # Hairline & sideburns
    for x in range(0, 8):
        point(atlas.draw, rect, x, 0, dark_hair)
    for x in (0, 7):
        point(atlas.draw, rect, x, 1, dark_hair)
        point(atlas.draw, rect, x, 2, dark_hair)
    # Expressive slanted eyebrows
    for x in (1, 2, 5, 6):
        point(atlas.draw, rect, x, 2, dark_hair)
    # Sclera & iris
    sclera = shade(color("f7f3e6"), 0)
    iris = shade(color(eye), 16)
    pupil = shade(color("181b20"), 0)
    point(atlas.draw, rect, 1, 3, sclera)
    point(atlas.draw, rect, 2, 3, iris)
    point(atlas.draw, rect, 2, 4, pupil)
    point(atlas.draw, rect, 6, 3, sclera)
    point(atlas.draw, rect, 5, 3, iris)
    point(atlas.draw, rect, 5, 4, pupil)
    # Cinnabar/golden forehead cultivation mark
    point(atlas.draw, rect, 3, 1, shade(color(trim), 32))
    point(atlas.draw, rect, 4, 1, shade(color(trim), 32))
    # Nose bridge & lips
    point(atlas.draw, rect, 3, 5, shade(color("9c6d52"), 0))
    point(atlas.draw, rect, 4, 5, shade(color("9c6d52"), 0))
    if beard:
        for x, y in ((2, 6), (3, 7), (4, 7), (5, 6)):
            point(atlas.draw, rect, x, y, dark_hair)
    else:
        point(atlas.draw, rect, 3, 6, shade(color("b56557"), 6))
        point(atlas.draw, rect, 4, 6, shade(color("b56557"), 6))


def robe_front(atlas: Atlas, rect: tuple[int, int, int, int], trim: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    cx = width // 2
    trim_bright = shade(color(trim), 28)
    trim_mid = shade(color(trim), 8)
    # Cross-collar V-neck
    line(atlas.draw, rect, [(cx - 3, 0), (cx, 4), (cx + 3, 0)], trim_bright)
    line(atlas.draw, rect, [(cx - 2, 0), (cx, 3), (cx + 2, 0)], trim_mid)
    # Embroidered waist sash with central jade buckle
    if height > 7:
        for x in range(width):
            point(atlas.draw, rect, x, 7, shade(color("4e3924"), 0))
            point(atlas.draw, rect, x, 8, trim_mid)
        point(atlas.draw, rect, cx, 7, shade(color("78f0df"), 20))
        point(atlas.draw, rect, cx, 8, trim_bright)


def pack_back(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    if width >= 7 and height >= 7:
        cx, cy = width // 2, height // 2
        line(atlas.draw, rect, [(cx - 2, cy), (cx, cy - 2), (cx + 2, cy), (cx, cy + 2), (cx - 2, cy)],
             shade(color(accent), 26))


def build_settler(name: str, skin: str, hair: str, robe: str, trim: str, pack: str,
                  eye: str, beard: bool, seed: int) -> None:
    atlas = Atlas((128, 128))
    head = atlas.box((0, 0), (8, 8, 8), skin, "skin", None, seed)
    human_face(atlas, head["north"], hair, eye, trim, beard)
    atlas.box((32, 0), (9, 3, 9), hair, "hair", trim, seed + 1)
    atlas.box((70, 0), (8, 8, 2), hair, "hair", trim, seed + 2)
    atlas.box((92, 0), (4, 3, 4), hair, "hair", trim, seed + 3)
    # Jade crown & hairpin
    atlas.box((108, 0), (3, 2, 3), trim, "metal", "88f5e6", seed + 4)
    atlas.box((108, 6), (8, 1, 1), "62e6d6", "crystal", "b4fff6", seed + 5)

    atlas.box((0, 20), (8, 12, 4), robe, "cloth", trim, seed + 6)
    robe_faces = atlas.box((26, 20), (9, 12, 5), robe, "cloth", trim, seed + 7)
    robe_front(atlas, robe_faces["north"], trim)
    skirt = atlas.box((56, 20), (9, 8, 5), robe, "cloth", trim, seed + 8)
    line(atlas.draw, skirt["north"], [(1, 6), (7, 6)], shade(color(trim), 22))

    # Diagonal lapels, cloud pauldrons, jade pendant, side skirt flaps
    atlas.box((88, 20), (2, 9, 1), trim, "metal", robe, seed + 9)
    atlas.box((96, 20), (2, 9, 1), trim, "metal", robe, seed + 10)
    atlas.box((104, 20), (4, 2, 6), trim, "cloth", robe, seed + 11)
    atlas.box((104, 28), (4, 2, 6), trim, "cloth", robe, seed + 12)
    atlas.box((88, 32), (2, 6, 1), "4cdbc9", "crystal", "a8fff4", seed + 13)
    atlas.box((74, 42), (1, 8, 5), robe, "cloth", trim, seed + 14)
    atlas.box((88, 42), (1, 8, 5), robe, "cloth", trim, seed + 15)

    for uv in ((0, 42), (16, 42)):
        atlas.box(uv, (4, 12, 4), robe, "cloth", trim, seed + 16 + uv[0])
    for uv in ((32, 42), (52, 42)):
        atlas.box(uv, (5, 8, 5), robe, "cloth", trim, seed + 17 + uv[0])
    for uv in ((72, 60), (92, 60)):
        atlas.box(uv, (4, 5, 5), trim, "cloth", robe, seed + 18 + uv[0])
    for uv in ((0, 60), (16, 60)):
        atlas.box(uv, (4, 12, 4), robe, "cloth", trim, seed + 19 + uv[0])
    for uv in ((32, 60), (52, 60)):
        atlas.box(uv, (5, 5, 5), "342820", "metal", trim, seed + 20 + uv[0])

    merchant = atlas.box((0, 80), (10, 13, 5), pack, "metal", trim, seed + 21)
    pack_back(atlas, merchant["south"], trim)
    atlas.box((32, 80), (10, 4, 4), "6a543e", "cloth", trim, seed + 22)
    atlas.box((62, 80), (4, 6, 4), "d99a3c", "crystal", "ffe28a", seed + 23)
    spring = atlas.box((80, 80), (9, 9, 5), pack, "metal", trim, seed + 24)
    pack_back(atlas, spring["south"], trim)
    atlas.box((112, 80), (3, 7, 3), "48dcd0", "crystal", "a8fff8", seed + 25)
    steward = atlas.box((0, 104), (9, 12, 4), pack, "metal", trim, seed + 26)
    pack_back(atlas, steward["south"], trim)
    atlas.box((28, 104), (2, 12, 1), trim, "cloth", "f2d478", seed + 27)
    atlas.box((36, 104), (2, 20, 2), "583e2a", "metal", trim, seed + 28)
    atlas.box((46, 104), (4, 6, 4), "e8a844", "crystal", "fff0a6", seed + 29)
    atlas.save(name)


def main() -> None:
    build_settler("cultivation_settler_merchant", "d8a886", "22201e", "3e565e", "d8b25c", "5a4634", "2e8a82", True, 501)
    build_settler("cultivation_settler_spring_keeper", "dfb292", "202224", "2e6c68", "6ce0d2", "365852", "3aa69c", False, 531)
    build_settler("cultivation_settler_steward", "d4a280", "1c1e24", "283044", "e0b85c", "36343e", "c8963e", True, 561)


if __name__ == "__main__":
    main()
