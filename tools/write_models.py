"""Write the five beast models end to end from the shared spec.

The model files are fully generated: fields, constructor, createBodyLayer and setupAnim all
come from the same part/box spec that paints the texture, so geometry, UVs and animation
can never drift apart again.
"""

from __future__ import annotations

import io
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import rebuild_beast_models as spec  # noqa: E402
from gen_model_layers import emit_layer  # noqa: E402
from uv_layout import PartSpec, build  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "neoforge-26.2" / "src" / "main" / "java" / "com" / "xxsx" / "xuanhuanearth" / "client" / "model"

HEADER = """package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.{state};
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public final class {model} extends EntityModel<{state}> {{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("{layer}"), "main");
"""


def part_paths(parts: list[PartSpec], prefix: str = "") -> dict[str, str]:
    """Map "a.b.c" part path -> Java expression that resolves that part."""
    out = {}

    def walk(part: PartSpec, expr: str, path: str):
        out[path] = expr
        for child in part.children:
            walk(child, f'{expr}.getChild("{child.name}")', f"{path}.{child.name}")

    for part in parts:
        walk(part, f'root.getChild("{part.name}")', part.name)
    return out


def resolve(paths: dict[str, str], path: str) -> str:
    return paths[path]


def fox_body(paths: dict[str, str]) -> tuple[str, str]:
    fields = """
    private final ModelPart head;
    private final ModelPart crest;
    private final ModelPart muzzle;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart tail;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart[] ears;
"""
    ctor = """
    public SpiritFoxModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.crest = this.head.getChild("crest");
        this.muzzle = this.head.getChild("muzzle");
        this.jaw = this.head.getChild("jaw");
        this.body = root.getChild("body");
        this.frontRightLeg = root.getChild("front_right");
        this.frontLeftLeg = root.getChild("front_left");
        this.backRightLeg = root.getChild("back_right");
        this.backLeftLeg = root.getChild("back_left");
        this.tail = root.getChild("tail");
        this.tailMid = this.tail.getChild("tail_mid");
        this.tailTip = this.tailMid.getChild("tail_tip");
        this.ears = new ModelPart[]{this.head.getChild("left_ear"), this.head.getChild("right_ear")};
    }
"""
    anim = """
    @Override
    public void setupAnim(SpiritFoxRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float age = state.ageInTicks;
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.zRot = hurt * 0.2F;
        this.body.xRot = Mth.sin(age * 0.055F) * 0.022F + state.breathPhase * 0.012F - leap * 0.14F;

        float stride = Mth.lerp(run, 1.0F, 1.4F);
        this.frontRightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.15F * speed * stride;
        this.frontLeftLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.15F * speed * stride;
        this.backRightLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.05F * speed * stride;
        this.backLeftLeg.xRot = Mth.cos(walk * 0.6662F) * 1.05F * speed * stride;

        if (leap > 0.05F) {
            this.frontRightLeg.xRot -= leap * 0.5F;
            this.frontLeftLeg.xRot -= leap * 0.5F;
            this.backRightLeg.xRot += leap * 0.4F;
            this.backLeftLeg.xRot += leap * 0.4F;
        }

        this.tail.yRot = Mth.sin(age * 0.085F) * (0.24F + state.spiritAffinity * 0.12F) + state.tailWag * 0.10F;
        this.tail.xRot = 0.42F + Mth.cos(age * 0.07F) * 0.08F - run * 0.16F;
        this.tailMid.yRot = Mth.sin(age * 0.11F + 0.5F) * (0.18F + state.spiritAffinity * 0.1F);
        this.tailMid.xRot = 0.06F + Mth.sin(age * 0.09F + 0.4F) * 0.05F;
        this.tailTip.yRot = Mth.sin(age * 0.14F + 1.0F) * (0.22F + state.spiritAffinity * 0.1F);
        this.tailTip.xRot = 0.08F + Mth.sin(age * 0.1F + 0.9F) * 0.06F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.xRot -= attack * 0.55F;
        this.body.xRot -= attack * 0.12F;
        this.frontRightLeg.xRot -= attack * 0.42F;
        this.frontLeftLeg.xRot -= attack * 0.42F;
        this.jaw.xRot = attack * 0.55F + (state.aggressive ? 0.12F : 0.0F);
        this.muzzle.xRot = attack * 0.10F;

        if (state.sitting) {
            this.body.xRot = 0.18F;
            this.frontRightLeg.xRot = -0.10F;
            this.frontLeftLeg.xRot = -0.10F;
            this.backRightLeg.xRot = -1.08F;
            this.backLeftLeg.xRot = -1.08F;
            this.tail.xRot = 1.02F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(age * 0.5F);
            this.frontRightLeg.xRot = paddle * 0.5F;
            this.frontLeftLeg.xRot = -paddle * 0.5F;
            this.backRightLeg.xRot = -paddle * 0.45F;
            this.backLeftLeg.xRot = paddle * 0.45F;
            this.body.xRot = 0.05F;
        }

        float earTilt = hurt * 0.6F + (state.aggressive ? 0.2F : 0.0F);
        this.ears[0].xRot = -0.08F - earTilt * 0.55F + Mth.sin(age * 0.13F) * 0.02F;
        this.ears[1].xRot = this.ears[0].xRot;
        this.ears[0].zRot = 0.12F + earTilt * 0.4F;
        this.ears[1].zRot = -0.12F - earTilt * 0.4F;

        float pulse = 1.0F + Mth.sin(age * 0.18F) * (0.05F + state.spiritAffinity * 0.05F);
        this.crest.xScale = pulse;
        this.crest.yScale = pulse;
        this.crest.zScale = pulse;
    }
}
"""
    return fields, ctor + anim


