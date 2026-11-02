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
                        .texOffs(0, 0).addBox(-4.0F, -3.5F, -5.0F, 8.0F, 7.0F, 10.0F)
                        .texOffs(37, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 13.5F, 0.0F));
        root.addOrReplaceChild(
                "armor",
                CubeListBuilder.create()
                        .texOffs(66, 0).addBox(-4.5F, -2.0F, -4.0F, 9.0F, 3.0F, 8.0F, new CubeDeformation(0.04F))
                        .texOffs(101, 0).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 13.5F, 0.0F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 9.0F, -5.5F));
        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create()
                        .texOffs(25, 18).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 3.0F, 3.0F)
                        .texOffs(40, 18).addBox(-1.0F, 0.0F, -3.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 1.0F, -3.0F));
        head.addOrReplaceChild(
                "beard",
                CubeListBuilder.create()
                        .texOffs(47, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -2.0F, 0.26F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "left_horn_base",
                CubeListBuilder.create()
                        .texOffs(56, 18).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, -2.0F, 0.0F, -1.45F, 0.0F, -0.26F));
        head.addOrReplaceChild(
                "left_horn_mid",
                CubeListBuilder.create()
                        .texOffs(65, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(2.5F, -3.5F, 1.5F, -1.55F, 0.0F, -0.18F));
        head.addOrReplaceChild(
                "left_horn_tip",
                CubeListBuilder.create()
                        .texOffs(74, 18).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.5F, -4.5F, 3.0F, -1.6F, 0.0F, -0.12F));
        head.addOrReplaceChild(
                "right_horn_base",
                CubeListBuilder.create()
                        .texOffs(79, 18).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 0.0F, -1.45F, 0.0F, 0.26F));
        head.addOrReplaceChild(
                "right_horn_mid",
                CubeListBuilder.create()
                        .texOffs(88, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.5F, -3.5F, 1.5F, -1.55F, 0.0F, 0.18F));
        head.addOrReplaceChild(
                "right_horn_tip",
                CubeListBuilder.create()
                        .texOffs(97, 18).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-2.5F, -4.5F, 3.0F, -1.6F, 0.0F, 0.12F));
        root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(102, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(0, 31).addBox(-1.5F, 5.5F, -2.0F, 3.0F, 2.0F, 4.0F),
                PartPose.offset(-3.0F, 16.5F, -3.5F));
        root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(15, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(28, 31).addBox(-1.5F, 5.5F, -2.0F, 3.0F, 2.0F, 4.0F),
                PartPose.offset(3.0F, 16.5F, -3.5F));
        root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(43, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(56, 31).addBox(-1.5F, 5.5F, -2.0F, 3.0F, 2.0F, 4.0F),
                PartPose.offset(-3.0F, 16.5F, 3.5F));
        root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(71, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(84, 31).addBox(-1.5F, 5.5F, -2.0F, 3.0F, 2.0F, 4.0F),
                PartPose.offset(3.0F, 16.5F, 3.5F));
        root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(99, 31).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 11.5F, 4.5F, 0.7F, 0.0F, 0.0F));

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
