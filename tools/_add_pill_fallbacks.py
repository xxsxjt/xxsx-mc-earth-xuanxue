"""Crafting-table fallbacks for the two lowest pills.

Every other pill is furnace-only, but a player who has not built a furnace yet should still
be able to make the first step, otherwise the alchemy ladder has no entry point.
"""

import io
import json
import os

ROOT = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources\data\earth_xuanxue\recipe'

RECIPES = {
    # Grass + spring water: the same shape as the furnace recipe but weaker, so the furnace
    # stays the efficient route.
    'grainfast_pill_simple.json': {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [
            "earth_xuanxue:spirit_grass",
            "earth_xuanxue:spirit_grass",
            "minecraft:wheat",
            "minecraft:bowl",
        ],
        "result": {"id": "earth_xuanxue:grainfast_pill", "count": 1},
    },
    'nightsight_pill_simple.json': {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [
            "earth_xuanxue:spirit_grass",
            "minecraft:golden_carrot",
            "minecraft:glass_bottle",
        ],
        "result": {"id": "earth_xuanxue:nightsight_pill", "count": 1},
    },
}

for name, payload in RECIPES.items():
    path = os.path.join(ROOT, name)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(
        json.dumps(payload, indent=2) + '\n')
    print('wrote', name)
