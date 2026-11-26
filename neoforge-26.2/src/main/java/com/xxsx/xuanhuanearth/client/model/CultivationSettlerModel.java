package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.CultivationSettlerRenderState;
import com.xxsx.xuanhuanearth.entity.CultivationSettlerEntity;
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
    private final ModelPart elderCrown;
    private final ModelPart elderBeard;
    private final ModelPart body;
    private final ModelPart jadePendant;
    private final ModelPart ribbonLeft;
    private final ModelPart ribbonRight;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart merchantPack;
    private final ModelPart springPack;
    private final ModelPart stewardPack;
    private final ModelPart weaponStaff;
    private final ModelPart weaponSword;
    private final ModelPart weaponSpear;
    private final ModelPart weaponFan;

    public CultivationSettlerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.hairBun = this.head.getChild("hair_bun");
        this.elderCrown = this.head.getChild("elder_crown");
        this.elderBeard = this.head.getChild("elder_beard");
        this.body = root.getChild("body");
        this.jadePendant = this.body.getChild("jade_pendant");
        this.ribbonLeft = this.body.getChild("ribbon_left");
        this.ribbonRight = this.body.getChild("ribbon_right");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.merchantPack = root.getChild("merchant_pack");
        this.springPack = root.getChild("spring_pack");
        this.stewardPack = root.getChild("steward_pack");
        this.weaponStaff = this.rightArm.getChild("merchant_staff");
        this.weaponSword = this.rightArm.getChild("spirit_sword");
        this.weaponSpear = this.rightArm.getChild("jade_spear");
        this.weaponFan = this.rightArm.getChild("iron_fan");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Refined Xianxia head silhouette: layered hair, topknot + jade pin, optional Elder tall crown & flowing beard
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("hair_cap", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.4F, -8.5F, -4.4F, 8.8F, 3.2F, 8.8F,
                                new CubeDeformation(0.06F)),
                PartPose.ZERO);
        head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-4.0F, -6.5F, 3.4F, 8.0F, 9.5F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        head.addOrReplaceChild("sideburn_left", CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-0.6F, -5.0F, -2.5F, 1.2F, 6.5F, 1.5F),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.06F, 0.0F, -0.06F));
        head.addOrReplaceChild("sideburn_right", CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-0.6F, -5.0F, -2.5F, 1.2F, 6.5F, 1.5F),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.06F, 0.0F, 0.06F));
        PartDefinition hairBun = head.addOrReplaceChild("hair_bun", CubeListBuilder.create()
                        .texOffs(92, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(108, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.5F, -0.14F, 0.0F, 0.0F));
        hairBun.addOrReplaceChild("hairpin", CubeListBuilder.create()
                        .texOffs(108, 6).addBox(-4.2F, -4.0F, -0.5F, 8.4F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.08F));
        head.addOrReplaceChild("elder_crown", CubeListBuilder.create()
                        .texOffs(92, 0).addBox(-2.4F, -5.2F, -2.2F, 4.8F, 5.2F, 4.4F)
                        .texOffs(108, 6).addBox(-5.0F, -4.2F, -0.5F, 10.0F, 1.1F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.2F, 0.2F, -0.06F, 0.0F, 0.0F));
        head.addOrReplaceChild("elder_beard", CubeListBuilder.create()
                        .texOffs(88, 32).addBox(-1.8F, -0.6F, -4.2F, 3.6F, 5.5F, 1.2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.08F, 0.0F, 0.0F));

        // Layered Daoist robe body with standing collar, sash, pauldrons, jade pendant & flowing silk ribbons
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("robe", CubeListBuilder.create()
                        .texOffs(26, 20).addBox(-4.4F, -0.4F, -2.4F, 8.8F, 12.0F, 4.8F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        body.addOrReplaceChild("collar", CubeListBuilder.create()
                        .texOffs(88, 20).addBox(-4.4F, -1.2F, -1.0F, 8.8F, 2.2F, 3.8F),
                PartPose.offset(0.0F, 0.2F, 0.2F));
        body.addOrReplaceChild("lapel_left", CubeListBuilder.create()
                        .texOffs(88, 20).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, 0.0F, 0.0F, -0.34F));
        body.addOrReplaceChild("lapel_right", CubeListBuilder.create()
                        .texOffs(96, 20).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, 0.0F, 0.0F, 0.34F));
        body.addOrReplaceChild("pauldron_left", CubeListBuilder.create()
                        .texOffs(104, 20).addBox(0.0F, -1.0F, -2.8F, 4.2F, 2.2F, 5.6F),
                PartPose.offsetAndRotation(3.5F, 0.4F, 0.0F, 0.0F, 0.0F, 0.20F));
        body.addOrReplaceChild("pauldron_right", CubeListBuilder.create()
                        .texOffs(104, 28).addBox(-4.2F, -1.0F, -2.8F, 4.2F, 2.2F, 5.6F),
                PartPose.offsetAndRotation(-3.5F, 0.4F, 0.0F, 0.0F, 0.0F, -0.20F));
        body.addOrReplaceChild("jade_pendant", CubeListBuilder.create()
                        .texOffs(88, 32).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.2F, 1.0F),
                PartPose.offsetAndRotation(2.4F, 8.5F, -2.1F, -0.08F, 0.0F, -0.14F));
        body.addOrReplaceChild("ribbon_left", CubeListBuilder.create()
                        .texOffs(74, 42).addBox(-0.6F, 0.0F, -0.5F, 1.4F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(4.4F, 1.5F, 2.2F, 0.22F, 0.0F, -0.18F));
        body.addOrReplaceChild("ribbon_right", CubeListBuilder.create()
                        .texOffs(88, 42).addBox(-0.8F, 0.0F, -0.5F, 1.4F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(-4.4F, 1.5F, 2.2F, 0.22F, 0.0F, 0.18F));
        body.addOrReplaceChild("robe_skirt", CubeListBuilder.create()
                        .texOffs(56, 20).addBox(-4.4F, 0.0F, -2.4F, 8.8F, 8.5F, 4.8F,
                                new CubeDeformation(0.03F)),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.04F, 0.0F, 0.0F));
        body.addOrReplaceChild("skirt_left", CubeListBuilder.create()
                        .texOffs(74, 42).addBox(-0.5F, 0.0F, -2.4F, 1.0F, 8.5F, 4.8F),
                PartPose.offsetAndRotation(4.4F, 10.0F, 0.0F, 0.0F, 0.0F, -0.14F));
        body.addOrReplaceChild("skirt_right", CubeListBuilder.create()
                        .texOffs(88, 42).addBox(-0.5F, 0.0F, -2.4F, 1.0F, 8.5F, 4.8F),
                PartPose.offsetAndRotation(-4.4F, 10.0F, 0.0F, 0.0F, 0.0F, 0.14F));

        // Right arm + 4 distinct 3D weapon models (Staff, Spirit Sword, Jade Spear, Iron Fan)
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, -0.08F, 0.0F, 0.08F));
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(32, 42).addBox(-3.4F, -2.0F, -2.4F, 4.8F, 8.0F, 4.8F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("right_cuff", CubeListBuilder.create()
                        .texOffs(72, 60).addBox(-3.4F, 0.0F, -2.4F, 4.0F, 5.0F, 4.8F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.5F, 0.16F, 0.0F, 0.10F));

        // Weapon 1: Daoist Staff with hanging spirit lantern
        PartDefinition staff = rightArm.addOrReplaceChild("merchant_staff", CubeListBuilder.create()
                        .texOffs(36, 104).addBox(-0.8F, -8.0F, -0.8F, 1.6F, 22.0F, 1.6F),
                PartPose.offsetAndRotation(-1.5F, 8.0F, -1.5F, 0.24F, 0.0F, 0.0F));
        staff.addOrReplaceChild("lantern", CubeListBuilder.create()
                        .texOffs(46, 104).addBox(-1.8F, 0.0F, -1.8F, 3.6F, 5.2F, 3.6F),
                PartPose.offsetAndRotation(0.0F, -7.2F, -2.2F, -0.24F, 0.0F, 0.0F));

        // Weapon 2: 3D Spirit Sword (grip, wide cloud guard, tapered blade, tip)
        rightArm.addOrReplaceChild("spirit_sword", CubeListBuilder.create()
                        .texOffs(36, 104).addBox(-0.6F, -1.0F, -1.0F, 1.2F, 3.8F, 1.2F)
                        .texOffs(108, 6).addBox(-2.4F, -2.0F, -1.4F, 4.8F, 1.2F, 2.0F)
                        .texOffs(112, 80).addBox(-0.9F, -14.5F, -0.7F, 1.8F, 12.8F, 0.8F)
                        .texOffs(112, 80).addBox(-0.5F, -17.0F, -0.6F, 1.0F, 2.6F, 0.6F),
                PartPose.offsetAndRotation(-1.4F, 9.2F, -2.0F, 1.38F, 0.0F, 0.0F));

        // Weapon 3: 3D Jade Spear (long shaft, crimson tassel collar, spearhead)
        rightArm.addOrReplaceChild("jade_spear", CubeListBuilder.create()
                        .texOffs(36, 104).addBox(-0.7F, -14.0F, -0.7F, 1.4F, 28.0F, 1.4F)
                        .texOffs(46, 104).addBox(-1.4F, -15.5F, -1.4F, 2.8F, 2.2F, 2.8F)
                        .texOffs(112, 80).addBox(-1.1F, -21.5F, -0.7F, 2.2F, 6.2F, 1.4F),
                PartPose.offsetAndRotation(-1.5F, 8.8F, -1.8F, 1.12F, 0.0F, 0.0F));

        // Weapon 4: 3D Iron Battle Fan (ribbed folding fan)
        rightArm.addOrReplaceChild("iron_fan", CubeListBuilder.create()
                        .texOffs(36, 104).addBox(-0.6F, -1.2F, -0.5F, 1.2F, 3.2F, 1.0F)
                        .texOffs(28, 104).addBox(-4.2F, -7.8F, -0.5F, 8.4F, 6.8F, 0.9F)
                        .texOffs(108, 6).addBox(-4.6F, -8.2F, -0.6F, 9.2F, 1.2F, 1.1F),
                PartPose.offsetAndRotation(-1.4F, 9.0F, -2.0F, 1.18F, 0.18F, 0.0F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(16, 42).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, -0.08F, 0.0F, -0.08F));
        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(52, 42).addBox(-1.4F, -2.0F, -2.4F, 4.8F, 8.0F, 4.8F,
                                new CubeDeformation(0.04F)),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("left_cuff", CubeListBuilder.create()
                        .texOffs(92, 60).addBox(-0.5F, 0.0F, -2.4F, 4.0F, 5.0F, 4.8F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.5F, 0.16F, 0.0F, -0.10F));

        // Grounded slight outward stance on boots (never pigeon-toed!)
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-2.1F, 12.0F, 0.0F, 0.0F, 0.06F, 0.0F));
        rightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create()
                        .texOffs(32, 60).addBox(-2.4F, -1.0F, -2.4F, 4.8F, 5.0F, 4.8F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(2.1F, 12.0F, 0.0F, 0.0F, -0.06F, 0.0F));
        leftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create()
                        .texOffs(52, 60).addBox(-2.4F, -1.0F, -2.4F, 4.8F, 5.0F, 4.8F),
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
            this.rightArm.xRot += -0.14F + gesture * 0.05F + breath * 0.03F;
            this.leftArm.xRot += -0.10F - gesture * 0.04F + breath * 0.03F;
        } else {
            this.rightArm.xRot += this.leftLeg.xRot * 0.60F - 0.12F;
            this.leftArm.xRot += this.rightLeg.xRot * 0.60F;
        }

        if (state.combatPose == CultivationSettlerEntity.POSE_BURST) {
            float pulse = 0.65F + 0.35F * Mth.sin(state.ageInTicks * 0.42F);
            this.rightArm.xRot = -2.05F - pulse * 0.18F;
            this.leftArm.xRot = -1.95F - pulse * 0.18F;
            this.rightArm.zRot = 0.48F * pulse;
            this.leftArm.zRot = -0.48F * pulse;
            this.body.xRot -= 0.12F * pulse;
        } else if (state.combatPose == CultivationSettlerEntity.POSE_CAST) {
            float castPulse = state.attackTime > 0.0F
                    ? Mth.sin(state.attackTime * Mth.PI)
                    : 0.65F + 0.35F * Mth.sin(state.ageInTicks * 0.35F);
            this.rightArm.xRot = -1.35F - castPulse * 0.28F;
            this.leftArm.xRot = -1.25F - castPulse * 0.24F;
            this.rightArm.yRot = -0.32F + castPulse * 0.45F;
            this.leftArm.yRot = 0.32F - castPulse * 0.45F;
            this.rightArm.zRot = 0.22F * castPulse;
            this.leftArm.zRot = -0.22F * castPulse;
            this.body.xRot -= 0.06F * castPulse;
        } else if (state.attackTime > 0.0F || state.combatPose == CultivationSettlerEntity.POSE_MELEE) {
            float swing = state.attackTime > 0.0F
                    ? Mth.sin(Mth.sqrt(state.attackTime) * Mth.TWO_PI)
                    : Mth.sin(state.ageInTicks * 0.45F) * 0.5F;
            float thrust = state.attackTime > 0.0F
                    ? Mth.sin(state.attackTime * Mth.PI)
                    : 0.7F;
            this.body.yRot += swing * 0.24F;
            this.rightArm.xRot = -1.52F * thrust + swing * 0.35F;
            this.rightArm.yRot += this.body.yRot * 1.4F;
            this.rightArm.zRot += Mth.sin(state.attackTime * Mth.PI) * -0.32F;
            this.leftArm.xRot -= thrust * 0.45F;
            this.leftArm.yRot += 0.25F * thrust;
        }

        this.body.xRot += breath * 0.012F + hurt * 0.06F;
        this.body.yRot += Mth.sin(state.ageInTicks * 0.03F) * 0.015F;
        this.head.zRot += hurt * 0.14F;
        this.jadePendant.zRot += Mth.sin(state.ageInTicks * 0.09F) * 0.06F;

        float ribbonWave = Mth.sin(state.ageInTicks * 0.16F) * (0.12F + speed * 0.22F);
        this.ribbonLeft.xRot += 0.12F + speed * 0.45F + ribbonWave;
        this.ribbonRight.xRot += 0.12F + speed * 0.45F - ribbonWave;

        boolean isElder = state.role == CultivationSettlerEntity.Role.SECT_ELDER.id();
        boolean isSteward = state.role == CultivationSettlerEntity.Role.SECT_STEWARD.id();
        this.elderCrown.visible = isElder;
        this.elderBeard.visible = isElder;
        this.hairBun.visible = !isElder;
        this.ribbonLeft.visible = isElder || isSteward || state.role == CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER.id();
        this.ribbonRight.visible = this.ribbonLeft.visible;

        this.merchantPack.visible = state.role == CultivationSettlerEntity.Role.WANDERING_MERCHANT.id();
        this.springPack.visible = state.role == CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER.id();
        this.stewardPack.visible = isSteward || isElder;

        this.weaponSword.visible = state.weapon == CultivationSettlerEntity.WeaponStyle.SPIRIT_SWORD.id();
        this.weaponSpear.visible = state.weapon == CultivationSettlerEntity.WeaponStyle.JADE_SPEAR.id();
        this.weaponStaff.visible = state.weapon == CultivationSettlerEntity.WeaponStyle.DAOIST_STAFF.id();
        this.weaponFan.visible = state.weapon == CultivationSettlerEntity.WeaponStyle.IRON_FAN.id();
    }
}
