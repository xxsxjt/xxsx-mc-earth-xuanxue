"""Beast model specs: one source of truth for geometry, UVs and textures.

Proportion rules applied here:

* All BoxSpec dimensions (w, h, d) MUST be exact positive integers, and all pivots and
  box offsets (x, y, z) MUST be multiples of 0.5 (binary-exact IEEE-754 floats) so
  Blockbench's Box UV floor(to - from) and Minecraft's CubeListBuilder.addBox integer UV
  truncation never drop 1 pixel or sample outside painted face rects.
* Torso depth stays near 1.3x its height; longer than that reads as a sausage.
* The head is at least 0.85x the torso height, and its top sits above the torso top so the
  silhouette shows a neck line.
* Ears are small wedges on the skull, never wide splayed plates.
* Tails taper through three segments instead of one big slab.
* Legs are 2-3 units thick and reach y=24 (the entity's feet).
"""

from __future__ import annotations

from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))

from uv_layout import BoxSpec, PartSpec  # noqa: E402

GROUND = 24.0


def fox():
    body = PartSpec("body", (0.0, 13.5, 0.0), boxes=[
        BoxSpec("torso", -3.0, -3.0, -4.5, 6.0, 6.0, 9.0, "fur"),
        BoxSpec("spine", -2.5, -3.5, -4.0, 5.0, 1.0, 8.0, "fur", grow=0.04),
        BoxSpec("brisket", -2.5, 1.0, -4.5, 5.0, 2.0, 3.0, "fur", grow=0.03),
        BoxSpec("haunch", -3.5, -2.0, 2.5, 7.0, 5.0, 3.0, "fur", grow=0.04),
    ])

    head = PartSpec("head", (0.0, 9.5, -6.0), boxes=[
        BoxSpec("skull", -3.0, -2.5, -3.5, 6.0, 5.0, 7.0, "fur"),
    ])
    head.children.append(PartSpec("muzzle", (0.0, 0.5, -3.5), boxes=[
        BoxSpec("bridge", -1.5, -1.0, -3.0, 3.0, 2.0, 3.0, "fur"),
        BoxSpec("nose", -1.0, 0.0, -4.0, 2.0, 1.0, 1.0, "metal"),
    ]))
    head.children.append(PartSpec("jaw", (0.0, 2.0, -2.5), boxes=[
        BoxSpec("chin", -1.5, 0.0, -2.0, 3.0, 1.0, 2.0, "fur"),
    ]))
    # Ears: small upright wedges close to the skull, tilted slightly outward.
    head.children.append(PartSpec("left_ear", (1.5, -2.5, 0.0), (-0.12, 0.0, 0.14), [
        BoxSpec("outer", -1.0, -3.0, -0.5, 2.0, 3.0, 1.0, "fur"),
        BoxSpec("inner", -0.5, -2.0, -1.0, 1.0, 2.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("right_ear", (-1.5, -2.5, 0.0), (-0.12, 0.0, -0.14), [
        BoxSpec("outer", -1.0, -3.0, -0.5, 2.0, 3.0, 1.0, "fur"),
        BoxSpec("inner", -0.5, -2.0, -1.0, 1.0, 2.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("crest", (0.0, -2.5, -1.0), boxes=[
        BoxSpec("tuft", -0.5, -2.0, -0.5, 1.0, 2.0, 1.0, "crystal"),
    ]))

    neck = PartSpec("neck", (0.0, 11.0, -4.5), (0.34, 0.0, 0.0), [
        BoxSpec("bridge", -2.0, -2.0, -2.0, 4.0, 4.0, 4.0, "fur"),
    ])
    parts = [body, neck, head]
    for name, x, z in (("front_right", -2.0, -3.0), ("front_left", 2.0, -3.0),
                       ("back_right", -2.0, 3.0), ("back_left", 2.0, 3.0)):
        parts.append(PartSpec(name, (x, 16.5, z), boxes=[
            BoxSpec("upper", -1.0, 0.0, -1.0, 2.0, 6.0, 2.0, "fur"),
            BoxSpec("paw", -1.5, 5.5, -2.0, 3.0, 2.0, 3.0, "fur"),
        ]))

    tail = PartSpec("tail", (0.0, 12.0, 4.5), (0.48, 0.0, 0.0), [
        BoxSpec("base", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur"),
    ])
    mid = PartSpec("tail_mid", (0.0, 0.0, 4.5), (0.14, 0.0, 0.0), [
        BoxSpec("mid", -2.5, -2.5, 0.0, 5.0, 5.0, 4.0, "fur"),
    ])
    mid.children.append(PartSpec("tail_tip", (0.0, 0.0, 4.0), (0.12, 0.0, 0.0), [
        BoxSpec("tip", -2.0, -2.0, 0.0, 4.0, 4.0, 4.0, "crystal"),
    ]))
    tail.children.append(mid)
    parts.append(tail)

    return 128, parts, (146, 154, 170), (96, 226, 214)


def wolf():
    body = PartSpec("body", (0.0, 13.0, 0.0), boxes=[
        BoxSpec("torso", -4.0, -3.5, -5.0, 8.0, 7.0, 10.0, "fur"),
        BoxSpec("spine", -3.0, -4.0, -4.5, 6.0, 1.0, 9.0, "fur", grow=0.05),
        BoxSpec("brisket", -3.0, 1.0, -5.0, 6.0, 2.0, 4.0, "fur", grow=0.03),
        BoxSpec("haunch", -4.0, -2.0, 2.5, 8.0, 6.0, 3.0, "fur", grow=0.05),
    ])

    mane = PartSpec("mane", (0.0, 13.0, -4.5), boxes=[
        BoxSpec("ruff", -4.5, -4.5, -2.5, 9.0, 9.0, 5.0, "fur", grow=0.07),
    ])
    mane.children.append(PartSpec("mane_top", (0.0, -4.0, -0.5), (-0.2, 0.0, 0.0), [
        BoxSpec("ridge", -1.5, -1.5, 0.0, 3.0, 3.0, 7.0, "fur"),
    ]))
    mane.children.append(PartSpec("mane_left", (-3.5, -1.0, 0.0), (-0.08, 0.16, -0.26), [
        BoxSpec("flank", -2.0, -2.0, 0.0, 2.0, 4.0, 6.0, "fur"),
    ]))
    mane.children.append(PartSpec("mane_right", (3.5, -1.0, 0.0), (-0.08, -0.16, 0.26), [
        BoxSpec("flank", 0.0, -2.0, 0.0, 2.0, 4.0, 6.0, "fur"),
    ]))

    head = PartSpec("head", (0.0, 9.0, -7.0), boxes=[
        BoxSpec("skull", -3.0, -3.0, -3.5, 6.0, 6.0, 7.0, "fur"),
    ])
    head.children.append(PartSpec("snout", (0.0, 0.5, -3.5), boxes=[
        BoxSpec("muzzle", -2.0, -1.0, -3.0, 4.0, 3.0, 3.0, "fur"),
        BoxSpec("nose", -1.0, 0.0, -4.0, 2.0, 1.0, 1.0, "metal"),
    ]))
    head.children.append(PartSpec("left_ear", (2.0, -3.0, 0.5), (-0.14, 0.0, 0.16), [
        BoxSpec("outer", -1.0, -3.0, -1.0, 2.0, 3.0, 2.0, "fur"),
    ]))
    head.children.append(PartSpec("right_ear", (-2.0, -3.0, 0.5), (-0.14, 0.0, -0.16), [
        BoxSpec("outer", -1.0, -3.0, -1.0, 2.0, 3.0, 2.0, "fur"),
    ]))

    collar = PartSpec("collar", (0.0, 13.0, -4.5), boxes=[
        BoxSpec("band", -4.5, -1.0, -3.5, 9.0, 2.0, 7.0, "metal", grow=0.05),
    ])
    collar.children.append(PartSpec("pendant", (0.0, 1.0, -4.0), boxes=[
        BoxSpec("gem", -1.5, -0.5, -0.5, 3.0, 4.0, 1.0, "crystal"),
    ]))

    neck = PartSpec("neck", (0.0, 11.0, -5.0), (0.36, 0.0, 0.0), [
        BoxSpec("bridge", -3.0, -2.5, -2.0, 6.0, 5.0, 5.0, "fur"),
    ])
    parts = [body, neck, mane, head, collar]
    for name, x, z in (("front_right", -2.5, -3.0), ("front_left", 2.5, -3.0),
                       ("back_right", -2.5, 3.5), ("back_left", 2.5, 3.5)):
        parts.append(PartSpec(name, (x, 16.5, z), boxes=[
            BoxSpec("upper", -1.5, 0.0, -1.5, 3.0, 6.0, 3.0, "fur"),
            BoxSpec("paw", -1.5, 5.5, -2.0, 3.0, 2.0, 3.0, "fur"),
        ]))

    tail = PartSpec("tail", (0.0, 10.5, 5.0), (0.26, 0.0, 0.0), [
        BoxSpec("base", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur"),
    ])
    mid = PartSpec("tail_mid", (0.0, 0.0, 5.0), (0.14, 0.0, 0.0), [
        BoxSpec("mid", -2.5, -2.5, 0.0, 5.0, 5.0, 5.0, "fur"),
    ])
    mid.children.append(PartSpec("tail_tip", (0.0, 0.0, 4.5), (0.12, 0.0, 0.0), [
        BoxSpec("tip", -2.0, -2.0, 0.0, 4.0, 4.0, 4.0, "crystal"),
    ]))
    tail.children.append(mid)
    parts.append(tail)

    return 128, parts, (108, 124, 140), (110, 228, 250)


def goat():
    body = PartSpec("body", (0.0, 13.5, 0.0), boxes=[
        BoxSpec("torso", -4.0, -3.5, -5.0, 8.0, 7.0, 10.0, "fur"),
        BoxSpec("back", -3.0, -4.0, -4.0, 6.0, 1.0, 8.0, "fur", grow=0.04),
    ])
    armor = PartSpec("armor", (0.0, 13.5, 0.0), boxes=[
        BoxSpec("plate", -4.5, -2.0, -4.0, 9.0, 3.0, 8.0, "metal", grow=0.04),
        BoxSpec("saddle", -3.0, -3.0, -1.5, 6.0, 1.0, 3.0, "fur"),
    ])

    head = PartSpec("head", (0.0, 9.0, -5.5), boxes=[
        BoxSpec("skull", -3.0, -3.0, -3.0, 6.0, 6.0, 6.0, "fur"),
    ])
    head.children.append(PartSpec("muzzle", (0.0, 1.0, -3.0), boxes=[
        BoxSpec("muzzle", -2.0, -1.0, -3.0, 4.0, 3.0, 3.0, "fur"),
        BoxSpec("nose", -1.0, 0.0, -3.5, 2.0, 1.0, 1.0, "metal"),
    ]))
    head.children.append(PartSpec("beard", (0.0, 2.0, -2.0), (0.26, 0.0, 0.0), [
        BoxSpec("beard", -1.0, 0.0, -1.0, 2.0, 4.0, 2.0, "fur"),
    ]))

    # Ram horns: three segments curling back and out, each smaller than the last.
    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        head.children.append(PartSpec(f"{name}_base", (2.0 * side, -2.0, 0.0),
                (-1.45, 0.0, -0.26 * side), [
                    BoxSpec("base", -1.0, -3.0, -1.0, 2.0, 3.0, 2.0, "metal"),
                ]))
        head.children.append(PartSpec(f"{name}_mid", (2.5 * side, -3.5, 1.5),
                (-1.55, 0.0, -0.18 * side), [
                    BoxSpec("mid", -1.0, -2.0, -1.0, 2.0, 2.0, 2.0, "metal"),
                ]))
        head.children.append(PartSpec(f"{name}_tip", (2.5 * side, -4.5, 3.0),
                (-1.60, 0.0, -0.12 * side), [
                    BoxSpec("tip", -0.5, -2.0, -0.5, 1.0, 2.0, 1.0, "metal"),
                ]))

    parts = [body, armor, head]
    for name, x, z in (("front_right_leg", -3.0, -3.5), ("front_left_leg", 3.0, -3.5),
                       ("back_right_leg", -3.0, 3.5), ("back_left_leg", 3.0, 3.5)):
        parts.append(PartSpec(name, (x, 16.5, z), boxes=[
            BoxSpec("upper", -1.5, 0.0, -1.5, 3.0, 6.0, 3.0, "fur"),
            BoxSpec("hoof", -1.5, 5.5, -2.0, 3.0, 2.0, 4.0, "metal"),
        ]))

    tail = PartSpec("tail", (0.0, 11.5, 4.5), (0.7, 0.0, 0.0), [
        BoxSpec("tail", -1.0, -1.0, 0.0, 2.0, 2.0, 4.0, "fur"),
    ])
    parts.append(tail)

    return 128, parts, (118, 110, 98), (244, 164, 62)


def turtle():
    # A turtle must read from the side: shallow dome, head above the rim, splayed flippers.
    body = PartSpec("body", (0.0, 18.5, 0.0), boxes=[
        BoxSpec("belly", -4.0, 0.0, -5.0, 8.0, 2.0, 10.0, "fur"),
    ])
    shell = PartSpec("shell", (0.0, 15.5, 0.0), boxes=[
        BoxSpec("dome", -5.0, -2.0, -5.5, 10.0, 3.0, 11.0, "crystal"),
        BoxSpec("rim", -5.5, 0.5, -6.0, 11.0, 1.0, 12.0, "crystal", grow=0.04),
    ])
    crystals = PartSpec("crystals", (0.0, -2.0, 0.0), boxes=[
        BoxSpec("main", -1.0, -4.0, -1.0, 2.0, 4.0, 2.0, "crystal"),
    ])
    crystals.children.append(PartSpec("left_crystal", (-2.0, 0.5, 1.0), (-0.2, 0.0, -0.42), [
        BoxSpec("shard", -0.5, -2.0, -0.5, 1.0, 2.0, 1.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("rear_crystal", (2.0, 0.5, 2.5), (0.16, 0.0, 0.36), [
        BoxSpec("shard", -0.5, -2.0, -0.5, 1.0, 2.0, 1.0, "crystal"),
    ]))
    shell.children.append(crystals)

    # Head: top above the shell, projecting forward, with a neck bridging the gap.
    head = PartSpec("head", (0.0, 14.5, -7.5), boxes=[
        BoxSpec("skull", -2.0, -2.5, -3.5, 4.0, 5.0, 5.0, "fur"),
        BoxSpec("beak", -1.0, -1.0, -4.5, 2.0, 2.0, 1.0, "metal"),
        BoxSpec("crest", -0.5, -3.5, -1.0, 1.0, 1.0, 2.0, "crystal"),
    ])
    head.children.append(PartSpec("neck", (0.0, 2.0, 1.5), boxes=[
        BoxSpec("bridge", -2.5, -2.0, 0.0, 5.0, 4.0, 4.0, "fur"),
    ]))

    parts = [body, shell, head]
    for name, x, z in (("front_right", -5.0, -3.0), ("front_left", 5.0, -3.0),
                       ("back_right", -5.0, 3.0), ("back_left", 5.0, 3.0)):
        parts.append(PartSpec(name, (x, 19.5, z), boxes=[
            BoxSpec("flipper", -2.5, -1.0, -2.0, 5.0, 3.0, 4.0, "fur"),
        ]))

    tail = PartSpec("tail", (0.0, 19.5, 5.5), boxes=[
        BoxSpec("stub", -1.0, -1.0, 0.0, 2.0, 2.0, 3.0, "fur"),
    ])
    parts.append(tail)

    return 128, parts, (44, 92, 78), (88, 232, 224)


def crane():
    body = PartSpec("body", (0.0, 10.5, 0.0), boxes=[
        BoxSpec("torso", -3.5, -3.5, -4.5, 7.0, 7.0, 9.0, "feather"),
        BoxSpec("breast", -3.0, 2.0, -4.0, 6.0, 2.0, 4.0, "feather", grow=0.04),
    ])

    # S-curve neck: lower segment leans forward, upper segment leans back.
    neck = PartSpec("neck", (0.0, 7.5, -3.5), (-0.34, 0.0, 0.0), [
        BoxSpec("lower", -1.5, -5.0, -1.5, 3.0, 5.0, 3.0, "feather"),
    ])
    upper = PartSpec("upper", (0.0, -5.0, 0.0), (0.62, 0.0, 0.0), [
        BoxSpec("upper", -1.5, -4.0, -1.5, 3.0, 4.0, 3.0, "feather"),
    ])
    head = PartSpec("head", (0.0, -4.0, 0.0), boxes=[
        BoxSpec("skull", -2.0, -2.0, -2.5, 4.0, 4.0, 4.0, "feather"),
        BoxSpec("crest", -0.5, -3.0, -1.0, 1.0, 1.0, 2.0, "metal"),
        BoxSpec("beak", -1.0, -0.5, -4.5, 2.0, 2.0, 2.0, "metal"),
    ])
    upper.children.append(head)
    neck.children.append(upper)

    wings = []
    for name, side in (("left_wing", 1.0), ("right_wing", -1.0)):
        wing = PartSpec(name, (3.0 * side, 8.5, 0.0), (0.0, 0.0, -0.42 * side), [
            BoxSpec("arm", -1.0, -2.5, -3.5, 2.0, 5.0, 7.0, "feather"),
        ])
        for i in range(3):
            wing.children.append(PartSpec(
                f"feather_{i}", (1.0 * side, 1.5 + i * 1.0, -2.0 + i * 1.5),
                (0.06 + i * 0.09, 0.0, -0.1 * side), [
                    BoxSpec("vane", -0.5, -1.0, -1.0, 1.0, 2.0, 7.0, "feather"),
                ]))
        wings.append(wing)

    legs = []
    for name, side in (("left_leg", 1.0), ("right_leg", -1.0)):
        leg = PartSpec(name, (1.5 * side, 14.5, 0.0), boxes=[
            BoxSpec("shin", -0.5, 0.0, -0.5, 1.0, 8.0, 1.0, "metal"),
        ])
        leg.children.append(PartSpec("foot", (0.0, 7.5, 0.0), boxes=[
            BoxSpec("foot", -1.0, 0.0, -2.5, 2.0, 1.0, 3.0, "metal"),
        ]))
        legs.append(leg)

    tail = PartSpec("tail", (0.0, 10.0, 4.5), (0.38, 0.0, 0.0), [
        BoxSpec("base", -1.5, -1.0, 0.0, 3.0, 2.0, 6.0, "feather"),
    ])
    tail.children.append(PartSpec("left_tail_feather", (1.0, 0.0, 0.5), (0.06, 0.2, 0.0), [
        BoxSpec("vane", -1.0, -1.0, 0.0, 2.0, 2.0, 7.0, "feather"),
    ]))
    tail.children.append(PartSpec("right_tail_feather", (-1.0, 0.0, 0.5), (0.06, -0.2, 0.0), [
        BoxSpec("vane", -1.0, -1.0, 0.0, 2.0, 2.0, 7.0, "feather"),
    ]))

    return 128, [body, neck, *wings, *legs, tail], (216, 204, 172), (214, 92, 48)
