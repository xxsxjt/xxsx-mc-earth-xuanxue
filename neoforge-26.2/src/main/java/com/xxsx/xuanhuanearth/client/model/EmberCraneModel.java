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

public final class EmberCraneModel extends EntityModel<SpiritBeastRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("ember_crane"), "main");

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;

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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -3.5F, -4.5F, 7.0F, 7.0F, 9.0F)
                        .texOffs(33, 0).addBox(-3.0F, 2.0F, -4.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 10.5F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(54, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 7.5F, -3.5F, -0.34F, 0.0F, 0.0F));
        PartDefinition neck_upper = neck.addOrReplaceChild(
                "upper",
                CubeListBuilder.create()
                        .texOffs(67, 0).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.62F, 0.0F, 0.0F));
        neck_upper.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 4.0F)
                        .texOffs(97, 0).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(104, 0).addBox(-1.0F, -0.5F, -4.5F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition left_wing = root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.0F, -2.5F, -3.5F, 2.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(3.0F, 8.5F, 0.0F, 0.0F, 0.0F, -0.42F));
        left_wing.addOrReplaceChild(
                "feather_0",
                CubeListBuilder.create()
                        .texOffs(19, 17).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(1.0F, 1.5F, -2.0F, 0.06F, 0.0F, -0.1F));
        left_wing.addOrReplaceChild(
                "feather_1",
                CubeListBuilder.create()
                        .texOffs(36, 17).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(1.0F, 2.5F, -0.5F, 0.15F, 0.0F, -0.1F));
        left_wing.addOrReplaceChild(
                "feather_2",
                CubeListBuilder.create()
                        .texOffs(53, 17).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(1.0F, 3.5F, 1.0F, 0.24F, 0.0F, -0.1F));
        PartDefinition right_wing = root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create()
                        .texOffs(70, 17).addBox(-1.0F, -2.5F, -3.5F, 2.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(-3.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.42F));
        right_wing.addOrReplaceChild(
                "feather_0",
                CubeListBuilder.create()
                        .texOffs(89, 17).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 1.5F, -2.0F, 0.06F, 0.0F, 0.1F));
        right_wing.addOrReplaceChild(
                "feather_1",
                CubeListBuilder.create()
                        .texOffs(106, 17).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 2.5F, -0.5F, 0.15F, 0.0F, 0.1F));
        right_wing.addOrReplaceChild(
                "feather_2",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 3.5F, 1.0F, 0.24F, 0.0F, 0.1F));
        PartDefinition left_leg = root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(17, 30).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F),
                PartPose.offset(1.5F, 14.5F, 0.0F));
        left_leg.addOrReplaceChild(
                "foot",
                CubeListBuilder.create()
                        .texOffs(22, 30).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 7.5F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(33, 30).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F),
                PartPose.offset(-1.5F, 14.5F, 0.0F));
        right_leg.addOrReplaceChild(
                "foot",
                CubeListBuilder.create()
                        .texOffs(38, 30).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 7.5F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(49, 30).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 4.5F, 0.38F, 0.0F, 0.0F));
        tail.addOrReplaceChild(
                "left_tail_feather",
                CubeListBuilder.create()
                        .texOffs(68, 30).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.06F, 0.2F, 0.0F));
        tail.addOrReplaceChild(
                "right_tail_feather",
                CubeListBuilder.create()
                        .texOffs(87, 30).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.5F, 0.06F, -0.2F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

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
