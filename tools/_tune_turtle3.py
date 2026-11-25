import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

pairs = [
    # Pull the head further forward and lift it above the shell rim.
    ('head = PartSpec("head", (0.0, 16.8, -8.2), boxes=[',
     'head = PartSpec("head", (0.0, 15.6, -9.0), boxes=['),
    # Shorten the shell so it cannot occlude the head from the side.
    ('BoxSpec("dome", -5.0, -3.0, -6.0, 10.0, 3.6, 12.0, "crystal"),',
     'BoxSpec("dome", -5.0, -3.0, -5.0, 10.0, 3.6, 11.0, "crystal"),'),
    ('BoxSpec("rim", -5.6, 0.4, -6.6, 11.2, 1.4, 13.2, "crystal", grow=0.04),',
     'BoxSpec("rim", -5.6, 0.4, -5.6, 11.2, 1.4, 12.2, "crystal", grow=0.04),'),
    ('BoxSpec("belly", -4.6, 0.0, -5.6, 9.2, 2.4, 11.2, "fur"),',
     'BoxSpec("belly", -4.6, 0.0, -4.6, 9.2, 2.4, 10.2, "fur"),'),
]

for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1)
        print('ok', old[:44])
    else:
        print('MISS', old[:44])

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
