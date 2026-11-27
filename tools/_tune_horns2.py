import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

old = '''    # Horns sweep backward, not upward: each segment leans further back so the curl reads
    # from the side and the silhouette stays lower than the body is long.
    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        horn = PartSpec(name, (1.9 * side, -2.4, 0.0), (-1.05, 0.0, -0.30 * side), [
            BoxSpec("base", -1.0, -2.8, -1.0, 2.0, 2.8, 2.0, "metal"),
        ])
        mid = PartSpec("mid", (0.0, -2.5, 0.0), (-1.15, 0.0, -0.26 * side), [
            BoxSpec("mid", -0.8, -2.4, -0.8, 1.6, 2.4, 1.6, "metal"),
        ])
        mid.children.append(PartSpec("tip", (0.0, -2.0, 0.0), (-1.2, 0.0, -0.22 * side), [
            BoxSpec("tip", -0.6, -1.8, -0.6, 1.2, 1.8, 1.2, "metal"),
        ]))
        horn.children.append(mid)
        head.children.append(horn)'''

new = '''    # Horns sweep backward in three flat segments. The segments are siblings under the head
    # rather than nested, because nesting compounds each rotation and previously pushed the
    # tip above the animal's back.
    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        head.children.append(PartSpec(f"{name}_base", (1.9 * side, -2.2, 0.0),
                (-1.15, 0.0, -0.26 * side), [
                    BoxSpec("base", -1.0, -2.6, -1.0, 2.0, 2.6, 2.0, "metal"),
                ]))
        head.children.append(PartSpec(f"{name}_mid", (2.4 * side, -3.6, 1.6),
                (-1.30, 0.0, -0.18 * side), [
                    BoxSpec("mid", -0.8, -2.2, -0.8, 1.6, 2.2, 1.6, "metal"),
                ]))
        head.children.append(PartSpec(f"{name}_tip", (2.7 * side, -4.4, 3.2),
                (-1.40, 0.0, -0.12 * side), [
                    BoxSpec("tip", -0.6, -1.7, -0.6, 1.2, 1.7, 1.2, "metal"),
                ]))'''

if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('horns flattened to siblings')
else:
    print('MISS horns')