def wolf_body(paths: dict[str, str]) -> tuple[str, str]:
    fields = """
    private final ModelPart head;
    private final ModelPart snout;
    private final ModelPart body;
    private final ModelPart mane;
    private final ModelPart maneTop;
    private final ModelPart maneLeft;
    private final ModelPart maneRight;
    private final ModelPart pendant;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart tail;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart[] ears;
"""
    ctor = """
    public WindWolfModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.snout = this.head.getChild("snout");
        this.body = root.getChild("body");
        this.mane = root.getChild("mane");
        this.maneTop = this.mane.getChild("mane_top");
        this.maneLeft = this.mane.getChild("mane_left");
        this.maneRight = this.mane.getChild("mane_right");
        this.pendant = root.getChild("collar").getChild("pendant");
        this.frontRightLeg = root.getChild("front_right");
        this.frontLeftLeg = root.getChild("front_left");
        this.backRightLeg = root.getChild("back_right");
        this.backLeftLeg = root.getChild("back_left");
        this.tail = root.getChild("tail");
        this.tailMid = this.tail.getChild("tail_mid");
        this.tailTip = this.tailMid.getChild("tail_tip");
        this.ears = new ModelPart[]{this.head.getChild("left_ear"), this.head.getChild("right_ear")};
    }
"""
    anim = """
    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float age = state.ageInTicks;
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.08F : 0.0F);
        this.head.zRot = hurt * 0.18F;

        float stride = Mth.lerp(run, 1.0F, 1.45F);
        this.frontRightLeg.xRot = Mth.cos(walk * 0.72F) * 1.2F * speed * stride;
        this.frontLeftLeg.xRot = Mth.cos(walk * 0.72F + Mth.PI) * 1.2F * speed * stride;
        this.backRightLeg.xRot = this.frontLeftLeg.xRot;
        this.backLeftLeg.xRot = this.frontRightLeg.xRot;

        if (leap > 0.05F) {
            this.frontRightLeg.xRot -= leap * 0.55F;
            this.frontLeftLeg.xRot -= leap * 0.55F;
            this.backRightLeg.xRot += leap * 0.45F;
            this.backLeftLeg.xRot += leap * 0.45F;
        }

        float affinityMotion = 0.18F + state.affinity * 0.18F;
        this.maneTop.yRot = Mth.sin(age * 0.07F) * affinityMotion + run * 0.10F;
        this.maneLeft.yRot = 0.18F + Mth.sin(age * 0.08F + 0.8F) * affinityMotion * 0.7F + run * 0.14F;
        this.maneRight.yRot = -0.18F - Mth.sin(age * 0.08F + 0.8F) * affinityMotion * 0.7F - run * 0.14F;
        this.tail.yRot = Mth.sin(age * 0.09F) * (0.24F + affinityMotion) + state.tailWag * 0.12F;
        this.tailMid.yRot = Mth.sin(age * 0.12F + 0.6F) * (0.3F + affinityMotion);
        this.tailTip.yRot = Mth.sin(age * 0.15F + 1.1F) * (0.36F + affinityMotion);
        this.tail.xRot = 0.45F + Mth.cos(age * 0.06F) * 0.05F - run * 0.18F;
        this.tailMid.xRot = 0.08F + Mth.sin(age * 0.07F + 0.4F) * 0.04F;
        this.tailTip.xRot = 0.12F + Mth.sin(age * 0.08F + 0.9F) * 0.05F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.body.xRot = (state.aggressive ? -0.06F : 0.0F) + state.breathPhase * 0.012F - leap * 0.12F;
        this.head.xRot -= attack * 0.62F;
        this.frontRightLeg.xRot -= attack * 0.38F;
        this.frontLeftLeg.xRot -= attack * 0.38F;
        this.snout.xRot = attack * 0.18F;

        if (state.sitting) {
            this.body.xRot = 0.16F;
            this.frontRightLeg.xRot = -0.12F;
            this.frontLeftLeg.xRot = -0.12F;
            this.backRightLeg.xRot = -1.05F;
            this.backLeftLeg.xRot = -1.05F;
            this.tail.xRot = 0.95F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(age * 0.45F);
            this.frontRightLeg.xRot = paddle * 0.55F;
            this.frontLeftLeg.xRot = -paddle * 0.55F;
            this.backRightLeg.xRot = -paddle * 0.5F;
            this.backLeftLeg.xRot = paddle * 0.5F;
            this.body.xRot = 0.06F;
        }

        float earTilt = hurt * 0.55F + (state.aggressive ? 0.18F : 0.0F);
        this.ears[0].xRot = -0.1F - earTilt * 0.6F + Mth.sin(age * 0.11F) * 0.02F;
        this.ears[1].xRot = this.ears[0].xRot;
        this.ears[0].zRot = 0.16F + earTilt * 0.35F;
        this.ears[1].zRot = -0.16F - earTilt * 0.35F;

        float pulse = 1.0F + Mth.sin(age * 0.2F) * (0.04F + state.affinity * 0.04F);
        this.pendant.xScale = pulse;
        this.pendant.yScale = pulse;
        this.pendant.zScale = pulse;
    }
}
"""
    return fields, ctor + anim


