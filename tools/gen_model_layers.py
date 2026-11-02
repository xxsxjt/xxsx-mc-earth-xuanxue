"""Emit ``createBodyLayer`` from the same spec that paints the texture.

Keeping one spec for both geometry and UV is the only way to stop the two drifting apart:
if a box moves, its texOffs moves with it.
"""

from __future__ import annotations

import io
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import rebuild_beast_models as spec  # noqa: E402
from uv_layout import PartSpec, build  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]


def fmt(value: float) -> str:
    text = f"{value:.4f}".rstrip("0").rstrip(".")
    if "." not in text:
        text += ".0"
    return text + "F"


def emit_layer(size: int, parts: list[PartSpec]) -> str:
    lines = [
        "    public static LayerDefinition createBodyLayer() {",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
    ]

    def emit_part(part: PartSpec, parent_expr: str, path: str) -> str:
        var = parent_expr
        calls = []
        for box in part.boxes:
            call = (f"                        .texOffs({box.uv[0]}, {box.uv[1]})"
                    f".addBox({fmt(box.x)}, {fmt(box.y)}, {fmt(box.z)}, "
                    f"{fmt(box.w)}, {fmt(box.h)}, {fmt(box.d)}")
            if box.grow:
                call += f", new CubeDeformation({fmt(box.grow)})"
            calls.append(call + ")")
        if part.rot == (0.0, 0.0, 0.0):
            pose = (f"PartPose.offset({fmt(part.pivot[0])}, {fmt(part.pivot[1])}, "
                    f"{fmt(part.pivot[2])})")
        else:
            pose = (f"PartPose.offsetAndRotation({fmt(part.pivot[0])}, {fmt(part.pivot[1])}, "
                    f"{fmt(part.pivot[2])}, {fmt(part.rot[0])}, {fmt(part.rot[1])}, "
                    f"{fmt(part.rot[2])})")
        if part.children:
            var = re.sub(r"\W", "_", path)
            lines.append(f"        PartDefinition {var} = {parent_expr}.addOrReplaceChild(")
        else:
            lines.append(f"        {parent_expr}.addOrReplaceChild(")
        lines.append(f'                "{part.name}",')
        if calls:
            lines.append("                CubeListBuilder.create()")
            lines.extend(calls)
            lines[-1] = lines[-1] + ","
        else:
            lines.append("                CubeListBuilder.create(),")
        lines.append(f"                {pose});")
        for child in part.children:
            emit_part(child, var, f"{path}_{child.name}")
        return var

    for part in parts:
        emit_part(part, "root", part.name)
    lines.append("")
    lines.append(f"        return LayerDefinition.create(mesh, {size}, {size});")
    lines.append("    }")
    return "\n".join(lines)


def main() -> None:
    targets = {
        "SpiritFoxModel": ("spirit_fox", spec.fox),
        "WindWolfModel": ("wind_wolf", spec.wolf),
        "StonehornGoatModel": ("stonehorn_goat", spec.goat),
        "CrystalTurtleModel": ("crystal_turtle", spec.turtle),
        "EmberCraneModel": ("ember_crane", spec.crane),
    }
    chunks = []
    for model_name, (tex_id, factory) in targets.items():
        size, parts, base, accent = factory()
        # Paint the texture and assign UVs on this exact object graph, then read it back.
        build(tex_id, size, parts, base, accent)
        chunks.append(f"// ===== {model_name} =====")
        chunks.append(emit_layer(size, parts))
        chunks.append("")
    out = ROOT / "tmp" / "generated_layers.txt"
    out.parent.mkdir(parents=True, exist_ok=True)
    io.open(out, "w", encoding="utf-8", newline="\n").write("\n".join(chunks))
    print("wrote", out.relative_to(ROOT))


if __name__ == "__main__":
    main()
