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

public final class CrystalTurtleModel extends EntityModel<SpiritBeastRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("crystal_turtle"), "main");

    private final ModelPart head;
    private final ModelPart shell;
    private final ModelPart crystals;
    private final ModelPart tail;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart backRight;
    private final ModelPart backLeft;

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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, 0.0F, -5.0F, 9.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        PartDefinition shell = root.addOrReplaceChild(
                "shell",
                CubeListBuilder.create()
                        .texOffs(39, 0).addBox(-5.5F, -2.5F, -6.0F, 11.0F, 3.0F, 12.0F)
                        .texOffs(86, 0).addBox(-4.0F, -4.0F, -4.5F, 8.0F, 2.0F, 9.0F)
                        .texOffs(0, 16).addBox(-6.0F, 0.0F, -6.5F, 12.0F, 1.0F, 13.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 16.5F, 0.0F));
        shell.addOrReplaceChild(
                "skirt_left",
                CubeListBuilder.create()
                        .texOffs(51, 16).addBox(0.0F, 0.0F, -5.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(5.5F, -1.0F, 0.0F, 0.0F, 0.0F, 0.38F));
        shell.addOrReplaceChild(
                "skirt_right",
                CubeListBuilder.create()
                        .texOffs(76, 16).addBox(-2.0F, 0.0F, -5.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(-5.5F, -1.0F, 0.0F, 0.0F, 0.0F, -0.38F));
        PartDefinition shell_crystals = shell.addOrReplaceChild(
                "crystals",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.5F, 0.0F));
        shell_crystals.addOrReplaceChild(
                "main_spire",
                CubeListBuilder.create()
                        .texOffs(101, 16).addBox(-1.5F, -4.5F, -1.5F, 3.0F, 5.0F, 3.0F)
                        .texOffs(114, 16).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -0.5F, -0.42F, 0.0F, 0.08F));
        shell_crystals.addOrReplaceChild(
                "left_crystal",
                CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(9, 31).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 1.5F, -1.0F, -0.24F, 0.18F, 0.48F));
        shell_crystals.addOrReplaceChild(
                "right_crystal",
                CubeListBuilder.create()
                        .texOffs(14, 31).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(23, 31).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 1.5F, -1.0F, -0.24F, -0.18F, -0.48F));
        shell_crystals.addOrReplaceChild(
                "front_left_crystal",
                CubeListBuilder.create()
                        .texOffs(28, 31).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(1.5F, 1.5F, -2.5F, 0.36F, -0.22F, 0.38F));
        shell_crystals.addOrReplaceChild(
                "front_right_crystal",
                CubeListBuilder.create()
                        .texOffs(37, 31).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, 1.5F, -2.5F, 0.36F, 0.22F, -0.38F));
        shell_crystals.addOrReplaceChild(
                "rear_crystal",
                CubeListBuilder.create()
                        .texOffs(46, 31).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 2.0F, -0.58F, 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(55, 31).addBox(-2.5F, -2.5F, -3.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(76, 31).addBox(-1.5F, -1.0F, -5.0F, 3.0F, 2.0F, 2.0F)
                        .texOffs(87, 31).addBox(-1.5F, 1.0F, -4.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 15.5F, -7.5F, -0.08F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "left_horn",
                CubeListBuilder.create()
                        .texOffs(100, 31).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, -2.0F, -1.0F, -0.52F, 0.22F, 0.34F));
        head.addOrReplaceChild(
                "right_horn",
                CubeListBuilder.create()
                        .texOffs(105, 31).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, -2.0F, -1.0F, -0.52F, -0.22F, -0.34F));
        head.addOrReplaceChild(
                "whisker_left",
                CubeListBuilder.create()
                        .texOffs(110, 31).addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 0.5F, -3.5F, 0.28F, 0.32F, -0.24F));
        head.addOrReplaceChild(
                "whisker_right",
                CubeListBuilder.create()
                        .texOffs(115, 31).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 0.5F, -3.5F, 0.28F, -0.32F, 0.24F));
        head.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(0, 41).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, 1.0F, 0.22F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "front_right",
                CubeListBuilder.create()
                        .texOffs(15, 41).addBox(-2.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F)
                        .texOffs(30, 41).addBox(-4.0F, -0.5F, -1.5F, 2.0F, 1.0F, 4.0F)
                        .texOffs(43, 41).addBox(-4.5F, 0.0F, -2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 19.5F, -3.5F, 0.08F, 0.28F, 0.18F));
        root.addOrReplaceChild(
                "front_left",
                CubeListBuilder.create()
                        .texOffs(50, 41).addBox(-0.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F)
                        .texOffs(65, 41).addBox(1.5F, -0.5F, -1.5F, 2.0F, 1.0F, 4.0F)
                        .texOffs(78, 41).addBox(2.5F, 0.0F, -2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 19.5F, -3.5F, 0.08F, -0.28F, -0.18F));
        root.addOrReplaceChild(
                "back_right",
                CubeListBuilder.create()
                        .texOffs(85, 41).addBox(-2.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F)
                        .texOffs(100, 41).addBox(-4.0F, -0.5F, -1.5F, 2.0F, 1.0F, 4.0F)
                        .texOffs(113, 41).addBox(-4.5F, 0.0F, -2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, 19.5F, 3.5F, 0.08F, 0.52F, 0.18F));
        root.addOrReplaceChild(
                "back_left",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-0.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F)
                        .texOffs(15, 48).addBox(1.5F, -0.5F, -1.5F, 2.0F, 1.0F, 4.0F)
                        .texOffs(28, 48).addBox(2.5F, 0.0F, -2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, 19.5F, 3.5F, 0.08F, -0.52F, -0.18F));
        PartDefinition tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(35, 48).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 18.5F, 6.0F, -0.18F, 0.0F, 0.0F));
        tail.addOrReplaceChild(
                "tail_spike",
                CubeListBuilder.create()
                        .texOffs(50, 48).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 2.0F, -0.38F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SpiritBeastRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;

        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.5F;
        this.head.zRot += hurt * 0.12F;
        float swing = Mth.cos(walk * 0.58F) * 0.48F * speed;
        float opp = Mth.cos(walk * 0.58F + Mth.PI) * 0.48F * speed;
        this.frontRight.yRot += swing;
        this.frontLeft.yRot += opp;
        this.backRight.yRot += opp;
        this.backLeft.yRot += swing;

        float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.16F) * (0.025F + state.affinity * 0.045F);
        this.crystals.xScale = pulse;
        this.crystals.yScale = pulse;
        this.crystals.zScale = pulse;
        this.crystals.xRot += Mth.sin(state.ageInTicks * 0.07F) * 0.03F;
        this.shell.yRot += Mth.sin(state.ageInTicks * 0.025F) * 0.012F;
        this.shell.xRot += state.breathPhase * 0.008F;
        this.tail.yRot += state.tailWag * 0.18F;

        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.z -= attack * 1.8F;
        this.head.xRot -= attack * 0.32F;

        if (state.sitting) {
            this.head.z += 2.2F;
            this.head.xRot += 0.42F;
            this.frontRight.yRot += 0.45F;
            this.frontLeft.yRot -= 0.45F;
            this.backRight.yRot -= 0.38F;
            this.backLeft.yRot += 0.38F;
            this.crystals.yScale *= 0.96F;
        }

        if (state.inWater) {
            float paddle = Mth.sin(state.ageInTicks * 0.3F);
            this.frontRight.yRot += paddle * 0.55F;
            this.frontLeft.yRot -= paddle * 0.55F;
            this.backRight.yRot -= paddle * 0.48F;
            this.backLeft.yRot += paddle * 0.48F;
        }
    }
}
