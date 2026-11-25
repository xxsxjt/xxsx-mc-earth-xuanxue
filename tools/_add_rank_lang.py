import io, json, collections

LANG = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\resources\assets\earth_xuanxue\lang'

ZH = {
    'beast_rank.earth_xuanxue.1': '一阶',
    'beast_rank.earth_xuanxue.2': '二阶',
    'beast_rank.earth_xuanxue.3': '三阶',
    'beast_rank.earth_xuanxue.4': '四阶',
    'beast_rank.earth_xuanxue.5': '五阶',
    'message.earth_xuanxue.beast.rank_up': '%s 晋升为%s（%s 阶），体魄与攻伐随之增长。',
    'message.earth_xuanxue.beast.rank_progress': '%s 的修行进度：%s，%s/%s。',
    'message.earth_xuanxue.beast.rank_max': '%s 已达五阶，无法再以灵晶精进。',
}

EN = {
    'beast_rank.earth_xuanxue.1': 'Rank I',
    'beast_rank.earth_xuanxue.2': 'Rank II',
    'beast_rank.earth_xuanxue.3': 'Rank III',
    'beast_rank.earth_xuanxue.4': 'Rank IV',
    'beast_rank.earth_xuanxue.5': 'Rank V',
    'message.earth_xuanxue.beast.rank_up': '%s advanced to %s (rank %s); its body and attack grew.',
    'message.earth_xuanxue.beast.rank_progress': '%s growth: %s, %s/%s.',
    'message.earth_xuanxue.beast.rank_max': '%s is already rank V and cannot advance further.',
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
