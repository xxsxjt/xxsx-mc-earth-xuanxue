import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

pairs = [
    ('head = PartSpec("head", (0.0, 20.2, -7.4), boxes=[',
     'head = PartSpec("head", (0.0, 16.8, -8.2), boxes=['),
    ('BoxSpec("skull", -2.2, -2.6, -3.6, 4.4, 5.2, 5.2, "fur"),',
     'BoxSpec("skull", -2.2, -2.8, -3.8, 4.4, 5.6, 5.4, "fur"),'),
]

for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1)
        print('ok', old[:40])
    else:
        print('MISS', old[:40])

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
