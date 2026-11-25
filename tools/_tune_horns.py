import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\rebuild_beast_models.py'
s = io.open(p, encoding='utf-8').read()

# Goat horns were stacking three 3.4/2.8/2.2 tall segments almost straight up, which pushed
# total height past the body length. Curl them back and shorten each segment.
old = '''    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        horn = PartSpec(name, (2.0 * side, -2.6, -0.2), (-0.75, 0.0, -0.34 * side), [
            BoxSpec("base", -1.1, -3.4, -1.1, 2.2, 3.4, 2.2, "metal"),
        ])
        mid = PartSpec("mid", (0.0, -3.0, 0.0), (-0.85, 0.0, -0.30 * side), [
            BoxSpec("mid", -0.9, -2.8, -0.9, 1.8, 2.8, 1.8, "metal"),
        ])
        mid.children.append(PartSpec("tip", (0.0, -2.4, 0.0), (-0.9, 0.0, -0.26 * side), [
            BoxSpec("tip", -0.7, -2.2, -0.7, 1.4, 2.2, 1.4, "metal"),
        ]))
        horn.children.append(mid)
        head.children.append(horn)'''

new = '''    # Horns sweep backward, not upward: each segment leans further back so the curl reads
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

if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('goat horns curled')
else:
    print('MISS goat horns')
