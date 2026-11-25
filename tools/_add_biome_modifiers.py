import io, json, os

base = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2'
        r'\src\main\resources\data\earth_xuanxue\neoforge\biome_modifier')

SPECS = {
    'add_mountain_spirit_shrines.json': {
        "type": "neoforge:add_features",
        "biomes": "#minecraft:is_mountain",
        "features": ["earth_xuanxue:mountain_spirit_shrine"],
        "step": "surface_structures",
    },
    'add_spirit_beast_keeper_camps.json': {
        "type": "neoforge:add_features",
        "biomes": "#minecraft:is_forest",
        "features": ["earth_xuanxue:spirit_beast_keeper_camp"],
        "step": "surface_structures",
    },
}

for name, payload in SPECS.items():
    io.open(os.path.join(base, name), 'w', encoding='utf-8', newline='\n').write(
        json.dumps(payload, indent=2) + '\n')
    print('wrote', name)
