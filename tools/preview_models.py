"""Render the mod's box models offline so they can be inspected without a client.

Parses ``addOrReplaceChild`` calls out of the Java model sources, rebuilds the part tree,
projects every cube face with a simple painter's algorithm and samples the real entity
texture using vanilla's box UV layout. Output goes to ``tmp/model-preview``.
"""

from __future__ import annotations

import math
import re
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "neoforge-26.1.2" / "src" / "main" / "java" / "com" / "xxsx" / "earth" / "xuanxue" / "client" / "model"
ENTITY = ROOT / "neoforge-26.1.2" / "src" / "main" / "resources" / "assets" / "earth_xuanxue" / "textures" / "entity"
OUT = ROOT / "tmp" / "model-preview"

SHADE = {"up": 1.0, "down": 0.55, "west": 0.78, "east": 0.86, "north": 0.92, "south": 0.72}


@dataclass
class Box:
    x: float
    y: float
    z: float
    w: float
    h: float
    d: float
    u: int
    v: int
    grow: float = 0.0


@dataclass
class Part:
    name: str
    pivot: tuple[float, float, float]
    rot: tuple[float, float, float]
    boxes: list[Box] = field(default_factory=list)
    children: list["Part"] = field(default_factory=list)


def match_paren(text: str, start: int) -> int:
    depth = 0
    for i in range(start, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("unbalanced parentheses")


def parse_floats(expr: str) -> list[float]:
    values = []
    for token in re.findall(r"-?\d+(?:\.\d+)?F?", expr):
        values.append(float(token.rstrip("F")))
    return values


def parse_model(path: Path) -> Part:
    source = path.read_text(encoding="utf-8")
    root = Part("root", (0.0, 0.0, 0.0), (0.0, 0.0, 0.0))

    # Every statement looks like: <parent>.addOrReplaceChild("name", <builder>, <pose>);
    pattern = re.compile(r"(\w+)\.addOrReplaceChild\(")
    parts_by_var: dict[str, Part] = {}
    for match in pattern.finditer(source):
        parent_var = match.group(1)
        open_paren = source.index("(", match.start())
        close_paren = match_paren(source, open_paren)
        call = source[open_paren + 1:close_paren]

        name_match = re.match(r'\s*"([^"]+)"\s*,', call)
        if not name_match:
            continue
        name = name_match.group(1)

        # PartPose.ZERO is a constant, not a call; the others take arguments.
        if re.search(r"PartPose\.ZERO\b(?!\()", call):
            pivot = (0.0, 0.0, 0.0)
            rot = (0.0, 0.0, 0.0)
            pose_start = call.index("PartPose.ZERO")
        else:
            pose_match = re.search(r"PartPose\.(offset|offsetAndRotation)\(", call)
            if not pose_match:
                continue
            pose_start = pose_match.start()
            pose_open = call.index("(", pose_match.start())
            pose_close = match_paren(call, pose_open)
            pose_args = parse_floats(call[pose_open + 1:pose_close])
            if pose_match.group(1) == "offset":
                pivot = tuple(pose_args[:3])
                rot = (0.0, 0.0, 0.0)
            else:
                pivot = tuple(pose_args[:3])
                rot = tuple(pose_args[3:6])

        builder = call[:pose_start]
        boxes = []
        for tex in re.finditer(r"\.texOffs\((\d+),\s*(\d+)\)", builder):
            u, v = int(tex.group(1)), int(tex.group(2))
            tail = builder[tex.end():]
            box_match = re.search(r"\.addBox\(", tail)
            if not box_match:
                continue
            box_open = tail.index("(", box_match.start())
            box_close = match_paren(tail, box_open)
            args = parse_floats(tail[box_open + 1:box_close])
            grow = args[6] if len(args) >= 7 else 0.0
            boxes.append(Box(*args[:6], u=u, v=v, grow=grow))

        part = Part(name, pivot, rot, boxes)
        parent = parts_by_var.get(parent_var, root)
        parent.children.append(part)
        # The text between the previous statement and this call is
        # "PartDefinition name = parent.", so strip the trailing "parent." first.
        stmt_start = source.rfind(";", 0, match.start()) + 1
        stmt = source[stmt_start:match.start()]
        stmt = re.sub(r"\w+\.\s*$", "", stmt)
        assign = re.search(r"(\w+)\s*=\s*$", stmt)
        if assign:
            parts_by_var[assign.group(1)] = part
        parts_by_var.setdefault(parent_var, parent)
    return root


def rot_matrix(rx: float, ry: float, rz: float):
    cx, sx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)

    def mul(a, b):
        return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]

    mx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    my = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    mz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    return mul(mz, mul(my, mx))


