from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "xuanhuan_earth"


def color(value: str) -> tuple[int, int, int]:
    value = value.removeprefix("#")
    return tuple(int(value[index:index + 2], 16) for index in (0, 2, 4))


def shade(value: tuple[int, int, int], delta: int) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, channel + delta)) for channel in value) + (255,)


class Atlas:
    FACE_LIGHT = {"up": 18, "down": -26, "west": -7, "north": 5, "east": 9, "south": -12}

    def __init__(self, size: tuple[int, int]) -> None:
        self.image = Image.new("RGBA", size, (0, 0, 0, 0))
        self.draw = ImageDraw.Draw(self.image)

    def surface(self, rect: tuple[int, int, int, int], base: tuple[int, int, int], face: str,
                style: str, accent: tuple[int, int, int] | None, seed: int) -> None:
        x0, y0, x1, y1 = rect
        if x1 <= x0 or y1 <= y0:
            return
        rng = random.Random(seed)
        self.draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=shade(base, self.FACE_LIGHT[face]))
        if x1 - x0 > 2 and y1 - y0 > 2:
            self.draw.line((x0, y0, x1 - 1, y0), fill=shade(base, 25))
            self.draw.line((x0, y0, x0, y1 - 1), fill=shade(base, 14))
            self.draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=shade(base, -28))
            self.draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=shade(base, -18))
        width = x1 - x0
        height = y1 - y0
        if style == "fur":
            for y in range(y0 + 2, y1 - 1, 3):
                for x in range(x0 + 1 + ((y - y0) & 1), x1 - 1, 4):
                    self.draw.point((x, y), fill=shade(base, rng.choice((-15, 13, 20))))
        elif style == "cloth":
            for y in range(y0 + 3, y1 - 1, 4):
                self.draw.line((x0 + 1, y, x1 - 2, y), fill=shade(base, -8))
            for x in range(x0 + 2, x1 - 1, 5):
                for y in range(y0 + 1, y1 - 1, 4):
                    self.draw.point((x, y), fill=shade(base, 12))
        elif style == "stone":
            for _ in range(max(1, width * height // 18)):
                x = rng.randrange(x0, x1)
                y = rng.randrange(y0, y1)
                self.draw.point((x, y), fill=shade(base, rng.choice((-24, -13, 16))))
        elif style == "metal":
            if height >= 5:
                y = y0 + height // 2
                self.draw.line((x0, y, x1 - 1, y), fill=shade(base, -24))
                self.draw.line((x0, y - 1, x1 - 1, y - 1), fill=shade(base, 15))
            for x, y in ((x0 + 1, y0 + 1), (x1 - 2, y0 + 1), (x0 + 1, y1 - 2), (x1 - 2, y1 - 2)):
                if x0 <= x < x1 and y0 <= y < y1:
                    self.draw.point((x, y), fill=shade(base, 28))
        elif style == "crystal":
            crystal = accent or shade(base, 35)[:3]
            for offset in range(-height, width, 4):
                self.draw.line((x0 + offset, y1 - 1, x0 + offset + height, y0), fill=shade(crystal, 22))
        elif style == "feather":
            feather = accent or shade(base, 20)[:3]
            for y in range(y0 + 2, y1, 3):
                self.draw.line((x0 + 1, y, x1 - 2, max(y0, y - 2)), fill=shade(feather, 7))

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


def wolf_face(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    bright = shade(color(accent), 25)
    dark = shade(color("1b252d"), 0)
    for x in (1, 5):
        point(atlas.draw, rect, x, 2, dark)
        point(atlas.draw, rect, x + 1, 2, bright)
    line(atlas.draw, rect, [(3, 0), (3, 1), (2, 2), (3, 3), (4, 2), (3, 1)], bright)


def muzzle(atlas: Atlas, rect: tuple[int, int, int, int]) -> None:
    dark = shade(color("172027"), 0)
    x0, y0, x1, y1 = rect
    cx = (x0 + x1) // 2
    atlas.draw.rectangle((max(x0, cx - 1), y0, min(x1 - 1, cx + 1), min(y1 - 1, y0 + 1)), fill=dark)


def side_rune(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    if width < 6 or height < 5:
        return
    points = [(1, height // 2), (3, 1), (width // 2, 2), (width // 2 + 1, height - 2),
              (width - 2, height // 2), (width - 4, 1)]
    line(atlas.draw, rect, points, shade(color(accent), 18))


def human_face(atlas: Atlas, rect: tuple[int, int, int, int], hair: str, eye: str, beard: bool) -> None:
    dark_hair = shade(color(hair), -12)
    for x in range(1, 7):
        point(atlas.draw, rect, x, 0, dark_hair)
    for x in (1, 2, 5, 6):
        point(atlas.draw, rect, x, 1, dark_hair)
    for x in (2, 5):
        point(atlas.draw, rect, x, 3, shade(color("f4f0db"), 0))
        point(atlas.draw, rect, x, 4, shade(color(eye), 12))
    if beard:
        for x, y in ((1, 6), (2, 6), (3, 7), (4, 7), (5, 6), (6, 6)):
            point(atlas.draw, rect, x, y, dark_hair)


def robe_front(atlas: Atlas, rect: tuple[int, int, int, int], trim: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    cx = width // 2
    trim_color = shade(color(trim), 12)
    line(atlas.draw, rect, [(cx - 2, 0), (cx, 3), (cx + 2, 0)], trim_color)
    if height > 7:
        line(atlas.draw, rect, [(0, 7), (width - 1, 7)], shade(color("6a4d2f"), 0))
        point(atlas.draw, rect, cx, 7, shade(color("d0af62"), 8))


def pack_back(atlas: Atlas, rect: tuple[int, int, int, int], accent: str, symbol: bool) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    for x in range(2, width - 1, 3):
        line(atlas.draw, rect, [(x, 1), (x, height - 2)], shade(color("3c2d20"), -5))
    if symbol and width >= 7 and height >= 7:
        cx, cy = width // 2, height // 2
        line(atlas.draw, rect, [(cx - 2, cy), (cx, cy - 2), (cx + 2, cy), (cx, cy + 2), (cx - 2, cy)],
             shade(color(accent), 14))


def build_wind_wolf() -> None:
    atlas = Atlas((128, 128))
    body = atlas.box((0, 24), (8, 8, 14), "526879", "fur", "43d9ed", 101)
    side_rune(atlas, body["west"], "43d9ed")
    side_rune(atlas, body["east"], "43d9ed")
    atlas.box((48, 20), (9, 10, 6), "d5e2e8", "fur", "f4fbff", 102)
    atlas.box((80, 24), (3, 3, 9), "dce8ee", "feather", "56dced", 103)
    atlas.box((104, 24), (2, 4, 8), "c7d8e0", "feather", "56dced", 104)
    atlas.box((104, 40), (2, 4, 8), "c7d8e0", "feather", "56dced", 105)
    head = atlas.box((0, 0), (7, 7, 7), "657b8b", "fur", "43d9ed", 106)
    wolf_face(atlas, head["north"], "43d9ed")
    snout = atlas.box((30, 0), (4, 3, 5), "b7c7d0", "fur", None, 107)
    muzzle(atlas, snout["north"])
    atlas.box((48, 0), (2, 5, 2), "657b8b", "fur", "dce8ee", 108)
    atlas.box((58, 0), (2, 5, 2), "657b8b", "fur", "dce8ee", 109)
    atlas.box((80, 0), (9, 2, 7), "257f78", "metal", "d1b465", 110)
    pendant = atlas.box((104, 0), (3, 4, 1), "3aa69b", "crystal", "76f0e6", 111)
    side_rune(atlas, pendant["north"], "d5bd6a")
    for index in range(4):
        atlas.box((index * 12, 52), (2, 6, 2), "526879", "fur", None, 120 + index)
        paw = atlas.box((index * 16, 68), (3, 2, 4), "dce7eb", "fur", "43d9ed", 130 + index)
        line(atlas.draw, paw["north"], [(1, 0), (1, 1)], shade(color("26333d"), 0))
    tail = atlas.box((0, 84), (4, 4, 7), "718796", "fur", "43d9ed", 140)
    side_rune(atlas, tail["east"], "43d9ed")
    atlas.box((28, 84), (5, 5, 7), "b9cbd4", "fur", "43d9ed", 141)
    tip = atlas.box((60, 84), (6, 6, 7), "e1ebef", "fur", "43d9ed", 142)
    side_rune(atlas, tip["east"], "43d9ed")
    atlas.save("wind_wolf")


def build_stonehorn_goat() -> None:
    atlas = Atlas((128, 96))
    body = atlas.box((0, 30), (12, 12, 16), "45423f", "stone", "ef9a33", 201)
    side_rune(atlas, body["west"], "ef9a33")
    side_rune(atlas, body["east"], "ef9a33")
    armor = atlas.box((58, 30), (13, 5, 14), "292b2c", "stone", "ef9a33", 202)
    side_rune(atlas, armor["west"], "ef9a33")
    side_rune(atlas, armor["east"], "ef9a33")
    atlas.box((64, 52), (11, 2, 8), "363536", "metal", "ef9a33", 203)
    head = atlas.box((0, 0), (9, 9, 8), "514c47", "stone", "ef9a33", 204)
    for x in (2, 6):
        point(atlas.draw, head["north"], x, 4, shade(color("ffb243"), 20))
    muzzle_face = atlas.box((34, 0), (5, 4, 4), "746c62", "stone", None, 205)
    muzzle(atlas, muzzle_face["north"])
    atlas.box((0, 18), (4, 7, 2), "6f675d", "fur", None, 206)
    horn_specs = (
        ((54, 0), (3, 6, 3)), ((68, 0), (3, 5, 3)), ((82, 0), (2, 4, 2)),
        ((92, 0), (3, 6, 3)), ((106, 0), (3, 5, 3)), ((120, 0), (2, 4, 2)),
    )
    for index, (uv, dims) in enumerate(horn_specs):
        atlas.box(uv, dims, "6c655e", "stone", "ef9a33", 207 + index)
    for index, uv in enumerate(((0, 62), (18, 62), (36, 62), (54, 62))):
        leg_faces = atlas.box(uv, (4, 10, 4), "3b3938", "stone", "ef9a33", 220 + index)
        line(atlas.draw, leg_faces["north"], [(1, 8), (2, 8)], shade(color("171819"), 0))
    atlas.save("stonehorn_goat")


def build_crystal_turtle() -> None:
    atlas = Atlas((96, 80))
    atlas.box((0, 24), (12, 5, 14), "235246", "stone", "4be0db", 301)
    shell = atlas.box((0, 44), (14, 6, 15), "2c6256", "stone", "54ece7", 302)
    for face in ("up", "west", "east"):
        side_rune(atlas, shell[face], "54ece7")
    for uv, dims, seed in (((60, 0), (3, 8, 3), 303), ((72, 0), (2, 5, 2), 304), ((82, 0), (2, 4, 2), 305)):
        atlas.box(uv, dims, "39b8b2", "crystal", "a1fff8", seed)
    head = atlas.box((0, 0), (6, 5, 6), "2f6d5d", "stone", "54ece7", 306)
    for x in (1, 4):
        point(atlas.draw, head["north"], x, 2, shade(color("f4d966"), 18))
    for index, uv in enumerate(((54, 30), (70, 30), (54, 44), (70, 44))):
        atlas.box(uv, (5, 3, 6), "24584b", "stone", "54ece7", 310 + index)
    atlas.save("crystal_turtle")


def build_ember_crane() -> None:
    atlas = Atlas((128, 128))
    atlas.box((0, 24), (8, 10, 11), "e6d9ba", "feather", "f0a13a", 401)
    atlas.box((0, 0), (4, 14, 4), "efe5cc", "feather", "f0a13a", 402)
    head = atlas.box((18, 0), (5, 5, 5), "eee1c6", "feather", "ce4930", 403)
    for x in (1, 3):
        point(atlas.draw, head["north"], x, 2, shade(color("2a211e"), 0))
    atlas.box((38, 0), (2, 2, 6), "d8792e", "metal", "f5b64c", 404)
    atlas.box((50, 0), (2, 4, 2), "c84a30", "feather", "f5a73e", 405)
    left = atlas.box((0, 48), (2, 7, 10), "eee2c7", "feather", "d75335", 406)
    right = atlas.box((26, 48), (2, 7, 10), "eee2c7", "feather", "d75335", 407)
    for face in (left["west"], left["east"], right["west"], right["east"]):
        height = face[3] - face[1]
        line(atlas.draw, face, [(1, max(0, height - 4)), (max(1, face[2] - face[0] - 2), max(0, height - 2))],
             shade(color("d64e32"), 12))
    feather_uvs = ((0, 72), (22, 72), (44, 72), (66, 72), (88, 72), (108, 72))
    for index, uv in enumerate(feather_uvs):
        atlas.box(uv, (1, 3, 9), "eadcbc", "feather", "d34f31", 410 + index)
    atlas.box((80, 92), (1, 10, 1), "332a25", "metal", None, 420)
    atlas.box((88, 92), (1, 10, 1), "332a25", "metal", None, 421)
    atlas.box((96, 100), (2, 1, 4), "3b3028", "metal", None, 422)
    atlas.box((110, 100), (2, 1, 4), "3b3028", "metal", None, 423)
    atlas.box((0, 92), (3, 2, 10), "d45a31", "feather", "f4aa3c", 424)
    atlas.box((28, 92), (2, 2, 9), "e47638", "feather", "f7b548", 425)
    atlas.box((52, 92), (2, 2, 9), "e47638", "feather", "f7b548", 426)
    atlas.save("ember_crane")


def build_settler(name: str, skin: str, hair: str, robe: str, trim: str, pack: str,
                  eye: str, beard: bool, seed: int) -> None:
    atlas = Atlas((128, 128))
    head = atlas.box((0, 0), (8, 8, 8), skin, "flat", None, seed)
    human_face(atlas, head["north"], hair, eye, beard)
    atlas.box((32, 0), (9, 3, 9), hair, "fur", trim, seed + 1)
    atlas.box((70, 0), (8, 7, 2), hair, "fur", trim, seed + 2)
    atlas.box((92, 0), (4, 4, 4), hair, "fur", trim, seed + 3)
    atlas.box((0, 20), (8, 12, 4), robe, "cloth", trim, seed + 4)
    robe_faces = atlas.box((26, 20), (9, 13, 5), robe, "cloth", trim, seed + 5)
    robe_front(atlas, robe_faces["north"], trim)
    skirt = atlas.box((56, 20), (10, 8, 6), robe, "cloth", trim, seed + 6)
    line(atlas.draw, skirt["north"], [(1, 1), (8, 1)], shade(color(trim), 10))
    for uv in ((0, 42), (16, 42)):
        atlas.box(uv, (4, 12, 4), robe, "cloth", trim, seed + 7 + uv[0])
    for uv in ((32, 42), (52, 42)):
        atlas.box(uv, (5, 9, 5), robe, "cloth", trim, seed + 8 + uv[0])
    for uv in ((0, 60), (16, 60)):
        atlas.box(uv, (4, 12, 4), robe, "cloth", trim, seed + 9 + uv[0])
    for uv in ((32, 60), (52, 60)):
        atlas.box(uv, (5, 5, 5), "3b2d24", "cloth", trim, seed + 10 + uv[0])
    merchant = atlas.box((0, 80), (10, 14, 5), pack, "metal", trim, seed + 11)
    pack_back(atlas, merchant["south"], trim, True)
    atlas.box((32, 80), (10, 4, 4), "5a4937", "cloth", trim, seed + 12)
    atlas.box((62, 80), (4, 6, 4), "6f5732", "metal", "f2b14a", seed + 13)
    spring = atlas.box((80, 80), (10, 9, 5), pack, "stone", trim, seed + 14)
    pack_back(atlas, spring["south"], trim, True)
    atlas.box((112, 80), (3, 7, 3), "3ab8ad", "crystal", "90fff5", seed + 15)
    steward = atlas.box((0, 104), (9, 13, 4), pack, "metal", trim, seed + 16)
    pack_back(atlas, steward["south"], trim, True)
    atlas.box((28, 104), (2, 14, 1), trim, "cloth", "d8bd68", seed + 17)
    atlas.box((36, 104), (2, 18, 2), "4a3526", "stone", trim, seed + 18)
    atlas.box((46, 104), (4, 6, 4), "715936", "metal", "f3b54c", seed + 19)
    atlas.save(name)


def egg_icon(path: Path, base: str, spots: str, rune: str) -> None:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    b = color(base)
    s = color(spots)
    r = color(rune)
    for y in range(3, 29):
        half = int(10 * (1.0 - abs(y - 17) / 18.0)) + 2
        for x in range(16 - half, 17 + half):
            edge = min(x - (16 - half), (16 + half) - x, y - 3, 28 - y)
            draw.point((x, y), fill=shade(b, -18 if edge < 1 else (10 if (x + y) % 5 == 0 else 0)))
    for x, y in ((11, 10), (20, 13), (13, 20), (21, 23), (16, 7)):
        draw.rectangle((x, y, x + 2, y + 2), fill=shade(s, 4))
    draw.line((12, 17, 16, 13, 20, 17, 16, 22, 12, 17), fill=shade(r, 18), width=1)
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def seal_icon(path: Path) -> None:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    jade = color("3b9b8d")
    gold = color("d8b45a")
    paper = color("e7dfc4")
    draw.rounded_rectangle((5, 3, 26, 28), radius=2, fill=shade(paper, 0), outline=shade(gold, -35), width=2)
    draw.rectangle((8, 6, 23, 25), outline=shade(jade, -12), width=1)
    draw.ellipse((10, 9, 21, 20), outline=shade(jade, 15), width=2)
    draw.line((12, 15, 16, 11, 20, 15, 16, 21, 12, 15), fill=shade(gold, 8), width=1)
    draw.rectangle((13, 23, 18, 29), fill=shade(jade, -8))
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def main() -> None:
    build_settler("cultivation_settler_merchant", "b98b68", "252421", "6b6659", "3b978a", "574536", "2f756f", True, 501)
    build_settler("cultivation_settler_spring_keeper", "c99a76", "242522", "3d7771", "68d5c6", "385a54", "3e8f87", False, 531)
    build_settler("cultivation_settler_steward", "b98767", "1d2024", "252b35", "c5a454", "343238", "654a2e", True, 561)

    item_dir = ASSETS / "textures" / "item"
    seal_icon(item_dir / "spirit_beast_seal.png")
    egg_icon(item_dir / "wind_wolf_spawn_egg.png", "566b7f", "dbe8ed", "42d7ef")
    egg_icon(item_dir / "stonehorn_goat_spawn_egg.png", "514b45", "81786e", "e99a2f")
    egg_icon(item_dir / "crystal_turtle_spawn_egg.png", "24594a", "777a70", "59ebeb")
    egg_icon(item_dir / "ember_crane_spawn_egg.png", "e5d8b7", "b93f28", "f2a83d")
    egg_icon(item_dir / "cultivation_settler_spawn_egg.png", "34332f", "4c7c75", "d4b25e")


if __name__ == "__main__":
    main()
