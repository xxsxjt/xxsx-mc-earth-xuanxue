#!/usr/bin/env python3
"""Export all 6 Xuanhuan Earth entity models to Blockbench .bbmodel files under
D:\\PRIVATE_RESTRICTED\\Assets\\Blockbench\\xuanhuan_earth\\ and capture live
Blockbench viewport previews via bb_mcp_client.py.
"""
from __future__ import annotations

import base64
import json
import math
import os
import sys
import uuid
from pathlib import Path

sys.path.insert(0, r"D:\AGENT\WORKSPACES\Projects\mc-agent\tools")
sys.path.insert(0, str(Path(__file__).resolve().parent))

import bb_mcp_client as bb  # noqa: E402
import preview_models as pm  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
MODELS_DIR = ROOT / "neoforge-26.2" / "src" / "main" / "java" / "com" / "xxsx" / "xuanhuanearth" / "client" / "model"
TEX_DIR = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "xuanhuan_earth" / "textures" / "entity"

OUT_DIR = Path(r"D:\PRIVATE_RESTRICTED\Assets\Blockbench\xuanhuan_earth")
PREVIEW_DIR = OUT_DIR / "previews"


def make_uuid(seed_str: str) -> str:
    return str(uuid.uuid5(uuid.NAMESPACE_DNS, f"xuanhuan_earth.{seed_str}"))


def box_faces_uv(u: int, v: int, w: float, h: float, d: float) -> dict:
    # Blockbench modded_entity box_uv face UV format
    wi = math.floor(w)
    hi = math.floor(h)
    di = math.floor(d)
    return {
        "north": {"uv": [u + di, v + di, u + di + wi, v + di + hi], "texture": 0},
        "east": {"uv": [u, v + di, u + di, v + di + hi], "texture": 0},
        "south": {"uv": [u + 2 * di + wi, v + di, u + 2 * di + 2 * wi, v + di + hi], "texture": 0},
        "west": {"uv": [u + di + wi, v + di, u + 2 * di + wi, v + di + hi], "texture": 0},
        "up": {"uv": [u + di + wi, v + di, u + di, v], "texture": 0},
        "down": {"uv": [u + di + 2 * wi, v, u + di + wi, v + di], "texture": 0},
    }


def build_bbmodel_dict(model_id: str, java_file: Path, tex_file: Path) -> dict:
    root_part = pm.parse_model(java_file)
    tex_bytes = tex_file.read_bytes()
    b64_tex = "data:image/png;base64," + base64.b64encode(tex_bytes).decode("ascii")

    elements = []
    groups = []
    outliner = []

    def walk_part(part: pm.Part, parent_bb_origin: tuple[float, float, float], path_prefix: str):
        path = f"{path_prefix}.{part.name}" if path_prefix else part.name
        # Convert Java PartPose offset (relative to parent) to Blockbench world origin (flip_y=True)
        if not path_prefix:
            # Direct child of root: in Java root is (0, 0, 0) where y=24 is ground
            bb_ox = round(part.pivot[0] * 2.0) / 2.0
            bb_oy = round((24.0 - part.pivot[1]) * 2.0) / 2.0
            bb_oz = round(part.pivot[2] * 2.0) / 2.0
        else:
            bb_ox = round((parent_bb_origin[0] + part.pivot[0]) * 2.0) / 2.0
            bb_oy = round((parent_bb_origin[1] - part.pivot[1]) * 2.0) / 2.0
            bb_oz = round((parent_bb_origin[2] + part.pivot[2]) * 2.0) / 2.0

        # Convert radians to Blockbench degrees (Blockbench X/Y/Z with flip_y)
        rx_deg = round(-math.degrees(part.rot[0]), 2)
        ry_deg = round(-math.degrees(part.rot[1]), 2)
        rz_deg = round(math.degrees(part.rot[2]), 2)

        g_uuid = make_uuid(f"{model_id}.group.{path}")
        group_entry = {
            "name": part.name,
            "uuid": g_uuid,
            "export": True,
            "locked": False,
            "scope": 0,
            "selected": False,
            "visibility": True,
            "_static": {"properties": {}, "temp_data": {}},
            "origin": [round(bb_ox, 4), round(bb_oy, 4), round(bb_oz, 4)],
            "rotation": [rx_deg, ry_deg, rz_deg],
            "color": 0,
            "children": [],
            "reset": False,
            "shade": True,
            "mirror_uv": False,
            "autouv": 0,
            "isOpen": True,
            "primary_selected": False,
        }
        groups.append(group_entry)

        outliner_children = []
        for idx, box in enumerate(part.boxes):
            c_uuid = make_uuid(f"{model_id}.cube.{path}.{idx}")
            # Snap origin and box offsets to 1/4-unit (binary-exact float) and integer dimensions
            # so (to[i] - from[i]) is an exact integer in IEEE-754 floating point!
            w_int = int(round(box.w))
            h_int = int(round(box.h))
            d_int = int(round(box.d))
            x0 = round((bb_ox + box.x) * 2.0) / 2.0
            x1 = x0 + w_int
            y0 = round((bb_oy - box.y - h_int) * 2.0) / 2.0
            y1 = y0 + h_int
            z0 = round((bb_oz + box.z) * 2.0) / 2.0
            z1 = z0 + d_int

            cube_obj = {
                "name": f"{part.name}_{idx}" if len(part.boxes) > 1 else part.name,
                "box_uv": True,
                "render_order": "default",
                "locked": False,
                "export": True,
                "scope": 0,
                "allow_mirror_modeling": True,
                "from": [x0, y0, z0],
                "to": [x1, y1, z1],
                "autouv": 0,
                "color": 0,
                "origin": [round(bb_ox, 4), round(bb_oy, 4), round(bb_oz, 4)],
                "uv_offset": [box.u, box.v],
                "faces": box_faces_uv(box.u, box.v, box.w, box.h, box.d),
                "type": "cube",
                "uuid": c_uuid,
            }
            if box.grow:
                cube_obj["inflate"] = round(box.grow, 4)
            elements.append(cube_obj)
            outliner_children.append(c_uuid)

        for child in part.children:
            child_node = walk_part(child, (bb_ox, bb_oy, bb_oz), path)
            outliner_children.append(child_node)

        return {
            "uuid": g_uuid,
            "isOpen": True,
            "children": outliner_children,
        }

    for top_part in root_part.children:
        outliner.append(walk_part(top_part, (0.0, 24.0, 0.0), ""))

    tex_uuid = make_uuid(f"{model_id}.tex.0")
    return {
        "meta": {
            "format_version": "5.0",
            "model_format": "modded_entity",
            "box_uv": True,
        },
        "name": model_id,
        "model_identifier": model_id,
        "modded_entity_entity_class": "",
        "modded_entity_version": "1.17",
        "modded_entity_flip_y": True,
        "visible_box": [1, 1, 0],
        "variable_placeholders": "",
        "multi_file_ruleset": "",
        "variable_placeholder_buttons": [],
        "timeline_setups": [],
        "unhandled_root_fields": {},
        "resolution": {"width": 128, "height": 128},
        "elements": elements,
        "groups": groups,
        "outliner": outliner,
        "textures": [
            {
                "path": "",
                "relative_path": "",
                "name": tex_file.name,
                "folder": "entity",
                "namespace": "xuanhuan_earth",
                "id": "0",
                "group": "",
                "width": 128,
                "height": 128,
                "uv_width": 128,
                "uv_height": 128,
                "particle": True,
                "use_as_default": True,
                "layers_enabled": False,
                "sync_to_project": "",
                "render_mode": "default",
                "render_sides": "auto",
                "frame_time": 1,
                "frame_order_type": "loop",
                "frame_order": "",
                "frame_interpolate": False,
                "visible": True,
                "internal": True,
                "saved": True,
                "uuid": str(uuid.uuid4()),
                "source": b64_tex,
            }
        ],
    }


