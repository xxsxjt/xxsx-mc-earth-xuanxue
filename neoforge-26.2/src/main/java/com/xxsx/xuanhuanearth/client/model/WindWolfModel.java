package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.SpiritBeastRenderState;
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

public final class WindWolfModel extends EntityModel<SpiritBeastRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("wind_wolf"), "main");

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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.5F, -5.5F, 8.0F, 7.0F, 6.0F)
                        .texOffs(29, 0).addBox(-3.0F, -3.0F, 0.5F, 6.0F, 6.0F, 5.0F)
                        .texOffs(52, 0).addBox(-3.5F, 1.0F, -6.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.04F)),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.06F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "dorsal_fin_1",
                CubeListBuilder.create()
                        .texOffs(75, 0).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -3.5F, -3.0F, -0.42F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "dorsal_fin_2",
                CubeListBuilder.create()
                        .texOffs(90, 0).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 1.5F, -0.48F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(103, 0).addBox(-3.0F, -2.5F, -2.0F, 6.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, -5.0F, 0.38F, 0.0F, 0.0F));
        PartDefinition mane = root.addOrReplaceChild(
                "mane",
                CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-4.5F, -4.0F, -2.5F, 9.0F, 8.0F, 5.0F, new CubeDeformation(0.06F)),
                PartPose.offsetAndRotation(0.0F, 12.5F, -4.5F, 0.12F, 0.0F, 0.0F));
        mane.addOrReplaceChild(
                "mane_top",
                CubeListBuilder.create()
                        .texOffs(29, 14).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 3.0F, 7.0F)
                        .texOffs(52, 14).addBox(-1.0F, -3.0F, 2.0F, 2.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, -0.36F, 0.0F, 0.0F));
        mane.addOrReplaceChild(
                "mane_left",
                CubeListBuilder.create()
                        .texOffs(69, 14).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 4.0F, 7.0F)
                        .texOffs(88, 14).addBox(-1.0F, -1.0F, 4.0F, 1.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(-4.0F, -1.5F, -1.0F, 0.14F, -0.38F, -0.32F));
        mane.addOrReplaceChild(
                "mane_right",
                CubeListBuilder.create()
                        .texOffs(101, 14).addBox(0.0F, -2.0F, 0.0F, 2.0F, 4.0F, 7.0F)
                        .texOffs(0, 28).addBox(0.0F, -1.0F, 4.0F, 1.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(4.0F, -1.5F, -1.0F, 0.14F, 0.38F, 0.32F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(13, 28).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 6.0F, 6.0F)
                        .texOffs(40, 28).addBox(-3.5F, -3.5F, -4.0F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.04F)),
                PartPose.offsetAndRotation(0.0F, 9.0F, -7.5F, 0.04F, 0.0F, 0.0F));
        PartDefinition head_snout = head.addOrReplaceChild(
                "snout",
                CubeListBuilder.create()
                        .texOffs(61, 28).addBox(-2.0F, -1.0F, -3.5F, 4.0F, 3.0F, 4.0F)
                        .texOffs(78, 28).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, -3.5F, 0.06F, 0.0F, 0.0F));
        head_snout.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(85, 28).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(98, 28).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(107, 28).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.5F, -2.5F, -0.5F, -0.28F, -0.16F, 0.32F));
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(112, 28).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(121, 28).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-2.5F, -2.5F, -0.5F, -0.28F, 0.16F, -0.32F));
        PartDefinition collar = root.addOrReplaceChild(
                "collar",
                CubeListBuilder.create()
                        .texOffs(0, 41).addBox(-4.5F, -1.0F, -3.0F, 9.0F, 2.0F, 6.0F, new CubeDeformation(0.06F)),
                PartPose.offsetAndRotation(0.0F, 12.5F, -5.0F, 0.18F, 0.0F, 0.0F));
        collar.addOrReplaceChild(
                "pendant",
                CubeListBuilder.create()
                        .texOffs(31, 41).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -3.5F, -0.16F, 0.0F, 0.0F));
        PartDefinition front_right = root.addOrReplaceChild(
                "front_right",
                CubeListBuilder.create()
                        .texOffs(40, 41).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-2.5F, 15.0F, -3.5F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_right_shin = front_right.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(53, 41).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.16F, 0.0F, 0.0F));
        front_right_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(62, 41).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_left = root.addOrReplaceChild(
                "front_left",
                CubeListBuilder.create()
                        .texOffs(75, 41).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(2.5F, 15.0F, -3.5F, -0.08F, 0.0F, 0.0F));
        PartDefinition front_left_shin = front_left.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(88, 41).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.16F, 0.0F, 0.0F));
        front_left_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(97, 41).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.08F, 0.0F, 0.0F));
        PartDefinition back_right = root.addOrReplaceChild(
                "back_right",
                CubeListBuilder.create()
                        .texOffs(110, 41).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-2.5F, 15.0F, 3.5F, -0.18F, 0.0F, 0.0F));
        PartDefinition back_right_shin = back_right.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(0, 50).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.34F, 0.0F, 0.0F));
        back_right_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(9, 50).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.16F, 0.0F, 0.0F));
        PartDefinition back_left = root.addOrReplaceChild(
                "back_left",
                CubeListBuilder.create()
                        .texOffs(22, 50).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(2.5F, 15.0F, 3.5F, -0.18F, 0.0F, 0.0F));
        PartDefinition back_left_shin = back_left.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(35, 50).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.34F, 0.0F, 0.0F));
        back_left_shin.addOrReplaceChild(
                "paw",
                CubeListBuilder.create()
                        .texOffs(44, 50).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.16F, 0.0F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(57, 50).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 11.5F, 5.0F, -0.22F, 0.0F, 0.0F));
        PartDefinition tail_tail_mid = tail.addOrReplaceChild(
                "tail_mid",
                CubeListBuilder.create()
                        .texOffs(76, 50).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.34F, 0.0F, 0.0F));
        tail_tail_mid.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create()
                        .texOffs(97, 50).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 5.0F)
                        .texOffs(116, 50).addBox(-1.0F, -1.0F, 4.5F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.32F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float age = state.ageInTicks;
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.08F : 0.0F);
        this.head.zRot += hurt * 0.18F;

        float stride = Mth.lerp(run, 1.0F, 1.45F);
        float swing = Mth.cos(walk * 0.72F) * 1.1F * speed * stride;
        float opp = Mth.cos(walk * 0.72F + Mth.PI) * 1.1F * speed * stride;
        this.frontRightLeg.xRot += swing;
        this.frontLeftLeg.xRot += opp;
        this.backRightLeg.xRot += opp;
        this.backLeftLeg.xRot += swing;

        if (leap > 0.05F) {
            this.frontRightLeg.xRot -= leap * 0.55F;
            this.frontLeftLeg.xRot -= leap * 0.55F;
            this.backRightLeg.xRot += leap * 0.45F;
            this.backLeftLeg.xRot += leap * 0.45F;
        }

        float affinityMotion = 0.14F + state.affinity * 0.14F;
        this.maneTop.yRot += Mth.sin(age * 0.07F) * affinityMotion + run * 0.10F;
        this.maneLeft.yRot += Mth.sin(age * 0.08F + 0.8F) * affinityMotion * 0.6F + run * 0.12F;
        this.maneRight.yRot -= Mth.sin(age * 0.08F + 0.8F) * affinityMotion * 0.6F + run * 0.12F;
        this.tail.yRot += Mth.sin(age * 0.09F) * (0.20F + affinityMotion) + state.tailWag * 0.12F;
        this.tailMid.yRot += Mth.sin(age * 0.12F + 0.6F) * (0.24F + affinityMotion);
        this.tailTip.yRot += Mth.sin(age * 0.15F + 1.1F) * (0.28F + affinityMotion);
        this.tail.xRot += Mth.cos(age * 0.06F) * 0.05F - run * 0.12F;
        this.tailMid.xRot += Mth.sin(age * 0.07F + 0.4F) * 0.04F;
        this.tailTip.xRot += Mth.sin(age * 0.08F + 0.9F) * 0.05F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.body.xRot += (state.aggressive ? -0.06F : 0.0F) + state.breathPhase * 0.012F - leap * 0.12F;
        this.head.xRot -= attack * 0.62F;
        this.frontRightLeg.xRot -= attack * 0.38F;
        this.frontLeftLeg.xRot -= attack * 0.38F;
        this.snout.xRot += attack * 0.18F;

        if (state.sitting) {
            this.body.xRot += 0.22F;
            this.frontRightLeg.xRot -= 0.20F;
            this.frontLeftLeg.xRot -= 0.20F;
            this.backRightLeg.xRot -= 1.10F;
            this.backLeftLeg.xRot -= 1.10F;
            this.tail.xRot += 0.50F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(age * 0.45F);
            this.frontRightLeg.xRot += paddle * 0.55F;
            this.frontLeftLeg.xRot -= paddle * 0.55F;
            this.backRightLeg.xRot -= paddle * 0.5F;
            this.backLeftLeg.xRot += paddle * 0.5F;
        }

        float earTilt = hurt * 0.55F + (state.aggressive ? 0.18F : 0.0F);
        this.ears[0].xRot += -earTilt * 0.45F + Mth.sin(age * 0.11F) * 0.02F;
        this.ears[1].xRot += -earTilt * 0.45F + Mth.sin(age * 0.11F + 0.3F) * 0.02F;
        this.ears[0].zRot += earTilt * 0.30F;
        this.ears[1].zRot -= earTilt * 0.30F;

        float pulse = 1.0F + Mth.sin(age * 0.2F) * (0.04F + state.affinity * 0.04F);
        this.pendant.xScale = pulse;
        this.pendant.yScale = pulse;
        this.pendant.zScale = pulse;
    }
}
