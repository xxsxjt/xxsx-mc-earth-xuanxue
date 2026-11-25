import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

pairs = [
    # Fox: the old base was almost white, so the two-tone shading had nothing to work with.
    ('return 128, parts, (196, 202, 210), (96, 226, 214)',
     'return 128, parts, (146, 154, 170), (96, 226, 214)'),
    # Turtle: darken the shell so the crystal facets read instead of blowing out.
    ('return 128, parts, (56, 108, 92), (96, 240, 232)',
     'return 128, parts, (44, 92, 78), (88, 232, 224)'),
    # Crane: warm the cream so the orange bars contrast.
    ('return 128, [body, neck, *wings, *legs, tail], (238, 228, 200), (226, 108, 62)',
     'return 128, [body, neck, *wings, *legs, tail], (216, 204, 172), (214, 92, 48)'),
]

for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1)
        print('ok', old[:50])
    else:
        print('MISS', old[:50])

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
