"""Assets and recipes for the four spirit iron implements."""

from __future__ import annotations

import io
import json
import collections
import math
import os
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources')
DATA = ROOT / 'data' / 'earth_xuanxue'
ASSETS = ROOT / 'assets' / 'earth_xuanxue'
LANG = ASSETS / 'lang'
ITEMS = ASSETS / 'items'
MODELS = ASSETS / 'models' / 'item'
TEX = ASSETS / 'textures' / 'item'

# id -> (zh name, en name, zh tooltip, en tooltip, zh cast message, en cast message, main colour, accent)
IMPLEMENTS = {
    'wind_fan': (
        '风灵扇', 'Wind Fan',
        '灵铁扇骨，挥动时把周围敌人推开并减速。潜行右键从灵气场充能。',
        'Spirit iron ribs. Sweeps nearby enemies away and slows them. Sneak-use to charge from the local qi field.',
        '风灵扇一展：余电 %s/%s AEU，周身气浪翻涌。',
        'The wind fan opens: %s/%s AEU remaining; a pressure wave rolls out.',
        (188, 214, 226), (72, 148, 176),
    ),
    'stone_seal': (
        '镇岳印', 'Stone Seal',
        '灵铁铸印，落下时镇身护体并震伤近敌。潜行右键充能。',
        'A spirit iron seal. Grants resistance and absorption while crushing nearby enemies. Sneak-use to charge.',
        '镇岳印落下：余电 %s/%s AEU，地气自足下升起。',
        'The stone seal falls: %s/%s AEU remaining; earth qi rises underfoot.',
        (200, 178, 150), (124, 84, 48),
    ),
    'gather_orb': (
        '聚灵珠', 'Gather Orb',
        '灵铁与灵晶合炼的球体，可将周围灵气直接转为法力。潜行右键充能。',
        'Spirit iron fused with crystal. Converts ambient qi directly into mana. Sneak-use to charge.',
        '聚灵珠旋转：余电 %s/%s AEU，法力回流。',
        'The gather orb spins: %s/%s AEU remaining; mana flows back.',
        (196, 216, 236), (84, 132, 196),
    ),
    'lantern_rod': (
        '照幽杖', 'Lantern Rod',
        '灵铁杖头嵌萤石，照亮四周并标记附近生灵。潜行右键充能。',
        'A spirit iron rod tipped with glowstone. Lights the area and marks nearby creatures. Sneak-use to charge.',
        '照幽杖亮起：余电 %s/%s AEU，幽影无所遁形。',
        'The lantern rod lights: %s/%s AEU remaining; no shadow hides.',
        (226, 214, 168), (188, 152, 56),
    ),
}


def shade(color, delta):
    return tuple(max(0, min(255, c + delta)) for c in color)


def draw_implement(name: str, base, accent) -> Image.Image:
    size = 32
    image = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(image)

    if name == 'wind_fan':
        # Fan: a quarter-disc of ribs with a handle at the lower left.
        cx, cy = 10, 22
        for radius in range(4, 21, 3):
            d.arc((cx - radius, cy - radius, cx + radius, cy + radius),
                  start=270, end=360, fill=shade(base, -10) + (235,), width=1)
        d.pieslice((cx - 20, cy - 20, cx + 20, cy + 20), start=270, end=360,
                   fill=shade(base, 12) + (110,))
        for angle in range(270, 361, 12):
            rad = math.radians(angle)
            d.line((cx, cy, cx + math.cos(rad) * 19, cy + math.sin(rad) * 19),
                   fill=shade(accent, -10) + (200,), width=1)
        d.line((cx, cy, cx + 6, cy + 6), fill=shade(accent, -40) + (255,), width=3)
    elif name == 'stone_seal':
        # Seal: a chunky square stamp with a knob.
        d.rectangle((7, 14, 24, 27), fill=shade(base, -6) + (255,))
        d.rectangle((7, 14, 24, 16), fill=shade(base, 22) + (255,))
        d.rectangle((7, 25, 24, 27), fill=shade(base, -40) + (255,))
        d.rectangle((13, 6, 18, 14), fill=shade(base, 8) + (255,))
        d.rectangle((12, 4, 19, 7), fill=shade(accent, 10) + (255,))
        d.line((10, 20, 21, 20), fill=shade(accent, 0) + (255,), width=1)
        d.line((15, 17, 15, 23), fill=shade(accent, 0) + (255,), width=1)
    elif name == 'gather_orb':
        # Orb: a sphere in a three-pronged cradle.
        for y in range(5, 25):
            dy = (y - 15) / 10.0
            if abs(dy) > 1:
                continue
            half = math.sqrt(max(0.0, 1 - dy * dy)) * 10
            lit = int(-40 + max(0.0, 1 - abs(dy + 0.4) * 1.6) * 80)
            d.line((16 - half, y, 16 + half, y), fill=shade(base, lit) + (255,))
        d.arc((6, 5, 26, 25), start=0, end=360, fill=shade(accent, -20) + (180,))
        for angle in (210, 330, 90):
            rad = math.radians(angle)
            d.line((16 + math.cos(rad) * 10, 15 + math.sin(rad) * 10,
                    16 + math.cos(rad) * 14, 15 + math.sin(rad) * 14),
                   fill=shade(accent, -40) + (255,), width=2)
        d.ellipse((11, 9, 14, 12), fill=(255, 255, 255, 170))
    else:  # lantern_rod
        d.line((9, 26, 20, 7), fill=shade(accent, -50) + (255,), width=3)
        d.line((9, 26, 20, 7), fill=shade(accent, 10) + (255,), width=1)
        d.ellipse((18, 3, 27, 12), fill=shade(base, 20) + (255,))
        d.ellipse((20, 5, 25, 10), fill=(255, 248, 200, 255))
        d.ellipse((21, 6, 23, 8), fill=(255, 255, 255, 255))

    return image.filter(ImageFilter.SMOOTH)