def goat_body(paths: dict[str, str]) -> tuple[str, str]:
    fields = """
    private final ModelPart head;
    private final ModelPart muzzle;
    private final ModelPart beard;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart body;
    private final ModelPart armor;
    private final ModelPart tail;
    private final ModelPart[] legs;
"""
    ctor = """
    public StonehornGoatModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.muzzle = this.head.getChild("muzzle");
        this.beard = this.head.getChild("beard");
        this.leftHorn = this.head.getChild("left_horn_base");
        this.rightHorn = this.head.getChild("right_horn_base");
        this.body = root.getChild("body");
        this.armor = root.getChild("armor");
        this.tail = root.getChild("tail");
        this.legs = new ModelPart[]{root.getChild("front_right_leg"), root.getChild("front_left_leg"),
                root.getChild("back_right_leg"), root.getChild("back_left_leg")};
    }
"""
    anim = """
    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.22F : 0.0F);
        this.head.zRot = hurt * 0.16F;

        float stride = Mth.lerp(run, 1.0F, 1.35F);
        this.legs[0].xRot = Mth.cos(walk * 0.58F) * 0.85F * speed * stride;
        this.legs[1].xRot = Mth.cos(walk * 0.58F + Mth.PI) * 0.85F * speed * stride;
        this.legs[2].xRot = this.legs[1].xRot;
        this.legs[3].xRot = this.legs[0].xRot;

        if (leap > 0.05F) {
            this.legs[0].xRot -= leap * 0.4F;
            this.legs[1].xRot -= leap * 0.4F;
            this.legs[2].xRot += leap * 0.35F;
            this.legs[3].xRot += leap * 0.35F;
        }

        this.body.zRot = Mth.sin(state.ageInTicks * 0.035F) * 0.018F + state.breathPhase * 0.01F;
        this.body.xRot = state.breathPhase * 0.012F - leap * 0.1F;
        this.armor.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.12F) * 0.015F * state.affinity;
        this.tail.yRot = Mth.sin(state.ageInTicks * 0.12F) * 0.22F + state.tailWag * 0.1F;
        // Horn base only: the mid and tip segments are siblings in the generated tree, so
        // rotating the base alone keeps the curl rigid instead of compounding angles.
        this.leftHorn.zRot = -0.34F + Mth.sin(state.ageInTicks * 0.06F) * 0.015F;
        this.rightHorn.zRot = -this.leftHorn.zRot;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.xRot -= attack * 0.9F;
        this.body.xRot += attack * 0.08F;
        this.beard.xRot = attack * 0.3F + Mth.sin(state.ageInTicks * 0.09F) * 0.03F;

        if (state.sitting) {
            this.body.xRot = 0.12F;
            for (ModelPart leg : this.legs) {
                leg.xRot = -1.15F;
            }
            this.head.xRot += 0.18F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(state.ageInTicks * 0.4F);
            for (int i = 0; i < this.legs.length; i++) {
                this.legs[i].xRot = (i % 2 == 0 ? paddle : -paddle) * 0.42F;
            }
            this.body.xRot = 0.06F;
        }
    }
}
"""
    return fields, ctor + anim