def transform(part: Part, parent_matrix, parent_offset):
    """Return (matrix, offset) placing this part's local space into world space."""
    m = rot_matrix(*part.rot)
    # Compose parent rotation with this part's rotation.
    composed = [[sum(parent_matrix[i][k] * m[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    offset = [parent_offset[i] + part.pivot[i] for i in range(3)]
    return composed, offset


def apply(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


def collect(part: Part, parent_matrix, parent_offset, out, visible=True):
    m, offset = transform(part, parent_matrix, parent_offset)
    for box in part.boxes:
        out.append((box, m, offset, part.name))
    for child in part.children:
        collect(child, m, offset, out)


def face_quads(box: Box):
    x0, y0, z0 = box.x - box.grow, box.y - box.grow, box.z - box.grow
    x1, y1, z1 = box.x + box.w + box.grow, box.y + box.h + box.grow, box.z + box.d + box.grow
    return {
        "down": [(x0, y1, z0), (x0, y1, z1), (x1, y1, z1), (x1, y1, z0)],
        "up": [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)],
        "west": [(x0, y0, z0), (x0, y0, z1), (x0, y1, z1), (x0, y1, z0)],
        "north": [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)],
        "east": [(x1, y0, z0), (x1, y1, z0), (x1, y1, z1), (x1, y0, z1)],
        "south": [(x0, y0, z1), (x0, y1, z1), (x1, y1, z1), (x1, y0, z1)],
    }


def face_uv(box: Box, face: str):
    u, v = box.u, box.v
    w, h, d = int(box.w), int(box.h), int(box.d)
    rects = {
        "down": (u + d, v, u + d + w, v + d),
        "up": (u + d + w, v, u + d + w + w, v + d),
        "west": (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "east": (u + d + w, v + d, u + d + w + d, v + d + h),
        "south": (u + d + w + d, v + d, u + d + w + d + w, v + d + h),
    }
    return rects[face]


def render(model: Part, texture: Path, size: tuple[int, int], out_path: Path,
           yaw: float = 2.55, pitch: float = 0.30, scale: float = 7.0) -> None:
    tex = Image.open(texture).convert("RGBA")
    W, H = size
    canvas = Image.new("RGBA", (W, H), (18, 20, 26, 255))
    draw = ImageDraw.Draw(canvas)

    cubes = []
    collect(model, [[1, 0, 0], [0, 1, 0], [0, 0, 1]], [0.0, 0.0, 0.0], cubes)

    cy, sy = math.cos(yaw), math.sin(yaw)
    cp, sp = math.cos(pitch), math.sin(pitch)

    def project(p):
        # Minecraft entity models already use +Y down, matching screen space, so no flip.
        x, y, z = p[0], p[1], p[2]
        x, z = x * cy - z * sy, x * sy + z * cy
        y, z = y * cp - z * sp, y * sp + z * cp
        depth = z
        persp = 1.0 / (1.0 + z * 0.004)
        return (W / 2 + x * scale * persp, H / 2 - 40 + y * scale * persp), depth

    faces = []
    for box, m, offset, name in cubes:
        quads = face_quads(box)
        for face, corners in quads.items():
            world = []
            for corner in corners:
                local = apply(m, list(corner))
                world.append((local[0] + offset[0], local[1] + offset[1], local[2] + offset[2]))
            projected = [project(p) for p in world]
            depth = sum(p[1] for p in projected) / 4.0
            faces.append((depth, face, box, projected, name))

    faces.sort(key=lambda f: -f[0])

    for _, face, box, projected, _name in faces:
        points = [p[0] for p in projected]
        u0, v0, u1, v1 = face_uv(box, face)
        u0, v0 = max(0, u0), max(0, v0)
        u1, v1 = min(tex.width, u1), min(tex.height, v1)
        if u1 <= u0 or v1 <= v0:
            continue
        patch = tex.crop((u0, v0, u1, v1))
        if patch.width == 0 or patch.height == 0:
            continue

        # The quad is points[0..3] in order: TL, TR, BR, BL of the texture patch.
        # Solve the inverse affine map (screen -> patch) and let PIL do the forward warp,
        # which is what Image.transform expects.
        sx = [p[0] for p in points]
        sy = [p[1] for p in points]
        px0, py0 = sx[0], sy[0]
        px1, py1 = sx[1], sy[1]
        px3, py3 = sx[3], sy[3]
        det = (px1 - px0) * (py3 - py0) - (px3 - px0) * (py1 - py0)
        if abs(det) < 1e-6:
            continue
        # Forward map: screen = A * patch + t, where patch spans (0..w, 0..h).
        a = ((px1 - px0) / patch.width)
        b = ((px3 - px0) / patch.height)
        d = ((py1 - py0) / patch.width)
        e = ((py3 - py0) / patch.height)
        # PIL wants the inverse: patch = Ainv * screen + tinv.
        inv = 1.0 / (a * e - b * d)
        ia = e * inv
        ib = -b * inv
        id_ = -d * inv
        ie = a * inv
        ic = -(ia * px0 + ib * py0)
        if_ = -(id_ * px0 + ie * py0)

        warped = patch.transform((W, H), Image.AFFINE, (ia, ib, ic, id_, ie, if_),
                                 resample=Image.NEAREST)
        shade = SHADE[face]
        if shade < 1.0:
            rgb = warped.convert("RGB").point(lambda value, s=shade: int(value * s))
            warped = Image.merge("RGBA", (*rgb.split(), warped.getchannel("A")))
        canvas.alpha_composite(warped)

    OUT.mkdir(parents=True, exist_ok=True)
    canvas.save(out_path)


TARGETS = [
    ("SpiritFoxModel", "spirit_fox", (80, 80)),
    ("WindWolfModel", "wind_wolf", (128, 128)),
    ("StonehornGoatModel", "stonehorn_goat", (128, 96)),
    ("CrystalTurtleModel", "crystal_turtle", (96, 80)),
    ("EmberCraneModel", "ember_crane", (128, 128)),
    ("CultivationSettlerModel", "cultivation_settler_merchant", (128, 128)),
]


def main() -> None:
    for model_name, texture_name, size in TARGETS:
        path = MODELS / f"{model_name}.java"
        if not path.exists():
            print("missing", path)
            continue
        model = parse_model(path)
        texture = ENTITY / f"{texture_name}.png"
        out = OUT / f"{texture_name}.png"
        render(model, texture, (320, 320), out)
        print("rendered", out.relative_to(ROOT))


if __name__ == "__main__":
    main()
