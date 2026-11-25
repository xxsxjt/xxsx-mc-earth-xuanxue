import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

# Goat horns: curl backward and outward like a ram, not straight up like antlers.
old_horns = '''    horn_specs = (("left_horn", 2.6), ("right_horn", -2.6))
    for name, x in horn_specs:
        horn = PartSpec(name, (x, -3.0, 0.4), (-0.22, 0.0, 0.48 if x > 0 else -0.48), [
            BoxSpec("base", -1.4, -5.0, -1.4, 2.8, 5.0, 2.8, "metal"),
        ])
        mid = PartSpec("mid", (0.0, -4.6, 0.2), (-0.16, 0.0, 0.30 if x > 0 else -0.30), [
            BoxSpec("mid", -1.2, -4.4, -1.2, 2.4, 4.4, 2.4, "metal"),
        ])
        mid.children.append(PartSpec("tip", (0.0, -4.0, 0.3), (-0.12, 0.0, 0.28 if x > 0 else -0.28), [
            BoxSpec("tip", -0.9, -3.6, -0.9, 1.8, 3.6, 1.8, "metal"),
        ]))
        horn.children.append(mid)
        head.children.append(horn)'''

new_horns = '''    # Ram-style horns: three segments sweeping back and out, each shorter than the last.
    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        horn = PartSpec(name, (2.2 * side, -2.6, -0.4), (-0.35, 0.0, -0.55 * side), [
            BoxSpec("base", -1.3, -4.4, -1.3, 2.6, 4.4, 2.6, "metal"),
        ])
        mid = PartSpec("mid", (0.0, -4.0, 0.0), (-0.55, 0.0, -0.42 * side), [
            BoxSpec("mid", -1.1, -3.6, -1.1, 2.2, 3.6, 2.2, "metal"),
        ])
        mid.children.append(PartSpec("tip", (0.0, -3.2, 0.0), (-0.55, 0.0, -0.38 * side), [
            BoxSpec("tip", -0.8, -2.8, -0.8, 1.6, 2.8, 1.6, "metal"),
        ]))
        horn.children.append(mid)
        head.children.append(horn)'''

if old_horns in s:
    s = s.replace(old_horns, new_horns, 1)
    print('horns replaced')
else:
    print('MISS horns')

# Lift the goat palette so the model is readable at night and in shade.
s = s.replace('return 128, parts, (72, 68, 62), (240, 154, 52)',
              'return 128, parts, (118, 110, 98), (244, 164, 62)')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('done')