RECIPES = {
    'wind_fan': [('earth_xuanxue:spirit_iron_blank', 2), ('minecraft:feather', 2)],
    'stone_seal': [('earth_xuanxue:spirit_iron_blank', 2), ('minecraft:stone_bricks', 3)],
    'gather_orb': [('earth_xuanxue:spirit_iron_blank', 1), ('earth_xuanxue:spirit_crystal_shard', 3)],
    'lantern_rod': [('earth_xuanxue:spirit_iron_blank', 1), ('minecraft:glowstone_dust', 3)],
}


def main() -> None:
    zh = json.loads(io.open(LANG / 'zh_cn.json', encoding='utf-8').read(),
                    object_pairs_hook=collections.OrderedDict)
    en = json.loads(io.open(LANG / 'en_us.json', encoding='utf-8').read(),
                    object_pairs_hook=collections.OrderedDict)

    for impl_id, spec in IMPLEMENTS.items():
        zh_name, en_name, zh_tip, en_tip, zh_msg, en_msg, base, accent = spec
        zh[f'item.earth_xuanxue.{impl_id}'] = zh_name
        zh[f'tooltip.earth_xuanxue.{impl_id}'] = zh_tip
        zh[f'message.earth_xuanxue.implement.cast.{impl_id}'] = zh_msg
        en[f'item.earth_xuanxue.{impl_id}'] = en_name
        en[f'tooltip.earth_xuanxue.{impl_id}'] = en_tip
        en[f'message.earth_xuanxue.implement.cast.{impl_id}'] = en_msg

        io.open(ITEMS / f'{impl_id}.json', 'w', encoding='utf-8', newline='\n').write(
            json.dumps({"model": {"type": "minecraft:model",
                                  "model": f"earth_xuanxue:item/{impl_id}"}}, indent=2) + '\n')
        io.open(MODELS / f'{impl_id}.json', 'w', encoding='utf-8', newline='\n').write(
            json.dumps({"parent": "minecraft:item/handheld",
                        "textures": {"layer0": f"earth_xuanxue:item/{impl_id}"}}, indent=2) + '\n')
        draw_implement(impl_id, base, accent).save(TEX / f'{impl_id}.png')

        recipe = {
            "type": "minecraft:crafting_shapeless",
            "category": "equipment",
            "ingredients": [item for item, count in RECIPES[impl_id] for _ in range(count)],
            "result": {"id": f"earth_xuanxue:{impl_id}", "count": 1},
        }
        io.open(DATA / 'recipe' / f'{impl_id}.json', 'w', encoding='utf-8', newline='\n').write(
            json.dumps(recipe, indent=2) + '\n')
        print('implement', impl_id)

    zh['tooltip.earth_xuanxue.implement.charge'] = '法器储能：%s / %s AEU，每次施放消耗 %s AEU'
    zh['tooltip.earth_xuanxue.implement.use'] = '右键施放；潜行右键从当前灵气场充能。'
    zh['message.earth_xuanxue.implement.full'] = '法器已经充满，无需继续吸纳灵气。'
    zh['message.earth_xuanxue.implement.no_field'] = '当前灵气场过于稀薄，无法为法器充能。'
    zh['message.earth_xuanxue.implement.charged'] = '法器充能：吸纳 %s AEU（%s/%s）。'
    zh['message.earth_xuanxue.implement.empty'] = '法器储能不足：需要 %s AEU，当前 %s AEU。'

    en['tooltip.earth_xuanxue.implement.charge'] = 'Implement charge: %s / %s AEU; each cast costs %s AEU'
    en['tooltip.earth_xuanxue.implement.use'] = 'Right-click to cast; sneak-right-click to charge from the local qi field.'
    en['message.earth_xuanxue.implement.full'] = 'The implement is already fully charged.'
    en['message.earth_xuanxue.implement.no_field'] = 'The local qi field is too thin to charge the implement.'
    en['message.earth_xuanxue.implement.charged'] = 'Implement charged: drew %s AEU (%s/%s).'
    en['message.earth_xuanxue.implement.empty'] = 'Not enough charge: needs %s AEU, has %s AEU.'

    io.open(LANG / 'zh_cn.json', 'w', encoding='utf-8', newline='\n').write(
        json.dumps(zh, ensure_ascii=False, indent=2) + '\n')
    io.open(LANG / 'en_us.json', 'w', encoding='utf-8', newline='\n').write(
        json.dumps(en, ensure_ascii=False, indent=2) + '\n')
    print('lang', len(zh), len(en))


if __name__ == '__main__':
    main()
