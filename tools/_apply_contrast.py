import io

path = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\tools\uv_layout.py'
s = io.open(path, encoding='utf-8').read()

start = s.index('def paint_box(')
end = s.index('def emit_java_offsets(')

new = '''def paint_box(image: Image.Image, u: int, v: int, box: BoxSpec,
              base: tuple[int, int, int], accent: tuple[int, int, int]) -> None:
    draw = ImageDraw.Draw(image)
    w, h, d = int(box.w), int(box.h), int(box.d)
    rng = random.Random(sum(ord(c) for c in box.name))
    # Two-tone shading: dorsal darker, ventral lighter. A single flat fur colour made every
    # creature read as a white blob in the four-view sheets.
    dorsal = tuple(max(0, c - 46) for c in base)
    ventral = tuple(min(255, c + 34) for c in base)
    for face, rect in face_rects(u, v, w, h, d).items():
        x0, y0, x1, y1 = rect
        if x1 <= x0 or y1 <= y0:
            continue
        tone = shade(base, FACE_LIGHT[face])
        draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=tone)

        if box.style == "fur" and y1 - y0 >= 4:
            split = y0 + max(1, (y1 - y0) // 2)
            draw.rectangle((x0, y0, x1 - 1, split - 1),
                           fill=shade(dorsal, FACE_LIGHT[face] // 2))
            draw.rectangle((x0, split, x1 - 1, y1 - 1),
                           fill=shade(ventral, FACE_LIGHT[face] // 3))
            for y in range(y0 + 1, split, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(dorsal, FACE_LIGHT[face] // 2 + rng.choice((-16, 10))))
            for y in range(split + 1, y1 - 1, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(ventral, FACE_LIGHT[face] // 3 + rng.choice((-14, 12))))
        elif box.style == "fur":
            for y in range(y0 + 1, y1 - 1, 2):
                for x in range(x0 + 1 + (y & 1), x1 - 1, 3):
                    draw.point((x, y), fill=shade(base, FACE_LIGHT[face] + rng.choice((-18, 14))))

        if box.style == "crystal":
            for offset in range(-(y1 - y0), x1 - x0, 3):
                draw.line((x0 + offset, y1 - 1, x0 + offset + (y1 - y0), y0),
                          fill=shade(accent, 40))
            for offset in range(-(y1 - y0), x1 - x0, 6):
                draw.line((x0 + offset + 1, y1 - 1, x0 + offset + 1 + (y1 - y0), y0),
                          fill=shade(accent, -40))
        elif box.style == "metal":
            if y1 - y0 >= 4:
                mid = y0 + (y1 - y0) // 2
                draw.line((x0, mid, x1 - 1, mid), fill=shade(base, FACE_LIGHT[face] - 34))
                draw.line((x0, mid - 1, x1 - 1, mid - 1), fill=shade(base, FACE_LIGHT[face] + 26))
        elif box.style == "feather":
            for y in range(y0 + 1, y1 - 1, 2):
                draw.line((x0 + 1, y, x1 - 2, y - 1), fill=shade(accent, 8))
            for y in range(y0 + 2, y1 - 1, 4):
                draw.line((x0 + 2, y, x1 - 3, y - 1), fill=shade(base, FACE_LIGHT[face] - 26))

        # Dark bottom/right border plus light top/left: reads as an outline in game and
        # keeps adjacent parts visually separated.
        if x1 - x0 > 2 and y1 - y0 > 2:
            draw.line((x0, y0, x1 - 1, y0), fill=shade(base, FACE_LIGHT[face] + 30))
            draw.line((x0, y0, x0, y1 - 1), fill=shade(base, FACE_LIGHT[face] + 16))
            draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=shade(base, FACE_LIGHT[face] - 52))
            draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=shade(base, FACE_LIGHT[face] - 34))


'''

s = s[:start] + new + s[end:]
io.open(path, 'w', encoding='utf-8', newline='\n').write(s)
print('written, dorsal present:', 'dorsal' in io.open(path, encoding='utf-8').read())
