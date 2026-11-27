import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

pairs = [
    # Head sits forward and lower than the back ridge, so the neck line reads.
    ('head = PartSpec("head", (0.0, 9.6, -5.6), boxes=[\n'
     '        BoxSpec("skull", -3.2, -3.2, -3.2, 6.4, 6.4, 6.4, "fur"),',
     'head = PartSpec("head", (0.0, 10.2, -6.4), boxes=[\n'
     '        BoxSpec("skull", -3.0, -3.0, -3.0, 6.0, 6.0, 6.0, "fur"),'),
    # A short neck block bridges the gap between skull and torso.
    ('parts = [body, head]',
     'neck = PartSpec("neck", (0.0, 11.6, -4.4), (0.32, 0.0, 0.0), [\n'
     '        BoxSpec("bridge", -2.4, -2.4, -2.2, 4.8, 4.8, 4.4, "fur"),\n'
     '    ])\n'
     '    parts = [body, neck, head]'),
]

for old, new in pairs:
    if old not in s:
        print('MISS', old.splitlines()[0][:70])
        continue
    s = s.replace(old, new, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('done')
