package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.SpiritFoxRenderState;
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

public final class SpiritFoxModel extends EntityModel<SpiritFoxRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("spirit_fox"), "main");

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
    private final ModelPart leftPlume;
    private final ModelPart rightPlume;
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
        this.leftPlume = this.tail.getChild("left_plume");
        this.rightPlume = this.tail.getChild("right_plume");
        this.ears = new ModelPart[]{this.head.getChild("left_ear"), this.head.getChild("right_ear")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.0F, -4.5F, 6.0F, 6.0F, 5.0F)
                        .texOffs(23, 0).addBox(-2.5F, -2.5F, 0.5F, 5.0F, 5.0F, 5.0F)
                        .texOffs(44, 0).addBox(-3.5F, -2.0F, -5.0F, 7.0F, 5.0F, 3.0F, new CubeDeformation(0.04F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, -0.04F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "spine_crest",
                CubeListBuilder.create()
                        .texOffs(65, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 1.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -3.5F, 0.14F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(88, 0).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(105, 0).addBox(1.5F, -1.5F, -1.5F, 2.0F, 4.0F, 3.0F)
                        .texOffs(116, 0).addBox(-3.5F, -1.5F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, -4.0F, 0.42F, 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 5.0F)
                        .texOffs(23, 12).addBox(2.5F, -0.5F, -2.0F, 1.0F, 3.0F, 3.0F)
                        .texOffs(32, 12).addBox(-3.5F, -0.5F, -2.0F, 1.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.06F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create()
                        .texOffs(41, 12).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 2.0F)
                        .texOffs(54, 12).addBox(-1.0F, -0.5F, -4.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(63, 12).addBox(-0.5F, -1.0F, -4.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, -3.0F, 0.08F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(68, 12).addBox(-1.5F, 0.0F, -2.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -2.5F, 0.06F, 0.0F, 0.0F));
        PartDefinition head_left_ear = head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(81, 12).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F)
                        .texOffs(88, 12).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, -2.0F, -0.5F, -0.22F, -0.18F, 0.42F));
        head_left_ear.addOrReplaceChild(
                "ear_tip",
                CubeListBuilder.create()
                        .texOffs(93, 12).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.14F, 0.0F, 0.12F));
        PartDefinition head_right_ear = head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(98, 12).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F)
                        .texOffs(105, 12).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, -0.5F, -0.22F, 0.18F, -0.42F));
        head_right_ear.addOrReplaceChild(
                "ear_tip",
                CubeListBuilder.create()
                        .texOffs(110, 12).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.14F, 0.0F, -0.12F));
        head.addOrReplaceChild(
                "crest",
                CubeListBuilder.create()
                        .texOffs(115, 12).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, -1.5F, -0.36F, 0.0F, 0.0F));
        PartDefinition front_right = root.addOrReplaceChild(
                "front_right",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, 15.5F, -3.5F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_right_shin = front_right.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(11, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.16F, 0.0F, 0.0F));
        front_right_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(20, 23).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_left = root.addOrReplaceChild(
                "front_left",
                CubeListBuilder.create()
                        .texOffs(31, 23).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(2.0F, 15.5F, -3.5F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_left_shin = front_left.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(42, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.16F, 0.0F, 0.0F));
        front_left_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(51, 23).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.08F, 0.0F, 0.0F));
        PartDefinition back_right = root.addOrReplaceChild(
                "back_right",
                CubeListBuilder.create()
                        .texOffs(62, 23).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, 15.5F, 3.5F, -0.18F, 0.0F, 0.0F));
        PartDefinition back_right_shin = back_right.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(73, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.34F, 0.0F, 0.0F));
        back_right_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(82, 23).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.16F, 0.0F, 0.0F));
        PartDefinition back_left = root.addOrReplaceChild(
                "back_left",
                CubeListBuilder.create()
                        .texOffs(93, 23).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(2.0F, 15.5F, 3.5F, -0.18F, 0.0F, 0.0F));
        PartDefinition back_left_shin = back_left.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(104, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.34F, 0.0F, 0.0F));
        back_left_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(113, 23).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.16F, 0.0F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 5.0F, 0.38F, 0.0F, 0.0F));
        PartDefinition tail_tail_mid = tail.addOrReplaceChild(
                "tail_mid",
                CubeListBuilder.create()
                        .texOffs(19, 31).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.24F, 0.0F, 0.0F));
        tail_tail_mid.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create()
                        .texOffs(40, 31).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F)
                        .texOffs(59, 31).addBox(-1.0F, -1.0F, 4.5F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.22F, 0.0F, 0.0F));
        PartDefinition tail_left_plume = tail.addOrReplaceChild(
                "left_plume",
                CubeListBuilder.create()
                        .texOffs(70, 31).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(1.5F, 0.0F, 1.0F, 0.18F, 0.44F, 0.16F));
        PartDefinition tail_left_plume_plume_mid = tail_left_plume.addOrReplaceChild(
                "plume_mid",
                CubeListBuilder.create()
                        .texOffs(87, 31).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.22F, 0.18F, 0.0F));
        tail_left_plume_plume_mid.addOrReplaceChild(
                "plume_tip",
                CubeListBuilder.create()
                        .texOffs(106, 31).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.2F, 0.12F, 0.0F));
        PartDefinition tail_right_plume = tail.addOrReplaceChild(
                "right_plume",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-1.5F, 0.0F, 1.0F, 0.18F, -0.44F, -0.16F));
        PartDefinition tail_right_plume_plume_mid = tail_right_plume.addOrReplaceChild(
                "plume_mid",
                CubeListBuilder.create()
                        .texOffs(17, 42).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.22F, -0.18F, 0.0F));
        tail_right_plume_plume_mid.addOrReplaceChild(
                "plume_tip",
                CubeListBuilder.create()
                        .texOffs(36, 42).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.2F, -0.12F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SpiritFoxRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float age = state.ageInTicks;
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD;
        this.head.zRot += hurt * 0.2F;
        this.body.xRot += Mth.sin(age * 0.055F) * 0.022F + state.breathPhase * 0.012F - leap * 0.14F;

        float stride = Mth.lerp(run, 1.0F, 1.4F);
        this.frontRightLeg.xRot += Mth.cos(walk * 0.6662F) * 1.15F * speed * stride;
        this.frontLeftLeg.xRot += Mth.cos(walk * 0.6662F + Mth.PI) * 1.15F * speed * stride;
        this.backRightLeg.xRot += Mth.cos(walk * 0.6662F + Mth.PI) * 1.05F * speed * stride;
        this.backLeftLeg.xRot += Mth.cos(walk * 0.6662F) * 1.05F * speed * stride;

        if (leap > 0.05F) {
            this.frontRightLeg.xRot -= leap * 0.5F;
            this.frontLeftLeg.xRot -= leap * 0.5F;
            this.backRightLeg.xRot += leap * 0.4F;
            this.backLeftLeg.xRot += leap * 0.4F;
        }

        float sway = Mth.sin(age * 0.085F) * (0.24F + state.spiritAffinity * 0.12F) + state.tailWag * 0.10F;
        this.tail.yRot += sway;
        this.tail.xRot += Mth.cos(age * 0.07F) * 0.08F - run * 0.16F;
        this.tailMid.yRot += Mth.sin(age * 0.11F + 0.5F) * (0.18F + state.spiritAffinity * 0.1F);
        this.tailMid.xRot += Mth.sin(age * 0.09F + 0.4F) * 0.05F;
        this.tailTip.yRot += Mth.sin(age * 0.14F + 1.0F) * (0.22F + state.spiritAffinity * 0.1F);
        this.tailTip.xRot += Mth.sin(age * 0.1F + 0.9F) * 0.06F;
        this.leftPlume.yRot += sway * 0.85F;
        this.rightPlume.yRot += sway * 0.85F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.xRot -= attack * 0.55F;
        this.body.xRot -= attack * 0.12F;
        this.frontRightLeg.xRot -= attack * 0.42F;
        this.frontLeftLeg.xRot -= attack * 0.42F;
        this.jaw.xRot += attack * 0.55F + (state.aggressive ? 0.12F : 0.0F);
        this.muzzle.xRot += attack * 0.10F;

        if (state.sitting) {
            this.body.xRot += 0.22F;
            this.frontRightLeg.xRot -= 0.20F;
            this.frontLeftLeg.xRot -= 0.20F;
            this.backRightLeg.xRot -= 1.15F;
            this.backLeftLeg.xRot -= 1.15F;
            this.tail.xRot += 0.55F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(age * 0.5F);
            this.frontRightLeg.xRot += paddle * 0.5F;
            this.frontLeftLeg.xRot -= paddle * 0.5F;
            this.backRightLeg.xRot -= paddle * 0.45F;
            this.backLeftLeg.xRot += paddle * 0.45F;
        }

        float earTilt = hurt * 0.6F + (state.aggressive ? 0.2F : 0.0F);
        this.ears[0].xRot += -earTilt * 0.45F + Mth.sin(age * 0.13F) * 0.03F;
        this.ears[1].xRot += -earTilt * 0.45F + Mth.sin(age * 0.13F + 0.4F) * 0.03F;
        this.ears[0].zRot += earTilt * 0.35F;
        this.ears[1].zRot -= earTilt * 0.35F;

        float pulse = 1.0F + Mth.sin(age * 0.18F) * (0.05F + state.spiritAffinity * 0.05F);
        this.crest.xScale = pulse;
        this.crest.yScale = pulse;
        this.crest.zScale = pulse;
    }
}