TARGETS = [
    ("spirit_fox", "SpiritFoxModel", "spirit_fox.png", [22, 18, -24], [0, 9, -1]),
    ("wind_wolf", "WindWolfModel", "wind_wolf.png", [25, 20, -26], [0, 10, -1]),
    ("stonehorn_goat", "StonehornGoatModel", "stonehorn_goat.png", [24, 19, -24], [0, 10, -1]),
    ("crystal_turtle", "CrystalTurtleModel", "crystal_turtle.png", [24, 18, -24], [0, 7, -1]),
    ("ember_crane", "EmberCraneModel", "ember_crane.png", [36, 26, -36], [0, 15, -2]),
    ("cultivation_settler", "CultivationSettlerModel", "cultivation_settler_steward.png", [34, 22, -34], [0, 14, 0]),
]


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)

    bb.close_all_projects()

    for model_id, java_cls, tex_name, cam_pos, cam_target in TARGETS:
        java_file = MODELS_DIR / f"{java_cls}.java"
        tex_file = TEX_DIR / tex_name
        bbmodel_path = OUT_DIR / f"{model_id}.bbmodel"
        preview_path = PREVIEW_DIR / f"{model_id}.png"

        data = build_bbmodel_dict(model_id, java_file, tex_file)
        b64_src = data["textures"][0]["source"]
        bbmodel_path.write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")

        info = bb.load_bbmodel(str(bbmodel_path))
        bb.eval_js(
            "(() => {"
            f"  const src = {json.dumps(b64_src)};"
            "  if (Texture.all[0]) {"
            "    Texture.all[0].fromDataURL(src);"
            "    Canvas.updateAllTextures();"
            "    Canvas.updateAllUVs();"
            "  }"
            "  return true;"
            "})()"
        )
        # Resave via Blockbench compiler so the .bbmodel is 100% canonical Blockbench 5.2 output
        bb.save_bbmodel(str(bbmodel_path))
        saved_png = bb.save_screenshot(
            str(preview_path),
            camera_angle={
                "position": cam_pos,
                "target": cam_target,
                "projection": "perspective",
            },
        )
        print(f"OK {model_id}: {info} -> {bbmodel_path} & {saved_png}")
        bb.close_all_projects()


if __name__ == "__main__":
    main()
