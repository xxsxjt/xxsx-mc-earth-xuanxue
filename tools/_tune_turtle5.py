import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

pairs = [
    # Close the gap: head sits right at the shell rim instead of floating in front of it.
    ('head = PartSpec("head", (0.0, 17.0, -7.8), boxes=[',
     'head = PartSpec("head", (0.0, 17.2, -6.6), boxes=['),
    ('head.children.append(PartSpec("neck", (0.0, 1.0, 1.4), boxes=[\n'
     '        BoxSpec("bridge", -2.6, -2.0, 0.0, 5.2, 4.0, 3.4, "fur"),\n'
     '    ]))',
     'head.children.append(PartSpec("neck", (0.0, 1.2, 1.0), boxes=[\n'
     '        BoxSpec("bridge", -2.8, -2.2, 0.0, 5.6, 4.4, 4.2, "fur"),\n'
     '    ]))'),
]

for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1)
        print('ok')
    else:
        print('MISS', old[:50])

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
