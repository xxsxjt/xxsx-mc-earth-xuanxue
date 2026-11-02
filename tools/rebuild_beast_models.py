"""Beast model specs: one source of truth for geometry, UVs and textures.

Design requirements enforced here:
* All BoxSpec dimensions (w, h, d) are exact positive integers, and all pivots/offsets
  are multiples of 0.5 (binary-exact IEEE-754 floats).
* Rich sub-bone hierarchies with genuine PartPose.offsetAndRotation angles:
  - SpiritFox: multi-stage tapered muzzle, wide splayed ears with inner fluff & glowing tips,
    digitigrade 3-joint legs (thigh -> shin -> paw), and a 3-plume fan-splayed spirit tail
    with jade-flame tips.
  - WindWolf: broad muscular chest & tapered waist, multi-layered windswept mane & shoulder
    wind-blade fins, digitigrade predator legs, and a drooping-then-upturned 3-joint tail.
  - StonehornGoat: 4-segment true helical ram horns curling up, out, back, and forward with
    golden ley-line veins, angled goatee, sloped mountain-rock shoulder armor, and cloven hooves.
  - EmberCrane: 4-segment S-curved slender neck, cinnabar-crimson crown & long pointed beak,
    layered angled primary/secondary wing feathers (obsidian tips + fire veins), fan-splayed
    tail plumes, and long 3-joint legs with 3-toed talons.
  - CrystalTurtle: octagonal tiered dome carapace (no vertical chimney!), 6 radiating angled
    spirit crystal spires (main + 5 side/rear shards), dragon-turtle horned/bearded head,
    splayed swimming flippers with claws, and crystal-spiked tail.
"""

from __future__ import annotations

from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))

from uv_layout import BoxSpec, PartSpec  # noqa: E402

GROUND = 24.0


def _digitigrade_leg(name: str, x: float, y: float, z: float, is_hind: bool,
                     thigh_w: int = 2, thigh_h: int = 4, thigh_d: int = 3,
                     shin_w: int = 2, shin_h: int = 4, shin_d: int = 2,
                     paw_w: int = 3, paw_h: int = 2, paw_d: int = 3,
                     paw_style: str = "fur") -> PartSpec:
    # In MC (flip_y=True), +Y is down and +Z is backward.
    # A negative xRot tilts a downward (+Y) limb forward (-Z); a positive xRot tilts it backward (+Z).
    # For digitigrade legs, the thigh angles slightly forward, the hock/shin angles back, and the paw levels out.
    thigh_rx = -0.18 if is_hind else -0.08
    shin_rx = 0.34 if is_hind else 0.16
    paw_rx = -0.16 if is_hind else -0.08

    leg = PartSpec(name, (x, y, z), (thigh_rx, 0.0, 0.0), [
        BoxSpec("thigh", -thigh_w / 2.0, -0.5, -thigh_d / 2.0, float(thigh_w), float(thigh_h), float(thigh_d), "fur"),
    ])
    shin = PartSpec("shin", (0.0, float(thigh_h - 1.0), 0.0), (shin_rx, 0.0, 0.0), [
        BoxSpec("lower", -shin_w / 2.0, -0.5, -shin_d / 2.0, float(shin_w), float(shin_h), float(shin_d), "fur"),
    ])
    paw = PartSpec("paw", (0.0, float(shin_h - 1.0), 0.0), (paw_rx, 0.0, 0.0), [
        BoxSpec("foot", -paw_w / 2.0, -0.5, -paw_d / 2.0 - 0.5, float(paw_w), float(paw_h), float(paw_d), paw_style),
    ])
    shin.children.append(paw)
    leg.children.append(shin)
    return leg


