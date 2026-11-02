package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.CultivationSettlerRenderState;
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

public final class CultivationSettlerModel extends EntityModel<CultivationSettlerRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            XuanhuanEarth.id("cultivation_settler"), "main");

    private final ModelPart head;
    private final ModelPart hairBun;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart merchantPack;
    private final ModelPart springPack;
    private final ModelPart stewardPack;
    private final ModelPart merchantStaff;
    private final ModelPart jadePendant;

    public CultivationSettlerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.hairBun = this.head.getChild("hair_bun");
        this.body = root.getChild("body");
        this.jadePendant = this.body.getChild("jade_pendant");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.merchantPack = root.getChild("merchant_pack");
        this.springPack = root.getChild("spring_pack");
        this.stewardPack = root.getChild("steward_pack");
        this.merchantStaff = this.rightArm.getChild("merchant_staff");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("hair_cap", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 3.0F, 9.0F,
                                new CubeDeformation(0.06F)),
                PartPose.ZERO);
        head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-4.0F, -6.5F, 3.5F, 8.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        PartDefinition hairBun = head.addOrReplaceChild("hair_bun", CubeListBuilder.create()
                        .texOffs(92, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(108, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.5F, -0.14F, 0.0F, 0.0F));
        hairBun.addOrReplaceChild("hairpin", CubeListBuilder.create()
                        .texOffs(108, 6).addBox(-4.0F, -4.0F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.08F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("robe", CubeListBuilder.create()
                        .texOffs(26, 20).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 12.0F, 5.0F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        // Diagonal overlapping Daoist lapel
        body.addOrReplaceChild("lapel_left", CubeListBuilder.create()
                        .texOffs(88, 20).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, 0.0F, 0.0F, -0.34F));
        body.addOrReplaceChild("lapel_right", CubeListBuilder.create()
                        .texOffs(96, 20).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, 0.0F, 0.0F, 0.34F));
        // Cloud pauldrons on shoulders
        body.addOrReplaceChild("pauldron_left", CubeListBuilder.create()
                        .texOffs(104, 20).addBox(0.0F, -1.0F, -3.0F, 4.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(3.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.24F));
        body.addOrReplaceChild("pauldron_right", CubeListBuilder.create()
                        .texOffs(104, 28).addBox(-4.0F, -1.0F, -3.0F, 4.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(-3.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.24F));
        // Waist jade pendant & silk tassel
        body.addOrReplaceChild("jade_pendant", CubeListBuilder.create()
                        .texOffs(88, 32).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(2.5F, 8.5F, -2.0F, -0.08F, 0.0F, -0.14F));
        // Split lower robe flaps (front, back, left, right)
        body.addOrReplaceChild("robe_skirt", CubeListBuilder.create()
                        .texOffs(56, 20).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 8.0F, 5.0F,
                                new CubeDeformation(0.03F)),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.04F, 0.0F, 0.0F));
        body.addOrReplaceChild("skirt_left", CubeListBuilder.create()
                        .texOffs(74, 42).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 8.0F, 5.0F),
                PartPose.offsetAndRotation(4.5F, 10.0F, 0.0F, 0.0F, 0.0F, -0.14F));
        body.addOrReplaceChild("skirt_right", CubeListBuilder.create()
                        .texOffs(88, 42).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 8.0F, 5.0F),
                PartPose.offsetAndRotation(-4.5F, 10.0F, 0.0F, 0.0F, 0.0F, 0.14F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, -0.08F, 0.0F, 0.08F));
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(32, 42).addBox(-3.5F, -2.0F, -2.5F, 5.0F, 8.0F, 5.0F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("right_cuff", CubeListBuilder.create()
                        .texOffs(72, 60).addBox(-3.5F, 0.0F, -2.5F, 4.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.5F, 0.16F, 0.0F, 0.10F));
        PartDefinition staff = rightArm.addOrReplaceChild("merchant_staff", CubeListBuilder.create()
                        .texOffs(36, 104).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 20.0F, 2.0F),
                PartPose.offsetAndRotation(-2.5F, 2.0F, -1.5F, 0.24F, 0.0F, 0.0F));
        staff.addOrReplaceChild("lantern", CubeListBuilder.create()
                        .texOffs(46, 104).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, -2.5F, -0.24F, 0.0F, 0.0F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(16, 42).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, -0.08F, 0.0F, -0.08F));
        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(52, 42).addBox(-1.5F, -2.0F, -2.5F, 5.0F, 8.0F, 5.0F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("left_cuff", CubeListBuilder.create()
                        .texOffs(92, 60).addBox(-0.5F, 0.0F, -2.5F, 4.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.5F, 0.16F, 0.0F, -0.10F));

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create()
                        .texOffs(32, 60).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create()
                        .texOffs(52, 60).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition merchantPack = root.addOrReplaceChild("merchant_pack", CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 13.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 2.5F, 0.06F, 0.0F, 0.0F));
        merchantPack.addOrReplaceChild("bedroll", CubeListBuilder.create()
                        .texOffs(32, 80).addBox(-5.0F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 2.5F));
        merchantPack.addOrReplaceChild("pack_lantern", CubeListBuilder.create()
                        .texOffs(62, 80).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(5.5F, 7.0F, 2.5F, 0.0F, 0.0F, -0.14F));

        PartDefinition springPack = root.addOrReplaceChild("spring_pack", CubeListBuilder.create()
                        .texOffs(80, 80).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 2.5F, 0.06F, 0.0F, 0.0F));
        springPack.addOrReplaceChild("spring_crystal", CubeListBuilder.create()
                        .texOffs(112, 80).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 2.5F, -0.22F, 0.0F, 0.12F));

        PartDefinition stewardPack = root.addOrReplaceChild("steward_pack", CubeListBuilder.create()
                        .texOffs(0, 104).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 2.5F, 0.06F, 0.0F, 0.0F));
        stewardPack.addOrReplaceChild("banner", CubeListBuilder.create()
                        .texOffs(28, 104).addBox(-1.0F, -12.0F, -0.5F, 2.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 1.0F, 3.0F, -0.12F, 0.0F, 0.08F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CultivationSettlerRenderState state) {
        super.setupAnim(state);
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD;
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float hurt = state.hurtTime > 0.0F ? Mth.sin(state.hurtTime * 0.9F) : 0.0F;
        float breath = state.breathPhase;
        float gesture = state.gesturePhase;

        this.rightLeg.xRot += Mth.cos(walk * 0.6662F) * 1.0F * speed;
        this.leftLeg.xRot += Mth.cos(walk * 0.6662F + Mth.PI) * 1.0F * speed;
        this.rightLeg.zRot += hurt * 0.12F;
        this.leftLeg.zRot -= hurt * 0.12F;

        if (state.trading) {
            this.rightArm.xRot = -0.72F + gesture * 0.06F;
            this.leftArm.xRot = -0.72F - gesture * 0.05F;
            this.rightArm.yRot = -0.28F;
            this.leftArm.yRot = 0.28F;
            this.rightArm.zRot = 0.10F;
            this.leftArm.zRot = -0.10F;
        } else if (!state.walking) {
            this.rightArm.xRot += gesture * 0.05F + breath * 0.03F;
            this.leftArm.xRot += -gesture * 0.04F + breath * 0.03F;
            if (state.role == 0) {
                this.rightArm.xRot -= 0.18F + gesture * 0.08F;
            } else if (state.role == 1) {
                this.leftArm.xRot -= 0.12F + breath * 0.06F;
            } else if (state.role == 2) {
                this.rightArm.xRot -= 0.10F;
                this.head.xRot += gesture * 0.04F;
            }
        } else {
            this.rightArm.xRot += this.leftLeg.xRot * 0.65F;
            this.leftArm.xRot += this.rightLeg.xRot * 0.65F;
        }

        this.body.xRot += breath * 0.012F + hurt * 0.06F;
        this.body.yRot += Mth.sin(state.ageInTicks * 0.03F) * 0.015F;
        this.head.zRot += hurt * 0.14F;
        this.jadePendant.zRot += Mth.sin(state.ageInTicks * 0.09F) * 0.06F;

        this.merchantPack.visible = state.role == 0;
        this.merchantStaff.visible = state.role == 0;
        this.springPack.visible = state.role == 1;
        this.stewardPack.visible = state.role == 2;
        this.hairBun.visible = true;
    }
}
