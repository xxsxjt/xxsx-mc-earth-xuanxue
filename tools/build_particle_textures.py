from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "neoforge-26.1.2" / "src" / "main" / "resources" / "assets" / "earth_xuanxue"
OUT = ASSETS / "textures" / "particle"


def new(size: int = 32) -> Image.Image:
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def save(image: Image.Image, name: str) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    image.save(OUT / f"{name}.png")
    print("wrote", name, image.size)


def radial(size: int, inner: tuple[int, int, int], outer: tuple[int, int, int],
           softness: float = 1.6, core: float = 0.18) -> Image.Image:
    image = new(size)
    px = image.load()
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            dx = (x - c) / c
            dy = (y - c) / c
            d = math.sqrt(dx * dx + dy * dy)
            if d > 1.0:
                continue
            falloff = max(0.0, 1.0 - d) ** softness
            hot = max(0.0, 1.0 - d / core) if core > 0 else 0.0
            r = int(outer[0] * falloff + inner[0] * hot)
            g = int(outer[1] * falloff + inner[1] * hot)
            b = int(outer[2] * falloff + inner[2] * hot)
            a = int(255 * min(1.0, falloff * 1.25))
            px[x, y] = (min(255, r), min(255, g), min(255, b), a)
    return image


def ring(size: int, color: tuple[int, int, int], thickness: float = 0.16,
         radius: float = 0.82, fade: float = 2.2) -> Image.Image:
    image = new(size)
    px = image.load()
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            dx = (x - c) / c
            dy = (y - c) / c
            d = math.sqrt(dx * dx + dy * dy)
            edge = abs(d - radius)
            if edge > thickness:
                continue
            falloff = max(0.0, 1.0 - edge / thickness) ** fade
            a = int(255 * falloff)
            px[x, y] = (color[0], color[1], color[2], a)
    return image


def spark(size: int, color: tuple[int, int, int], core: tuple[int, int, int]) -> Image.Image:
    image = new(size)
    draw = ImageDraw.Draw(image)
    c = (size - 1) / 2.0
    for step in range(size):
        t = step / max(1, size - 1)
        length = abs(t - 0.5) * 2.0
        width = max(1, int((1.0 - length) * size * 0.16))
        alpha = int(220 * (1.0 - length) ** 1.4)
        draw.line((c, step, c + width, step), fill=(*color, alpha))
        draw.line((c, step, c - width, step), fill=(*color, alpha))
    draw.line((c, 1, c, size - 2), fill=(*core, 235), width=max(1, size // 12))
    draw.line((1, c, size - 2, c), fill=(*color, 180), width=1)
    return image.filter(ImageFilter.GaussianBlur(0.4))


def rune_glyph(size: int, color: tuple[int, int, int], seed: int) -> Image.Image:
    image = new(size)
    draw = ImageDraw.Draw(image)
    rng = random.Random(seed)
    c = size // 2
    draw.ellipse((2, 2, size - 3, size - 3), outline=(*color, 190), width=1)
    draw.ellipse((c - 3, c - 3, c + 3, c + 3), outline=(*color, 220), width=1)
    for i in range(3):
        angle = rng.uniform(0, math.tau)
        r = size * 0.28
        x = c + math.cos(angle) * r
        y = c + math.sin(angle) * r
        x2 = c + math.cos(angle + 2.1) * r
        y2 = c + math.sin(angle + 2.1) * r
        draw.line((x, y, x2, y2), fill=(*color, 160), width=1)
    return image


def smoke_puff(size: int, color: tuple[int, int, int]) -> Image.Image:
    image = new(size)
    px = image.load()
    rng = random.Random(7)
    blobs = [(rng.uniform(0.3, 0.7), rng.uniform(0.3, 0.7), rng.uniform(0.18, 0.3)) for _ in range(6)]
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            nx = x / (size - 1)
            ny = y / (size - 1)
            best = 0.0
            for bx, by, br in blobs:
                dx = nx - bx
                dy = ny - by
                d = math.sqrt(dx * dx + dy * dy)
                best = max(best, max(0.0, 1.0 - d / br))
            if best <= 0.0:
                continue
            a = int(190 * best ** 1.3)
            px[x, y] = (color[0], color[1], color[2], a)
    return image.filter(ImageFilter.GaussianBlur(1.2))


def leaf_shard(size: int, color: tuple[int, int, int], accent: tuple[int, int, int]) -> Image.Image:
    image = new(size)
    draw = ImageDraw.Draw(image)
    c = size // 2
    draw.polygon([(c, 1), (size - 3, c), (c, size - 2), (2, c)], fill=(*color, 210))
    draw.line((c, 3, c, size - 4), fill=(*accent, 235), width=1)
    return image


def main() -> None:
    save(radial(32, (235, 255, 255), (78, 224, 214), 1.7, 0.16), "qi_mote")
    save(radial(32, (255, 248, 210), (255, 190, 92), 1.5, 0.14), "spirit_flame")
    save(radial(32, (255, 236, 236), (214, 96, 132), 1.9, 0.1), "blood_mote")
    save(ring(32, (126, 245, 236), 0.13, 0.84, 2.4), "qi_ring")
    save(ring(32, (255, 210, 128), 0.11, 0.8, 2.0), "seal_ring")
    save(ring(32, (150, 208, 255), 0.1, 0.86, 2.6), "wind_ring")
    save(spark(32, (150, 244, 236), (255, 255, 255)), "qi_spark")
    save(spark(32, (255, 196, 108), (255, 250, 224)), "ember_spark")
    save(rune_glyph(32, (214, 184, 112), 11), "rune_glyph_a")
    save(rune_glyph(32, (126, 245, 236), 29), "rune_glyph_b")
    save(smoke_puff(32, (206, 220, 226)), "spirit_smoke")
    save(smoke_puff(32, (92, 66, 58)), "alchemy_smoke")
    save(leaf_shard(32, (128, 226, 158), (222, 255, 214)), "spirit_leaf")
    save(leaf_shard(32, (232, 150, 120), (255, 214, 190)), "ember_leaf")


if __name__ == "__main__":
    main()
