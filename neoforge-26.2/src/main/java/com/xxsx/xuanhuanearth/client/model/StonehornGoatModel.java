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

public final class StonehornGoatModel extends EntityModel<SpiritBeastRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("stonehorn_goat"), "main");

    private final ModelPart head;
    private final ModelPart muzzle;
    private final ModelPart beard;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart body;
    private final ModelPart armor;
    private final ModelPart tail;
    private final ModelPart[] legs;

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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -4.0F, -5.5F, 9.0F, 8.0F, 6.0F)
                        .texOffs(31, 0).addBox(-4.0F, -3.5F, 0.5F, 8.0F, 7.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 13.5F, 0.0F, -0.05F, 0.0F, 0.0F));
        PartDefinition armor = root.addOrReplaceChild(
                "armor",
                CubeListBuilder.create()
                        .texOffs(58, 0).addBox(-5.0F, -4.5F, -5.0F, 10.0F, 3.0F, 6.0F, new CubeDeformation(0.06F))
                        .texOffs(91, 0).addBox(-3.5F, -4.0F, 0.5F, 7.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        armor.addOrReplaceChild(
                "left_pauldron",
                CubeListBuilder.create()
                        .texOffs(0, 15).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 5.0F, 6.0F)
                        .texOffs(17, 15).addBox(0.0F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(4.5F, -2.5F, -2.5F, 0.08F, 0.0F, -0.34F));
        armor.addOrReplaceChild(
                "right_pauldron",
                CubeListBuilder.create()
                        .texOffs(26, 15).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 5.0F, 6.0F)
                        .texOffs(43, 15).addBox(-1.0F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-4.5F, -2.5F, -2.5F, 0.08F, 0.0F, 0.34F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(52, 15).addBox(-3.0F, -3.0F, -3.5F, 6.0F, 6.0F, 6.0F)
                        .texOffs(77, 15).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, -6.0F, 0.14F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create()
                        .texOffs(98, 15).addBox(-2.0F, -1.5F, -3.0F, 4.0F, 3.0F, 3.0F)
                        .texOffs(113, 15).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, -3.5F, 0.18F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "beard",
                CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(9, 28).addBox(-0.5F, 3.5F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.5F, -3.0F, 0.38F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(14, 28).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, -1.0F, -0.5F, 0.0F, -0.24F, 0.48F));
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(21, 28).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -0.5F, 0.0F, 0.24F, -0.48F));
        PartDefinition head_left_horn_base = head.addOrReplaceChild(
                "left_horn_base",
                CubeListBuilder.create()
                        .texOffs(28, 28).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(1.5F, -2.5F, -0.5F, -0.36F, 0.14F, -0.32F));
        PartDefinition head_left_horn_base_left_horn_mid = head_left_horn_base.addOrReplaceChild(
                "left_horn_mid",
                CubeListBuilder.create()
                        .texOffs(41, 28).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.48F, 0.18F, -0.24F));
        PartDefinition head_left_horn_base_left_horn_mid_left_horn_curl = head_left_horn_base_left_horn_mid.addOrReplaceChild(
                "left_horn_curl",
                CubeListBuilder.create()
                        .texOffs(50, 28).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -0.54F, 0.14F, -0.16F));
        head_left_horn_base_left_horn_mid_left_horn_curl.addOrReplaceChild(
                "left_horn_tip",
                CubeListBuilder.create()
                        .texOffs(59, 28).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.48F, -0.12F, -0.12F));
        PartDefinition head_right_horn_base = head.addOrReplaceChild(
                "right_horn_base",
                CubeListBuilder.create()
                        .texOffs(64, 28).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-1.5F, -2.5F, -0.5F, -0.36F, -0.14F, 0.32F));
        PartDefinition head_right_horn_base_right_horn_mid = head_right_horn_base.addOrReplaceChild(
                "right_horn_mid",
                CubeListBuilder.create()
                        .texOffs(77, 28).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.48F, -0.18F, 0.24F));
        PartDefinition head_right_horn_base_right_horn_mid_right_horn_curl = head_right_horn_base_right_horn_mid.addOrReplaceChild(
                "right_horn_curl",
                CubeListBuilder.create()
                        .texOffs(86, 28).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -0.54F, -0.14F, 0.16F));
        head_right_horn_base_right_horn_mid_right_horn_curl.addOrReplaceChild(
                "right_horn_tip",
                CubeListBuilder.create()
                        .texOffs(95, 28).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.48F, 0.12F, 0.12F));
        PartDefinition front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(100, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, 15.5F, -3.5F, -0.06F, 0.0F, 0.0F));
        front_right_leg.addOrReplaceChild(
                "shank",
                CubeListBuilder.create()
                        .texOffs(113, 28).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(0, 37).addBox(-1.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F)
                        .texOffs(9, 37).addBox(0.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(18, 37).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(3.0F, 15.5F, -3.5F, -0.06F, 0.0F, 0.0F));
        front_left_leg.addOrReplaceChild(
                "shank",
                CubeListBuilder.create()
                        .texOffs(31, 37).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(40, 37).addBox(-1.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F)
                        .texOffs(49, 37).addBox(0.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(58, 37).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, 15.5F, 3.5F, -0.12F, 0.0F, 0.0F));
        back_right_leg.addOrReplaceChild(
                "shank",
                CubeListBuilder.create()
                        .texOffs(71, 37).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(80, 37).addBox(-1.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F)
                        .texOffs(89, 37).addBox(0.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        PartDefinition back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(98, 37).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(3.0F, 15.5F, 3.5F, -0.12F, 0.0F, 0.0F));
        back_left_leg.addOrReplaceChild(
                "shank",
                CubeListBuilder.create()
                        .texOffs(111, 37).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(0, 46).addBox(-1.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F)
                        .texOffs(9, 46).addBox(0.5F, 2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(18, 46).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 11.5F, 5.0F, 0.58F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float run = state.runAmount;
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float leap = state.leapProgress;

        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.22F : 0.0F);
        this.head.zRot += hurt * 0.16F;

        float stride = Mth.lerp(run, 1.0F, 1.35F);
        float swing = Mth.cos(walk * 0.58F) * 0.85F * speed * stride;
        float opp = Mth.cos(walk * 0.58F + Mth.PI) * 0.85F * speed * stride;
        this.legs[0].xRot += swing;
        this.legs[1].xRot += opp;
        this.legs[2].xRot += opp;
        this.legs[3].xRot += swing;

        if (leap > 0.05F) {
            this.legs[0].xRot -= leap * 0.4F;
            this.legs[1].xRot -= leap * 0.4F;
            this.legs[2].xRot += leap * 0.35F;
            this.legs[3].xRot += leap * 0.35F;
        }

        this.body.zRot += Mth.sin(state.ageInTicks * 0.035F) * 0.018F + state.breathPhase * 0.01F;
        this.body.xRot += state.breathPhase * 0.012F - leap * 0.1F;
        this.armor.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.12F) * 0.015F * state.affinity;
        this.tail.yRot += Mth.sin(state.ageInTicks * 0.12F) * 0.22F + state.tailWag * 0.1F;
        this.leftHorn.zRot += Mth.sin(state.ageInTicks * 0.06F) * 0.018F;
        this.rightHorn.zRot -= Mth.sin(state.ageInTicks * 0.06F) * 0.018F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.xRot -= attack * 0.9F;
        this.body.xRot += attack * 0.08F;
        this.beard.xRot += attack * 0.3F + Mth.sin(state.ageInTicks * 0.09F) * 0.03F;

        if (state.sitting) {
            this.body.xRot += 0.16F;
            for (ModelPart leg : this.legs) {
                leg.xRot -= 1.15F;
            }
            this.head.xRot += 0.18F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(state.ageInTicks * 0.4F);
            for (int i = 0; i < this.legs.length; i++) {
                this.legs[i].xRot += (i % 2 == 0 ? paddle : -paddle) * 0.42F;
            }
        }
    }
}
