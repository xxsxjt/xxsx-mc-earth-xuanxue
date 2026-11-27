import io, json, collections

LANG = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources\assets\earth_xuanxue\lang'

ZH = {
    'realm.earth_xuanxue.0': '凡身',
    'realm.earth_xuanxue.1': '炼气',
    'realm.earth_xuanxue.2': '筑基',
    'realm.earth_xuanxue.3': '金丹',
    'realm.earth_xuanxue.4': '元婴',
    'realm.earth_xuanxue.5': '化神',
    'screen.earth_xuanxue.cultivation.breakthrough': '破境',
    'message.earth_xuanxue.realm.max': '已至化神，此界再无可破之境。',
    'message.earth_xuanxue.realm.route_low': '%s 尚未达到 %s 级（当前 %s 级），根基不足，无法破境。',
    'message.earth_xuanxue.realm.needs_pill': '破境需要一枚%s。',
    'message.earth_xuanxue.realm.advanced': '破境成功：踏入%s（第 %s 境），法力上限提升至 %s。',
}

EN = {
    'realm.earth_xuanxue.0': 'Mortal',
    'realm.earth_xuanxue.1': 'Qi Refining',
    'realm.earth_xuanxue.2': 'Foundation',
    'realm.earth_xuanxue.3': 'Golden Core',
    'realm.earth_xuanxue.4': 'Nascent Soul',
    'realm.earth_xuanxue.5': 'Spirit Transformation',
    'screen.earth_xuanxue.cultivation.breakthrough': 'Break Through',
    'message.earth_xuanxue.realm.max': 'You have reached Spirit Transformation; nothing higher exists in this world.',
    'message.earth_xuanxue.realm.route_low': '%s has not reached level %s (currently %s); your foundation is too shallow to break through.',
    'message.earth_xuanxue.realm.needs_pill': 'A breakthrough requires one %s.',
    'message.earth_xuanxue.realm.advanced': 'Breakthrough: you enter %s (realm %s) and your mana cap rises to %s.',
}

for loc, entries in (('zh_cn', ZH), ('en_us', EN)):
    path = f'{LANG}\\{loc}.json'
    data = json.loads(io.open(path, encoding='utf-8').read(),
                      object_pairs_hook=collections.OrderedDict)
    added = 0
    for k, v in entries.items():
        if k not in data:
            data[k] = v
            added += 1
    io.open(path, 'w', encoding='utf-8', newline='\n').write(
        json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    print(loc, 'added', added, 'total', len(data))
