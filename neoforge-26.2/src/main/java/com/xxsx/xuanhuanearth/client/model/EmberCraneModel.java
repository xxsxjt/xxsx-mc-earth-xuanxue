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
    private final ModelPart midNeck;
    private final ModelPart upperNeck;
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
        this.midNeck = this.neck.getChild("mid_neck");
        this.upperNeck = this.midNeck.getChild("upper");
        this.head = this.upperNeck.getChild("head");
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
                        .texOffs(0, 0).addBox(-3.0F, -2.5F, -4.0F, 6.0F, 5.0F, 8.0F)
                        .texOffs(29, 0).addBox(-2.5F, -1.5F, -5.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.04F))
                        .texOffs(46, 0).addBox(-2.5F, -3.0F, -2.5F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -0.18F, 0.0F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(69, 0).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 9.5F, -4.0F, 0.32F, 0.0F, 0.0F));
        PartDefinition neck_mid_neck = neck.addOrReplaceChild(
                "mid_neck",
                CubeListBuilder.create()
                        .texOffs(82, 0).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.48F, 0.0F, 0.0F));
        PartDefinition neck_mid_neck_upper = neck_mid_neck.addOrReplaceChild(
                "upper",
                CubeListBuilder.create()
                        .texOffs(91, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.32F, 0.0F, 0.0F));
        PartDefinition neck_mid_neck_upper_head = neck_mid_neck_upper.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(100, 0).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F)
                        .texOffs(115, 0).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 3.0F)
                        .texOffs(0, 14).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(9, 14).addBox(-0.5F, -0.5F, -6.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.12F, 0.0F, 0.0F));
        neck_mid_neck_upper_head.addOrReplaceChild(
                "plume",
                CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, -0.42F, 0.0F, 0.0F));
        PartDefinition left_wing = root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create()
                        .texOffs(29, 14).addBox(-0.5F, -1.0F, 0.0F, 2.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(3.0F, 9.5F, -2.5F, 0.14F, 0.08F, -0.18F));
        left_wing.addOrReplaceChild(
                "feather_0",
                CubeListBuilder.create()
                        .texOffs(46, 14).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(0.5F, 0.5F, 1.0F, -0.08F, 0.04F, -0.06F));
        left_wing.addOrReplaceChild(
                "feather_1",
                CubeListBuilder.create()
                        .texOffs(63, 14).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(0.5F, 1.0F, 2.5F, -0.02F, 0.04F, -0.06F));
        left_wing.addOrReplaceChild(
                "feather_2",
                CubeListBuilder.create()
                        .texOffs(80, 14).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(0.5F, 1.5F, 4.0F, 0.04F, 0.04F, -0.06F));
        left_wing.addOrReplaceChild(
                "feather_3",
                CubeListBuilder.create()
                        .texOffs(97, 14).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(0.5F, 2.0F, 5.5F, 0.1F, 0.04F, -0.06F));
        PartDefinition right_wing = root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-1.5F, -1.0F, 0.0F, 2.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(-3.0F, 9.5F, -2.5F, 0.14F, -0.08F, 0.18F));
        right_wing.addOrReplaceChild(
                "feather_0",
                CubeListBuilder.create()
                        .texOffs(17, 24).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-0.5F, 0.5F, 1.0F, -0.08F, -0.04F, 0.06F));
        right_wing.addOrReplaceChild(
                "feather_1",
                CubeListBuilder.create()
                        .texOffs(34, 24).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-0.5F, 1.0F, 2.5F, -0.02F, -0.04F, 0.06F));
        right_wing.addOrReplaceChild(
                "feather_2",
                CubeListBuilder.create()
                        .texOffs(51, 24).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-0.5F, 1.5F, 4.0F, 0.04F, -0.04F, 0.06F));
        right_wing.addOrReplaceChild(
                "feather_3",
                CubeListBuilder.create()
                        .texOffs(68, 24).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(-0.5F, 2.0F, 5.5F, 0.1F, -0.04F, 0.06F));
        PartDefinition left_leg = root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(85, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 13.0F, 0.5F, -0.12F, 0.0F, 0.0F));
        PartDefinition left_leg_shin = left_leg.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(90, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        PartDefinition left_leg_shin_foot = left_leg_shin.addOrReplaceChild(
                "foot",
                CubeListBuilder.create()
                        .texOffs(95, 24).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.06F, 0.0F, 0.0F));
        left_leg_shin_foot.addOrReplaceChild(
                "toe_in",
                CubeListBuilder.create()
                        .texOffs(106, 24).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.36F, 0.0F));
        left_leg_shin_foot.addOrReplaceChild(
                "toe_out",
                CubeListBuilder.create()
                        .texOffs(115, 24).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.36F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 13.0F, 0.5F, -0.12F, 0.0F, 0.0F));
        PartDefinition right_leg_shin = right_leg.addOrReplaceChild(
                "shin",
                CubeListBuilder.create()
                        .texOffs(5, 34).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        PartDefinition right_leg_shin_foot = right_leg_shin.addOrReplaceChild(
                "foot",
                CubeListBuilder.create()
                        .texOffs(10, 34).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.06F, 0.0F, 0.0F));
        right_leg_shin_foot.addOrReplaceChild(
                "toe_in",
                CubeListBuilder.create()
                        .texOffs(21, 34).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.36F, 0.0F));
        right_leg_shin_foot.addOrReplaceChild(
                "toe_out",
                CubeListBuilder.create()
                        .texOffs(30, 34).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.36F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(39, 34).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 10.5F, 4.0F, 0.28F, 0.0F, 0.0F));
        tail.addOrReplaceChild(
                "center_tail_feather",
                CubeListBuilder.create()
                        .texOffs(58, 34).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 0.18F, 0.0F, 0.0F));
        tail.addOrReplaceChild(
                "left_tail_feather",
                CubeListBuilder.create()
                        .texOffs(77, 34).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.12F, 0.32F, 0.14F));
        tail.addOrReplaceChild(
                "right_tail_feather",
                CubeListBuilder.create()
                        .texOffs(96, 34).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 1.0F, 0.12F, -0.32F, -0.14F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;

        this.neck.yRot += state.yRot * Mth.DEG_TO_RAD * 0.75F;
        this.neck.xRot += state.xRot * Mth.DEG_TO_RAD * 0.35F;
        this.midNeck.xRot += Mth.sin(state.ageInTicks * 0.05F) * 0.03F;
        this.head.xRot += Mth.sin(state.ageInTicks * 0.06F) * 0.025F;
        this.head.zRot += hurt * 0.14F;
        this.leftLeg.xRot += Mth.cos(walk * 0.68F) * 0.82F * speed;
        this.rightLeg.xRot += Mth.cos(walk * 0.68F + Mth.PI) * 0.82F * speed;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        boolean airborne = !state.onGround && state.leapProgress != 0.0F;
        float beatSpeed = state.aggressive || airborne ? 0.32F : 0.09F;
        float wingBeat = Mth.sin(state.ageInTicks * beatSpeed);
        float spread = state.aggressive ? 0.5F : 0.04F;
        if (airborne) {
            spread = 0.85F;
        }
        this.leftWing.zRot += -wingBeat * spread - attack * 0.72F;
        this.rightWing.zRot += wingBeat * spread + attack * 0.72F;
        this.leftWing.yRot += Mth.sin(state.ageInTicks * 0.05F) * 0.03F;
        this.rightWing.yRot -= Mth.sin(state.ageInTicks * 0.05F) * 0.03F;
        this.neck.xRot -= attack * 0.48F;
        this.tail.yRot += Mth.sin(state.ageInTicks * 0.07F) * 0.08F + state.tailWag * 0.04F;
        this.tail.xRot += Mth.sin(state.ageInTicks * 0.05F) * 0.05F;
        this.body.y += Mth.sin(state.ageInTicks * 0.08F) * 0.06F + state.breathPhase * 0.05F;

        if (state.sitting) {
            this.leftLeg.xRot -= 1.15F;
            this.rightLeg.xRot -= 1.15F;
            this.body.y += 1.2F;
        }

        if (state.isInWater) {
            this.leftLeg.xRot -= 0.45F;
            this.rightLeg.xRot -= 0.45F;
            this.leftWing.zRot -= 0.35F + wingBeat * 0.25F;
            this.rightWing.zRot += 0.35F + wingBeat * 0.25F;
            this.body.y += 1.0F;
        }
    }
}