def turtle_body(paths: dict[str, str]) -> tuple[str, str]:
    fields = """
    private final ModelPart head;
    private final ModelPart shell;
    private final ModelPart crystals;
    private final ModelPart tail;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart backRight;
    private final ModelPart backLeft;
"""
    ctor = """
    public CrystalTurtleModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.shell = root.getChild("shell");
        this.crystals = this.shell.getChild("crystals");
        this.tail = root.getChild("tail");
        this.frontRight = root.getChild("front_right");
        this.frontLeft = root.getChild("front_left");
        this.backRight = root.getChild("back_right");
        this.backLeft = root.getChild("back_left");
    }
"""
    anim = """
    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
        this.head.zRot = hurt * 0.12F;
        this.frontRight.yRot = Mth.cos(walk * 0.58F) * 0.55F * speed;
        this.frontLeft.yRot = Mth.cos(walk * 0.58F + Mth.PI) * 0.55F * speed;
        this.backRight.yRot = this.frontLeft.yRot;
        this.backLeft.yRot = this.frontRight.yRot;

        float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.16F) * (0.025F + state.affinity * 0.045F);
        this.crystals.xScale = pulse;
        this.crystals.yScale = pulse;
        this.crystals.zScale = pulse;
        this.crystals.xRot = Mth.sin(state.ageInTicks * 0.07F) * 0.03F;
        this.shell.yRot = Mth.sin(state.ageInTicks * 0.025F) * 0.012F;
        this.shell.xRot = state.breathPhase * 0.008F;
        this.tail.yRot = state.tailWag * 0.18F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.z -= attack * 1.8F;
        this.head.xRot -= attack * 0.32F;

        if (state.sitting) {
            this.head.z += 2.2F;
            this.head.xRot = 0.42F;
            this.frontRight.yRot = 0.72F;
            this.frontLeft.yRot = -0.72F;
            this.backRight.yRot = -0.62F;
            this.backLeft.yRot = 0.62F;
            this.crystals.yScale *= 0.96F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(state.ageInTicks * 0.3F);
            this.frontRight.yRot = paddle * 0.7F;
            this.frontLeft.yRot = -paddle * 0.7F;
            this.backRight.yRot = -paddle * 0.6F;
            this.backLeft.yRot = paddle * 0.6F;
            this.head.xRot = 0.1F + state.xRot * Mth.DEG_TO_RAD * 0.3F;
        }
    }
}
"""
    return fields, ctor + anim


