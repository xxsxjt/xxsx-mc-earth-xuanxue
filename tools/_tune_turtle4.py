import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

old = '''    # Head: taller than the shell rim, projecting forward beyond it.
    head = PartSpec("head", (0.0, 15.6, -9.0), boxes=[
        BoxSpec("skull", -2.2, -2.8, -3.8, 4.4, 5.6, 5.4, "fur"),
        BoxSpec("beak", -1.2, -1.0, -5.0, 2.4, 2.2, 1.6, "metal"),
        BoxSpec("crest", -0.7, -4.2, -1.4, 1.4, 1.8, 2.6, "crystal"),
    ])'''

new = '''    # Head: projects forward past the rim, with a neck block bridging it to the shell so
    # the head never reads as a detached cube.
    head = PartSpec("head", (0.0, 17.0, -7.8), boxes=[
        BoxSpec("skull", -2.2, -2.8, -4.2, 4.4, 5.6, 5.8, "fur"),
        BoxSpec("beak", -1.2, -1.0, -5.4, 2.4, 2.2, 1.6, "metal"),
        BoxSpec("crest", -0.7, -4.2, -1.8, 1.4, 1.8, 2.6, "crystal"),
    ])
    head.children.append(PartSpec("neck", (0.0, 1.0, 1.4), boxes=[
        BoxSpec("bridge", -2.6, -2.0, 0.0, 5.2, 4.0, 3.4, "fur"),
    ]))'''

if old in s:
    s = s.replace(old, new, 1)
    print('head + neck rebuilt')
else:
    print('MISS head block')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
