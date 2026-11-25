import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

start = s.index('def turtle() ->')
end = s.index('def crane() ->')

new = '''def turtle() -> tuple[int, list[PartSpec], tuple[int, int, int], tuple[int, int, int]]:
    # Turtle silhouette rules: a shallow dome narrower than the limb span, a head that
    # clearly projects forward and up, and four splayed flippers visible from the side.
    body = PartSpec("body", (0.0, 19.0, 0.0), boxes=[
        BoxSpec("belly", -4.6, 0.0, -5.6, 9.2, 2.4, 11.2, "fur"),
    ])
    shell = PartSpec("shell", (0.0, 16.6, 0.0), boxes=[
        BoxSpec("dome", -5.0, -3.0, -6.0, 10.0, 3.6, 12.0, "crystal"),
        BoxSpec("rim", -5.6, 0.4, -6.6, 11.2, 1.4, 13.2, "crystal", grow=0.04),
    ])
    crystals = PartSpec("crystals", (0.0, -2.8, 0.0), boxes=[
        BoxSpec("main", -1.2, -5.0, -1.2, 2.4, 5.0, 2.4, "crystal"),
    ])
    crystals.children.append(PartSpec("left_crystal", (-2.4, 0.4, 1.2), (-0.2, 0.0, -0.42), [
        BoxSpec("shard", -0.7, -3.0, -0.7, 1.4, 3.0, 1.4, "crystal"),
    ]))
    crystals.children.append(PartSpec("rear_crystal", (2.2, 0.6, 2.8), (0.16, 0.0, 0.36), [
        BoxSpec("shard", -0.7, -2.4, -0.7, 1.4, 2.4, 1.4, "crystal"),
    ]))
    shell.children.append(crystals)

    # Head: taller than the shell rim, projecting forward beyond it.
    head = PartSpec("head", (0.0, 20.2, -7.4), boxes=[
        BoxSpec("skull", -2.2, -2.6, -3.6, 4.4, 5.2, 5.2, "fur"),
        BoxSpec("beak", -1.2, -1.0, -5.0, 2.4, 2.2, 1.6, "metal"),
        BoxSpec("crest", -0.7, -4.2, -1.4, 1.4, 1.8, 2.6, "crystal"),
    ])

    parts = [body, shell, head]
    for name, x, z in (("front_right", -5.4, -3.6), ("front_left", 5.4, -3.6),
                       ("back_right", -5.4, 3.6), ("back_left", 5.4, 3.6)):
        parts.append(PartSpec(name, (x, 20.4, z), boxes=[
            BoxSpec("flipper", -2.8, -1.4, -2.4, 5.6, 2.8, 4.8, "fur"),
        ]))

    tail = PartSpec("tail", (0.0, 20.0, 6.2), boxes=[
        BoxSpec("stub", -0.9, -0.8, 0.0, 1.8, 1.6, 3.0, "fur"),
    ])
    parts.append(tail)

    return 128, parts, (56, 108, 92), (96, 240, 232)


'''

s = s[:start] + new + s[end:]
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('turtle rebuilt v2')
