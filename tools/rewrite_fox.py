"""Rewrite the constructor + setupAnim of each beast model against the new part names."""

from __future__ import annotations

import io
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "neoforge-26.1.2" / "src" / "main" / "java" / "com" / "xxsx" / "earth" / "xuanxue" / "client" / "model"


def block_bounds(source: str, signature: str) -> tuple[int, int]:
    start = source.index(signature)
    depth = 0
    for i in range(start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                return start, i + 1
    raise ValueError("unbalanced")


FOX = '''
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
'''

FOX_ANIM = '''
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

        float wag = state.tailWag;
        this.tail.yRot = Mth.sin(age * 0.085F) * (0.24F + state.spiritAffinity * 0.12F) + wag * 0.10F;
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
        this.muzzle.xRot = attack * 0.1F;

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
'''


def apply(name: str, fields: str, anim: str) -> None:
    path = MODELS / f"{name}.java"
    source = io.open(path, encoding="utf-8").read()
    # Replace the field block + constructor, which always sits between the class opening
    # and createBodyLayer.
    layer_start = source.index("    public static LayerDefinition createBodyLayer()")
    class_start = source.index("    private final ModelPart")
    source = source[:class_start] + fields.strip("\n") + "\n\n" + source[layer_start:]
    # Replace setupAnim through the end of the class.
    anim_start, _ = block_bounds(source, "    public void setupAnim(")
    tail = source.rindex("}")
    source = source[:anim_start] + anim.strip("\n") + "\n" + source[tail:]
    io.open(path, "w", encoding="utf-8", newline="\n").write(source)
    print("rewrote", name)


if __name__ == "__main__":
    apply("SpiritFoxModel", FOX, FOX_ANIM)
