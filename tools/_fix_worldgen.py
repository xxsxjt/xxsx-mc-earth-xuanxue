import io, json, os

base = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources\data\earth_xuanxue\worldgen'
conf = os.path.join(base, 'configured_feature')

for name in ('mountain_spirit_shrine', 'spirit_beast_keeper_camp'):
    payload = {"type": f"earth_xuanxue:{name}", "config": {}}
    io.open(os.path.join(conf, name + '.json'), 'w', encoding='utf-8', newline='\n').write(
        json.dumps(payload, indent=2) + '\n')
    print(name, 'fixed')