def fox():
    # Lithe, arched torso with chest ruff, shoulder mane, and tapered waist
    body = PartSpec("body", (0.0, 14.0, 0.0), (-0.04, 0.0, 0.0), [
        BoxSpec("torso", -3.0, -3.0, -4.5, 6.0, 6.0, 5.0, "fur"),
        BoxSpec("waist", -2.5, -2.5, 0.5, 5.0, 5.0, 5.0, "fur"),
        BoxSpec("brisket", -3.5, -2.0, -5.0, 7.0, 5.0, 3.0, "fur", grow=0.04),
    ])
    body.children.append(PartSpec("spine_crest", (0.0, -3.0, -3.5), (0.14, 0.0, 0.0), [
        BoxSpec("ridge", -1.5, -1.0, 0.0, 3.0, 1.0, 8.0, "fur_glow"),
    ]))

    neck = PartSpec("neck", (0.0, 12.0, -4.0), (0.42, 0.0, 0.0), [
        BoxSpec("bridge", -2.0, -2.5, -2.0, 4.0, 5.0, 4.0, "fur"),
        BoxSpec("ruff_left", 1.5, -1.5, -1.5, 2.0, 4.0, 3.0, "fur_glow"),
        BoxSpec("ruff_right", -3.5, -1.5, -1.5, 2.0, 4.0, 3.0, "fur_glow"),
    ])

    head = PartSpec("head", (0.0, 10.0, -6.0), (0.06, 0.0, 0.0), [
        BoxSpec("skull", -3.0, -2.5, -3.0, 6.0, 5.0, 5.0, "fur"),
        BoxSpec("cheek_left", 2.5, -0.5, -2.0, 1.0, 3.0, 3.0, "fur_glow"),
        BoxSpec("cheek_right", -3.5, -0.5, -2.0, 1.0, 3.0, 3.0, "fur_glow"),
    ])
    # Multi-stage tapered fox snout
    muzzle = PartSpec("muzzle", (0.0, 0.5, -3.0), (0.08, 0.0, 0.0), [
        BoxSpec("bridge", -2.0, -1.0, -2.0, 4.0, 2.0, 2.0, "fur"),
        BoxSpec("tip", -1.0, -0.5, -4.0, 2.0, 2.0, 2.0, "fur"),
        BoxSpec("nose", -0.5, -1.0, -4.5, 1.0, 1.0, 1.0, "metal"),
    ])
    head.children.append(muzzle)
    head.children.append(PartSpec("jaw", (0.0, 2.0, -2.5), (0.06, 0.0, 0.0), [
        BoxSpec("chin", -1.5, 0.0, -2.5, 3.0, 1.0, 3.0, "fur"),
    ]))

    # Large fennec/spirit-fox ears splayed outward and swept back with inner fluff & glowing tips
    for ename, side in (("left_ear", 1.0), ("right_ear", -1.0)):
        ear = PartSpec(ename, (2.0 * side, -2.0, -0.5), (-0.22, -0.18 * side, 0.42 * side), [
            BoxSpec("outer", -1.0, -4.0, -0.5, 2.0, 4.0, 1.0, "fur"),
            BoxSpec("inner", -0.5, -3.0, -1.0, 1.0, 3.0, 1.0, "crystal"),
        ])
        ear.children.append(PartSpec("ear_tip", (0.0, -4.0, 0.0), (-0.14, 0.0, 0.12 * side), [
            BoxSpec("tip", -0.5, -2.0, -0.5, 1.0, 2.0, 1.0, "fur_glow"),
        ]))
        head.children.append(ear)

    head.children.append(PartSpec("crest", (0.0, -2.5, -1.5), (-0.36, 0.0, 0.0), [
        BoxSpec("tuft", -0.5, -3.0, -0.5, 1.0, 3.0, 2.0, "crystal"),
    ]))

    parts = [body, neck, head]
    for lname, x, z, is_hind in (
        ("front_right", -2.0, -3.5, False),
        ("front_left", 2.0, -3.5, False),
        ("back_right", -2.0, 3.5, True),
        ("back_left", 2.0, 3.5, True),
    ):
        parts.append(_digitigrade_leg(lname, x, 15.5, z, is_hind,
                                      thigh_w=2, thigh_h=4, thigh_d=3,
                                      shin_w=2, shin_h=4, shin_d=2,
                                      paw_w=2, paw_h=2, paw_d=3, paw_style="fur_glow"))

    # Fan-splayed 3-plume celestial fox tail: center plume + left/right swept plumes
    tail = PartSpec("tail", (0.0, 13.0, 5.0), (0.38, 0.0, 0.0), [
        BoxSpec("base", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur"),
    ])
    mid = PartSpec("tail_mid", (0.0, 0.0, 4.5), (0.24, 0.0, 0.0), [
        BoxSpec("mid", -2.5, -2.5, 0.0, 5.0, 5.0, 5.0, "fur"),
    ])
    mid.children.append(PartSpec("tail_tip", (0.0, 0.0, 4.5), (0.22, 0.0, 0.0), [
        BoxSpec("tip", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur_glow"),
        BoxSpec("flame", -1.0, -1.0, 4.5, 2.0, 2.0, 3.0, "crystal"),
    ]))
    tail.children.append(mid)

    for sname, side in (("left_plume", 1.0), ("right_plume", -1.0)):
        plume = PartSpec(sname, (1.5 * side, 0.0, 1.0), (0.18, 0.44 * side, 0.16 * side), [
            BoxSpec("plume_base", -1.5, -1.5, 0.0, 3.0, 3.0, 5.0, "fur"),
        ])
        plume_mid = PartSpec("plume_mid", (0.0, 0.0, 4.5), (0.22, 0.18 * side, 0.0), [
            BoxSpec("plume_body", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur_glow"),
        ])
        plume_mid.children.append(PartSpec("plume_tip", (0.0, 0.0, 4.5), (0.20, 0.12 * side, 0.0), [
            BoxSpec("plume_flame", -1.0, -1.0, 0.0, 2.0, 2.0, 4.0, "crystal"),
        ]))
        plume.children.append(plume_mid)
        tail.children.append(plume)

    parts.append(tail)
    return 128, parts, (164, 174, 192), (82, 238, 224)


def wolf():
    # Powerful broad chest + sloped lupine waist
    body = PartSpec("body", (0.0, 13.0, 0.0), (-0.06, 0.0, 0.0), [
        BoxSpec("torso", -4.0, -3.5, -5.5, 8.0, 7.0, 6.0, "fur"),
        BoxSpec("waist", -3.0, -3.0, 0.5, 6.0, 6.0, 5.0, "fur"),
        BoxSpec("brisket", -3.5, 1.0, -6.0, 7.0, 3.0, 4.0, "fur", grow=0.04),
    ])
    # Swept dorsal wind-spines
    body.children.append(PartSpec("dorsal_fin_1", (0.0, -3.5, -3.0), (-0.42, 0.0, 0.0), [
        BoxSpec("fin1", -1.0, -2.0, 0.0, 2.0, 2.0, 5.0, "fur_glow"),
    ]))
    body.children.append(PartSpec("dorsal_fin_2", (0.0, -3.0, 1.5), (-0.48, 0.0, 0.0), [
        BoxSpec("fin2", -1.0, -2.0, 0.0, 2.0, 2.0, 4.0, "crystal"),
    ]))

    neck = PartSpec("neck", (0.0, 11.0, -5.0), (0.38, 0.0, 0.0), [
        BoxSpec("bridge", -3.0, -2.5, -2.0, 6.0, 5.0, 5.0, "fur"),
    ])

    # Multi-layered windswept mane + shoulder wind-blade wings
    mane = PartSpec("mane", (0.0, 12.5, -4.5), (0.12, 0.0, 0.0), [
        BoxSpec("ruff", -4.5, -4.0, -2.5, 9.0, 8.0, 5.0, "fur", grow=0.06),
    ])
    mane.children.append(PartSpec("mane_top", (0.0, -4.0, -1.0), (-0.36, 0.0, 0.0), [
        BoxSpec("ridge", -2.0, -2.0, 0.0, 4.0, 3.0, 7.0, "fur_glow"),
        BoxSpec("crest_blade", -1.0, -3.0, 2.0, 2.0, 2.0, 6.0, "crystal"),
    ]))
    mane.children.append(PartSpec("mane_left", (-4.0, -1.5, -1.0), (0.14, -0.38, -0.32), [
        BoxSpec("flank", -2.0, -2.0, 0.0, 2.0, 4.0, 7.0, "fur_glow"),
        BoxSpec("wind_blade_l", -1.0, -1.0, 4.0, 1.0, 2.0, 5.0, "crystal"),
    ]))
    mane.children.append(PartSpec("mane_right", (4.0, -1.5, -1.0), (0.14, 0.38, 0.32), [
        BoxSpec("flank", 0.0, -2.0, 0.0, 2.0, 4.0, 7.0, "fur_glow"),
        BoxSpec("wind_blade_r", 0.0, -1.0, 4.0, 1.0, 2.0, 5.0, "crystal"),
    ]))

    head = PartSpec("head", (0.0, 9.0, -7.5), (0.04, 0.0, 0.0), [
        BoxSpec("skull", -3.5, -3.0, -3.5, 7.0, 6.0, 6.0, "fur"),
        BoxSpec("brow", -3.5, -3.5, -4.0, 7.0, 1.0, 3.0, "fur", grow=0.04),
    ])
    snout = PartSpec("snout", (0.0, 0.5, -3.5), (0.06, 0.0, 0.0), [
        BoxSpec("muzzle", -2.0, -1.0, -3.5, 4.0, 3.0, 4.0, "fur"),
        BoxSpec("nose", -1.0, -1.0, -4.0, 2.0, 1.0, 1.0, "metal"),
    ])
    snout.children.append(PartSpec("jaw", (0.0, 2.0, 0.0), (0.08, 0.0, 0.0), [
        BoxSpec("lower_jaw", -1.5, 0.0, -3.0, 3.0, 1.0, 3.0, "fur"),
    ]))
    head.children.append(snout)

    for ename, side in (("left_ear", 1.0), ("right_ear", -1.0)):
        head.children.append(PartSpec(ename, (2.5 * side, -2.5, -0.5), (-0.28, -0.16 * side, 0.32 * side), [
            BoxSpec("outer", -1.0, -4.0, -1.0, 2.0, 4.0, 2.0, "fur"),
            BoxSpec("tip", -0.5, -5.0, -0.5, 1.0, 2.0, 1.0, "fur_glow"),
        ]))

    collar = PartSpec("collar", (0.0, 12.5, -5.0), (0.18, 0.0, 0.0), [
        BoxSpec("band", -4.5, -1.0, -3.0, 9.0, 2.0, 6.0, "metal", grow=0.06),
    ])
    collar.children.append(PartSpec("pendant", (0.0, 1.0, -3.5), (-0.16, 0.0, 0.0), [
        BoxSpec("gem", -1.5, 0.0, -0.5, 3.0, 4.0, 1.0, "crystal"),
    ]))

    parts = [body, neck, mane, head, collar]
    for lname, x, z, is_hind in (
        ("front_right", -2.5, -3.5, False),
        ("front_left", 2.5, -3.5, False),
        ("back_right", -2.5, 3.5, True),
        ("back_left", 2.5, 3.5, True),
    ):
        parts.append(_digitigrade_leg(lname, x, 15.0, z, is_hind,
                                      thigh_w=3, thigh_h=5, thigh_d=3,
                                      shin_w=2, shin_h=4, shin_d=2,
                                      paw_w=3, paw_h=2, paw_d=3, paw_style="fur_glow"))

    # Wolf tail: slopes downward from croup then sweeps upward at the tip
    tail = PartSpec("tail", (0.0, 11.5, 5.0), (-0.22, 0.0, 0.0), [
        BoxSpec("base", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur"),
    ])
    mid = PartSpec("tail_mid", (0.0, 0.0, 4.5), (0.34, 0.0, 0.0), [
        BoxSpec("mid", -2.5, -2.5, 0.0, 5.0, 5.0, 5.0, "fur"),
    ])
    mid.children.append(PartSpec("tail_tip", (0.0, 0.0, 4.5), (0.32, 0.0, 0.0), [
        BoxSpec("tip", -2.0, -2.0, 0.0, 4.0, 4.0, 5.0, "fur_glow"),
        BoxSpec("blade", -1.0, -1.0, 4.5, 2.0, 2.0, 3.0, "crystal"),
    ]))
    tail.children.append(mid)
    parts.append(tail)

    return 128, parts, (86, 108, 132), (96, 236, 255)


def goat():
    # Heavy mountain-ram torso with raised shoulder hump
    body = PartSpec("body", (0.0, 13.5, 0.0), (-0.05, 0.0, 0.0), [
        BoxSpec("torso", -4.5, -4.0, -5.5, 9.0, 8.0, 6.0, "fur"),
        BoxSpec("hind", -4.0, -3.5, 0.5, 8.0, 7.0, 5.0, "fur"),
    ])

    # Layered sloped mountain-rock shoulder & flank armor plates with golden ley veins
    armor = PartSpec("armor", (0.0, 13.0, 0.0), (0.0, 0.0, 0.0), [
        BoxSpec("plate", -5.0, -4.5, -5.0, 10.0, 3.0, 6.0, "stone", grow=0.06),
        BoxSpec("saddle", -3.5, -4.0, 0.5, 7.0, 2.0, 4.0, "stone"),
    ])
    armor.children.append(PartSpec("left_pauldron", (4.5, -2.5, -2.5), (0.08, 0.0, -0.34), [
        BoxSpec("l_slab", -1.0, 0.0, -3.0, 2.0, 5.0, 6.0, "stone"),
        BoxSpec("l_spike", 0.0, -2.0, -1.5, 1.0, 3.0, 3.0, "crystal"),
    ]))
    armor.children.append(PartSpec("right_pauldron", (-4.5, -2.5, -2.5), (0.08, 0.0, 0.34), [
        BoxSpec("r_slab", -1.0, 0.0, -3.0, 2.0, 5.0, 6.0, "stone"),
        BoxSpec("r_spike", -1.0, -2.0, -1.5, 1.0, 3.0, 3.0, "crystal"),
    ]))

    head = PartSpec("head", (0.0, 9.0, -6.0), (0.14, 0.0, 0.0), [
        BoxSpec("skull", -3.0, -3.0, -3.5, 6.0, 6.0, 6.0, "stone"),
        BoxSpec("brow_ridge", -3.5, -3.5, -3.5, 7.0, 2.0, 3.0, "horn"),
    ])
    head.children.append(PartSpec("muzzle", (0.0, 0.5, -3.5), (0.18, 0.0, 0.0), [
        BoxSpec("muzzle", -2.0, -1.5, -3.0, 4.0, 3.0, 3.0, "stone"),
        BoxSpec("nose", -1.0, -1.0, -3.5, 2.0, 1.0, 1.0, "metal"),
    ]))
    head.children.append(PartSpec("beard", (0.0, 2.5, -3.0), (0.38, 0.0, 0.0), [
        BoxSpec("beard", -1.0, 0.0, -1.0, 2.0, 4.0, 2.0, "fur"),
        BoxSpec("beard_tip", -0.5, 3.5, -0.5, 1.0, 2.0, 1.0, "fur"),
    ]))

    for ename, side in (("left_ear", 1.0), ("right_ear", -1.0)):
        head.children.append(PartSpec(ename, (3.0 * side, -1.0, -0.5), (0.0, -0.24 * side, 0.48 * side), [
            BoxSpec("ear_leaf", -0.5, -1.0, -1.0, 1.0, 3.0, 2.0, "fur"),
        ]))

    # True nested 4-segment spiral ram horns curling up, out, back, and forward
    for name, side in (("left_horn", 1.0), ("right_horn", -1.0)):
        hbase = PartSpec(f"{name}_base", (1.5 * side, -2.5, -0.5),
                         (-0.36, 0.14 * side, -0.32 * side), [
                             BoxSpec("base", -1.5, -3.5, -1.5, 3.0, 4.0, 3.0, "horn"),
                         ])
        hmid = PartSpec(f"{name}_mid", (0.0, -3.0, 0.0),
                        (-0.48, 0.18 * side, -0.24 * side), [
                            BoxSpec("mid", -1.0, -3.5, -1.0, 2.0, 4.0, 2.0, "horn"),
                        ])
        hcurl = PartSpec(f"{name}_curl", (0.0, -2.5, 0.0),
                         (-0.54, 0.14 * side, -0.16 * side), [
                             BoxSpec("curl", -1.0, -2.5, -1.0, 2.0, 3.0, 2.0, "horn"),
                         ])
        htip = PartSpec(f"{name}_tip", (0.0, -2.0, 0.0),
                        (-0.48, -0.12 * side, -0.12 * side), [
                            BoxSpec("tip", -0.5, -2.5, -0.5, 1.0, 3.0, 1.0, "crystal"),
                        ])
        hcurl.children.append(htip)
        hmid.children.append(hcurl)
        hbase.children.append(hmid)
        head.children.append(hbase)

    parts = [body, armor, head]
    for name, x, z, is_hind in (
        ("front_right_leg", -3.0, -3.5, False),
        ("front_left_leg", 3.0, -3.5, False),
        ("back_right_leg", -3.0, 3.5, True),
        ("back_left_leg", 3.0, 3.5, True),
    ):
        leg = PartSpec(name, (x, 15.5, z), (-0.12 if is_hind else -0.06, 0.0, 0.0), [
            BoxSpec("upper", -1.5, -0.5, -1.5, 3.0, 5.0, 3.0, "fur"),
        ])
        shank = PartSpec("shank", (0.0, 4.0, 0.0), (0.18 if is_hind else 0.10, 0.0, 0.0), [
            BoxSpec("lower", -1.0, -0.5, -1.0, 2.0, 3.0, 2.0, "stone"),
            BoxSpec("hoof_l", -1.5, 2.0, -2.0, 1.0, 2.0, 3.0, "horn"),
            BoxSpec("hoof_r", 0.5, 2.0, -2.0, 1.0, 2.0, 3.0, "horn"),
        ])
        leg.children.append(shank)
        parts.append(leg)

    tail = PartSpec("tail", (0.0, 11.5, 5.0), (0.58, 0.0, 0.0), [
        BoxSpec("tail", -1.0, -1.0, 0.0, 2.0, 2.0, 4.0, "fur"),
    ])
    parts.append(tail)

    return 128, parts, (112, 102, 92), (248, 174, 58)


def turtle():
    # Plastron underbelly
    body = PartSpec("body", (0.0, 19.0, 0.0), (0.0, 0.0, 0.0), [
        BoxSpec("belly", -4.5, 0.0, -5.0, 9.0, 2.0, 10.0, "stone"),
    ])

    # Multi-tiered octagonal dome carapace with sloped side/front/rear skirts
    shell = PartSpec("shell", (0.0, 16.5, 0.0), (0.0, 0.0, 0.0), [
        BoxSpec("dome", -5.5, -2.5, -6.0, 11.0, 3.0, 12.0, "shell"),
        BoxSpec("upper_dome", -4.0, -4.0, -4.5, 8.0, 2.0, 9.0, "shell"),
        BoxSpec("rim", -6.0, 0.0, -6.5, 12.0, 1.0, 13.0, "stone", grow=0.04),
    ])
    shell.children.append(PartSpec("skirt_left", (5.5, -1.0, 0.0), (0.0, 0.0, 0.38), [
        BoxSpec("s_left", 0.0, 0.0, -5.0, 2.0, 2.0, 10.0, "shell"),
    ]))
    shell.children.append(PartSpec("skirt_right", (-5.5, -1.0, 0.0), (0.0, 0.0, -0.38), [
        BoxSpec("s_right", -2.0, 0.0, -5.0, 2.0, 2.0, 10.0, "shell"),
    ]))

    # Radiating multi-spired spirit crystal cluster (NO vertical chimney!)
    crystals = PartSpec("crystals", (0.0, -3.5, 0.0), (0.0, 0.0, 0.0), [])
    # Primary central-back spire swept backward and tilted
    crystals.children.append(PartSpec("main_spire", (0.0, 1.0, -0.5), (-0.42, 0.0, 0.08), [
        BoxSpec("main", -1.5, -4.5, -1.5, 3.0, 5.0, 3.0, "crystal"),
        BoxSpec("main_tip", -1.0, -6.5, -1.0, 2.0, 2.0, 2.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("left_crystal", (2.0, 1.5, -1.0), (-0.24, 0.18, 0.48), [
        BoxSpec("shard", -1.0, -3.5, -1.0, 2.0, 4.0, 2.0, "crystal"),
        BoxSpec("tip", -0.5, -5.0, -0.5, 1.0, 2.0, 1.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("right_crystal", (-2.0, 1.5, -1.0), (-0.24, -0.18, -0.48), [
        BoxSpec("shard", -1.0, -3.5, -1.0, 2.0, 4.0, 2.0, "crystal"),
        BoxSpec("tip", -0.5, -5.0, -0.5, 1.0, 2.0, 1.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("front_left_crystal", (1.5, 1.5, -2.5), (0.36, -0.22, 0.38), [
        BoxSpec("shard", -1.0, -2.5, -1.0, 2.0, 3.0, 2.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("front_right_crystal", (-1.5, 1.5, -2.5), (0.36, 0.22, -0.38), [
        BoxSpec("shard", -1.0, -2.5, -1.0, 2.0, 3.0, 2.0, "crystal"),
    ]))
    crystals.children.append(PartSpec("rear_crystal", (0.0, 1.5, 2.0), (-0.58, 0.0, 0.0), [
        BoxSpec("shard", -1.0, -3.5, -1.0, 2.0, 4.0, 2.0, "crystal"),
    ]))
    shell.children.append(crystals)

    # Dragon-turtle head with horned brow, hooked beak, and whiskers
    head = PartSpec("head", (0.0, 15.5, -7.5), (-0.08, 0.0, 0.0), [
        BoxSpec("skull", -2.5, -2.5, -3.5, 5.0, 4.0, 5.0, "stone"),
        BoxSpec("beak", -1.5, -1.0, -5.0, 3.0, 2.0, 2.0, "metal"),
        BoxSpec("jaw", -1.5, 1.0, -4.0, 3.0, 1.0, 3.0, "stone"),
    ])
    head.children.append(PartSpec("left_horn", (1.5, -2.0, -1.0), (-0.52, 0.22, 0.34), [
        BoxSpec("horn_l", -0.5, -3.0, -0.5, 1.0, 3.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("right_horn", (-1.5, -2.0, -1.0), (-0.52, -0.22, -0.34), [
        BoxSpec("horn_r", -0.5, -3.0, -0.5, 1.0, 3.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("whisker_left", (2.0, 0.5, -3.5), (0.28, 0.32, -0.24), [
        BoxSpec("w_l", 0.0, 0.0, -0.5, 1.0, 3.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("whisker_right", (-2.0, 0.5, -3.5), (0.28, -0.32, 0.24), [
        BoxSpec("w_r", -1.0, 0.0, -0.5, 1.0, 3.0, 1.0, "crystal"),
    ]))
    head.children.append(PartSpec("neck", (0.0, 0.5, 1.0), (0.22, 0.0, 0.0), [
        BoxSpec("bridge", -2.0, -1.5, 0.0, 4.0, 3.0, 3.0, "stone"),
    ]))

    parts = [body, shell, head]
    # Splayed swimming flippers with angled paddles and claws
    for name, x, z, side, is_hind in (
        ("front_right", -5.0, -3.5, -1.0, False),
        ("front_left", 5.0, -3.5, 1.0, False),
        ("back_right", -4.5, 3.5, -1.0, True),
        ("back_left", 4.5, 3.5, 1.0, True),
    ):
        yaw = (-0.28 if not is_hind else -0.52) * side
        roll = -0.18 * side
        flip = PartSpec(name, (x, 19.5, z), (0.08, yaw, roll), [
            BoxSpec("flipper", -2.5 if side < 0 else -0.5, -1.0, -2.0, 3.0, 2.0, 4.0, "stone"),
            BoxSpec("blade", -4.0 if side < 0 else 1.5, -0.5, -1.5, 2.0, 1.0, 4.0, "shell"),
            BoxSpec("claws", -4.5 if side < 0 else 2.5, 0.0, -2.0, 2.0, 1.0, 1.0, "crystal"),
        ])
        parts.append(flip)

    tail = PartSpec("tail", (0.0, 18.5, 6.0), (-0.18, 0.0, 0.0), [
        BoxSpec("stub", -1.5, -1.0, 0.0, 3.0, 2.0, 4.0, "stone"),
    ])
    tail.children.append(PartSpec("tail_spike", (0.0, -1.0, 2.0), (-0.38, 0.0, 0.0), [
        BoxSpec("spike", -0.5, -2.0, 0.0, 1.0, 2.0, 3.0, "crystal"),
    ]))
    parts.append(tail)

    return 128, parts, (36, 84, 74), (76, 242, 232)


def crane():
    # Sleek tapered avian body tilting slightly upward at the chest
    body = PartSpec("body", (0.0, 11.0, 0.0), (-0.18, 0.0, 0.0), [
        BoxSpec("torso", -3.0, -2.5, -4.0, 6.0, 5.0, 8.0, "feather"),
        BoxSpec("breast", -2.5, -1.5, -5.0, 5.0, 4.0, 3.0, "feather", grow=0.04),
        BoxSpec("mantle", -2.5, -3.0, -2.5, 5.0, 1.0, 6.0, "feather_dark"),
    ])

    # Graceful 3-segment S-curve neck + head
    neck = PartSpec("neck", (0.0, 9.5, -4.0), (0.32, 0.0, 0.0), [
        BoxSpec("lower", -1.5, -3.5, -1.5, 3.0, 4.0, 3.0, "feather"),
    ])
    mid_neck = PartSpec("mid_neck", (0.0, -3.0, 0.0), (-0.48, 0.0, 0.0), [
        BoxSpec("mid", -1.0, -3.5, -1.0, 2.0, 4.0, 2.0, "feather_dark"),
    ])
    upper = PartSpec("upper", (0.0, -3.0, 0.0), (0.32, 0.0, 0.0), [
        BoxSpec("upper", -1.0, -2.5, -1.0, 2.0, 3.0, 2.0, "feather"),
    ])
    head = PartSpec("head", (0.0, -2.0, 0.0), (-0.12, 0.0, 0.0), [
        BoxSpec("skull", -1.5, -2.0, -2.0, 3.0, 3.0, 4.0, "feather"),
        BoxSpec("crown", -1.0, -2.5, -1.5, 2.0, 1.0, 3.0, "crest_red"),
        BoxSpec("beak_base", -1.0, -1.0, -4.0, 2.0, 1.0, 2.0, "metal"),
        BoxSpec("beak_tip", -0.5, -0.5, -6.5, 1.0, 1.0, 3.0, "metal"),
    ])
    head.children.append(PartSpec("plume", (0.0, -2.0, 1.0), (-0.42, 0.0, 0.0), [
        BoxSpec("crest_plume", -0.5, -1.0, 0.0, 1.0, 1.0, 4.0, "crest_red"),
    ]))
    upper.children.append(head)
    mid_neck.children.append(upper)
    neck.children.append(mid_neck)

    # Layered folded wings with covert feathers & long obsidian/ember primaries
    wings = []
    for name, side in (("left_wing", 1.0), ("right_wing", -1.0)):
        wing = PartSpec(name, (3.0 * side, 9.5, -2.5), (0.14, 0.08 * side, -0.18 * side), [
            BoxSpec("covert", -0.5 if side > 0 else -1.5, -1.0, 0.0, 2.0, 3.0, 6.0, "feather"),
        ])
        for i in range(4):
            style = "feather" if i < 2 else "feather_dark"
            wing.children.append(PartSpec(
                f"feather_{i}", (0.5 * side, 0.5 + i * 0.5, 1.0 + i * 1.5),
                (-0.08 + i * 0.06, 0.04 * side, -0.06 * side), [
                    BoxSpec("vane", -0.5, -1.0, 0.0, 1.0, 2.0, 7.0, style),
                ]))
        wings.append(wing)

    # Long slender 3-joint crane legs with avian knee/tarsus bend & 3-toed talons
    legs = []
    for name, side in (("left_leg", 1.0), ("right_leg", -1.0)):
        leg = PartSpec(name, (1.5 * side, 13.0, 0.5), (-0.12, 0.0, 0.0), [
            BoxSpec("thigh", -0.5, -0.5, -0.5, 1.0, 6.0, 1.0, "metal"),
        ])
        shin = PartSpec("shin", (0.0, 5.0, 0.0), (0.18, 0.0, 0.0), [
            BoxSpec("tarsus", -0.5, -0.5, -0.5, 1.0, 6.0, 1.0, "metal"),
        ])
        foot = PartSpec("foot", (0.0, 5.5, 0.0), (-0.06, 0.0, 0.0), [
            BoxSpec("toe_mid", -0.5, 0.0, -3.0, 1.0, 1.0, 4.0, "metal"),
        ])
        foot.children.append(PartSpec("toe_in", (0.0, 0.0, 0.0), (0.0, -0.36 * side, 0.0), [
            BoxSpec("t_in", -0.5, 0.0, -2.5, 1.0, 1.0, 3.0, "metal"),
        ]))
        foot.children.append(PartSpec("toe_out", (0.0, 0.0, 0.0), (0.0, 0.36 * side, 0.0), [
            BoxSpec("t_out", -0.5, 0.0, -2.5, 1.0, 1.0, 3.0, "metal"),
        ]))
        shin.children.append(foot)
        leg.children.append(shin)
        legs.append(leg)

    # Fan-splayed tail plumes with upturned ember tips
    tail = PartSpec("tail", (0.0, 10.5, 4.0), (0.28, 0.0, 0.0), [
        BoxSpec("base", -1.5, -1.0, 0.0, 3.0, 2.0, 6.0, "feather_dark"),
    ])
    tail.children.append(PartSpec("center_tail_feather", (0.0, -0.5, 3.0), (0.18, 0.0, 0.0), [
        BoxSpec("vane", -1.0, -0.5, 0.0, 2.0, 1.0, 7.0, "crest_red"),
    ]))
    tail.children.append(PartSpec("left_tail_feather", (1.0, 0.0, 1.0), (0.12, 0.32, 0.14), [
        BoxSpec("vane", -1.0, -0.5, 0.0, 2.0, 1.0, 7.0, "feather"),
    ]))
    tail.children.append(PartSpec("right_tail_feather", (-1.0, 0.0, 1.0), (0.12, -0.32, -0.14), [
        BoxSpec("vane", -1.0, -0.5, 0.0, 2.0, 1.0, 7.0, "feather"),
    ]))

    return 128, [body, neck, *wings, *legs, tail], (238, 228, 206), (232, 96, 44)
