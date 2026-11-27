"""Generate lang entries, item models and textures for the new pills."""

from __future__ import annotations

import io
import json
import collections
import math
import os
import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources')
ASSETS = ROOT / 'assets' / 'earth_xuanxue'
LANG = ASSETS / 'lang'
ITEMS = ASSETS / 'items'
MODELS = ASSETS / 'models' / 'item'
TEX = ASSETS / 'textures' / 'item'

# id -> (zh name, en name, zh tooltip, en tooltip, base colour, accent colour, zh message, en message)
PILLS = {
    'foundation_pill': (
        '筑基丹', 'Foundation Pill',
        '以回气丹为引，大幅回复法力并推进周天修为。',
        'Refined from qi pills: restores a large amount of mana and advances the circulation route.',
        (206, 196, 226), (108, 74, 158),
        '筑基丹化开：法力 +%s（%s/%s），周天修为随之精进。',
        'The foundation pill dissolves: mana +%s (%s/%s), and your circulation route advances.',
    ),
    'ironbody_pill': (
        '淬体丹', 'Ironbody Pill',
        '以矿质催化淬炼肉身，短时提高抗性与吸收。',
        'Tempered with mineral catalysts: brief resistance and absorption, plus body-tempering experience.',
        (198, 176, 150), (126, 78, 44),
        '淬体丹药力游走四肢：法力 +%s（%s/%s），皮肉一时如铁。',
        'The ironbody pill spreads through your limbs: mana +%s (%s/%s); your flesh hardens briefly.',
    ),
    'cloudbreath_pill': (
        '云息丹', 'Cloudbreath Pill',
        '灵泉与灵草合炼，长时水下呼吸。',
        'Spring water and spirit grass: long water breathing and fetal-breath experience.',
        (186, 214, 224), (56, 118, 148),
        '云息丹入口清凉：法力 +%s（%s/%s），气息绵长。',
        'The cloudbreath pill tastes cold: mana +%s (%s/%s); your breath lengthens.',
    ),
    'grainfast_pill': (
        '辟谷丹', 'Grainfast Pill',
        '灵草与谷物合炼，压制饥饿并推进辟谷修为。',
        'Spirit grass and grain: suppresses hunger and advances the fasting route.',
        (208, 200, 160), (128, 116, 56),
        '辟谷丹落腹：法力 +%s（%s/%s），数日不食亦可。',
        'The grainfast pill settles: mana +%s (%s/%s); hunger fades for a while.',
    ),
    'nightsight_pill': (
        '夜视丹', 'Nightsight Pill',
        '灵草配金胡萝卜，暗中视物并略微提速。',
        'Spirit grass with golden carrot: see in the dark and move a little faster.',
        (196, 200, 168), (92, 104, 40),
        '夜视丹入喉：法力 +%s（%s/%s），眼底微光。',
        'The nightsight pill goes down: mana +%s (%s/%s); a faint light fills your eyes.',
    ),
    'breakthrough_pill': (
        '破境丹', 'Breakthrough Pill',
        '两枚筑基丹合炼，是当前品阶的顶点。',
        'Two foundation pills fused: the top of the current alchemy ladder.',
        (232, 206, 244), (140, 66, 176),
        '破境丹药力汹涌：法力 +%s（%s/%s），境界似有松动。',
        'The breakthrough pill surges: mana +%s (%s/%s); your cultivation boundary trembles.',
    ),
}


def shade(color, delta):
    return tuple(max(0, min(255, c + delta)) for c in color)


def pill_texture(name: str, base, accent) -> Image.Image:
    """A 32x32 pill: round body with a highlight, a seam, and a rune inlay."""
    size = 32
    image = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    cx, cy = 16.0, 17.0
    r = 10.0

    # Drop shadow.
    draw.ellipse((cx - r, cy - r + 2, cx + r, cy + r + 2), fill=(0, 0, 0, 70))
    # Body with vertical shading.
    for y in range(int(cy - r), int(cy + r) + 1):
        dy = (y - cy) / r
        if abs(dy) > 1.0:
            continue
        half = math.sqrt(max(0.0, 1.0 - dy * dy)) * r
        light = int(-38 * dy)
        draw.line((cx - half, y, cx + half, y), fill=(*shade(base, light), 255))
    # Rim.
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=shade(base, -70) + (255,), width=1)
    # Seam across the middle.
    draw.line((cx - r + 2, cy, cx + r - 2, cy), fill=shade(base, -46) + (255,), width=1)
    # Rune inlay.
    draw.line((cx, cy - 5, cx, cy + 5), fill=(*accent, 255), width=1)
    draw.line((cx - 3, cy - 2, cx + 3, cy - 2), fill=(*accent, 255), width=1)
    # Highlight.
    draw.ellipse((cx - 5, cy - 6, cx - 2, cy - 3), fill=(255, 255, 255, 150))
    return image


def main() -> None:
    zh = json.loads(io.open(LANG / 'zh_cn.json', encoding='utf-8').read(),
                    object_pairs_hook=collections.OrderedDict)
    en = json.loads(io.open(LANG / 'en_us.json', encoding='utf-8').read(),
                    object_pairs_hook=collections.OrderedDict)

    for pill_id, (zh_name, en_name, zh_tip, en_tip, base, accent, zh_msg, en_msg) in PILLS.items():
        zh[f'item.earth_xuanxue.{pill_id}'] = zh_name
        zh[f'tooltip.earth_xuanxue.{pill_id}'] = zh_tip
        zh[f'message.earth_xuanxue.pill.{pill_id}'] = zh_msg
        en[f'item.earth_xuanxue.{pill_id}'] = en_name
        en[f'tooltip.earth_xuanxue.{pill_id}'] = en_tip
        en[f'message.earth_xuanxue.pill.{pill_id}'] = en_msg

        model = {
            "model": {
                "type": "minecraft:model",
                "model": f"earth_xuanxue:item/{pill_id}",
            }
        }
        io.open(ITEMS / f'{pill_id}.json', 'w', encoding='utf-8', newline='\n').write(
            json.dumps(model, indent=2) + '\n')

        item_model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"earth_xuanxue:item/{pill_id}"},
        }
        io.open(MODELS / f'{pill_id}.json', 'w', encoding='utf-8', newline='\n').write(
            json.dumps(item_model, indent=2) + '\n')

        pill_texture(pill_id, base, accent).save(TEX / f'{pill_id}.png')
        print('pill', pill_id)

    zh['message.earth_xuanxue.pill.no_need'] = '法力与身体状态均无需恢复（%s/%s），本次没有服丹。'
    en['message.earth_xuanxue.pill.no_need'] = 'Mana and body status need no recovery (%s/%s); no pill was used.'

    io.open(LANG / 'zh_cn.json', 'w', encoding='utf-8', newline='\n').write(
        json.dumps(zh, ensure_ascii=False, indent=2) + '\n')
    io.open(LANG / 'en_us.json', 'w', encoding='utf-8', newline='\n').write(
        json.dumps(en, ensure_ascii=False, indent=2) + '\n')
    print('lang updated', len(zh), len(en))


if __name__ == '__main__':
    main()