def crane_body(paths: dict[str, str]) -> tuple[str, str]:
    fields = """
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;
"""
    ctor = """
    public EmberCraneModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.body = root.getChild("body");
        this.neck = root.getChild("neck");
        this.head = this.neck.getChild("upper").getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tail = root.getChild("tail");
    }
"""
    anim = """
    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;

        this.neck.yRot = state.yRot * Mth.DEG_TO_RAD * 0.75F;
        this.neck.xRot = state.xRot * Mth.DEG_TO_RAD * 0.45F - 0.25F;
        this.head.xRot = Mth.sin(state.ageInTicks * 0.06F) * 0.025F;
        this.head.zRot = hurt * 0.14F;
        this.leftLeg.xRot = Mth.cos(walk * 0.68F) * 0.82F * speed;
        this.rightLeg.xRot = Mth.cos(walk * 0.68F + Mth.PI) * 0.82F * speed;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        boolean airborne = !state.onGround && state.leapProgress != 0.0F;
        float beatSpeed = state.aggressive || airborne ? 0.32F : 0.09F;
        float wingBeat = Mth.sin(state.ageInTicks * beatSpeed);
        float spread = state.aggressive ? 0.5F : 0.06F;
        if (airborne) {
            spread = 0.85F;
        }
        this.leftWing.zRot = -0.12F - wingBeat * spread - attack * 0.72F;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.leftWing.yRot = Mth.sin(state.ageInTicks * 0.05F) * 0.04F;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.neck.xRot -= attack * 0.48F;
        this.tail.yRot = Mth.sin(state.ageInTicks * 0.07F) * 0.08F + state.tailWag * 0.04F;
        this.tail.xRot = 0.42F + Mth.sin(state.ageInTicks * 0.05F) * 0.05F;
        this.body.y = 11.0F + Mth.sin(state.ageInTicks * 0.08F) * 0.06F + state.breathPhase * 0.05F;

        if (state.sitting) {
            this.leftLeg.xRot = -1.25F;
            this.rightLeg.xRot = -1.25F;
            this.leftWing.zRot = -0.08F;
            this.rightWing.zRot = 0.08F;
            this.body.y += 1.2F;
        }

        if (state.isInWater) {
            this.leftLeg.xRot = -0.6F;
            this.rightLeg.xRot = -0.6F;
            this.leftWing.zRot = -0.5F - wingBeat * 0.25F;
            this.rightWing.zRot = 0.5F + wingBeat * 0.25F;
            this.body.y = 12.0F;
        }
    }
}
"""
    return fields, ctor + anim


TARGETS = [
    ("SpiritFoxModel", "spirit_fox", "SpiritFoxRenderState", spec.fox, fox_body),
    ("WindWolfModel", "wind_wolf", "SpiritBeastRenderState", spec.wolf, wolf_body),
    ("StonehornGoatModel", "stonehorn_goat", "SpiritBeastRenderState", spec.goat, goat_body),
    ("CrystalTurtleModel", "crystal_turtle", "SpiritBeastRenderState", spec.turtle, turtle_body),
    ("EmberCraneModel", "ember_crane", "SpiritBeastRenderState", spec.crane, crane_body),
]


def main() -> None:
    for model, layer, state, factory, body_fn in TARGETS:
        size, parts, base, accent = factory()
        build(layer, size, parts, base, accent)
        paths = part_paths(parts)
        fields, rest = body_fn(paths)
        anim_body = rest.split("    @Override", 1)[1]
        # The anim template already closes the method and the class; drop the trailing "}".
        anim_body = anim_body.rstrip()
        if anim_body.endswith("}"):
            anim_body = anim_body[:-1].rstrip("\n")
        source = (HEADER.format(model=model, layer=layer, state=state)
                  + fields
                  + rest.split("    @Override")[0]
                  + emit_layer(size, parts)
                  + "\n\n    @Override"
                  + anim_body
                  + "\n}\n")
        path = MODELS / f"{model}.java"
        io.open(path, "w", encoding="utf-8", newline="\n").write(source)
        print("wrote", path.name, len(source))


if __name__ == "__main__":
    main()
