import json
import pathlib
import struct
import zlib

ROOT = pathlib.Path(__file__).resolve().parents[1] / "neoforge-26.2" / "src" / "main" / "resources"
ASSETS = ROOT / "assets" / "xuanhuan_earth"
DATA = ROOT / "data"

def write_json(path: pathlib.Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

def make_png(width: int, height: int, pixel_fn) -> bytes:
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            r, g, b, a = pixel_fn(x, y)
            raw.extend((max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), max(0, min(255, a))))
    def chunk(tag: bytes, payload: bytes) -> bytes:
        return struct.pack(">I", len(payload)) + tag + payload + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF)
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")

def write_item_png(path: pathlib.Path, primary, secondary, accent, shape="round"):
    path.parent.mkdir(parents=True, exist_ok=True)
    pr, pg, pb = primary
    sr, sg, sb = secondary
    ar, ag, ab = accent
    def px(x, y):
        cx, cy = x - 7.5, y - 7.5
        dist2 = cx * cx + cy * cy
        if shape == "pill":
            if dist2 > 22:
                return (0, 0, 0, 0)
            if dist2 > 16:
                return (sr, sg, sb, 255)
            if x in (5, 6) and y in (5, 6):
                return (ar, ag, ab, 255)
            return (pr, pg, pb, 255)
        if shape == "talisman":
            if 4 <= x <= 11 and 2 <= y <= 13:
                if x in (4, 11) or y in (2, 13):
                    return (sr, sg, sb, 255)
                if x in (7, 8) and 4 <= y <= 11 or (y in (5, 8, 10) and 6 <= x <= 9):
                    return (ar, ag, ab, 255)
                return (pr, pg, pb, 255)
            return (0, 0, 0, 0)
        if shape == "rod":
            if abs(x - (15 - y)) <= 1 and 2 <= x <= 13 and 2 <= y <= 13:
                if x >= 10 and y <= 5:
                    return (ar, ag, ab, 255)
                return (pr, pg, pb, 255) if (x + y) % 2 == 0 else (sr, sg, sb, 255)
            return (0, 0, 0, 0)
        if shape == "tablet":
            if 3 <= x <= 12 and 2 <= y <= 13:
                if x in (3, 12) or y in (2, 13):
                    return (sr, sg, sb, 255)
                if (x + y) % 3 == 0 and 5 <= x <= 10 and 4 <= y <= 11:
                    return (ar, ag, ab, 255)
                return (pr, pg, pb, 255)
            return (0, 0, 0, 0)
        if shape == "ingot":
            if 2 <= x <= 13 and 5 <= y <= 10:
                if y in (5, 10) or x in (2, 13):
                    return (sr, sg, sb, 255)
                if y == 7 and 4 <= x <= 11:
                    return (ar, ag, ab, 255)
                return (pr, pg, pb, 255)
            return (0, 0, 0, 0)
        if dist2 > 30:
            return (0, 0, 0, 0)
        if dist2 > 22:
            return (sr, sg, sb, 255)
        if abs(cx) + abs(cy) < 3.2:
            return (ar, ag, ab, 255)
        return (pr, pg, pb, 255)
    path.write_bytes(make_png(16, 16, px))

def write_block_png(path: pathlib.Path, primary, secondary, accent):
    path.parent.mkdir(parents=True, exist_ok=True)
    pr, pg, pb = primary
    sr, sg, sb = secondary
    ar, ag, ab = accent
    def px(x, y):
        if x in (0, 15) or y in (0, 15):
            return (sr, sg, sb, 255)
        if x in (7, 8) and 3 <= y <= 12:
            return (ar, ag, ab, 255)
        if y in (4, 11) and 4 <= x <= 11:
            return (ar, ag, ab, 255)
        return (pr, pg, pb, 255) if ((x + y) % 2 == 0) else (max(0, pr - 12), max(0, pg - 12), max(0, pb - 12), 255)
    path.write_bytes(make_png(16, 16, px))

