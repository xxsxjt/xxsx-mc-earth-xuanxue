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
                        .texOffs(0, 0).addBox(-3.8F, -3.6F, -4.8F, 7.6F, 7.2F, 9.6F)
                        .texOffs(19, 0).addBox(-3.2F, -4.1F, -4.0F, 6.4F, 0.8F, 8.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 13.5F, 0.0F));
        root.addOrReplaceChild(
                "armor",
                CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-4.3F, -2.2F, -4.2F, 8.6F, 2.6F, 8.4F, new CubeDeformation(0.04F))
                        .texOffs(53, 0).addBox(-2.8F, -2.9F, -1.6F, 5.6F, 0.9F, 3.2F),
                PartPose.offset(0.0F, 13.5F, 0.0F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-2.9F, -2.9F, -3.2F, 5.8F, 5.8F, 6.4F),
                PartPose.offset(0.0F, 9.2F, -5.6F));
        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create()
                        .texOffs(77, 0).addBox(-2.0F, -1.2F, -2.8F, 4.0F, 3.2F, 2.8F)
                        .texOffs(86, 0).addBox(-1.0F, 0.2F, -3.5F, 2.0F, 1.4F, 0.9F),
                PartPose.offset(0.0F, 1.0F, -3.2F));
        head.addOrReplaceChild(
                "beard",
                CubeListBuilder.create()
                        .texOffs(91, 0).addBox(-1.2F, 0.0F, -0.9F, 2.4F, 4.0F, 1.8F),
                PartPose.offsetAndRotation(0.0F, 2.2F, -2.2F, 0.26F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "left_horn_base",
                CubeListBuilder.create()
                        .texOffs(96, 0).addBox(-1.0F, -2.6F, -1.0F, 2.0F, 2.6F, 2.0F),
                PartPose.offsetAndRotation(1.9F, -2.2F, 0.0F, -1.45F, 0.0F, -0.26F));
        head.addOrReplaceChild(
                "left_horn_mid",
                CubeListBuilder.create()
                        .texOffs(101, 0).addBox(-0.8F, -2.2F, -0.8F, 1.6F, 2.2F, 1.6F),
                PartPose.offsetAndRotation(2.4F, -3.6F, 1.6F, -1.55F, 0.0F, -0.18F));
        head.addOrReplaceChild(
                "left_horn_tip",
                CubeListBuilder.create()
                        .texOffs(104, 0).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.7F, 1.2F),
                PartPose.offsetAndRotation(2.7F, -4.4F, 3.2F, -1.6F, 0.0F, -0.12F));
        head.addOrReplaceChild(
                "right_horn_base",
                CubeListBuilder.create()
                        .texOffs(107, 0).addBox(-1.0F, -2.6F, -1.0F, 2.0F, 2.6F, 2.0F),
                PartPose.offsetAndRotation(-1.9F, -2.2F, 0.0F, -1.45F, 0.0F, 0.26F));
        head.addOrReplaceChild(
                "right_horn_mid",
                CubeListBuilder.create()
                        .texOffs(112, 0).addBox(-0.8F, -2.2F, -0.8F, 1.6F, 2.2F, 1.6F),
                PartPose.offsetAndRotation(-2.4F, -3.6F, 1.6F, -1.55F, 0.0F, 0.18F));
        head.addOrReplaceChild(
                "right_horn_tip",
                CubeListBuilder.create()
                        .texOffs(115, 0).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.7F, 1.2F),
                PartPose.offsetAndRotation(-2.7F, -4.4F, 3.2F, -1.6F, 0.0F, 0.12F));
        root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(118, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.2F, 3.0F)
                        .texOffs(0, 17).addBox(-1.7F, 6.0F, -1.8F, 3.4F, 1.6F, 3.6F),
                PartPose.offset(-2.9F, 16.4F, -3.4F));
        root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(7, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.2F, 3.0F)
                        .texOffs(14, 17).addBox(-1.7F, 6.0F, -1.8F, 3.4F, 1.6F, 3.6F),
                PartPose.offset(2.9F, 16.4F, -3.4F));
        root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(21, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.2F, 3.0F)
                        .texOffs(28, 17).addBox(-1.7F, 6.0F, -1.8F, 3.4F, 1.6F, 3.6F),
                PartPose.offset(-2.9F, 16.4F, 3.4F));
        root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(35, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.2F, 3.0F)
                        .texOffs(42, 17).addBox(-1.7F, 6.0F, -1.8F, 3.4F, 1.6F, 3.6F),
                PartPose.offset(2.9F, 16.4F, 3.4F));
        root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(49, 17).addBox(-1.2F, -1.2F, 0.0F, 2.4F, 2.4F, 3.6F),
                PartPose.offsetAndRotation(0.0F, 11.6F, 4.6F, 0.7F, 0.0F, 0.0F));

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