def write_effect_png(path: pathlib.Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    def px(x, y):
        cx, cy = x - 8.5, y - 8.5
        d2 = cx * cx + cy * cy
        if d2 > 42:
            return (0, 0, 0, 0)
        if d2 > 30:
            return (186, 132, 38, 255)
        if abs(cx) < 1.6 or abs(cy) < 1.6:
            return (255, 238, 158, 255)
        return (236, 184, 68, 255)
    path.write_bytes(make_png(18, 18, px))

# 1. 35 Knowledge Nodes (15-tree-mystic.md §1)
NODES = [
    {
        "slug": "qi_guiding", "tier": 1, "gate": "soft",
        "zh": "引气入体", "en": "Qi Guiding",
        "zh_desc": "感应天地灵气并导引归元，开启修行之门与蒲团静修。",
        "en_desc": "Sense ambient qi and guide it inward, unlocking basic manuals, journals and meditation cushions.",
        "requires": [],
        "item_requires": [{"cond": "field>=20"}],
        "unlocks": [
            {"item": "xuanhuan_earth:qi_guiding_manual"},
            {"item": "xuanhuan_earth:cultivation_journal"},
            {"block": "xuanhuan_earth:meditation_cushion"},
            {"item": "xuanhuan_earth:meditation_cushion"}
        ],
        "literature": ["notes", "manual"],
        "teachers": ["xuanhuan_earth:cultivation_settler"],
        "research": {"items": [{"ingredient": "minecraft:paper", "count": 4}, {"ingredient": "xuanhuan_earth:spirit_dust", "count": 2}], "ticks": 600},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "circulation", "tier": 2, "gate": "hard",
        "zh": "周天", "en": "Meridian Circulation",
        "zh_desc": "贯通周天经脉，解锁打坐修炼、周天进阶与三门辅修秘籍。",
        "en_desc": "Complete meridian circulation to unlock seated meditation, route growth, built-in spell graphs and secondary manuals.",
        "requires": [{"all": "xuanhuan_earth:xua/qi_guiding"}],
        "item_requires": [{"cond": "field>=30"}],
        "unlocks": [
            {"ability": "xuanhuan_earth:practice"},
            {"ability": "xuanhuan_earth:progress"},
            {"ability": "xuanhuan_earth:spell_graph_builtin"},
            {"item": "xuanhuan_earth:body_tempering_manual"},
            {"item": "xuanhuan_earth:fetal_breath_manual"},
            {"item": "xuanhuan_earth:bigu_manual"}
        ],
        "literature": ["notes", "manual"],
        "teachers": ["xuanhuan_earth:cultivation_settler"],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone", "count": 1}, {"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 2}], "ticks": 1200},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "body_tempering", "tier": 3, "gate": "hard",
        "zh": "炼体", "en": "Body Tempering",
        "zh_desc": "引灵入骨锤炼筋骨皮膜，解锁炼体路线与洗髓丹。",
        "en_desc": "Temper sinew and bone with qi, unlocking the body-tempering route and Marrow Pill.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:marrow_pill"}],
        "literature": ["notes", "manual"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:bone_meal", "count": 8}, {"ingredient": "xuanhuan_earth:spirit_herb", "count": 2}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "fetal_breath", "tier": 3, "gate": "hard",
        "zh": "胎息", "en": "Fetal Breath",
        "zh_desc": "内息绵长如婴儿在胎，解锁胎息路线与闭息丹。",
        "en_desc": "Sustain internal respiration through qi, unlocking the fetal-breath route and Breath Pill.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:breath_pill"}],
        "literature": ["notes", "manual"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_spring_bottle", "count": 2}, {"ingredient": "minecraft:kelp", "count": 8}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "bigu", "tier": 3, "gate": "hard",
        "zh": "辟谷", "en": "Bigu Fasting",
        "zh_desc": "食气辟谷以减五谷之累，解锁辟谷路线与辟谷丸。",
        "en_desc": "Nourish the body with ambient qi instead of grain, unlocking the bigu route and Fast Pill.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:fast_pill"}],
        "literature": ["notes", "manual"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:wheat", "count": 16}, {"ingredient": "xuanhuan_earth:spirit_herb", "count": 2}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "spirit_stones", "tier": 2, "gate": "soft",
        "zh": "灵石学", "en": "Spirit Stone Studies",
        "zh_desc": "辨识灵石原胚、灵晶碎片与灵尘，掌握灵石充放能与灵气罗盘。",
        "en_desc": "Understand spirit stone matrices, crystal shards and dust, unlocking spirit stones and the Spirit Compass.",
        "requires": [{"all": "xuanhuan_earth:xua/qi_guiding"}],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:spirit_stone_raw"},
            {"item": "xuanhuan_earth:spirit_stone"},
            {"item": "xuanhuan_earth:spirit_stone_cracked"},
            {"item": "xuanhuan_earth:spirit_crystal_shard"},
            {"item": "xuanhuan_earth:spirit_dust"},
            {"ability": "xuanhuan_earth:spirit_stone_charge"},
            {"item": "xuanhuan_earth:spirit_compass"}
        ],
        "literature": ["notes"],
        "teachers": ["xuanhuan_earth:cultivation_settler"],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_raw", "count": 2}, {"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 2}], "ticks": 1200},
        "fail_codes": []
    },
    {
        "slug": "qi_refining", "tier": 4, "gate": "hard",
        "zh": "炼气", "en": "Qi Refining Realm",
        "zh_desc": "由凡入道，凝聚气海，解锁第一境「炼气」突破。",
        "en_desc": "Condense a stable sea of qi, unlocking breakthrough into Realm 1 (Qi Refining).",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}, {"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [{"cond": "field>=40"}],
        "unlocks": [{"ability": "xuanhuan_earth:realm_1"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone", "count": 2}, {"ingredient": "xuanhuan_earth:qi_recovery_pill", "count": 2}], "ticks": 4800},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "foundation_building", "tier": 6, "gate": "hard",
        "zh": "筑基", "en": "Foundation Building Realm",
        "zh_desc": "百脉筑基，固本培元，解锁第二境「筑基」突破。",
        "en_desc": "Forge an unshakeable cultivation foundation, unlocking breakthrough into Realm 2 (Foundation Building).",
        "requires": [
            {"all": "xuanhuan_earth:xua/qi_refining"},
            {"all": "xuanhuan_earth:xua/breakthrough_pills"},
            {"any": ["xuanhuan_earth:xua/body_tempering", "xuanhuan_earth:xua/fetal_breath", "xuanhuan_earth:xua/bigu"]}
        ],
        "item_requires": [{"cond": "field>=50"}],
        "unlocks": [{"ability": "xuanhuan_earth:realm_2"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone", "count": 4}, {"ingredient": "xuanhuan_earth:breakthrough_pill", "count": 1}], "ticks": 7200},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "golden_core", "tier": 7, "gate": "hard",
        "zh": "金丹", "en": "Golden Core Realm",
        "zh_desc": "气聚成丹，圆融无暇，解锁第三境「金丹」突破。",
        "en_desc": "Crystallize inner qi into a Golden Core, unlocking breakthrough into Realm 3 (Golden Core).",
        "requires": [{"all": "xuanhuan_earth:xua/foundation_building"}],
        "item_requires": [{"cond": "field>=60"}],
        "unlocks": [{"ability": "xuanhuan_earth:realm_3"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_core", "count": 1}, {"ingredient": "xuanhuan_earth:breakthrough_pill", "count": 2}], "ticks": 9600},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "nascent_soul", "tier": 8, "gate": "hard",
        "zh": "元婴", "en": "Nascent Soul Realm",
        "zh_desc": "碎丹成婴，神气合一，解锁第四境「元婴」突破。",
        "en_desc": "Awaken the Nascent Soul within the core, unlocking breakthrough into Realm 4 (Nascent Soul).",
        "requires": [{"all": "xuanhuan_earth:xua/golden_core"}],
        "item_requires": [{"cond": "field>=70"}],
        "unlocks": [{"ability": "xuanhuan_earth:realm_4"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_core", "count": 2}, {"ingredient": "xuanhuan_earth:breakthrough_pill", "count": 3}], "ticks": 12000},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "spirit_transformation", "tier": 9, "gate": "hard",
        "zh": "化神", "en": "Spirit Transformation Realm",
        "zh_desc": "炼婴化神，与天地灵机相合，解锁第五境「化神」突破。",
        "en_desc": "harmonize spirit with heaven and earth, unlocking breakthrough into Realm 5 (Spirit Transformation).",
        "requires": [{"all": "xuanhuan_earth:xua/nascent_soul"}],
        "item_requires": [{"cond": "field>=80"}],
        "unlocks": [{"ability": "xuanhuan_earth:realm_5"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_core", "count": 4}, {"ingredient": "xuanhuan_earth:breakthrough_pill", "count": 4}], "ticks": 14400},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "spirit_materials", "tier": 3, "gate": "soft",
        "zh": "灵材", "en": "Spirit Materials",
        "zh_desc": "以灵气淬炼金属、琉璃与蚕丝，制备灵铁胚、灵铁锭、灵化玻璃与灵丝。",
        "en_desc": "Refine metals, glass and silk with qi to produce Spirit Iron Blanks, Ingots, Spirit Glass and Spirit Thread.",
        "requires": [{"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:spirit_iron_blank"},
            {"item": "xuanhuan_earth:spirit_iron_ingot"},
            {"item": "xuanhuan_earth:spirit_glass"},
            {"item": "xuanhuan_earth:spirit_thread"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:iron_ingot", "count": 4}, {"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 4}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "pill_refining", "tier": 3, "gate": "soft",
        "zh": "丹道", "en": "Pill Refining",
        "zh_desc": "掌握丹炉控火与药材调配，解锁丹炉、朱砂灵墨与丹渣再炼。",
        "en_desc": "Master furnace heat control and herbal blending, unlocking the Alchemy Furnace, Cinnabar Spirit Ink and pill refining.",
        "requires": [
            {"all": "xuanhuan_earth:xua/spirit_stones"},
            {"any": ["earth_on_minecraft:med/materia_medica", "xuanhuan_earth:xua/herb_tasting"]}
        ],
        "item_requires": [],
        "unlocks": [
            {"block": "xuanhuan_earth:alchemy_furnace"},
            {"item": "xuanhuan_earth:alchemy_furnace"},
            {"ability": "xuanhuan_earth:alchemy_furnace"},
            {"item": "xuanhuan_earth:cinnabar_spirit_ink"},
            {"item": "xuanhuan_earth:pill_residue"},
            {"item": "xuanhuan_earth:failed_pill"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_herb", "count": 4}, {"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 2}, {"ingredient": "minecraft:coal", "count": 4}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "herb_tasting", "tier": 2, "gate": "theory",
        "zh": "百草自辨（自研）", "en": "Self-Taught Herbology",
        "zh_desc": "未习医家本草时，凭灵觉与亲尝辨识草木药性。",
        "en_desc": "Identify herbal properties through qi perception and field tasting when formal Materia Medica is unavailable.",
        "requires": [{"all": "xuanhuan_earth:xua/qi_guiding"}],
        "item_requires": [],
        "unlocks": [],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_herb", "count": 8}, {"ingredient": "minecraft:sweet_berries", "count": 16}, {"ingredient": "minecraft:paper", "count": 8}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "restorative_pills", "tier": 4, "gate": "soft",
        "zh": "补益丹", "en": "Restorative Pills",
        "zh_desc": "以灵草与灵泉温养凝丹，解锁回气丹与解毒丹。",
        "en_desc": "Refine spirit herbs and spring water into Qi Recovery Pills and Detox Pills.",
        "requires": [{"all": "xuanhuan_earth:xua/pill_refining"}],
        "item_requires": [{"hold": "xuanhuan_earth:alchemy_furnace"}],
        "unlocks": [
            {"item": "xuanhuan_earth:qi_recovery_pill"},
            {"item": "xuanhuan_earth:detox_pill"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_herb", "count": 4}, {"ingredient": "xuanhuan_earth:spirit_spring_bottle", "count": 2}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "breakthrough_pills", "tier": 5, "gate": "soft",
        "zh": "破境丹", "en": "Breakthrough Pills",
        "zh_desc": "烈火凝炼高阶灵晶与灵草，解锁破境丹炼制。",
        "en_desc": "Condense high-grade crystals and herbs at intense heat to refine Breakthrough Pills.",
        "requires": [{"all": "xuanhuan_earth:xua/qi_refining"}, {"all": "xuanhuan_earth:xua/restorative_pills"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:breakthrough_pill"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 8}, {"ingredient": "xuanhuan_earth:cinnabar_spirit_ink", "count": 2}], "ticks": 6000},
        "fail_codes": []
    },
    {
        "slug": "herbal_formulas", "tier": 5, "gate": "soft",
        "zh": "本草丹方", "en": "Herbal Pill Formulas",
        "zh_desc": "融合本草君臣佐使之理，解锁益气、安神、清热、活血、化毒五道丹方。",
        "en_desc": "Combine classical herbalism with pill alchemy to refine Nourish Qi, Calm Spirit, Clear Heat, Invigorate Blood and Resolve Toxin pills.",
        "requires": [
            {"all": "xuanhuan_earth:xua/restorative_pills"},
            {"any": ["earth_on_minecraft:med/herbal_processing", "xuanhuan_earth:xua/herb_tasting"]}
        ],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:nourish_qi_pill"},
            {"item": "xuanhuan_earth:calm_spirit_pill"},
            {"item": "xuanhuan_earth:clear_heat_pill"},
            {"item": "xuanhuan_earth:invigorate_blood_pill"},
            {"item": "xuanhuan_earth:resolve_toxin_pill"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_herb", "count": 8}, {"ingredient": "xuanhuan_earth:spirit_spring_bottle", "count": 4}], "ticks": 6000},
        "fail_codes": []
    },
    {
        "slug": "talisman_basics", "tier": 3, "gate": "soft",
        "zh": "符箓入门", "en": "Talisman Basics",
        "zh_desc": "裁纸调墨，执笔成符，解锁符纸、空白符、基础护身符、符笔与符案。",
        "en_desc": "Prepare talisman paper, cinnabar ink and brush at the Talisman Table to draw and activate basic talismans.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}, {"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:talisman_paper"},
            {"item": "xuanhuan_earth:empty_talisman"},
            {"item": "xuanhuan_earth:basic_talisman"},
            {"item": "xuanhuan_earth:talisman_brush"},
            {"block": "xuanhuan_earth:talisman_table"},
            {"item": "xuanhuan_earth:talisman_table"},
            {"ability": "xuanhuan_earth:talisman_craft"},
            {"ability": "xuanhuan_earth:talisman_activate"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:paper", "count": 8}, {"ingredient": "xuanhuan_earth:spirit_dust", "count": 4}, {"ingredient": "xuanhuan_earth:cinnabar_spirit_ink", "count": 1}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "fire_talisman", "tier": 4, "gate": "hard",
        "zh": "火符", "en": "Fire Talisman",
        "zh_desc": "封存离火真意于符纸之中，解锁火符绘制与激发。",
        "en_desc": "Seal elemental flame within a talisman, unlocking Fire Talisman inscription and casting.",
        "requires": [{"all": "xuanhuan_earth:xua/talisman_basics"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:fire_talisman"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:empty_talisman", "count": 2}, {"ingredient": "minecraft:blaze_powder", "count": 2}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "wind_talisman", "tier": 4, "gate": "soft",
        "zh": "风符", "en": "Wind Talisman",
        "zh_desc": "借巽风之势御空疾行，解锁风符绘制与激发。",
        "en_desc": "Capture swift wind currents in a talisman, unlocking Wind Talisman inscription and dash casting.",
        "requires": [{"all": "xuanhuan_earth:xua/talisman_basics"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:wind_talisman"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:empty_talisman", "count": 2}, {"ingredient": "minecraft:feather", "count": 4}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "seal_talisman", "tier": 5, "gate": "hard",
        "zh": "禁符", "en": "Seal Talisman",
        "zh_desc": "以灵丝符纹束缚气机，解锁禁符绘制与激发。",
        "en_desc": "Bind movement with sealing runes, unlocking Seal Talisman inscription and casting.",
        "requires": [{"all": "xuanhuan_earth:xua/talisman_basics"}, {"all": "xuanhuan_earth:xua/wind_talisman"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:seal_talisman"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:empty_talisman", "count": 2}, {"ingredient": "minecraft:string", "count": 8}], "ticks": 6000},
        "fail_codes": []
    },
    {
        "slug": "array_basics", "tier": 4, "gate": "soft",
        "zh": "阵法入门", "en": "Array Basics",
        "zh_desc": "以灵石核心为眼、阵纹石为基，布置3×3聚灵阵。",
        "en_desc": "Use Spirit Stone Cores and Array Rune Stones to construct 3x3 Spirit Arrays.",
        "requires": [{"all": "xuanhuan_earth:xua/spirit_materials"}, {"all": "xuanhuan_earth:xua/spirit_veins"}],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:spirit_stone_core"},
            {"block": "xuanhuan_earth:spirit_array_core"},
            {"item": "xuanhuan_earth:spirit_array_core"},
            {"item": "xuanhuan_earth:array_rune_stone"},
            {"ability": "xuanhuan_earth:array_form"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_iron_ingot", "count": 2}, {"ingredient": "xuanhuan_earth:spirit_soil", "count": 4}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "array_expansion", "tier": 5, "gate": "soft",
        "zh": "阵型扩展", "en": "Array Expansion",
        "zh_desc": "竖立阵柱并炼制阵法控制盘，远程调控大阵范围与状态。",
        "en_desc": "Erect Array Pillars and craft the Array Control Tablet to expand and manage formations.",
        "requires": [{"all": "xuanhuan_earth:xua/array_basics"}],
        "item_requires": [],
        "unlocks": [
            {"block": "xuanhuan_earth:array_pillar"},
            {"item": "xuanhuan_earth:array_pillar"},
            {"item": "xuanhuan_earth:array_control_tablet"},
            {"ability": "xuanhuan_earth:array_tablet"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:array_rune_stone", "count": 8}, {"ingredient": "xuanhuan_earth:spirit_iron_ingot", "count": 2}], "ticks": 6000},
        "fail_codes": []
    },
    {
        "slug": "array_functions", "tier": 6, "gate": "hard",
        "zh": "阵法五用", "en": "Five Array Functions",
        "zh_desc": "通晓聚灵、充能、护山、助炼、封禁五种阵法运转变化。",
        "en_desc": "Master the five operational modes of formed Spirit Arrays: Gather, Charge, Guard, Alchemy and Seal.",
        "requires": [{"all": "xuanhuan_earth:xua/array_expansion"}],
        "item_requires": [{"cond": "field>=50"}],
        "unlocks": [{"ability": "xuanhuan_earth:array_functions"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_core", "count": 1}, {"ingredient": "xuanhuan_earth:array_rune_stone", "count": 8}], "ticks": 7200},
        "fail_codes": ["field_low"]
    },
    {
        "slug": "spirit_veins", "tier": 3, "gate": "soft",
        "zh": "灵脉", "en": "Spirit Veins",
        "zh_desc": "勘察地下灵脉节点与灵泉石，汇聚地脉灵机。",
        "en_desc": "Understand underground Spirit Vein Nodes and Spirit Spring Stones.",
        "requires": [{"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [],
        "unlocks": [
            {"block": "xuanhuan_earth:spirit_vein_node"},
            {"item": "xuanhuan_earth:spirit_vein_node"},
            {"block": "xuanhuan_earth:spirit_spring_stone"},
            {"item": "xuanhuan_earth:spirit_spring_stone"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 4}, {"ingredient": "xuanhuan_earth:spirit_stone", "count": 1}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "jade_slips", "tier": 4, "gate": "soft",
        "zh": "玉简", "en": "Jade Slips & Wards",
        "zh_desc": "雕琢温润玉料，制作记载玄学经典的玉简、玉护符与玉阵阵盘。",
        "en_desc": "Carve fine jade into Jade Slips, Jade Ward Amulets and Jade Array Plates.",
        "requires": [
            {"all": "xuanhuan_earth:xua/spirit_stones"},
            {"any": ["earth_on_minecraft:geo/gemology", "xuanhuan_earth:xua/stone_reading"]}
        ],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:jade_slip"},
            {"item": "xuanhuan_earth:jade_ward_amulet"},
            {"item": "xuanhuan_earth:jade_array_plate"}
        ],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 4}, {"ingredient": "minecraft:paper", "count": 4}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "geomancy", "tier": 4, "gate": "soft",
        "zh": "堪舆", "en": "Geomancy",
        "zh_desc": "仰观天象、俯察山川水势，制作风水罗盘评定地气品阶。",
        "en_desc": "Survey terrain, water and sky alignment with a Geomantic Luopan.",
        "requires": [
            {"all": "xuanhuan_earth:xua/spirit_veins"},
            {"any": ["earth_on_minecraft:geo/stratigraphy", "xuanhuan_earth:xua/stone_reading"]}
        ],
        "item_requires": [{"cond": "sky"}],
        "unlocks": [{"item": "xuanhuan_earth:luopan"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:compass", "count": 1}, {"ingredient": "xuanhuan_earth:spirit_dust", "count": 4}, {"ingredient": "minecraft:paper", "count": 4}], "ticks": 4800},
        "fail_codes": ["sky_blocked"]
    },
    {
        "slug": "dragon_vein_seeking", "tier": 5, "gate": "soft",
        "zh": "寻龙点穴", "en": "Dragon-Vein Seeking",
        "zh_desc": "洞悉地脉流向，解锁罗盘详尽分项读数与寻龙尺点穴之术。",
        "en_desc": "Pinpoint chunk acupoints with the Dragon-Seeking Rod and unlock full Luopan sub-score readouts.",
        "requires": [{"all": "xuanhuan_earth:xua/geomancy"}],
        "item_requires": [{"hold": "xuanhuan_earth:luopan"}],
        "unlocks": [
            {"item": "xuanhuan_earth:dragon_seeking_rod"},
            {"readout": "xuanhuan_earth:luopan"}
        ],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_crystal_shard", "count": 8}, {"ingredient": "xuanhuan_earth:spirit_stone", "count": 2}], "ticks": 6000},
        "fail_codes": []
    },
    {
        "slug": "stone_reading", "tier": 2, "gate": "theory",
        "zh": "观山辨石（自研）", "en": "Self-Taught Stone Reading",
        "zh_desc": "未习地质学时，凭山势石理与灵气润泽辨识玉石地脉。",
        "en_desc": "Discern jade and strata through mountain forms and qi resonance when formal Geology is unavailable.",
        "requires": [{"all": "xuanhuan_earth:xua/qi_guiding"}],
        "item_requires": [],
        "unlocks": [],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:cobblestone", "count": 16}, {"ingredient": "minecraft:clay_ball", "count": 8}, {"ingredient": "minecraft:paper", "count": 8}], "ticks": 4800},
        "fail_codes": []
    },
    {
        "slug": "divination", "tier": 3, "gate": "soft",
        "zh": "卜卦", "en": "Coin Divination",
        "zh_desc": "以三枚铜钱推演六十四卦，占测晴雨雷霆与今日吉凶。",
        "en_desc": "Cast the 64 hexagrams with Divination Coins to forecast weather and receive auspicious omens.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:divination_coins"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:copper_ingot", "count": 3}, {"ingredient": "minecraft:paper", "count": 4}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "bone_oracle", "tier": 4, "gate": "soft",
        "zh": "甲骨占", "en": "Oracle Bone Divination",
        "zh_desc": "夜间灼骨观裂，感应远方古迹与聚落的方位距离。",
        "en_desc": "Scorch Oracle Bones at night to divine the direction and distance of distant structures.",
        "requires": [{"all": "xuanhuan_earth:xua/spirit_veins"}, {"all": "xuanhuan_earth:xua/divination"}],
        "item_requires": [{"cond": "night"}],
        "unlocks": [{"item": "xuanhuan_earth:oracle_bone"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:bone", "count": 8}, {"ingredient": "minecraft:flint_and_steel", "count": 1}], "ticks": 4800},
        "fail_codes": ["needs_night"]
    },
    {
        "slug": "spirit_beasts", "tier": 3, "gate": "soft",
        "zh": "灵兽", "en": "Spirit Beast Contracts",
        "zh_desc": "体察灵兽习性，制作灵兽契印与亲和圆满的灵兽缔结契约。",
        "en_desc": "Understand spirit creatures and craft Spirit Beast Seals to form lasting contracts.",
        "requires": [{"all": "xuanhuan_earth:xua/circulation"}, {"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [],
        "unlocks": [{"item": "xuanhuan_earth:spirit_beast_seal"}],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:sweet_berries", "count": 16}, {"ingredient": "xuanhuan_earth:spirit_dust", "count": 4}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "spirit_farming", "tier": 3, "gate": "soft",
        "zh": "灵植与灵土", "en": "Spirit Farming",
        "zh_desc": "取灵泉水培植灵土，栽种并催熟灵草。",
        "en_desc": "Collect Spirit Spring Bottles to enrich Spirit Soil and cultivate Spirit Herbs.",
        "requires": [{"all": "xuanhuan_earth:xua/spirit_stones"}],
        "item_requires": [],
        "unlocks": [
            {"item": "xuanhuan_earth:spirit_herb"},
            {"item": "xuanhuan_earth:spirit_spring_bottle"},
            {"block": "xuanhuan_earth:spirit_soil"},
            {"item": "xuanhuan_earth:spirit_soil"}
        ],
        "literature": ["notes"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:dirt", "count": 8}, {"ingredient": "minecraft:bone_meal", "count": 8}, {"ingredient": "minecraft:glass_bottle", "count": 2}], "ticks": 2400},
        "fail_codes": []
    },
    {
        "slug": "spell_graphs", "tier": 6, "gate": "hard",
        "zh": "术式推演", "en": "Spell Graph Deduction",
        "zh_desc": "推演能量、控制与目标节点，自定义加载高阶术式图。",
        "en_desc": "Deduce custom spell graph topologies and load data-driven spell graphs.",
        "requires": [{"all": "xuanhuan_earth:xua/talisman_basics"}, {"all": "xuanhuan_earth:xua/array_expansion"}],
        "item_requires": [],
        "unlocks": [
            {"ability": "xuanhuan_earth:spell_graph_structure"},
            {"ability": "xuanhuan_earth:spell_graph_load"}
        ],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "minecraft:book", "count": 2}, {"ingredient": "xuanhuan_earth:cinnabar_spirit_ink", "count": 4}, {"ingredient": "xuanhuan_earth:array_rune_stone", "count": 4}], "ticks": 7200},
        "fail_codes": []
    },
    {
        "slug": "xuanxue_mastery", "tier": 10, "gate": "cap",
        "zh": "玄学精研", "en": "Xuanxue Mastery",
        "zh_desc": "融汇丹鼎、符箓、阵法、堪舆、占卜与化神大道，登峰造极，解锁玄学传世品质。",
        "en_desc": "Unify inner cultivation, pill alchemy, talismans, formations and divination to unlock heirloom-grade Xuanxue creations.",
        "requires": [
            {"all": "xuanhuan_earth:xua/spirit_transformation"},
            {"all": "xuanhuan_earth:xua/herbal_formulas"},
            {"all": "xuanhuan_earth:xua/fire_talisman"},
            {"all": "xuanhuan_earth:xua/seal_talisman"},
            {"all": "xuanhuan_earth:xua/array_functions"},
            {"all": "xuanhuan_earth:xua/jade_slips"},
            {"all": "xuanhuan_earth:xua/dragon_vein_seeking"},
            {"all": "xuanhuan_earth:xua/bone_oracle"},
            {"all": "xuanhuan_earth:xua/spirit_beasts"},
            {"all": "xuanhuan_earth:xua/spirit_farming"},
            {"all": "xuanhuan_earth:xua/spell_graphs"}
        ],
        "item_requires": [],
        "unlocks": [{"capstone": "earth_lore:xua"}],
        "literature": ["notes", "jade_slip"],
        "teachers": [],
        "research": {"items": [{"ingredient": "xuanhuan_earth:spirit_stone_core", "count": 4}, {"ingredient": "xuanhuan_earth:jade_slip", "count": 2}, {"ingredient": "xuanhuan_earth:breakthrough_pill", "count": 2}], "ticks": 16800},
        "fail_codes": []
    }
]

for n in NODES:
    slug = n["slug"]
    node_json = {
        "discipline": "earth_lore:xua",
        "tier": n["tier"],
        "gate": n["gate"],
        "requires": n["requires"],
        "item_requires": n["item_requires"],
        "unlocks": n["unlocks"],
        "literature": n["literature"],
        "teachers": n["teachers"],
        "research": n["research"],
        "fail_codes": n["fail_codes"],
        "name_key": f"lore.xuanhuan_earth.xua.{slug}",
        "desc_key": f"lore.xuanhuan_earth.xua.{slug}.desc"
    }
    write_json(DATA / "xuanhuan_earth" / "earth_lore" / "knowledge_node" / "xua" / f"{slug}.json", node_json)

# 2. carrier_forms.json, teaching.json, tags
write_json(DATA / "xuanhuan_earth" / "earth_lore" / "carrier_forms.json", {
    "forms": [
        {
            "carrier": "xuanhuan_earth:jade_slip",
            "result": "xuanhuan_earth:jade_slip",
            "discipline": "earth_lore:xua",
            "study_factor": 0.5
        }
    ]
})

teaching_map = {
    "replace": False,
    "values": {
        "xuanhuan_earth:cultivation_settler": [
            {"node": "xuanhuan_earth:xua/qi_guiding", "price": 8, "max_uses": 3},
            {"node": "xuanhuan_earth:xua/circulation", "price": 12, "max_uses": 3},
            {"node": "xuanhuan_earth:xua/spirit_stones", "price": 12, "max_uses": 3}
        ]
    }
}
write_json(DATA / "earth_lore" / "data_maps" / "entity_type" / "teaching.json", teaching_map)
write_json(DATA / "xuanhuan_earth" / "data_maps" / "entity_type" / "earth_lore" / "teaching.json", teaching_map)

write_json(DATA / "earth_lore" / "tags" / "item" / "carriers.json", {
    "replace": False,
    "values": ["xuanhuan_earth:jade_slip"]
})
write_json(DATA / "earth_lore" / "tags" / "item" / "custom_study.json", {
    "replace": False,
    "values": [
        "xuanhuan_earth:jade_slip",
        "xuanhuan_earth:qi_guiding_manual",
        "xuanhuan_earth:body_tempering_manual",
        "xuanhuan_earth:fetal_breath_manual",
        "xuanhuan_earth:bigu_manual"
    ]
})
write_json(DATA / "earth_lore" / "tags" / "block" / "records_owner.json", {
    "replace": False,
    "values": [
        "xuanhuan_earth:alchemy_furnace",
        "xuanhuan_earth:talisman_table",
        "xuanhuan_earth:spirit_array_core"
    ]
})
write_json(DATA / "xuanhuan_earth" / "tags" / "worldgen" / "structure" / "oracle_targets.json", {
    "replace": False,
    "values": [
        "#minecraft:village",
        "minecraft:ancient_city",
        "minecraft:trial_chambers",
        "minecraft:stronghold"
    ]
})
write_json(DATA / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json", {
    "replace": False,
    "values": [
        "xuanhuan_earth:spirit_vein_node",
        "xuanhuan_earth:spirit_spring_stone",
        "xuanhuan_earth:alchemy_furnace",
        "xuanhuan_earth:spirit_array_core",
        "xuanhuan_earth:array_pillar"
    ]
})
write_json(DATA / "minecraft" / "tags" / "block" / "needs_stone_tool.json", {
    "replace": False,
    "values": [
        "xuanhuan_earth:spirit_vein_node",
        "xuanhuan_earth:spirit_spring_stone",
        "xuanhuan_earth:alchemy_furnace",
        "xuanhuan_earth:spirit_array_core",
        "xuanhuan_earth:array_pillar"
    ]
})

# 3. Blockstates, block models, loot tables
write_json(ASSETS / "blockstates" / "meditation_cushion.json", {
    "variants": {
        "facing=north,occupied=false": {"model": "xuanhuan_earth:block/meditation_cushion"},
        "facing=east,occupied=false": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 90},
        "facing=south,occupied=false": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 180},
        "facing=west,occupied=false": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 270},
        "facing=north,occupied=true": {"model": "xuanhuan_earth:block/meditation_cushion"},
        "facing=east,occupied=true": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 90},
        "facing=south,occupied=true": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 180},
        "facing=west,occupied=true": {"model": "xuanhuan_earth:block/meditation_cushion", "y": 270}
    }
})
write_json(ASSETS / "blockstates" / "array_pillar.json", {
    "variants": {
        "": {"model": "xuanhuan_earth:block/array_pillar"}
    }
})
write_json(ASSETS / "models" / "block" / "array_pillar.json", {
    "parent": "minecraft:block/cube_all",
    "textures": {"all": "xuanhuan_earth:block/array_pillar"}
})
write_json(ASSETS / "models" / "item" / "array_pillar.json", {
    "parent": "xuanhuan_earth:block/array_pillar"
})
write_json(ASSETS / "items" / "array_pillar.json", {
    "model": {"type": "minecraft:model", "model": "xuanhuan_earth:item/array_pillar"}
})
write_block_png(ASSETS / "textures" / "block" / "array_pillar.png", (74, 96, 112), (42, 56, 68), (118, 224, 212))
write_json(DATA / "xuanhuan_earth" / "loot_table" / "blocks" / "array_pillar.json", {
    "type": "minecraft:block",
    "pools": [{
        "rolls": 1.0,
        "entries": [{"type": "minecraft:item", "name": "xuanhuan_earth:array_pillar"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]
    }],
    "random_sequence": "xuanhuan_earth:blocks/array_pillar"
})
write_json(DATA / "xuanhuan_earth" / "loot_table" / "blocks" / "spirit_vein_node.json", {
    "type": "minecraft:block",
    "pools": [{
        "rolls": 1.0,
        "entries": [{
            "type": "minecraft:item",
            "name": "xuanhuan_earth:spirit_crystal_shard",
            "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 3.0}},
                {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
                {"function": "minecraft:explosion_decay"}
            ]
        }]
    }],
    "random_sequence": "xuanhuan_earth:blocks/spirit_vein_node"
})

# 4. New items: item definitions, models, textures
NEW_ITEMS = {
    "cultivation_journal": ((176, 138, 86), (98, 68, 38), (242, 214, 142), "tablet"),
    "spirit_herb": ((76, 178, 108), (38, 104, 58), (168, 244, 184), "round"),
    "empty_talisman": ((236, 214, 146), (168, 136, 74), (212, 182, 108), "talisman"),
    "fire_talisman": ((236, 198, 128), (162, 74, 42), (244, 86, 38), "talisman"),
    "wind_talisman": ((218, 236, 214), (84, 148, 128), (112, 232, 196), "talisman"),
    "seal_talisman": ((214, 204, 238), (92, 68, 142), (176, 118, 248), "talisman"),
    "talisman_brush": ((142, 98, 58), (78, 52, 28), (218, 58, 52), "rod"),
    "spirit_dust": ((156, 218, 238), (82, 138, 168), (224, 248, 255), "round"),
    "spirit_stone_raw": ((132, 146, 158), (76, 86, 96), (142, 216, 228), "round"),
    "spirit_stone_cracked": ((118, 164, 178), (64, 82, 92), (72, 48, 54), "round"),
    "spirit_stone_core": ((96, 214, 238), (44, 118, 148), (232, 252, 255), "round"),
    "spirit_iron_ingot": ((148, 176, 196), (78, 98, 116), (172, 236, 248), "ingot"),
    "spirit_glass": ((186, 236, 246), (108, 168, 188), (242, 254, 255), "tablet"),
    "spirit_thread": ((226, 238, 248), (138, 164, 186), (186, 244, 255), "round"),
    "marrow_pill": ((196, 142, 92), (112, 72, 42), (248, 212, 156), "pill"),
    "breath_pill": ((104, 198, 218), (48, 112, 132), (206, 248, 255), "pill"),
    "fast_pill": ((214, 188, 102), (128, 104, 46), (252, 236, 168), "pill"),
    "detox_pill": ((98, 196, 132), (44, 112, 68), (198, 248, 212), "pill"),
    "pill_residue": ((96, 84, 74), (54, 46, 40), (132, 116, 98), "round"),
    "failed_pill": ((84, 92, 68), (46, 52, 36), (138, 156, 82), "pill"),
    "nourish_qi_pill": ((228, 176, 88), (138, 96, 36), (255, 228, 156), "pill"),
    "calm_spirit_pill": ((164, 142, 218), (88, 68, 136), (228, 214, 255), "pill"),
    "clear_heat_pill": ((112, 208, 186), (52, 118, 102), (208, 252, 238), "pill"),
    "invigorate_blood_pill": ((212, 78, 84), (124, 36, 42), (252, 164, 168), "pill"),
    "resolve_toxin_pill": ((132, 208, 118), (64, 118, 54), (218, 252, 198), "pill"),
    "jade_slip": ((118, 212, 168), (56, 128, 94), (212, 252, 232), "tablet"),
    "jade_ward_amulet": ((96, 198, 154), (48, 114, 84), (238, 212, 124), "round"),
    "jade_array_plate": ((84, 188, 146), (42, 104, 78), (168, 248, 218), "tablet"),
    "array_rune_stone": ((112, 124, 138), (62, 70, 82), (228, 92, 78), "tablet"),
    "array_control_tablet": ((92, 134, 158), (48, 74, 92), (148, 236, 248), "tablet"),
    "luopan": ((188, 148, 84), (98, 72, 36), (228, 68, 58), "round"),
    "dragon_seeking_rod": ((156, 182, 204), (78, 96, 114), (138, 238, 248), "rod"),
    "divination_coins": ((208, 142, 76), (118, 74, 34), (248, 212, 128), "round"),
    "oracle_bone": ((228, 222, 198), (146, 138, 114), (168, 78, 48), "tablet")
}

for slug, (c1, c2, c3, shape) in NEW_ITEMS.items():
    item_def = ASSETS / "items" / f"{slug}.json"
    if not item_def.exists():
        write_json(item_def, {"model": {"type": "minecraft:model", "model": f"xuanhuan_earth:item/{slug}"}})
    model_def = ASSETS / "models" / "item" / f"{slug}.json"
    if not model_def.exists():
        parent = "minecraft:item/handheld" if shape == "rod" else "minecraft:item/generated"
        write_json(model_def, {"parent": parent, "textures": {"layer0": f"xuanhuan_earth:item/{slug}"}})
    tex_path = ASSETS / "textures" / "item" / f"{slug}.png"
    if not tex_path.exists():
        write_item_png(tex_path, c1, c2, c3, shape)

write_effect_png(ASSETS / "textures" / "mob_effect" / "auspicious.png")

# 5. Crafting Recipes (02-xuanhuan-earth.md)
RECIPES = {
    "spirit_dust": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["xuanhuan_earth:spirit_crystal_shard"],
        "result": {"id": "xuanhuan_earth:spirit_dust", "count": 2}
    },
    "spirit_stone_raw": {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": [" D ", "DSD", " D "],
        "key": {"D": "xuanhuan_earth:spirit_dust", "S": "minecraft:stone"},
        "result": {"id": "xuanhuan_earth:spirit_stone_raw", "count": 2}
    },
    "spirit_stone_core": {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["CSC", "SIS", "CSC"],
        "key": {
            "C": "xuanhuan_earth:spirit_crystal_shard",
            "S": "xuanhuan_earth:spirit_stone",
            "I": "xuanhuan_earth:spirit_iron_ingot"
        },
        "result": {"id": "xuanhuan_earth:spirit_stone_core", "count": 1}
    },
    "cultivation_journal": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["minecraft:book", "xuanhuan_earth:spirit_dust"],
        "result": {"id": "xuanhuan_earth:cultivation_journal", "count": 1}
    },
    "empty_talisman": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["xuanhuan_earth:talisman_paper", "xuanhuan_earth:cinnabar_spirit_ink"],
        "result": {"id": "xuanhuan_earth:empty_talisman", "count": 2}
    },
    "talisman_brush": {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["  F", " S ", "C  "],
        "key": {
            "F": "minecraft:feather",
            "S": "minecraft:stick",
            "C": "xuanhuan_earth:spirit_crystal_shard"
        },
        "result": {"id": "xuanhuan_earth:talisman_brush", "count": 1}
    },
    "array_rune_stone": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["minecraft:stone", "minecraft:stone", "xuanhuan_earth:cinnabar_spirit_ink"],
        "result": {"id": "xuanhuan_earth:array_rune_stone", "count": 2}
    },
    "array_pillar": {
        "type": "minecraft:crafting_shapeless",
        "category": "building",
        "ingredients": ["xuanhuan_earth:spirit_iron_ingot", "xuanhuan_earth:array_rune_stone"],
        "result": {"id": "xuanhuan_earth:array_pillar", "count": 2}
    },
    "array_control_tablet": {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": [" R ", "ISI", " R "],
        "key": {
            "R": "xuanhuan_earth:array_rune_stone",
            "I": "xuanhuan_earth:spirit_iron_ingot",
            "S": "xuanhuan_earth:spirit_stone"
        },
        "result": {"id": "xuanhuan_earth:array_control_tablet", "count": 1}
    },
    "luopan": {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": [" P ", "PCP", " S "],
        "key": {
            "P": "#minecraft:planks",
            "C": "minecraft:compass",
            "S": "xuanhuan_earth:spirit_crystal_shard"
        },
        "result": {"id": "xuanhuan_earth:luopan", "count": 1}
    },
    "dragon_seeking_rod": {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["  C", " I ", "S  "],
        "key": {
            "C": "xuanhuan_earth:spirit_crystal_shard",
            "I": "xuanhuan_earth:spirit_iron_ingot",
            "S": "minecraft:stick"
        },
        "result": {"id": "xuanhuan_earth:dragon_seeking_rod", "count": 1}
    },
    "divination_coins": {
        "type": "minecraft:crafting_shapeless",
        "category": "equipment",
        "ingredients": [
            "minecraft:copper_ingot",
            "minecraft:copper_ingot",
            "minecraft:copper_ingot",
            "xuanhuan_earth:cinnabar_spirit_ink"
        ],
        "result": {"id": "xuanhuan_earth:divination_coins", "count": 1}
    },
    "oracle_bone": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["minecraft:bone", "xuanhuan_earth:spirit_dust"],
        "result": {"id": "xuanhuan_earth:oracle_bone", "count": 2}
    },
    "spirit_herb": {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["xuanhuan_earth:spirit_grass", "minecraft:bone_meal"],
        "result": {"id": "xuanhuan_earth:spirit_herb", "count": 2}
    },
    "cinnabar_spirit_ink": {
        "neoforge:conditions": [{"type": "neoforge:not", "value": {"type": "neoforge:mod_loaded", "modid": "earth_on_minecraft"}}],
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": ["minecraft:redstone", "minecraft:ink_sac", "xuanhuan_earth:spirit_crystal_shard"],
        "result": {"id": "xuanhuan_earth:cinnabar_spirit_ink", "count": 2}
    },
    "jade_ward_amulet_from_earth": {
        "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "earth_on_minecraft"}],
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": [" S ", "NJN", " S "],
        "key": {
            "S": "xuanhuan_earth:spirit_iron_ingot",
            "N": "#earth_on_minecraft:gems/nephrite",
            "J": "xuanhuan_earth:empty_talisman"
        },
        "result": {"id": "xuanhuan_earth:jade_ward_amulet", "count": 1}
    },
    "jade_ward_amulet": {
        "neoforge:conditions": [{"type": "neoforge:not", "value": {"type": "neoforge:mod_loaded", "modid": "earth_on_minecraft"}}],
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": [" S ", "NJN", " S "],
        "key": {
            "S": "xuanhuan_earth:spirit_iron_ingot",
            "N": "minecraft:emerald",
            "J": "xuanhuan_earth:empty_talisman"
        },
        "result": {"id": "xuanhuan_earth:jade_ward_amulet", "count": 1}
    },
    "jade_array_plate_from_earth": {
        "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "earth_on_minecraft"}],
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["JRJ"],
        "key": {
            "J": "#earth_on_minecraft:gems/jadeite",
            "R": "xuanhuan_earth:array_rune_stone"
        },
        "result": {"id": "xuanhuan_earth:jade_array_plate", "count": 1}
    },
    "jade_array_plate": {
        "neoforge:conditions": [{"type": "neoforge:not", "value": {"type": "neoforge:mod_loaded", "modid": "earth_on_minecraft"}}],
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["JRJ"],
        "key": {
            "J": "minecraft:amethyst_shard",
            "R": "xuanhuan_earth:array_rune_stone"
        },
        "result": {"id": "xuanhuan_earth:jade_array_plate", "count": 1}
    }
}
for r_name, r_json in RECIPES.items():
    write_json(DATA / "xuanhuan_earth" / "recipe" / f"{r_name}.json", r_json)

# 6. Language files (zh_cn.json & en_us.json)
HEXAGRAMS_ZH = [
    "乾", "坤", "屯", "蒙", "需", "讼", "师", "比",
    "小畜", "履", "泰", "否", "同人", "大有", "谦", "豫",
    "随", "蛊", "临", "观", "噬嗑", "贲", "剥", "复",
    "无妄", "大畜", "颐", "大过", "坎", "离", "咸", "恒",
    "遯", "大壮", "晋", "明夷", "家人", "睽", "蹇", "解",
    "损", "益", "夬", "姤", "萃", "升", "困", "井",
    "革", "鼎", "震", "艮", "渐", "归妹", "丰", "旅",
    "巽", "兑", "涣", "节", "中孚", "小过", "既济", "未济"
]
HEXAGRAMS_EN = [
    "Qian (The Creative)", "Kun (The Receptive)", "Zhun (Difficulty at the Beginning)", "Meng (Youthful Folly)",
    "Xu (Waiting)", "Song (Conflict)", "Shi (The Army)", "Bi (Holding Together)",
    "Xiao Chu (Small Taming)", "Lv (Treading)", "Tai (Peace)", "Pi (Standstill)",
    "Tong Ren (Fellowship)", "Da You (Great Possession)", "Qian (Modesty)", "Yu (Enthusiasm)",
    "Sui (Following)", "Gu (Work on the Decayed)", "Lin (Approach)", "Guan (Contemplation)",
    "Shi He (Biting Through)", "Bi (Grace)", "Bo (Splitting Apart)", "Fu (Return)",
    "Wu Wang (Innocence)", "Da Chu (Great Taming)", "Yi (Corners of the Mouth)", "Da Guo (Preponderance of the Great)",
    "Kan (The Abysmal)", "Li (The Clinging)", "Xian (Influence)", "Heng (Duration)",
    "Dun (Retreat)", "Da Zhuang (Great Power)", "Jin (Progress)", "Ming Yi (Darkening of the Light)",
    "Jia Ren (The Family)", "Kui (Opposition)", "Jian (Obstruction)", "Xie (Deliverance)",
    "Sun (Decrease)", "Yi (Increase)", "Guai (Breakthrough)", "Gou (Coming to Meet)",
    "Cui (Gathering Together)", "Sheng (Pushing Upward)", "Kun (Oppression)", "Jing (The Well)",
    "Ge (Revolution)", "Ding (The Cauldron)", "Zhen (The Arousing)", "Gen (Keeping Still)",
    "Jian (Development)", "Gui Mei (The Marrying Maiden)", "Feng (Abundance)", "Lv (The Wanderer)",
    "Xun (The Gentle)", "Dui (The Joyous)", "Huan (Dispersion)", "Jie (Limitation)",
    "Zhong Fu (Inner Truth)", "Xiao Guo (Small Preponderance)", "Ji Ji (After Completion)", "Wei Ji (Before Completion)"
]

zh_path = ASSETS / "lang" / "zh_cn.json"
en_path = ASSETS / "lang" / "en_us.json"
zh = json.loads(zh_path.read_text(encoding="utf-8"))
en = json.loads(en_path.read_text(encoding="utf-8"))

zh_updates = {
    "itemGroup.xuanhuan_earth": "地球：玄学",
    "key.categories.xuanhuan_earth.controls": "地球：玄学",
    "item.xuanhuan_earth.xuanhuan_handbook": "地球：玄学 手册",
    "screen.xuanhuan_earth.handbook.title": "地球：玄学 手册",
    "effect.xuanhuan_earth.auspicious": "吉兆",
    "block.xuanhuan_earth.array_pillar": "阵柱",
    "item.xuanhuan_earth.array_pillar": "阵柱",
    "message.xuanhuan_earth.block.array_pillar": "阵柱立定：%s（%s/100）。配合聚灵阵可扩展阵法威能（最多 12 根）。",
    "item.xuanhuan_earth.cultivation_journal": "修行札记",
    "tooltip.xuanhuan_earth.cultivation_journal": "记录四条修炼路线的等级、修为进度与破境瓶颈提示。",
    "item.xuanhuan_earth.spirit_herb": "灵草",
    "tooltip.xuanhuan_earth.spirit_herb": "在灵土上温养而生的炼丹主药。",
    "item.xuanhuan_earth.empty_talisman": "空白符",
    "tooltip.xuanhuan_earth.empty_talisman": "以符纸配朱砂灵墨裁成的制符基材。",
    "item.xuanhuan_earth.fire_talisman": "火符",
    "tooltip.xuanhuan_earth.fire_talisman": "封存离火之力的攻击符箓。右键消耗 2 点法力激发火弹。",
    "item.xuanhuan_earth.wind_talisman": "风符",
    "tooltip.xuanhuan_earth.wind_talisman": "封存巽风之力的身法符箓。右键消耗 1 点法力向前疾行 6 格。",
    "item.xuanhuan_earth.seal_talisman": "禁符",
    "tooltip.xuanhuan_earth.seal_talisman": "封存禁锢之力的控制符箓。右键消耗 3 点法力迟滞前方目标。",
    "item.xuanhuan_earth.talisman_brush": "符笔",
    "tooltip.xuanhuan_earth.talisman_brush": "符案制符工具，耐久 64，每次成符消耗 1 点耐久（吉兆时不耗）。",
    "item.xuanhuan_earth.spirit_dust": "灵尘",
    "tooltip.xuanhuan_earth.spirit_dust": "由灵晶碎片研磨而成的微细灵粉，用于制墨、符纸与学识研究。",
    "item.xuanhuan_earth.spirit_stone_raw": "灵石原胚",
    "tooltip.xuanhuan_earth.spirit_stone_raw": "尚未充能的矿物基质原胚，可在聚灵阵中富集为灵石。",
    "tooltip.xuanhuan_earth.spirit_stone_raw.grade": "基质品位：%s 阶（可于聚灵阵富集）",
    "item.xuanhuan_earth.spirit_stone_cracked": "裂纹灵石",
    "tooltip.xuanhuan_earth.spirit_stone_cracked": "因过度充能而受损的灵石，储能上限降至 60 AEU。",
    "message.xuanhuan_earth.spirit_stone.cracked": "灵石基质稳定度跌破阈值，化为裂纹灵石（上限 60 AEU）！",
    "item.xuanhuan_earth.spirit_stone_core": "灵石核心",
    "tooltip.xuanhuan_earth.spirit_stone_core": "容量 480 AEU 的阵眼储能核心，仅接受成形聚灵阵充能。",
    "message.xuanhuan_earth.spirit_stone_core.not_formed": "聚灵阵尚未按 3×3 正式成型，无法为灵石核心充能。",
    "message.xuanhuan_earth.spirit_stone_core.charged": "灵石核心充能：%s / %s AEU。",
    "item.xuanhuan_earth.spirit_iron_ingot": "灵铁锭",
    "tooltip.xuanhuan_earth.spirit_iron_ingot": "由两枚灵铁胚在丹炉高温锻铸而成的灵材金属锭。",
    "item.xuanhuan_earth.spirit_glass": "灵化玻璃",
    "tooltip.xuanhuan_earth.spirit_glass": "掺入灵尘熔炼的通透琉璃。",
    "item.xuanhuan_earth.spirit_thread": "灵丝",
    "tooltip.xuanhuan_earth.spirit_thread": "蕴含灵气的柔韧细丝，可用于符绳、法器与契约。",
    "item.xuanhuan_earth.marrow_pill": "洗髓丹",
    "tooltip.xuanhuan_earth.marrow_pill": "需已习炼体路线。服用后炼体修为 +120。",
    "message.xuanhuan_earth.pill.marrow_pill": "洗髓丹化入骨髓：法力 +%s（%s/%s），炼体修为大进。",
    "item.xuanhuan_earth.breath_pill": "闭息丹",
    "tooltip.xuanhuan_earth.breath_pill": "需已习胎息路线。服用后胎息修为 +120。",
    "message.xuanhuan_earth.pill.breath_pill": "闭息丹护住心脉：法力 +%s（%s/%s），胎息修为精进。",
    "item.xuanhuan_earth.fast_pill": "辟谷丸",
    "tooltip.xuanhuan_earth.fast_pill": "需已习辟谷路线。服用后锁定饱食度 1200 刻。",
    "message.xuanhuan_earth.pill.fast_pill": "辟谷丸化开：法力 +%s（%s/%s），饥饿感消退。",
    "item.xuanhuan_earth.detox_pill": "解毒丹",
    "tooltip.xuanhuan_earth.detox_pill": "清除中毒、虚弱与反胃状态。",
    "message.xuanhuan_earth.pill.detox_pill": "解毒丹清涤浊气：法力 +%s（%s/%s），体内诸毒尽散。",
    "item.xuanhuan_earth.pill_residue": "丹渣",
    "tooltip.xuanhuan_earth.pill_residue": "炼丹残留的药渣，可在丹炉中回炉重炼。",
    "item.xuanhuan_earth.failed_pill": "废丹",
    "tooltip.xuanhuan_earth.failed_pill": "火候失当或药性冲突产生的废丹，服用会引起中毒。",
    "message.xuanhuan_earth.pill.failed_pill": "废丹入口苦涩，浊气攻心（%s/%s）！",
    "item.xuanhuan_earth.nourish_qi_pill": "益气丹",
    "tooltip.xuanhuan_earth.nourish_qi_pill": "本草丹方：人参、黄芪配灵泉，温和补益法力与体魄。",
    "message.xuanhuan_earth.pill.nourish_qi_pill": "益气丹温润化开：法力 +%s（%s/%s），元气充盈。",
    "item.xuanhuan_earth.calm_spirit_pill": "安神丹",
    "tooltip.xuanhuan_earth.calm_spirit_pill": "本草丹方：酸枣仁、柏子仁配灵泉，清除反胃与虚弱并免疫 600 刻。",
    "message.xuanhuan_earth.pill.calm_spirit_pill": "安神丹定心凝神：法力 +%s（%s/%s），神魂清明。",
    "item.xuanhuan_earth.clear_heat_pill": "清热丹",
    "tooltip.xuanhuan_earth.clear_heat_pill": "本草丹方：黄连、连翘配灵晶，减轻毒性负面效果。",
    "message.xuanhuan_earth.pill.clear_heat_pill": "清热丹涤荡燥火：法力 +%s（%s/%s），暑毒消减。",
    "item.xuanhuan_earth.invigorate_blood_pill": "活血丹",
    "tooltip.xuanhuan_earth.invigorate_blood_pill": "本草丹方：丹参、红花配灵泉，赋予生命恢复并修复躯干四肢。",
    "message.xuanhuan_earth.pill.invigorate_blood_pill": "活血丹通络生肌：法力 +%s（%s/%s），气血畅行。",
    "item.xuanhuan_earth.resolve_toxin_pill": "化毒丹",
    "tooltip.xuanhuan_earth.resolve_toxin_pill": "本草丹方：甘草、绿豆配灵泉，清除中毒并免疫中毒 300 刻。",
    "message.xuanhuan_earth.pill.resolve_toxin_pill": "化毒丹解厄除瘴：法力 +%s（%s/%s），百毒不侵。",
    "tooltip.xuanhuan_earth.pill.grade": "丹药品质：%s（纯度 %s%%）",
    "message.xuanhuan_earth.pill.breakthrough_only": "破境丹药力霸道，不可直接吞服；请在修炼内景界面点击「破境」使用。",
    "message.xuanhuan_earth.pill.route_locked": "尚未修习%s路线，无法承受此丹药力。",
    "message.xuanhuan_earth.pill.no_effect": "当前未受中毒、虚弱或反胃困扰，无需服用此丹。",
    "item.xuanhuan_earth.jade_slip": "玉简",
    "tooltip.xuanhuan_earth.jade_slip": "软玉雕琢的玄学载体（研读时长减半），亦可储藏 48 AEU 灵气。",
    "item.xuanhuan_earth.jade_ward_amulet": "玉护符",
    "tooltip.xuanhuan_earth.jade_ward_amulet": "温润护身玉符，右键激发护体抗性与伤害吸收。",
    "item.xuanhuan_earth.jade_array_plate": "玉阵阵盘",
    "tooltip.xuanhuan_earth.jade_array_plate": "硬玉阵盘，对聚灵阵使用可绑定并扩展扫描半径（+0..2）。",
    "tooltip.xuanhuan_earth.jade.charge": "玉器灵储：%s / %s AEU",
    "tooltip.xuanhuan_earth.jade_plate.status": "绑定阵眼：%s · 半径加成 +%s",
    "message.xuanhuan_earth.jade_slip.no_field": "玉简已满或当前灵气不足。",
    "message.xuanhuan_earth.jade_slip.charged": "玉简纳气：%s / %s AEU。",
    "message.xuanhuan_earth.jade_slip.release_skip": "法力已满或玉简无储气。",
    "message.xuanhuan_earth.jade_slip.released": "玉简回灵：恢复法力 +%s（余 %s AEU）。",
    "message.xuanhuan_earth.jade_ward.low_charge": "玉护符灵储不足 12 AEU，请先充能。",
    "message.xuanhuan_earth.jade_ward.activated": "玉护符激发温润护罩（余 %s AEU，稳定度 %s）。",
    "message.xuanhuan_earth.jade_plate.linked": "玉阵阵盘已绑定至阵眼 %s（半径加成 +%s）。",
    "message.xuanhuan_earth.jade_plate.radius": "玉阵阵盘半径加成切换为：+%s 格。",
    "item.xuanhuan_earth.array_rune_stone": "阵纹石",
    "tooltip.xuanhuan_earth.array_rune_stone": "镌刻阵法符纹的基石。潜行右键切换阵纹（聚力/护御/流转/封缚）。",
    "tooltip.xuanhuan_earth.array_rune_stone.rune": "当前阵纹：%s",
    "message.xuanhuan_earth.array_rune_stone.cycled": "阵纹石已切换为：%s",
    "rune.xuanhuan_earth.power": "聚力（Power）",
    "rune.xuanhuan_earth.guard": "护御（Guard）",
    "rune.xuanhuan_earth.flow": "流转（Flow）",
    "rune.xuanhuan_earth.bind": "封缚（Bind）",
    "item.xuanhuan_earth.array_control_tablet": "阵法控制盘",
    "tooltip.xuanhuan_earth.array_control_tablet": "对聚灵阵右键绑定；之后右键切换阵法五用模式，潜行右键调节作用半径（4..16）。",
    "tooltip.xuanhuan_earth.array_tablet.status": "绑定阵眼：%s · 作用半径：%s 格",
    "message.xuanhuan_earth.array_tablet.linked": "阵法控制盘已绑定阵眼：%s。",
    "message.xuanhuan_earth.array_tablet.no_linked_array": "尚未绑定有效的聚灵阵眼。",
    "message.xuanhuan_earth.array_tablet.mode": "大阵模式切换为：%s（场域 %s/100，阵眼储能 %s AEU）",
    "message.xuanhuan_earth.array_tablet.radius": "大阵作用半径调整为：%s 格。",
    "item.xuanhuan_earth.luopan": "罗盘",
    "tooltip.xuanhuan_earth.luopan": "堪舆罗盘：右键评定脚下地气品阶（1..9）；习得「寻龙点穴」后显示四项分值与最近灵脉。",
    "tooltip.xuanhuan_earth.luopan.last_rating": "上次地气评级：%s 阶",
    "message.xuanhuan_earth.luopan.cooldown": "罗盘天池磁针仍在定盘，请稍候。",
    "message.xuanhuan_earth.luopan.rating": "堪舆定盘：此处地气评级为 %s 阶（1..9）。",
    "message.xuanhuan_earth.luopan.detail": "分项读数：场值 %s / 近水 %s / 北山 %s / 露天 %s。",
    "message.xuanhuan_earth.luopan.vein": "灵脉感应：%s方向 约 %s 格（高差 %s）。",
    "message.xuanhuan_earth.luopan.no_vein": "附近无灵脉",
    "item.xuanhuan_earth.dragon_seeking_rod": "寻龙尺",
    "tooltip.xuanhuan_earth.dragon_seeking_rod": "右键勘定本区块灵气源块加权质心并点出穴位；穴位上聚灵阵加成 +4，蒲团打坐修为 +1。",
    "message.xuanhuan_earth.dragon_rod.broken": "寻龙尺已受损，无法继续点穴。",
    "message.xuanhuan_earth.dragon_rod.cooldown": "寻龙尺气机未平，请稍候。",
    "message.xuanhuan_earth.dragon_rod.marked": "寻龙点穴完成：本区块灵穴定于 (%s, %s, %s)。",
    "item.xuanhuan_earth.divination_coins": "卜卦铜钱",
    "tooltip.xuanhuan_earth.divination_coins": "每日一卦：推演文王六十四卦，占测未来一日晴雨雷暴，得吉卦获「吉兆」加持。",
    "message.xuanhuan_earth.divination.already_cast_today": "今日已起过一卦，天机不可再三渎问。",
    "message.xuanhuan_earth.divination.omen.auspicious": "吉（获吉兆加持）",
    "message.xuanhuan_earth.divination.omen.ominous": "凶（宜守正慎行）",
    "message.xuanhuan_earth.divination.weather.clear": "未来一日晴朗无雨",
    "message.xuanhuan_earth.divination.weather.rain": "未来一日将有降雨",
    "message.xuanhuan_earth.divination.weather.thunder": "未来一日将有雷暴",
    "message.xuanhuan_earth.divination.result": "今日卜得【%s】卦 · 卦象：%s · 天象：%s。",
    "item.xuanhuan_earth.oracle_bone": "卜骨",
    "tooltip.xuanhuan_earth.oracle_bone": "夜间副手持打火石右键：灼骨观裂，占指百区块内最近古迹或村落方位。",
    "message.xuanhuan_earth.oracle_bone.cooldown": "甲骨余温未散，请稍候再占。",
    "message.xuanhuan_earth.oracle_bone.missing_flint_and_steel": "需要在副手持有打火石方能灼骨观裂。",
    "message.xuanhuan_earth.oracle_bone.nothing_found": "百里之内未感应到目标古迹或聚落，卜骨未消耗。",
    "message.xuanhuan_earth.oracle_bone.found": "兆纹显现：目标结构位于 %s方向 约 %s 格处。",
    "message.xuanhuan_earth.wind_talisman.blocked": "前方道路受阻，风符未能激发。",
    "message.xuanhuan_earth.seal_talisman.no_target": "视线前方未锁定有效目标，禁符未消耗。",
    "message.xuanhuan_earth.journal.header": "=== 修行札记 · 当前境界：%s（第 %s 境）===",
    "message.xuanhuan_earth.journal.route": "· %s：等级 %s（进度 %s）",
    "message.xuanhuan_earth.journal.bottleneck": "下一境界【%s】要求：任一已学路线达到 %s 级并备齐破境丹。",
    "direction.xuanhuan_earth.n": "正北",
    "direction.xuanhuan_earth.ne": "东北",
    "direction.xuanhuan_earth.e": "正东",
    "direction.xuanhuan_earth.se": "东南",
    "direction.xuanhuan_earth.s": "正南",
    "direction.xuanhuan_earth.sw": "西南",
    "direction.xuanhuan_earth.w": "正西",
    "direction.xuanhuan_earth.nw": "西北",
    "direction.xuanhuan_earth.here": "脚下",
    "fire_mode.xuanhuan_earth.gentle": "文火（0.70）",
    "fire_mode.xuanhuan_earth.medium": "中火（0.85）",
    "fire_mode.xuanhuan_earth.fierce": "武火（1.00）",
    "array_mode.xuanhuan_earth.gather": "聚灵（Gather）",
    "array_mode.xuanhuan_earth.charge": "充能（Charge）",
    "array_mode.xuanhuan_earth.guard": "护山（Guard）",
    "array_mode.xuanhuan_earth.alchemy": "助炼（Alchemy）",
    "array_mode.xuanhuan_earth.seal": "封禁（Seal）",
    "screen.xuanhuan_earth.furnace.fire": "火候：%s · 当前炉温 %s K（点击切换）",
    "screen.xuanhuan_earth.array.mode": "阵法模式：%s（点击切换）",
    "recipe.xuanhuan_earth.machine.marrow_pill": "灵草与灵晶淬炼洗髓丹",
    "recipe.xuanhuan_earth.machine.breath_pill": "灵草与灵泉凝炼闭息丹",
    "recipe.xuanhuan_earth.machine.fast_pill": "灵草配五谷精炼辟谷丸",
    "recipe.xuanhuan_earth.machine.detox_pill": "灵草合牛乳炼制解毒丹",
    "recipe.xuanhuan_earth.machine.pill_residue_refine": "丹渣回炉重炼回气丹",
    "recipe.xuanhuan_earth.machine.spirit_iron_ingot": "双灵铁胚高温锻成灵铁锭",
    "recipe.xuanhuan_earth.machine.spirit_glass": "玻璃融灵尘烧制灵化玻璃",
    "recipe.xuanhuan_earth.machine.nourish_qi_pill": "本草君臣配伍炼制益气丹",
    "recipe.xuanhuan_earth.machine.calm_spirit_pill": "枣仁柏子温养炼制安神丹",
    "recipe.xuanhuan_earth.machine.clear_heat_pill": "黄连连翘清火炼制清热丹",
    "recipe.xuanhuan_earth.machine.invigorate_blood_pill": "丹参红花化瘀炼制活血丹",
    "recipe.xuanhuan_earth.machine.resolve_toxin_pill": "甘草绿豆调和炼制化毒丹",
    "recipe.xuanhuan_earth.machine.foundation_pill": "双回气丹凝炼筑基丹",
    "recipe.xuanhuan_earth.machine.ironbody_pill": "回气丹配铁质催化淬体丹",
    "recipe.xuanhuan_earth.machine.cloudbreath_pill": "灵泉灵草合炼云息丹",
    "recipe.xuanhuan_earth.machine.grainfast_pill": "灵草麦粒合炼辟谷丹",
    "recipe.xuanhuan_earth.machine.nightsight_pill": "灵草金胡萝卜炼制夜视丹",
    "recipe.xuanhuan_earth.machine.breakthrough_pill": "筑基丹合灵晶冲炼破境丹",
    "recipe.xuanhuan_earth.machine.breakthrough_pill_herb": "灵草合高阶灵晶烈火炼破境丹",
    "recipe.xuanhuan_earth.machine.fire_talisman": "空白符引烈焰粉绘制火符",
    "recipe.xuanhuan_earth.machine.wind_talisman": "空白符引轻羽绘制风符",
    "recipe.xuanhuan_earth.machine.seal_talisman": "空白符引灵丝绘制禁符",
    "recipe.xuanhuan_earth.machine.spirit_stone_from_raw": "灵石原胚在聚灵阵富集为灵石",
    "recipe.xuanhuan_earth.machine.jade_slip": "软玉与灵晶在阵眼中雕琢为玉简"
}

en_updates = {
    "itemGroup.xuanhuan_earth": "Earth: Xuanxue",
    "key.categories.xuanhuan_earth.controls": "Earth: Xuanxue",
    "item.xuanhuan_earth.xuanhuan_handbook": "Earth: Xuanxue Handbook",
    "screen.xuanhuan_earth.handbook.title": "Earth: Xuanxue Handbook",
    "effect.xuanhuan_earth.auspicious": "Auspicious Omen",
    "block.xuanhuan_earth.array_pillar": "Array Pillar",
    "item.xuanhuan_earth.array_pillar": "Array Pillar",
    "message.xuanhuan_earth.block.array_pillar": "Array pillar anchored: %s (%s/100). Expands nearby spirit arrays (up to 12 pillars).",
    "item.xuanhuan_earth.cultivation_journal": "Cultivation Journal",
    "tooltip.xuanhuan_earth.cultivation_journal": "Displays levels, XP progress and realm bottlenecks across all four cultivation routes.",
    "item.xuanhuan_earth.spirit_herb": "Spirit Herb",
    "tooltip.xuanhuan_earth.spirit_herb": "Primary alchemy herb grown on Spirit Soil.",
    "item.xuanhuan_earth.empty_talisman": "Empty Talisman",
    "tooltip.xuanhuan_earth.empty_talisman": "Prepared talisman base made from talisman paper and cinnabar spirit ink.",
    "item.xuanhuan_earth.fire_talisman": "Fire Talisman",
    "tooltip.xuanhuan_earth.fire_talisman": "Offensive talisman. Spend 2 mana to launch a small fireball.",
    "item.xuanhuan_earth.wind_talisman": "Wind Talisman",
    "tooltip.xuanhuan_earth.wind_talisman": "Mobility talisman. Spend 1 mana to dash 6 blocks forward.",
    "item.xuanhuan_earth.seal_talisman": "Seal Talisman",
    "tooltip.xuanhuan_earth.seal_talisman": "Binding talisman. Spend 3 mana to slow a target ahead for 200 ticks.",
    "item.xuanhuan_earth.talisman_brush": "Talisman Brush",
    "tooltip.xuanhuan_earth.talisman_brush": "Inscribing tool for the Talisman Table (durability 64, -1 per craft unless auspicious).",
    "item.xuanhuan_earth.spirit_dust": "Spirit Dust",
    "tooltip.xuanhuan_earth.spirit_dust": "Fine spiritual powder ground from crystal shards, used in ink, paper and research.",
    "item.xuanhuan_earth.spirit_stone_raw": "Raw Spirit Stone",
    "tooltip.xuanhuan_earth.spirit_stone_raw": "Uncharged mineral matrix that can be enriched into a Spirit Stone inside a Spirit Array.",
    "tooltip.xuanhuan_earth.spirit_stone_raw.grade": "Matrix grade: Tier %s (enrichable in Spirit Array)",
    "item.xuanhuan_earth.spirit_stone_cracked": "Cracked Spirit Stone",
    "tooltip.xuanhuan_earth.spirit_stone_cracked": "Damaged by overcharging; maximum capacity reduced to 60 AEU.",
    "message.xuanhuan_earth.spirit_stone.cracked": "The spirit stone matrix fractured from instability and became cracked (60 AEU cap)!",
    "item.xuanhuan_earth.spirit_stone_core": "Spirit Stone Core",
    "tooltip.xuanhuan_earth.spirit_stone_core": "480 AEU high-capacity core for spirit arrays; charges only in a formed Spirit Array.",
    "message.xuanhuan_earth.spirit_stone_core.not_formed": "The Spirit Array must be formed in a 3x3 structure to charge a Spirit Stone Core.",
    "message.xuanhuan_earth.spirit_stone_core.charged": "Spirit Stone Core charged: %s / %s AEU.",
    "item.xuanhuan_earth.spirit_iron_ingot": "Spirit Iron Ingot",
    "tooltip.xuanhuan_earth.spirit_iron_ingot": "Forged from two Spirit Iron Blanks at high furnace heat.",
    "item.xuanhuan_earth.spirit_glass": "Spirit Glass",
    "tooltip.xuanhuan_earth.spirit_glass": "Clear glass fused with spirit dust in an Alchemy Furnace.",
    "item.xuanhuan_earth.spirit_thread": "Spirit Thread",
    "tooltip.xuanhuan_earth.spirit_thread": "Qi-infused silk thread produced by spirit silkworms.",
    "item.xuanhuan_earth.marrow_pill": "Marrow Cleansing Pill",
    "tooltip.xuanhuan_earth.marrow_pill": "Requires Body Tempering route. Grants +120 Body Tempering XP.",
    "message.xuanhuan_earth.pill.marrow_pill": "The Marrow Pill tempers your bones: mana +%s (%s/%s), body-tempering XP +120.",
    "item.xuanhuan_earth.breath_pill": "Breath Holding Pill",
    "tooltip.xuanhuan_earth.breath_pill": "Requires Fetal Breath route. Grants +120 Fetal Breath XP.",
    "message.xuanhuan_earth.pill.breath_pill": "The Breath Pill steadies your lungs: mana +%s (%s/%s), fetal-breath XP +120.",
    "item.xuanhuan_earth.fast_pill": "Fasting Pill",
    "tooltip.xuanhuan_earth.fast_pill": "Requires Bigu route. Freezes hunger drain for 1200 ticks.",
    "message.xuanhuan_earth.pill.fast_pill": "The Fasting Pill settles your metabolism: mana +%s (%s/%s).",
    "item.xuanhuan_earth.detox_pill": "Detox Pill",
    "tooltip.xuanhuan_earth.detox_pill": "Clears Poison, Weakness and Nausea.",
    "message.xuanhuan_earth.pill.detox_pill": "The Detox Pill purges toxins: mana +%s (%s/%s).",
    "item.xuanhuan_earth.pill_residue": "Pill Residue",
    "tooltip.xuanhuan_earth.pill_residue": "Leftover medicinal slag that can be re-refined at a 35% yield.",
    "item.xuanhuan_earth.failed_pill": "Failed Pill",
    "tooltip.xuanhuan_earth.failed_pill": "A ruined pill from poor heat control or excess toxicity; causes brief poison.",
    "message.xuanhuan_earth.pill.failed_pill": "The failed pill tastes bitter and upsets your qi (%s/%s)!",
    "item.xuanhuan_earth.nourish_qi_pill": "Nourish Qi Pill",
    "tooltip.xuanhuan_earth.nourish_qi_pill": "Herbal formula: ginseng and astragalus with spring water to restore mana.",
    "message.xuanhuan_earth.pill.nourish_qi_pill": "The Nourish Qi Pill warms your meridians: mana +%s (%s/%s).",
    "item.xuanhuan_earth.calm_spirit_pill": "Calm Spirit Pill",
    "tooltip.xuanhuan_earth.calm_spirit_pill": "Herbal formula: clears Nausea and Weakness and grants 600 ticks of immunity.",
    "message.xuanhuan_earth.pill.calm_spirit_pill": "The Calm Spirit Pill steadies your mind: mana +%s (%s/%s).",
    "item.xuanhuan_earth.clear_heat_pill": "Clear Heat Pill",
    "tooltip.xuanhuan_earth.clear_heat_pill": "Herbal formula: reduces toxic debuff tiers by 1.",
    "message.xuanhuan_earth.pill.clear_heat_pill": "The Clear Heat Pill cools internal fire: mana +%s (%s/%s).",
    "item.xuanhuan_earth.invigorate_blood_pill": "Invigorate Blood Pill",
    "tooltip.xuanhuan_earth.invigorate_blood_pill": "Herbal formula: grants Regeneration I and supports limb recovery.",
    "message.xuanhuan_earth.pill.invigorate_blood_pill": "The Invigorate Blood Pill quickens circulation: mana +%s (%s/%s).",
    "item.xuanhuan_earth.resolve_toxin_pill": "Resolve Toxin Pill",
    "tooltip.xuanhuan_earth.resolve_toxin_pill": "Herbal formula: clears Poison and grants 300 ticks of poison immunity.",
    "message.xuanhuan_earth.pill.resolve_toxin_pill": "The Resolve Toxin Pill neutralizes venom: mana +%s (%s/%s).",
    "tooltip.xuanhuan_earth.pill.grade": "Pill grade: %s (purity %s%%)",
    "message.xuanhuan_earth.pill.breakthrough_only": "Breakthrough Pills cannot be eaten directly; click Break Through in the Cultivation Panel.",
    "message.xuanhuan_earth.pill.route_locked": "You must learn %s before taking this pill.",
    "message.xuanhuan_earth.pill.no_effect": "You have no poison, weakness or nausea to clear.",
    "item.xuanhuan_earth.jade_slip": "Jade Slip",
    "tooltip.xuanhuan_earth.jade_slip": "Nephrite Xuanxue carrier (halves study duration) and 48 AEU energy vessel.",
    "item.xuanhuan_earth.jade_ward_amulet": "Jade Ward Amulet",
    "tooltip.xuanhuan_earth.jade_ward_amulet": "Nephrite protective amulet. Right-click to spend charge for Resistance and Absorption.",
    "item.xuanhuan_earth.jade_array_plate": "Jade Array Plate",
    "tooltip.xuanhuan_earth.jade_array_plate": "Jadeite formation plate. Use on a Spirit Array to link and add +0..2 scan radius.",
    "tooltip.xuanhuan_earth.jade.charge": "Jade charge: %s / %s AEU",
    "tooltip.xuanhuan_earth.jade_plate.status": "Linked array: %s · Radius bonus +%s",
    "message.xuanhuan_earth.jade_slip.no_field": "The jade slip is full or local qi is too thin.",
    "message.xuanhuan_earth.jade_slip.charged": "Jade slip charged: %s / %s AEU.",
    "message.xuanhuan_earth.jade_slip.release_skip": "Mana is full or the jade slip has no charge.",
    "message.xuanhuan_earth.jade_slip.released": "Jade slip released +%s mana (%s AEU remaining).",
    "message.xuanhuan_earth.jade_ward.low_charge": "The Jade Ward Amulet needs at least 12 AEU.",
    "message.xuanhuan_earth.jade_ward.activated": "Jade Ward Amulet activated (%s AEU left, stability %s).",
    "message.xuanhuan_earth.jade_plate.linked": "Jade Array Plate linked to %s (radius bonus +%s).",
    "message.xuanhuan_earth.jade_plate.radius": "Jade Array Plate radius bonus set to +%s.",
    "item.xuanhuan_earth.array_rune_stone": "Array Rune Stone",
    "tooltip.xuanhuan_earth.array_rune_stone": "Inscribed formation stone. Sneak-right-click to cycle rune (Power / Guard / Flow / Bind).",
    "tooltip.xuanhuan_earth.array_rune_stone.rune": "Current rune: %s",
    "message.xuanhuan_earth.array_rune_stone.cycled": "Array rune switched to: %s",
    "rune.xuanhuan_earth.power": "Power",
    "rune.xuanhuan_earth.guard": "Guard",
    "rune.xuanhuan_earth.flow": "Flow",
    "rune.xuanhuan_earth.bind": "Bind",
    "item.xuanhuan_earth.array_control_tablet": "Array Control Tablet",
    "tooltip.xuanhuan_earth.array_control_tablet": "Use on a Spirit Array to link; right-click to cycle array mode, sneak-right-click to adjust radius (4..16).",
    "tooltip.xuanhuan_earth.array_tablet.status": "Linked array: %s · Radius: %s blocks",
    "message.xuanhuan_earth.array_tablet.linked": "Array Control Tablet linked to %s.",
    "message.xuanhuan_earth.array_tablet.no_linked_array": "No valid Spirit Array is linked.",
    "message.xuanhuan_earth.array_tablet.mode": "Array mode set to %s (field %s/100, stored %s AEU).",
    "message.xuanhuan_earth.array_tablet.radius": "Array radius set to %s blocks.",
    "item.xuanhuan_earth.luopan": "Geomantic Luopan",
    "tooltip.xuanhuan_earth.luopan": "Evaluates local site qi (rating 1..9); learning Dragon-Vein Seeking unlocks detailed sub-scores and vein direction.",
    "tooltip.xuanhuan_earth.luopan.last_rating": "Last geomantic rating: %s / 9",
    "message.xuanhuan_earth.luopan.cooldown": "The Luopan needle is still settling.",
    "message.xuanhuan_earth.luopan.rating": "Geomantic reading: site rating %s / 9.",
    "message.xuanhuan_earth.luopan.detail": "Breakdown: field %s / water %s / north mountain %s / open sky %s.",
    "message.xuanhuan_earth.luopan.vein": "Nearest spirit vein: %s, ~%s blocks (height diff %s).",
    "message.xuanhuan_earth.luopan.no_vein": "No spirit vein nearby",
    "item.xuanhuan_earth.dragon_seeking_rod": "Dragon-Seeking Rod",
    "tooltip.xuanhuan_earth.dragon_seeking_rod": "Marks the chunk acupoint from weighted qi sources; formed arrays on the acupoint gain +4 gather bonus and cushions grant +1 XP.",
    "message.xuanhuan_earth.dragon_rod.broken": "The Dragon-Seeking Rod is worn out.",
    "message.xuanhuan_earth.dragon_rod.cooldown": "The Dragon-Seeking Rod is still resonating.",
    "message.xuanhuan_earth.dragon_rod.marked": "Acupoint marked for this chunk at (%s, %s, %s).",
    "item.xuanhuan_earth.divination_coins": "Divination Coins",
    "tooltip.xuanhuan_earth.divination_coins": "Once per day, cast one of the 64 hexagrams to forecast weather and gain Auspicious Omen on even hexagrams.",
    "message.xuanhuan_earth.divination.already_cast_today": "You have already cast a hexagram today.",
    "message.xuanhuan_earth.divination.omen.auspicious": "Auspicious (grants Auspicious Omen)",
    "message.xuanhuan_earth.divination.omen.ominous": "Ominous (proceed with caution)",
    "message.xuanhuan_earth.divination.weather.clear": "clear skies over the next day",
    "message.xuanhuan_earth.divination.weather.rain": "rain expected within a day",
    "message.xuanhuan_earth.divination.weather.thunder": "thunderstorm expected within a day",
    "message.xuanhuan_earth.divination.result": "Hexagram [%s] · Omen: %s · Sky: %s.",
    "item.xuanhuan_earth.oracle_bone": "Oracle Bone",
    "tooltip.xuanhuan_earth.oracle_bone": "Use at night with Flint and Steel in offhand to locate the nearest target structure within 100 chunks.",
    "message.xuanhuan_earth.oracle_bone.cooldown": "The oracle bone is still hot.",
    "message.xuanhuan_earth.oracle_bone.missing_flint_and_steel": "Hold Flint and Steel in your offhand to scorch the oracle bone.",
    "message.xuanhuan_earth.oracle_bone.nothing_found": "No target structure found within 100 chunks; the bone was not consumed.",
    "message.xuanhuan_earth.oracle_bone.found": "Cracks point %s, about %s blocks away.",
    "message.xuanhuan_earth.wind_talisman.blocked": "The path ahead is blocked; the Wind Talisman was not spent.",
    "message.xuanhuan_earth.seal_talisman.no_target": "No valid target in sight; the Seal Talisman was not spent.",
    "message.xuanhuan_earth.journal.header": "=== Cultivation Journal · Realm: %s (Tier %s) ===",
    "message.xuanhuan_earth.journal.route": "· %s: Lv.%s (%s)",
    "message.xuanhuan_earth.journal.bottleneck": "Next realm [%s] requires any learned route at Lv.%s and 1 Breakthrough Pill.",
    "direction.xuanhuan_earth.n": "North",
    "direction.xuanhuan_earth.ne": "Northeast",
    "direction.xuanhuan_earth.e": "East",
    "direction.xuanhuan_earth.se": "Southeast",
    "direction.xuanhuan_earth.s": "South",
    "direction.xuanhuan_earth.sw": "Southwest",
    "direction.xuanhuan_earth.w": "West",
    "direction.xuanhuan_earth.nw": "Northwest",
    "direction.xuanhuan_earth.here": "Here",
    "fire_mode.xuanhuan_earth.gentle": "Gentle Fire (0.70)",
    "fire_mode.xuanhuan_earth.medium": "Medium Fire (0.85)",
    "fire_mode.xuanhuan_earth.fierce": "Fierce Fire (1.00)",
    "array_mode.xuanhuan_earth.gather": "Gather",
    "array_mode.xuanhuan_earth.charge": "Charge",
    "array_mode.xuanhuan_earth.guard": "Guard",
    "array_mode.xuanhuan_earth.alchemy": "Alchemy",
    "array_mode.xuanhuan_earth.seal": "Seal",
    "screen.xuanhuan_earth.furnace.fire": "Fire: %s · Temp %s K (click to cycle)",
    "screen.xuanhuan_earth.array.mode": "Array mode: %s (click to cycle)",
    "recipe.xuanhuan_earth.machine.marrow_pill": "Refine Spirit Herbs and Crystal Shards into a Marrow Pill",
    "recipe.xuanhuan_earth.machine.breath_pill": "Simmer Spirit Herbs and Spring Water into a Breath Pill",
    "recipe.xuanhuan_earth.machine.fast_pill": "Blend Spirit Herbs and Wheat into Fasting Pills",
    "recipe.xuanhuan_earth.machine.detox_pill": "Decoct Spirit Herbs with Milk into Detox Pills",
    "recipe.xuanhuan_earth.machine.pill_residue_refine": "Re-refine Pill Residue into a Qi Recovery Pill",
    "recipe.xuanhuan_earth.machine.spirit_iron_ingot": "Forge two Spirit Iron Blanks into a Spirit Iron Ingot",
    "recipe.xuanhuan_earth.machine.spirit_glass": "Fuse glass and spirit dust into Spirit Glass",
    "recipe.xuanhuan_earth.machine.nourish_qi_pill": "Refine herbal tonics into Nourish Qi Pills",
    "recipe.xuanhuan_earth.machine.calm_spirit_pill": "Simmer calming seeds into Calm Spirit Pills",
    "recipe.xuanhuan_earth.machine.clear_heat_pill": "Refine bitter herbs into Clear Heat Pills",
    "recipe.xuanhuan_earth.machine.invigorate_blood_pill": "Refine blood-quickening herbs into Invigorate Blood Pills",
    "recipe.xuanhuan_earth.machine.resolve_toxin_pill": "Blend detoxifying herbs into Resolve Toxin Pills",
    "recipe.xuanhuan_earth.machine.foundation_pill": "Condense Qi Recovery Pills into a Foundation Pill",
    "recipe.xuanhuan_earth.machine.ironbody_pill": "Temper a Qi Pill with mineral catalysts into an Ironbody Pill",
    "recipe.xuanhuan_earth.machine.cloudbreath_pill": "Simmer spring water and spirit grass into a Cloudbreath Pill",
    "recipe.xuanhuan_earth.machine.grainfast_pill": "Refine spirit grass and wheat into a Grainfast Pill",
    "recipe.xuanhuan_earth.machine.nightsight_pill": "Refine spirit grass and golden carrot into a Nightsight Pill",
    "recipe.xuanhuan_earth.machine.breakthrough_pill": "Fuse Foundation Pills and crystal shards into a Breakthrough Pill",
    "recipe.xuanhuan_earth.machine.breakthrough_pill_herb": "Condense Spirit Herbs and crystal shards at high heat into a Breakthrough Pill",
    "recipe.xuanhuan_earth.machine.fire_talisman": "Inscribe a Fire Talisman with blaze powder and cinnabar ink",
    "recipe.xuanhuan_earth.machine.wind_talisman": "Inscribe a Wind Talisman with feathers and cinnabar ink",
    "recipe.xuanhuan_earth.machine.seal_talisman": "Inscribe a Seal Talisman with string and cinnabar ink",
    "recipe.xuanhuan_earth.machine.spirit_stone_from_raw": "Enrich a Raw Spirit Stone in the Spirit Array",
    "recipe.xuanhuan_earth.machine.jade_slip": "Carve nephrite and crystal shards into a Jade Slip"
}

for n in NODES:
    s = n["slug"]
    zh_updates[f"lore.xuanhuan_earth.xua.{s}"] = n["zh"]
    zh_updates[f"lore.xuanhuan_earth.xua.{s}.desc"] = n["zh_desc"]
    en_updates[f"lore.xuanhuan_earth.xua.{s}"] = n["en"]
    en_updates[f"lore.xuanhuan_earth.xua.{s}.desc"] = n["en_desc"]

for idx, (hz, he) in enumerate(zip(HEXAGRAMS_ZH, HEXAGRAMS_EN)):
    zh_updates[f"hexagram.xuanhuan_earth.{idx}"] = hz
    en_updates[f"hexagram.xuanhuan_earth.{idx}"] = he

zh.update(zh_updates)
en.update(en_updates)
write_json(zh_path, zh)
write_json(en_path, en)
print(f"Generated {len(NODES)} nodes, {len(NEW_ITEMS)} items, {len(RECIPES)} recipes, {len(zh)} zh keys, {len(en)} en keys.")
