package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.AstralSovereignRenderState;
import com.xxsx.xuanhuanearth.entity.AstralSovereignEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * Unified Phase 1 &amp; Phase 2 EntityModel for Astral Sovereign (镇岳星君).
 *
 * <p>Preserves all 107 rotated Blockbench sub-parts and drives full-body articulated motion
 * across hips, chest, shoulders, forearms, halberd wrist, knees/shins, cape, star halos,
 * and gated VFX layers. {@code vfx_slash_arc} is strictly bound to melee combos and never
 * displayed during Eight-Wastes Astral Judgment (which expands {@code vfx_magic_circle}
 * as a 360-degree circular shockwave) or Sword Array Empower.</p>
 */
public final class AstralSovereignModel extends EntityModel<AstralSovereignRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(XuanhuanEarth.id("astral_sovereign"), "main");

    private final ModelPart sovereignRoot;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart haloInner;
    private final ModelPart haloOuter;
    private final ModelPart haloTier3;
    private final ModelPart astralWings;
    private final ModelPart tabardFront;
    private final ModelPart tassetLeft;
    private final ModelPart tassetRight;
    private final ModelPart capeRoot;
    private final ModelPart capeMid;
    private final ModelPart capeLower;
    private final ModelPart faceThirdEye;
    private final ModelPart p2CrownCrest;
    private final ModelPart p2ChestCore;
    private final ModelPart p2PauldronL;
    private final ModelPart p2PauldronR;
    private final ModelPart p2HalberdEdge;
    private final ModelPart halberdArm;
    private final ModelPart halberdForearm;
    private final ModelPart astralHalberd;
    private final ModelPart offHandArm;
    private final ModelPart offHandForearm;
    private final ModelPart rightLeg;
    private final ModelPart rightShin;
    private final ModelPart leftLeg;
    private final ModelPart leftShin;
    private final ModelPart orbitingSwordsRoot;
    private final ModelPart vfxMagicCircle;
    private final ModelPart vfxSlashArc;
    private final ModelPart vfxAuraPillars;
    private final ModelPart vfxWeaponAura;
    private final ModelPart vfxBodyAura;

    public AstralSovereignModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.sovereignRoot = root.getChild("sovereign_root");
        this.body = this.sovereignRoot.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.haloInner = this.chest.getChild("halo_inner");
        this.haloOuter = this.chest.getChild("halo_outer");
        this.haloTier3 = this.chest.getChild("halo_tier3");
        this.astralWings = this.chest.getChild("astral_wings");
        this.tabardFront = this.body.getChild("tabard_front");
        this.tassetLeft = this.body.getChild("tasset_left");
        this.tassetRight = this.body.getChild("tasset_right");
        this.capeRoot = this.chest.getChild("cape_root");
        this.capeMid = this.capeRoot.getChild("cape_mid");
        this.capeLower = this.capeMid.getChild("cape_lower");
        this.faceThirdEye = this.head.getChild("face_third_eye");
        this.p2CrownCrest = this.head.getChild("p2_crown_crest");
        this.p2ChestCore = this.chest.getChild("p2_chest_core");
        this.halberdArm = this.chest.getChild("left_arm");
        this.p2PauldronL = this.halberdArm.getChild("p2_pauldron_wing_l");
        this.halberdForearm = this.halberdArm.getChild("left_forearm");
        this.astralHalberd = this.halberdForearm.getChild("astral_halberd");
        this.p2HalberdEdge = this.astralHalberd.getChild("p2_halberd_edge");
        this.vfxWeaponAura = this.astralHalberd.getChild("vfx_weapon_aura");
        this.vfxBodyAura = this.chest.getChild("vfx_body_aura");
        this.offHandArm = this.chest.getChild("right_arm");
        this.p2PauldronR = this.offHandArm.getChild("p2_pauldron_wing_r");
        this.offHandForearm = this.offHandArm.getChild("right_forearm");
        this.rightLeg = this.sovereignRoot.getChild("right_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftLeg = this.sovereignRoot.getChild("left_leg");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.orbitingSwordsRoot = this.sovereignRoot.getChild("orbiting_swords_root");
        this.vfxMagicCircle = this.sovereignRoot.getChild("vfx_magic_circle");
        this.vfxSlashArc = this.sovereignRoot.getChild("vfx_slash_arc");
        this.vfxAuraPillars = this.sovereignRoot.getChild("vfx_aura_pillars");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition sovereign_root = root.addOrReplaceChild("sovereign_root", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, 24.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition body = sovereign_root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.60F, -0.50F, -3.60F, 13.20F, 3.50F, 7.20F)
            .texOffs(41, 0).addBox(-2.40F, 0.80F, -4.40F, 4.80F, 4.00F, 1.40F)
            .texOffs(54, 0).addBox(-5.20F, -10.00F, -3.40F, 10.40F, 10.00F, 6.80F)
            .texOffs(89, 0).addBox(-4.60F, -4.60F, -4.10F, 9.20F, 2.60F, 1.30F)
            .texOffs(110, 0).addBox(-5.00F, -7.80F, -4.30F, 10.00F, 2.80F, 1.40F)
            .texOffs(133, 0).addBox(4.40F, -8.50F, -2.50F, 1.40F, 7.00F, 5.00F)
            .texOffs(146, 0).addBox(-5.80F, -8.50F, -2.50F, 1.40F, 7.00F, 5.00F)
            .texOffs(159, 0).addBox(-6.80F, -1.20F, -4.20F, 13.60F, 2.70F, 8.40F)
            .texOffs(204, 0).addBox(-2.40F, -1.60F, -5.00F, 4.80F, 3.60F, 1.40F)
            .texOffs(217, 0).addBox(-1.40F, -0.80F, -5.40F, 2.80F, 2.00F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, -23.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition tabard_front = body.addOrReplaceChild("tabard_front", CubeListBuilder.create()
            .texOffs(226, 0).addBox(-2.80F, 0.00F, -0.75F, 5.60F, 12.20F, 1.10F)
            .texOffs(241, 0).addBox(-3.40F, 0.00F, -0.95F, 1.20F, 9.80F, 1.10F)
            .texOffs(246, 0).addBox(2.20F, 0.00F, -0.95F, 1.20F, 9.80F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, 1.60F, -4.45F, -0.24F, 0.0000F, 0.0000F));
        PartDefinition tasset_left = body.addOrReplaceChild("tasset_left", CubeListBuilder.create()
            .texOffs(251, 0).addBox(-0.40F, 0.00F, -3.80F, 1.40F, 6.50F, 7.60F)
            , PartPose.offsetAndRotation(8.10F, 1.00F, 0.00F, 0.0000F, 0.0000F, 0.34F));
        tasset_left.addOrReplaceChild("tasset_l_lower", CubeListBuilder.create().texOffs(270, 0).addBox(0.30F, 5.80F, -3.20F, 1.20F, 5.80F, 6.40F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, 0.0000F, 0.0000F, 0.14F));
        PartDefinition tasset_right = body.addOrReplaceChild("tasset_right", CubeListBuilder.create()
            .texOffs(285, 0).addBox(-1.00F, 0.00F, -3.80F, 1.40F, 6.50F, 7.60F)
            , PartPose.offsetAndRotation(-8.10F, 1.00F, 0.00F, 0.0000F, 0.0000F, -0.34F));
        tasset_right.addOrReplaceChild("tasset_r_lower", CubeListBuilder.create().texOffs(304, 0).addBox(-1.50F, 5.80F, -3.20F, 1.20F, 5.80F, 6.40F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, 0.0000F, 0.0000F, -0.14F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
            .texOffs(319, 0).addBox(-8.00F, -12.00F, -4.60F, 16.00F, 11.50F, 9.20F)
            .texOffs(46, 22).addBox(-5.50F, -11.00F, 4.20F, 11.00F, 9.00F, 2.80F)
            .texOffs(128, 0).addBox(-7.40F, -10.60F, -5.60F, 14.80F, 8.80F, 1.60F)
            .texOffs(416, 0).addBox(-7.60F, -11.30F, -5.80F, 15.20F, 1.10F, 1.90F)
            .texOffs(416, 0).addBox(-6.80F, -2.30F, -5.80F, 13.60F, 1.10F, 1.90F)
            .texOffs(384, 0).addBox(2.20F, -12.30F, -5.10F, 4.00F, 1.50F, 2.70F)
            .texOffs(384, 0).addBox(-6.20F, -12.30F, -5.10F, 4.00F, 1.50F, 2.70F)
            .texOffs(416, 0).addBox(-0.90F, -10.60F, -6.00F, 1.80F, 8.80F, 1.00F)
            .texOffs(416, 0).addBox(-2.60F, -8.40F, -6.20F, 5.20F, 4.00F, 1.10F)
            .texOffs(384, 0).addBox(3.60F, -8.20F, -5.90F, 2.40F, 3.60F, 0.90F)
            .texOffs(416, 0).addBox(4.20F, -7.40F, -6.10F, 1.20F, 2.00F, 0.90F)
            .texOffs(384, 0).addBox(-6.00F, -8.20F, -5.90F, 2.40F, 3.60F, 0.90F)
            .texOffs(416, 0).addBox(-5.40F, -7.40F, -6.10F, 1.20F, 2.00F, 0.90F)
            , PartPose.offsetAndRotation(0.00F, -9.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        chest.addOrReplaceChild("gorget_back", CubeListBuilder.create().texOffs(481, 0).addBox(-6.20F, -4.00F, -1.80F, 12.40F, 4.50F, 3.00F), PartPose.offsetAndRotation(0.00F, -12.00F, 4.00F, 0.2443F, -0.0000F, 0.0000F));
        chest.addOrReplaceChild("back_reactor_gem", CubeListBuilder.create().texOffs(75, 22).addBox(-2.20F, -2.60F, -0.60F, 4.40F, 4.40F, 1.40F), PartPose.offsetAndRotation(0.00F, -6.00F, 6.80F, -0.0000F, -0.0000F, 0.7854F));
        chest.addOrReplaceChild("halo_strut_l", CubeListBuilder.create().texOffs(86, 22).addBox(-0.90F, -4.50F, -1.00F, 1.80F, 7.00F, 2.00F), PartPose.offsetAndRotation(3.70F, -10.00F, 6.80F, 0.2793F, -0.0000F, -0.2443F));
        chest.addOrReplaceChild("halo_strut_r", CubeListBuilder.create().texOffs(95, 22).addBox(-0.90F, -4.50F, -1.00F, 1.80F, 7.00F, 2.00F), PartPose.offsetAndRotation(-3.70F, -10.00F, 6.80F, 0.2793F, -0.0000F, 0.2443F));
        chest.addOrReplaceChild("cuirass_side_l", CubeListBuilder.create().texOffs(384, 0).addBox(-0.70F, -3.90F, -1.40F, 1.50F, 7.90F, 2.90F), PartPose.offsetAndRotation(7.50F, -6.50F, -4.00F, -0.0000F, 0.2443F, 0.0000F));
        chest.addOrReplaceChild("cuirass_side_r", CubeListBuilder.create().texOffs(384, 0).addBox(-0.80F, -3.90F, -1.40F, 1.50F, 7.90F, 2.90F), PartPose.offsetAndRotation(-7.50F, -6.50F, -4.00F, -0.0000F, -0.2443F, 0.0000F));
        chest.addOrReplaceChild("heart_bezel_b", CubeListBuilder.create().texOffs(416, 0).addBox(-2.60F, -2.00F, -0.50F, 5.20F, 4.00F, 0.90F), PartPose.offsetAndRotation(0.00F, -6.40F, -5.60F, -0.0000F, -0.0000F, 0.7854F));
        chest.addOrReplaceChild("heart_core_gem", CubeListBuilder.create().texOffs(448, 0).addBox(-1.70F, -1.70F, -0.70F, 3.40F, 3.40F, 1.00F), PartPose.offsetAndRotation(0.00F, -6.40F, -5.80F, -0.0000F, -0.0000F, 0.7854F));
        PartDefinition astral_wings = chest.addOrReplaceChild("astral_wings", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -10.00F, 7.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition sword_r1 = astral_wings.addOrReplaceChild("sword_r1", CubeListBuilder.create()
            .texOffs(396, 63).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(405, 63).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(418, 63).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(427, 63).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(-20.00F, -10.00F, -0.50F, -0.0000F, -0.0000F, 0.4189F));
        PartDefinition sword_l1 = astral_wings.addOrReplaceChild("sword_l1", CubeListBuilder.create()
            .texOffs(434, 63).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(443, 63).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(456, 63).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(465, 63).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(20.00F, -10.00F, -0.50F, -0.0000F, -0.0000F, -0.4189F));
        PartDefinition sword_r2 = astral_wings.addOrReplaceChild("sword_r2", CubeListBuilder.create()
            .texOffs(472, 63).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(481, 63).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(494, 63).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(503, 63).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(-25.00F, 0.00F, -1.00F, -0.0000F, -0.0000F, 0.6981F));
        PartDefinition sword_l2 = astral_wings.addOrReplaceChild("sword_l2", CubeListBuilder.create()
            .texOffs(0, 79).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(9, 79).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(22, 79).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(31, 79).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(25.00F, 0.00F, -1.00F, -0.0000F, -0.0000F, -0.6981F));
        PartDefinition sword_r3 = astral_wings.addOrReplaceChild("sword_r3", CubeListBuilder.create()
            .texOffs(38, 79).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(47, 79).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(60, 79).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(69, 79).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(-23.00F, 11.00F, -1.50F, -0.0000F, -0.0000F, 0.9774F));
        PartDefinition sword_l3 = astral_wings.addOrReplaceChild("sword_l3", CubeListBuilder.create()
            .texOffs(76, 79).addBox(-0.80F, 6.50F, -0.80F, 1.60F, 2.00F, 1.60F)
            .texOffs(85, 79).addBox(-2.20F, 5.20F, -0.90F, 4.40F, 1.30F, 1.80F)
            .texOffs(98, 79).addBox(-1.30F, -7.00F, -0.60F, 2.60F, 12.20F, 1.20F)
            .texOffs(107, 79).addBox(-0.60F, -8.50F, -0.80F, 1.20F, 12.70F, 1.60F)
            , PartPose.offsetAndRotation(23.00F, 11.00F, -1.50F, -0.0000F, -0.0000F, -0.9774F));
        PartDefinition cape_root = chest.addOrReplaceChild("cape_root", CubeListBuilder.create()
            .texOffs(104, 22).addBox(-4.00F, 0.00F, 0.00F, 8.00F, 12.50F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, -10.00F, 4.70F, 0.18F, 0.0000F, 0.0000F));
        cape_root.addOrReplaceChild("cape_top_l", CubeListBuilder.create().texOffs(123, 22).addBox(-2.10F, 0.00F, -0.20F, 5.80F, 13.00F, 1.15F), PartPose.offsetAndRotation(5.40F, 0.00F, -0.10F, 0.03F, 0.08F, 0.10F));
        cape_root.addOrReplaceChild("cape_top_r", CubeListBuilder.create().texOffs(138, 22).addBox(-3.70F, 0.00F, -0.20F, 5.80F, 13.00F, 1.15F), PartPose.offsetAndRotation(-5.40F, 0.00F, -0.10F, 0.03F, -0.08F, -0.10F));
        PartDefinition cape_mid = cape_root.addOrReplaceChild("cape_mid", CubeListBuilder.create()
            .texOffs(104, 34).addBox(-4.20F, 0.00F, -0.10F, 8.40F, 12.50F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, 12.20F, 0.10F, 0.08F, 0.0000F, 0.0000F));
        cape_mid.addOrReplaceChild("cape_mid_l", CubeListBuilder.create().texOffs(123, 35).addBox(-2.20F, 0.00F, -0.20F, 6.20F, 13.00F, 1.05F), PartPose.offsetAndRotation(5.90F, 0.00F, -0.10F, 0.02F, 0.08F, 0.08F));
        cape_mid.addOrReplaceChild("cape_mid_r", CubeListBuilder.create().texOffs(138, 35).addBox(-4.00F, 0.00F, -0.20F, 6.20F, 13.00F, 1.05F), PartPose.offsetAndRotation(-5.90F, 0.00F, -0.10F, 0.02F, -0.08F, -0.08F));
        PartDefinition cape_lower = cape_mid.addOrReplaceChild("cape_lower", CubeListBuilder.create()
            .texOffs(104, 46).addBox(-4.40F, 0.00F, -0.10F, 8.80F, 12.50F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, 12.20F, 0.00F, 0.10F, 0.0000F, 0.0000F));
        cape_lower.addOrReplaceChild("cape_bot_l", CubeListBuilder.create().texOffs(123, 48).addBox(-2.30F, 0.00F, -0.20F, 6.60F, 13.00F, 0.95F), PartPose.offsetAndRotation(6.30F, 0.00F, -0.10F, 0.02F, 0.08F, 0.08F));
        cape_lower.addOrReplaceChild("cape_bot_r", CubeListBuilder.create().texOffs(138, 48).addBox(-4.30F, 0.00F, -0.20F, 6.60F, 13.00F, 0.95F), PartPose.offsetAndRotation(-6.30F, 0.00F, -0.10F, 0.02F, -0.08F, -0.08F));
        PartDefinition halo_outer = chest.addOrReplaceChild("halo_outer", CubeListBuilder.create()
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 17.80F, -0.0000F, -0.0000F, 0.0000F));
        halo_outer.addOrReplaceChild("halo_out_1", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.5236F));
        halo_outer.addOrReplaceChild("halo_out_2", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.0472F));
        halo_outer.addOrReplaceChild("halo_ray_2", CubeListBuilder.create().texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.0472F));
        halo_outer.addOrReplaceChild("halo_out_3", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.5708F));
        halo_outer.addOrReplaceChild("halo_out_4", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.0944F));
        halo_outer.addOrReplaceChild("halo_ray_4", CubeListBuilder.create().texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.0944F));
        halo_outer.addOrReplaceChild("halo_out_5", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.6180F));
        halo_outer.addOrReplaceChild("halo_out_6", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.1416F));
        halo_outer.addOrReplaceChild("halo_ray_6", CubeListBuilder.create().texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.1416F));
        halo_outer.addOrReplaceChild("halo_out_7", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.6652F));
        halo_outer.addOrReplaceChild("halo_out_8", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.1888F));
        halo_outer.addOrReplaceChild("halo_ray_8", CubeListBuilder.create().texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.1888F));
        halo_outer.addOrReplaceChild("halo_out_9", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.7124F));
        halo_outer.addOrReplaceChild("halo_out_10", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.2360F));
        halo_outer.addOrReplaceChild("halo_ray_10", CubeListBuilder.create().texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.2360F));
        halo_outer.addOrReplaceChild("halo_out_11", CubeListBuilder.create().texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.7596F));
        PartDefinition halo_inner = chest.addOrReplaceChild("halo_inner", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 15.60F, -0.0000F, -0.0000F, 0.0000F));
        halo_inner.addOrReplaceChild("halo_in_1", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.7854F));
        halo_inner.addOrReplaceChild("halo_in_2", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.5708F));
        halo_inner.addOrReplaceChild("halo_in_3", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.3562F));
        halo_inner.addOrReplaceChild("halo_in_4", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.1416F));
        halo_inner.addOrReplaceChild("halo_in_5", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.9270F));
        halo_inner.addOrReplaceChild("halo_in_6", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.7124F));
        halo_inner.addOrReplaceChild("halo_in_7", CubeListBuilder.create().texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.4978F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(384, 0).addBox(-2.50F, -0.40F, -2.30F, 5.00F, 3.20F, 4.60F)
            .texOffs(384, 0).addBox(-4.20F, -8.40F, -4.20F, 8.40F, 8.40F, 8.40F)
            .texOffs(384, 0).addBox(-4.60F, -8.80F, -4.60F, 9.20F, 2.60F, 9.20F)
            .texOffs(416, 0).addBox(-4.80F, -7.40F, -4.80F, 9.60F, 1.60F, 9.60F)
            .texOffs(448, 0).addBox(-1.20F, -8.20F, -5.20F, 2.40F, 2.20F, 1.00F)
            .texOffs(416, 0).addBox(-2.80F, -11.40F, -2.80F, 5.60F, 2.80F, 5.60F)
            .texOffs(416, 0).addBox(-1.50F, -15.00F, -2.20F, 3.00F, 3.80F, 4.00F)
            .texOffs(448, 0).addBox(-1.00F, -13.80F, -2.60F, 2.00F, 2.20F, 1.00F)
            .texOffs(416, 0).addBox(-0.55F, -6.30F, -5.10F, 1.10F, 2.20F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -13.80F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_neck_guard_l", CubeListBuilder.create().texOffs(384, 0).addBox(-0.60F, -0.50F, -3.20F, 1.20F, 7.50F, 6.60F), PartPose.offsetAndRotation(4.40F, -5.70F, 1.00F, -0.0000F, -0.0000F, 0.2094F));
        head.addOrReplaceChild("wg_neck_guard_r", CubeListBuilder.create().texOffs(384, 0).addBox(-0.60F, -0.50F, -3.20F, 1.20F, 7.50F, 6.60F), PartPose.offsetAndRotation(-4.40F, -5.70F, 1.00F, -0.0000F, -0.0000F, -0.2094F));
        head.addOrReplaceChild("wg_neck_guard_b", CubeListBuilder.create().texOffs(384, 0).addBox(-4.40F, -0.50F, -0.60F, 8.80F, 8.00F, 1.40F), PartPose.offsetAndRotation(0.00F, -5.70F, 4.20F, 0.2094F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_wing_l", CubeListBuilder.create().texOffs(416, 0).addBox(-0.20F, -5.70F, -1.50F, 1.20F, 7.00F, 3.50F), PartPose.offsetAndRotation(4.60F, -6.70F, 0.00F, 0.2793F, -0.0000F, -0.3142F));
        head.addOrReplaceChild("wg_wing_r", CubeListBuilder.create().texOffs(416, 0).addBox(-1.00F, -5.70F, -1.50F, 1.20F, 7.00F, 3.50F), PartPose.offsetAndRotation(-4.60F, -6.70F, 0.00F, 0.2793F, -0.0000F, 0.3142F));
        // Sleek 3D curved crimson-gold pheasant-tail warrior plumes (no flat UV(0,0) billboard rectangles!)
        PartDefinition wg_plume_l = head.addOrReplaceChild("wg_plume_l", CubeListBuilder.create()
            .texOffs(416, 0).addBox(0.00F, -7.20F, -0.50F, 1.30F, 7.60F, 1.00F)
            .texOffs(480, 0).addBox(0.80F, -9.00F, -0.20F, 4.80F, 2.20F, 0.90F)
            .texOffs(416, 32).addBox(5.10F, -9.80F, 0.20F, 5.20F, 1.50F, 0.75F)
            .texOffs(480, 0).addBox(9.70F, -8.70F, 0.60F, 4.80F, 1.10F, 0.60F)
            , PartPose.offsetAndRotation(1.40F, -12.20F, 0.80F, 0.22F, 0.42F, 0.14F));
        PartDefinition wg_plume_r = head.addOrReplaceChild("wg_plume_r", CubeListBuilder.create()
            .texOffs(416, 0).addBox(-1.30F, -7.20F, -0.50F, 1.30F, 7.60F, 1.00F)
            .texOffs(480, 0).addBox(-5.60F, -9.00F, -0.20F, 4.80F, 2.20F, 0.90F)
            .texOffs(416, 32).addBox(-10.30F, -9.80F, 0.20F, 5.20F, 1.50F, 0.75F)
            .texOffs(480, 0).addBox(-14.50F, -8.70F, 0.60F, 4.80F, 1.10F, 0.60F)
            , PartPose.offsetAndRotation(-1.40F, -12.20F, 0.80F, 0.22F, -0.42F, -0.14F));
        head.addOrReplaceChild("wg_tassel", CubeListBuilder.create().texOffs(480, 0).addBox(-1.50F, -0.50F, -0.20F, 3.00F, 10.00F, 1.60F), PartPose.offsetAndRotation(0.00F, -8.20F, 4.80F, 0.2793F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_brow_l", CubeListBuilder.create().texOffs(384, 0).addBox(0.00F, -1.30F, -0.55F, 4.50F, 1.30F, 1.15F), PartPose.offsetAndRotation(0.00F, -4.40F, -4.40F, -0.0000F, 0.1396F, 0.2094F));
        head.addOrReplaceChild("wg_brow_r", CubeListBuilder.create().texOffs(384, 0).addBox(-4.50F, -1.30F, -0.55F, 4.50F, 1.30F, 1.15F), PartPose.offsetAndRotation(0.00F, -4.40F, -4.40F, -0.0000F, -0.1396F, -0.2094F));
        head.addOrReplaceChild("wg_cheek_l", CubeListBuilder.create().texOffs(384, 0).addBox(-1.20F, -0.70F, -1.00F, 2.60F, 2.40F, 2.20F), PartPose.offsetAndRotation(3.00F, -2.00F, -3.80F, -0.0000F, 0.1745F, 0.0000F));
        head.addOrReplaceChild("wg_cheek_r", CubeListBuilder.create().texOffs(384, 0).addBox(-1.40F, -0.70F, -1.00F, 2.60F, 2.40F, 2.20F), PartPose.offsetAndRotation(-3.00F, -2.00F, -3.80F, -0.0000F, -0.1745F, 0.0000F));
        head.addOrReplaceChild("wg_mask_jaw", CubeListBuilder.create().texOffs(384, 0).addBox(-2.10F, -0.00F, -0.60F, 4.20F, 2.40F, 1.10F), PartPose.offsetAndRotation(0.00F, -2.30F, -4.30F, -0.1396F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_mask_chin_trim", CubeListBuilder.create().texOffs(416, 0).addBox(-1.50F, -0.50F, -0.55F, 3.00F, 0.90F, 1.05F), PartPose.offsetAndRotation(0.00F, -0.00F, -4.50F, -0.1396F, -0.0000F, 0.0000F));
        // Sculpted celestial martial face details (3D angled luminous eyes, sculpted nose bridge, forehead star-seal)
        head.addOrReplaceChild("face_nose_bridge", CubeListBuilder.create().texOffs(416, 0).addBox(-0.55F, -4.40F, -4.75F, 1.10F, 2.30F, 0.95F), PartPose.ZERO);
        head.addOrReplaceChild("face_eye_l", CubeListBuilder.create().texOffs(448, 32).addBox(0.85F, -0.45F, -0.35F, 2.15F, 0.85F, 0.50F), PartPose.offsetAndRotation(0.20F, -3.55F, -4.35F, 0.0F, 0.08F, -0.16F));
        head.addOrReplaceChild("face_eye_r", CubeListBuilder.create().texOffs(448, 32).addBox(-3.00F, -0.45F, -0.35F, 2.15F, 0.85F, 0.50F), PartPose.offsetAndRotation(-0.20F, -3.55F, -4.35F, 0.0F, -0.08F, 0.16F));
        head.addOrReplaceChild("face_third_eye", CubeListBuilder.create().texOffs(384, 32).addBox(-0.75F, -0.75F, -0.45F, 1.50F, 1.50F, 0.65F), PartPose.offsetAndRotation(0.00F, -5.65F, -4.85F, 0.0F, 0.0F, 0.7854F));
        // Phase 2 physical crown evolution (blazing celestial war-god crown horns & astral crest)
        PartDefinition p2_crown_crest = head.addOrReplaceChild("p2_crown_crest", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.90F, -19.50F, -2.50F, 1.80F, 6.20F, 1.40F)
            , PartPose.ZERO);
        p2_crown_crest.addOrReplaceChild("p2_horn_l", CubeListBuilder.create().texOffs(416, 32).addBox(0.00F, -7.80F, -1.00F, 1.70F, 8.60F, 1.80F), PartPose.offsetAndRotation(4.60F, -7.60F, -1.20F, -0.26F, -0.18F, 0.44F));
        p2_crown_crest.addOrReplaceChild("p2_horn_r", CubeListBuilder.create().texOffs(416, 32).addBox(-1.70F, -7.80F, -1.00F, 1.70F, 8.60F, 1.80F), PartPose.offsetAndRotation(-4.60F, -7.60F, -1.20F, -0.26F, 0.18F, -0.44F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(239, 79).addBox(-3.60F, 0.50F, -2.60F, 4.80F, 10.30F, 5.20F)
            , PartPose.offsetAndRotation(-11.00F, -10.00F, 0.00F, -0.1745F, -0.0000F, -0.3142F));
        right_arm.addOrReplaceChild("rpaul_top", CubeListBuilder.create().texOffs(114, 79).addBox(-8.60F, -2.20F, -5.20F, 9.80F, 4.70F, 10.40F), PartPose.offsetAndRotation(2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, 0.2443F));
        right_arm.addOrReplaceChild("rpaul_mid", CubeListBuilder.create().texOffs(155, 79).addBox(-7.60F, -0.00F, -4.60F, 8.00F, 4.20F, 9.20F), PartPose.offsetAndRotation(2.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, 0.1396F));
        right_arm.addOrReplaceChild("rpaul_low", CubeListBuilder.create().texOffs(190, 79).addBox(-6.60F, 0.80F, -4.00F, 6.40F, 3.60F, 8.00F), PartPose.offsetAndRotation(2.00F, 4.00F, 0.00F, -0.0000F, -0.0000F, 0.0698F));
        right_arm.addOrReplaceChild("rpaul_crest", CubeListBuilder.create().texOffs(219, 79).addBox(-1.40F, -5.00F, -1.20F, 2.60F, 5.50F, 2.40F), PartPose.offsetAndRotation(-5.00F, -2.50F, 0.00F, -0.0000F, -0.0000F, 0.3142F));
        right_arm.addOrReplaceChild("rpaul_gem", CubeListBuilder.create().texOffs(230, 79).addBox(-6.80F, -0.80F, -5.60F, 3.20F, 2.20F, 1.20F), PartPose.offsetAndRotation(2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, 0.2443F));
        PartDefinition p2_pauldron_wing_r = right_arm.addOrReplaceChild("p2_pauldron_wing_r", CubeListBuilder.create()
            .texOffs(416, 32).addBox(-1.60F, -9.80F, -1.80F, 2.40F, 10.20F, 3.60F)
            .texOffs(416, 0).addBox(-2.20F, -5.20F, -2.20F, 3.20F, 3.20F, 4.40F)
            , PartPose.offsetAndRotation(-6.60F, -2.20F, 0.20F, -0.18F, 0.00F, -0.48F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
            .texOffs(260, 79).addBox(-2.50F, -0.50F, -3.00F, 5.20F, 10.50F, 6.00F)
            .texOffs(283, 79).addBox(-3.50F, -0.00F, -1.20F, 1.40F, 7.50F, 3.00F)
            .texOffs(292, 79).addBox(-1.90F, 8.70F, -2.50F, 4.00F, 2.80F, 5.00F)
            , PartPose.offsetAndRotation(-1.50F, 10.50F, 0.00F, -0.4887F, -0.0000F, 0.1745F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(84, 126).addBox(-1.20F, 0.50F, -2.60F, 4.80F, 10.30F, 5.20F)
            , PartPose.offsetAndRotation(11.00F, -10.00F, 0.00F, -0.3142F, -0.0000F, 0.2443F));
        left_arm.addOrReplaceChild("lpaul_top", CubeListBuilder.create().texOffs(446, 79).addBox(-1.20F, -2.20F, -5.20F, 9.80F, 4.70F, 10.40F), PartPose.offsetAndRotation(-2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, -0.2443F));
        left_arm.addOrReplaceChild("lpaul_mid", CubeListBuilder.create().texOffs(0, 126).addBox(-0.40F, -0.00F, -4.60F, 8.00F, 4.20F, 9.20F), PartPose.offsetAndRotation(-2.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, -0.1396F));
        left_arm.addOrReplaceChild("lpaul_low", CubeListBuilder.create().texOffs(35, 126).addBox(0.20F, 0.80F, -4.00F, 6.40F, 3.60F, 8.00F), PartPose.offsetAndRotation(-2.00F, 4.00F, 0.00F, -0.0000F, -0.0000F, -0.0698F));
        left_arm.addOrReplaceChild("lpaul_crest", CubeListBuilder.create().texOffs(64, 126).addBox(-1.20F, -5.00F, -1.20F, 2.60F, 5.50F, 2.40F), PartPose.offsetAndRotation(5.00F, -2.50F, 0.00F, -0.0000F, -0.0000F, -0.3142F));
        left_arm.addOrReplaceChild("lpaul_gem", CubeListBuilder.create().texOffs(75, 126).addBox(3.60F, -0.80F, -5.60F, 3.20F, 2.20F, 1.20F), PartPose.offsetAndRotation(-2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, -0.2443F));
        PartDefinition p2_pauldron_wing_l = left_arm.addOrReplaceChild("p2_pauldron_wing_l", CubeListBuilder.create()
            .texOffs(416, 32).addBox(-0.80F, -9.80F, -1.80F, 2.40F, 10.20F, 3.60F)
            .texOffs(416, 0).addBox(-1.00F, -5.20F, -2.20F, 3.20F, 3.20F, 4.40F)
            , PartPose.offsetAndRotation(6.60F, -2.20F, 0.20F, -0.18F, 0.00F, 0.48F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
            .texOffs(105, 126).addBox(-2.70F, -0.50F, -3.00F, 5.20F, 10.50F, 6.00F)
            .texOffs(128, 126).addBox(-2.10F, 8.70F, -2.50F, 4.00F, 2.80F, 5.00F)
            , PartPose.offsetAndRotation(1.50F, 10.50F, 0.00F, -0.6981F, -0.0000F, -0.1396F));
        PartDefinition astral_halberd = left_forearm.addOrReplaceChild("astral_halberd", CubeListBuilder.create()
            .texOffs(384, 0).addBox(-0.60F, -22.00F, -0.60F, 1.20F, 44.00F, 1.20F)
            .texOffs(480, 0).addBox(-0.90F, -4.50F, -0.90F, 1.80F, 9.00F, 1.80F)
            .texOffs(416, 0).addBox(-1.10F, 4.20F, -1.10F, 2.20F, 1.30F, 2.20F)
            .texOffs(416, 0).addBox(-1.10F, -5.50F, -1.10F, 2.20F, 1.30F, 2.20F)
            .texOffs(416, 0).addBox(-1.00F, -13.90F, -1.00F, 2.00F, 1.40F, 2.00F)
            .texOffs(416, 0).addBox(-1.10F, -24.50F, -1.10F, 2.20F, 5.00F, 2.20F)
            .texOffs(448, 0).addBox(-1.50F, -23.30F, -0.70F, 3.00F, 2.60F, 1.40F)
            .texOffs(416, 0).addBox(-1.20F, 21.00F, -1.20F, 2.40F, 2.50F, 2.40F)
            .texOffs(448, 0).addBox(-0.50F, 23.20F, -0.50F, 1.00F, 5.60F, 1.00F)
            // Sculpted 3D Fangtian Celestial Halberd spearhead + crescent moon-blades (verified cyan/gold UVs!)
            .texOffs(448, 32).addBox(-0.70F, -44.00F, -1.50F, 1.40F, 20.00F, 3.00F)
            .texOffs(448, 32).addBox(-0.45F, -49.50F, -0.90F, 0.90F, 6.00F, 1.80F)
            .texOffs(416, 0).addBox(-0.95F, -32.00F, -4.60F, 1.90F, 6.80F, 9.20F)
            // Front crescent moon-blade arc
            .texOffs(448, 0).addBox(-0.55F, -37.50F, -11.40F, 1.10F, 16.50F, 5.20F)
            .texOffs(448, 32).addBox(-0.40F, -41.00F, -13.40F, 0.80F, 8.20F, 3.40F)
            .texOffs(448, 32).addBox(-0.40F, -24.50F, -12.60F, 0.80F, 7.40F, 3.20F)
            .texOffs(416, 32).addBox(-0.65F, -34.20F, -7.20F, 1.30F, 10.60F, 2.80F)
            // Rear counterweight crescent spike
            .texOffs(448, 0).addBox(-0.50F, -34.00F, 4.40F, 1.00F, 11.20F, 4.60F)
            .texOffs(416, 32).addBox(-0.38F, -36.50F, 7.60F, 0.76F, 5.50F, 2.80F)
            , PartPose.offsetAndRotation(-0.30F, 9.50F, -1.20F, 1.3963F, -0.0000F, 0.0698F));
        astral_halberd.addOrReplaceChild("halb_pommel_fin_f", CubeListBuilder.create().texOffs(416, 0).addBox(-0.40F, -1.00F, -2.00F, 0.80F, 3.70F, 1.70F), PartPose.offsetAndRotation(0.00F, 23.50F, 0.00F, -0.3142F, -0.0000F, 0.0000F));
        astral_halberd.addOrReplaceChild("halb_pommel_fin_b", CubeListBuilder.create().texOffs(416, 0).addBox(-0.40F, -1.00F, 0.30F, 0.80F, 3.70F, 1.70F), PartPose.offsetAndRotation(0.00F, 23.50F, 0.00F, 0.3142F, -0.0000F, 0.0000F));
        PartDefinition vfx_weapon_aura = astral_halberd.addOrReplaceChild("vfx_weapon_aura", CubeListBuilder.create()
            .texOffs(384, 32).addBox(-0.85F, -50.20F, -1.80F, 1.70F, 24.00F, 3.60F)
            .texOffs(416, 32).addBox(-0.55F, -41.50F, -14.20F, 1.10F, 23.50F, 2.20F)
            , PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition p2_halberd_edge = astral_halberd.addOrReplaceChild("p2_halberd_edge", CubeListBuilder.create()
                .texOffs(408, 256).addBox(-6.0F, -25.6F, -1.65F, 12.0F, 1.3F, 3.3F)
                .texOffs(408, 256).addBox(-5.2F, -20.8F, -1.55F, 10.4F, 1.2F, 3.1F)
                .texOffs(440, 260).addBox(-11.4F, -29.2F, -0.75F, 1.8F, 11.2F, 1.5F)
                .texOffs(440, 260).addBox(9.6F, -29.2F, -0.75F, 1.8F, 11.2F, 1.5F)
                .texOffs(448, 240).addBox(-1.2F, -42.5F, -0.85F, 2.4F, 8.2F, 1.7F)
                .texOffs(448, 240).addBox(-0.65F, -47.0F, -0.55F, 1.3F, 5.2F, 1.1F)
                .texOffs(408, 256).addBox(-1.45F, -13.0F, -1.45F, 2.9F, 1.4F, 2.9F)
                .texOffs(408, 256).addBox(-1.45F, -1.0F, -1.45F, 2.9F, 1.4F, 2.9F)
                .texOffs(408, 256).addBox(-1.45F, 12.5F, -1.45F, 2.9F, 1.5F, 2.9F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition vfx_body_aura = chest.addOrReplaceChild("vfx_body_aura", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -6.40F, -5.60F, -0.0000F, -0.0000F, 0.0000F));
        vfx_body_aura.addOrReplaceChild("vfx_ba_core", CubeListBuilder.create().texOffs(384, 32).addBox(-3.60F, -3.60F, -1.80F, 7.20F, 7.20F, 2.80F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.0000F, 0.7854F));
        PartDefinition p2_chest_core = chest.addOrReplaceChild("p2_chest_core", CubeListBuilder.create()
            .texOffs(384, 32).addBox(-2.80F, -2.80F, -1.60F, 5.60F, 5.60F, 1.80F)
            .texOffs(416, 32).addBox(-7.80F, -0.65F, -0.75F, 15.60F, 1.30F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -6.40F, -6.10F, 0.0F, 0.0F, 0.7854F));
        PartDefinition halo_tier3 = chest.addOrReplaceChild("halo_tier3", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 20.20F, -0.0000F, -0.0000F, 0.0000F));
        halo_tier3.addOrReplaceChild("halo3_seg_1", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.3927F));
        halo_tier3.addOrReplaceChild("halo3_seg_2", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.7854F));
        halo_tier3.addOrReplaceChild("halo3_ray_2", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.7854F));
        halo_tier3.addOrReplaceChild("halo3_seg_3", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.1781F));
        halo_tier3.addOrReplaceChild("halo3_seg_4", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.5708F));
        halo_tier3.addOrReplaceChild("halo3_ray_4", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.5708F));
        halo_tier3.addOrReplaceChild("halo3_seg_5", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 1.9635F));
        halo_tier3.addOrReplaceChild("halo3_seg_6", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.3562F));
        halo_tier3.addOrReplaceChild("halo3_ray_6", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.3562F));
        halo_tier3.addOrReplaceChild("halo3_seg_7", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 2.7489F));
        halo_tier3.addOrReplaceChild("halo3_seg_8", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.1416F));
        halo_tier3.addOrReplaceChild("halo3_ray_8", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.1416F));
        halo_tier3.addOrReplaceChild("halo3_seg_9", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.5343F));
        halo_tier3.addOrReplaceChild("halo3_seg_10", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.9270F));
        halo_tier3.addOrReplaceChild("halo3_ray_10", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 3.9270F));
        halo_tier3.addOrReplaceChild("halo3_seg_11", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.3197F));
        halo_tier3.addOrReplaceChild("halo3_seg_12", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.7124F));
        halo_tier3.addOrReplaceChild("halo3_ray_12", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 4.7124F));
        halo_tier3.addOrReplaceChild("halo3_seg_13", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.1051F));
        halo_tier3.addOrReplaceChild("halo3_seg_14", CubeListBuilder.create().texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.4978F));
        halo_tier3.addOrReplaceChild("halo3_ray_14", CubeListBuilder.create().texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.4978F));
        halo_tier3.addOrReplaceChild("halo3_seg_15", CubeListBuilder.create().texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 5.8905F));
        PartDefinition right_leg = sovereign_root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(354, 126).addBox(-2.90F, -0.20F, -3.00F, 5.80F, 12.00F, 6.00F)
            .texOffs(379, 126).addBox(-3.50F, 1.20F, -2.40F, 1.20F, 7.30F, 4.80F)
            , PartPose.offsetAndRotation(-5.50F, -23.00F, 0.00F, -0.1396F, -0.1745F, -0.1396F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
            .texOffs(392, 126).addBox(-2.50F, -2.70F, -4.40F, 5.00F, 4.20F, 1.80F)
            .texOffs(407, 126).addBox(-1.10F, -1.70F, -4.90F, 2.20F, 2.20F, 1.10F)
            .texOffs(414, 126).addBox(-3.10F, -0.10F, -3.60F, 6.20F, 9.10F, 7.20F)
            .texOffs(441, 126).addBox(-3.30F, 8.30F, -4.20F, 6.60F, 3.20F, 8.00F)
            .texOffs(472, 126).addBox(-2.50F, 9.30F, -5.00F, 5.00F, 2.20F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, 11.50F, 0.00F, 0.1396F, -0.0000F, 0.1396F));
        PartDefinition left_leg = sovereign_root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(485, 126).addBox(-2.90F, -0.20F, -3.00F, 5.80F, 12.00F, 6.00F)
            .texOffs(0, 172).addBox(2.30F, 1.20F, -2.40F, 1.20F, 7.30F, 4.80F)
            , PartPose.offsetAndRotation(5.50F, -23.00F, 0.00F, -0.1396F, 0.1745F, 0.1396F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
            .texOffs(13, 172).addBox(-2.50F, -2.70F, -4.40F, 5.00F, 4.20F, 1.80F)
            .texOffs(28, 172).addBox(-1.10F, -1.70F, -4.90F, 2.20F, 2.20F, 1.10F)
            .texOffs(35, 172).addBox(-3.10F, -0.10F, -3.60F, 6.20F, 9.10F, 7.20F)
            .texOffs(62, 172).addBox(-3.30F, 8.30F, -4.20F, 6.60F, 3.20F, 8.00F)
            .texOffs(93, 172).addBox(-2.50F, 9.30F, -5.00F, 5.00F, 2.20F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, 11.50F, 0.00F, 0.1396F, -0.0000F, -0.1396F));
        // VFX containers kept empty here; dedicated per-skill emissive translucent VFX render in AstralSovereignRenderer!
        sovereign_root.addOrReplaceChild("vfx_slash_arc", CubeListBuilder.create(), PartPose.offset(0.00F, -28.00F, -4.00F));
        sovereign_root.addOrReplaceChild("vfx_magic_circle", CubeListBuilder.create(), PartPose.offset(0.00F, -1.00F, 0.00F));
        sovereign_root.addOrReplaceChild("vfx_aura_pillars", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition orbiting_swords_root = sovereign_root.addOrReplaceChild("orbiting_swords_root", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -34.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        // Wide majestic Phase 2 orbiting 3D celestial sword halo (radius ~41.5px / 2.6 blocks, verified cyan/gold/crimson UVs)
        orbiting_swords_root.addOrReplaceChild("orbit_sword_0", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(15.88F, -5.00F, -38.34F, -0.18F, 0.0000F, 0.08F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_1", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(38.34F, 1.50F, -15.88F, -0.08F, 0.0000F, 0.18F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_2", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(38.34F, -5.00F, 15.88F, 0.08F, 0.0000F, 0.18F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_3", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(15.88F, 1.50F, 38.34F, 0.18F, 0.0000F, 0.08F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_4", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(-15.88F, -5.00F, 38.34F, 0.18F, 0.0000F, -0.08F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_5", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(-38.34F, 1.50F, 15.88F, 0.08F, 0.0000F, -0.18F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_6", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(-38.34F, -5.00F, -15.88F, -0.08F, 0.0000F, -0.18F));
        orbiting_swords_root.addOrReplaceChild("orbit_sword_7", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
            .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
            .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
            .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
            .texOffs(416, 0).addBox(-3.00F, 1.20F, -0.70F, 6.00F, 1.15F, 1.40F)
            .texOffs(480, 0).addBox(-0.62F, 2.35F, -0.62F, 1.24F, 4.20F, 1.24F)
            .texOffs(416, 0).addBox(-0.95F, 6.45F, -0.95F, 1.90F, 1.35F, 1.90F)
            , PartPose.offsetAndRotation(-15.88F, 1.50F, -38.34F, -0.18F, 0.0000F, -0.08F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(AstralSovereignRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float age = state.ageInTicks;
        float p = Mth.clamp(state.actionProgress, 0.0F, 1.0F);
        boolean isTransitioning = state.actionType == AstralSovereignEntity.ACTION_PHASE_TRANSITION;
        float awakenFactor = isTransitioning
                ? smoothWindow(p, 0.46F, 0.56F)
                : (state.phase2 ? 1.0F : 0.0F);
        boolean showPhase2Parts = awakenFactor > 0.02F;

        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.55F;

        this.haloInner.visible = true;
        this.haloInner.zRot += age * (0.042F + awakenFactor * 0.048F);

        this.haloOuter.visible = showPhase2Parts;
        this.haloOuter.zRot -= age * 0.055F;
        setUniformScale(this.haloOuter, 0.35F + 0.65F * awakenFactor);

        this.haloTier3.visible = showPhase2Parts;
        this.haloTier3.zRot += age * 0.038F;
        setUniformScale(this.haloTier3, 0.25F + 0.75F * awakenFactor);

        this.orbitingSwordsRoot.visible = showPhase2Parts;
        this.orbitingSwordsRoot.yRot += age * 0.095F;
        this.orbitingSwordsRoot.y += Mth.sin(age * 0.12F) * 1.4F;
        setUniformScale(this.orbitingSwordsRoot, 0.20F + 0.80F * awakenFactor);

        this.p2CrownCrest.visible = showPhase2Parts;
        setUniformScale(this.p2CrownCrest, awakenFactor);
        this.p2ChestCore.visible = showPhase2Parts;
        setUniformScale(this.p2ChestCore, awakenFactor);
        this.p2PauldronL.visible = showPhase2Parts;
        setUniformScale(this.p2PauldronL, awakenFactor);
        this.p2PauldronR.visible = showPhase2Parts;
        setUniformScale(this.p2PauldronR, awakenFactor);
        this.p2HalberdEdge.visible = showPhase2Parts;
        setUniformScale(this.p2HalberdEdge, awakenFactor);

        this.vfxWeaponAura.visible = false;
        this.vfxBodyAura.visible = awakenFactor > 0.55F && (state.swordBuffActive || state.actionType == AstralSovereignEntity.ACTION_ULTIMATE_JUDGMENT);
        this.vfxMagicCircle.visible = false;
        this.vfxAuraPillars.visible = false;
        this.vfxSlashArc.visible = false;

        float breathe = Mth.sin(age * 0.11F);
        float sway = Mth.sin(age * 0.065F);
        float walkPos = state.walkAnimationPos;
        float walkSpeed = Math.min(1.0F, state.walkAnimationSpeed);

        this.sovereignRoot.y += -awakenFactor * 4.2F + Mth.sin(age * 0.11F) * (0.55F + awakenFactor * 0.75F);
        this.body.yRot += sway * 0.075F;
        this.chest.yRot -= sway * 0.055F;
        this.chest.xRot += 0.04F + walkSpeed * 0.16F;
        this.chest.y += breathe * 0.42F;

        float legSwing = Mth.cos(walkPos * 0.72F) * 0.58F * walkSpeed;
        if (awakenFactor > 0.5F && walkSpeed < 0.12F && !isTransitioning) {
            this.rightLeg.xRot += 0.18F + Mth.sin(age * 0.10F) * 0.06F;
            this.leftLeg.xRot += -0.14F - Mth.sin(age * 0.10F) * 0.06F;
            this.rightShin.xRot += 0.28F;
            this.leftShin.xRot += 0.16F;
        } else {
            this.rightLeg.xRot += legSwing;
            this.leftLeg.xRot -= legSwing;
            this.rightShin.xRot += Math.max(0.0F, -legSwing) * 0.62F;
            this.leftShin.xRot += Math.max(0.0F, legSwing) * 0.62F;
        }

        this.halberdArm.xRot += -0.12F + breathe * 0.045F - walkSpeed * 0.18F;
        this.halberdArm.zRot += 0.08F + sway * 0.03F;
        this.offHandArm.xRot += 0.10F - breathe * 0.05F + walkSpeed * 0.20F;
        this.offHandArm.zRot += -0.14F - sway * 0.04F;

        float capeWave1 = Mth.sin(age * 0.18F);
        float capeWave2 = Mth.sin(age * 0.18F - 0.95F);
        float capeWave3 = Mth.sin(age * 0.18F - 1.90F);
        float updraft = awakenFactor * 0.32F + walkSpeed * 0.44F;
        this.capeRoot.xRot += 0.08F + updraft * 0.58F + capeWave1 * 0.08F;
        this.capeMid.xRot += 0.10F + updraft * 0.72F + capeWave2 * 0.15F;
        this.capeLower.xRot += 0.12F + updraft * 0.85F + capeWave3 * 0.22F;
        this.capeMid.zRot += Mth.sin(age * 0.13F) * 0.065F;
        this.capeLower.zRot += Mth.sin(age * 0.13F - 1.2F) * 0.105F;

        switch (state.actionType) {
            case AstralSovereignEntity.ACTION_COMBO_SWEEP -> animateComboSweep(p);
            case AstralSovereignEntity.ACTION_COMBO_OVERHEAD -> animateComboOverhead(p);
            case AstralSovereignEntity.ACTION_COMBO_THRUST -> animateComboThrust(p);
            case AstralSovereignEntity.ACTION_SWORD_ARRAY_BUFF -> animateSwordArrayBuff(p, age);
            case AstralSovereignEntity.ACTION_ULTIMATE_JUDGMENT -> animateUltimateJudgment(p, age);
            case AstralSovereignEntity.ACTION_PHASE_TRANSITION -> animatePhaseTransition(p, age);
            default -> {
                if (state.swordBuffActive) {
                    this.vfxMagicCircle.visible = true;
                    this.vfxMagicCircle.yRot += age * 0.18F;
                    this.vfxMagicCircle.xScale = 0.82F;
                    this.vfxMagicCircle.zScale = 0.82F;
                }
            }
        }

        float forwardThigh = Math.max(0.0F, Math.max(-this.leftLeg.xRot, -this.rightLeg.xRot));
        this.tabardFront.xRot += -forwardThigh * 1.18F - 0.08F;
        this.tassetLeft.zRot += Math.abs(this.leftLeg.xRot) * 0.42F + 0.06F;
        this.tassetRight.zRot += -Math.abs(this.rightLeg.xRot) * 0.42F - 0.06F;
    }

    private static void setUniformScale(ModelPart part, float scale) {
        part.xScale = scale;
        part.yScale = scale;
        part.zScale = scale;
    }

    /**
     * Combo 1: TRUE horizontal chest-height crescent sweep (never dips downward into the floor!).
     * Lifts halberdArm up to shoulder height (-0.98 rad) and sweeps purely around vertical Y at chest level.
     */
    private void animateComboSweep(float p) {
        float windup = smoothWindow(p, 0.0F, 0.28F);
        float strike = smoothWindow(p, 0.24F, 0.64F);
        float recover = 1.0F - smoothWindow(p, 0.70F, 1.0F);
        float active = windup * recover;

        float sweepYaw = (-0.98F * windup * (1.0F - strike)) + (1.36F * strike * recover);
        this.sovereignRoot.y += 1.2F * active;
        this.sovereignRoot.z += 2.0F * windup * (1.0F - strike) - 4.8F * strike * recover;
        this.body.yRot += sweepYaw * 0.44F;
        this.chest.yRot += sweepYaw * 0.56F;
        this.chest.xRot += 0.14F * strike * recover;
        this.head.yRot -= sweepYaw * 0.45F;

        // Raise arm up to shoulder/chest height (-0.98 rad), extend forearm (+0.38 rad), level halberd shaft horizontally (+1.62 rad)
        // Keep zRot subtle (+0.18 rad) so the crescent sweeps HORIZONTALLY at chest level, NOT down into the ground!
        this.halberdArm.xRot += -0.98F * active;
        this.halberdArm.yRot += sweepYaw * 0.65F;
        this.halberdArm.zRot += 0.18F * active;
        this.halberdForearm.xRot += 0.38F * active;
        this.astralHalberd.xRot += 1.62F * active;

        this.offHandArm.xRot += -0.35F * windup * (1.0F - strike) + 0.55F * strike * recover;
        this.offHandArm.zRot += -0.45F * active;
        this.rightLeg.xRot += -0.32F * strike * recover;
        this.leftLeg.xRot += 0.34F * strike * recover;
        this.rightShin.xRot += 0.28F * active;
        this.capeRoot.yRot -= sweepYaw * 0.35F;
        this.capeRoot.xRot += 0.32F * strike * recover;

        if (p >= 0.26F && p <= 0.76F) {
            float arcT = (p - 0.26F) / 0.50F;
            this.vfxSlashArc.visible = true;
            this.vfxSlashArc.yRot += Mth.lerp(arcT, -1.15F, 1.15F);
            this.vfxSlashArc.zRot += 0.10F;
            float s = 0.88F + Mth.sin(arcT * Mth.PI) * 0.42F;
            this.vfxSlashArc.xScale = s;
            this.vfxSlashArc.zScale = s;
        }
    }

    /** Combo 2: Overhead Sky-Splitter — raise halberd cleanly behind crown, then drive straight down into ground at -Z. */
    private void animateComboOverhead(float p) {
        float raise = smoothWindow(p, 0.0F, 0.36F);
        float slam = smoothWindow(p, 0.36F, 0.60F);
        float holdRecover = 1.0F - smoothWindow(p, 0.68F, 1.0F);
        float liftFactor = raise * (1.0F - slam);
        float slamFactor = slam * holdRecover;

        this.sovereignRoot.y += -2.6F * liftFactor + 3.4F * slamFactor;
        this.sovereignRoot.z += 2.2F * liftFactor - 5.2F * slamFactor;
        this.body.xRot += -0.14F * liftFactor + 0.22F * slamFactor;
        this.chest.xRot += -0.28F * liftFactor + 0.44F * slamFactor;
        this.head.xRot += 0.16F * liftFactor - 0.18F * slamFactor;

        // Raise: arm high overhead (-2.10), forearm extended (+0.45), halberd angled cleanly up-back (+0.95 -> net -0.32 rad)
        // Slam: arm drives forward (-0.42), forearm extends (+0.48), halberd whips forward-down (+1.50 -> net +1.94 rad into floor!)
        this.halberdArm.xRot += -2.10F * liftFactor - 0.42F * slamFactor;
        this.halberdArm.zRot += -0.18F * liftFactor + 0.12F * slamFactor;
        this.halberdForearm.xRot += 0.45F * liftFactor + 0.48F * slamFactor;
        this.astralHalberd.xRot += 0.95F * liftFactor + 1.50F * slamFactor;

        this.offHandArm.xRot += -1.05F * liftFactor + 0.72F * slamFactor;
        this.offHandArm.zRot += -0.28F * liftFactor - 0.42F * slamFactor;
        this.rightLeg.xRot += -0.28F * slamFactor;
        this.rightShin.xRot += 0.42F * slamFactor;
        this.leftLeg.xRot += 0.30F * slamFactor;
        this.leftShin.xRot += 0.26F * slamFactor;
        this.capeRoot.xRot += 0.46F * slamFactor;

        if (p >= 0.38F && p <= 0.78F) {
            float arcT = (p - 0.38F) / 0.40F;
            this.vfxSlashArc.visible = true;
            this.vfxSlashArc.zRot += Mth.HALF_PI;
            this.vfxSlashArc.xRot += Mth.lerp(arcT, -0.70F, 0.80F);
            float s = 0.92F + Mth.sin(arcT * Mth.PI) * 0.42F;
            this.vfxSlashArc.xScale = s;
            this.vfxSlashArc.yScale = s;
            this.vfxSlashArc.zScale = s;
        }
    }

    /** Combo 3: Coiled stance -> forward lunging Astral Dragon Thrust (halberd spear tip aligned dead straight along -Z). */
    private void animateComboThrust(float p) {
        float coil = smoothWindow(p, 0.0F, 0.28F);
        float thrust = smoothWindow(p, 0.26F, 0.56F);
        float flick = smoothWindow(p, 0.56F, 0.80F);
        float endFade = 1.0F - smoothWindow(p, 0.80F, 1.0F);
        float coilOnly = coil * (1.0F - thrust);
        float thrustFactor = thrust * endFade;

        this.sovereignRoot.z += 3.6F * coilOnly - 7.0F * thrustFactor;
        this.sovereignRoot.y += 2.2F * coilOnly + 1.4F * thrustFactor;
        this.body.yRot += -0.46F * coilOnly + 0.32F * thrustFactor;
        this.chest.xRot += 0.14F * coilOnly + 0.24F * thrustFactor;
        this.head.yRot += 0.36F * coilOnly - 0.24F * thrustFactor;

        // Thrust aligns halberd dead level forward at chest height, compensating for chest pitch (+0.24 rad):
        // chest(+0.24) + bind(+0.384) + arm(-0.90) + forearm(+0.64) + halberd(+1.20) = +1.564 rad (-Z horizontal!)
        this.halberdArm.xRot += 0.18F * coilOnly - 0.90F * thrustFactor - 0.28F * flick * endFade;
        this.halberdArm.zRot += -0.20F * coilOnly + 0.12F * thrustFactor;
        this.halberdForearm.xRot += -0.10F * coilOnly + 0.64F * thrustFactor;
        this.astralHalberd.xRot += 0.96F * coilOnly + 1.20F * thrustFactor - 0.25F * flick * endFade;

        this.offHandArm.xRot += -0.35F * coilOnly + 0.65F * thrustFactor;
        this.offHandArm.zRot += -0.52F * thrustFactor;
        this.leftLeg.xRot += 0.28F * coilOnly - 0.44F * thrustFactor;
        this.rightLeg.xRot += -0.28F * coilOnly + 0.46F * thrustFactor;
        this.leftShin.xRot += 0.36F * (coilOnly + thrustFactor * 0.7F);
        this.capeRoot.xRot += 0.48F * thrustFactor;

        if (p >= 0.32F && p <= 0.72F) {
            float arcT = (p - 0.32F) / 0.40F;
            this.vfxSlashArc.visible = true;
            this.vfxSlashArc.zRot += 0.62F;
            this.vfxSlashArc.xScale = 0.60F;
            this.vfxSlashArc.zScale = 1.30F + Mth.sin(arcT * Mth.PI) * 0.35F;
        }
    }

    /** Phase 2 Sword Array Empower cast: heroic open-armed sword-command stance with surging halos & runes. */
    private void animateSwordArrayBuff(float p, float age) {
        float bell = Mth.sin(p * Mth.PI);
        this.sovereignRoot.y -= bell * 4.5F;
        this.chest.xRot -= bell * 0.22F;
        this.head.xRot -= bell * 0.16F;

        // Halberd held proudly outward to the side while off-hand casts sword-command seal
        this.halberdArm.xRot += -0.35F * bell;
        this.halberdArm.zRot += 0.65F * bell;
        this.halberdForearm.xRot += 0.28F * bell;
        this.astralHalberd.xRot += -0.18F * bell;

        this.offHandArm.xRot += -1.35F * bell;
        this.offHandArm.zRot += -0.55F * bell;
        this.offHandForearm.xRot += -0.32F * bell;
        this.capeRoot.xRot += bell * 0.52F + Mth.sin(age * 0.45F) * 0.08F;

        this.vfxMagicCircle.visible = true;
        this.vfxMagicCircle.yRot += age * 0.26F;
        float s = 0.78F + bell * 0.35F;
        this.vfxMagicCircle.xScale = s;
        this.vfxMagicCircle.zScale = s;
        this.vfxAuraPillars.visible = true;
        this.vfxAuraPillars.yRot -= age * 0.18F;
    }

    /** Eight-Wastes Astral Judgment: levitate & gather -> vertical halberd ground-plunge -> 360-degree circular shockwave. */
    private void animateUltimateJudgment(float p, float age) {
        float chargePhase = smoothWindow(p, 0.0F, 0.48F);
        float slamPhase = smoothWindow(p, 0.48F, 0.60F);
        float recover = 1.0F - smoothWindow(p, 0.78F, 1.0F);
        float hover = chargePhase * (1.0F - slamPhase);
        float slam = slamPhase * recover;

        this.sovereignRoot.y += -7.5F * hover + 4.0F * slam;
        this.chest.xRot += -0.26F * hover + 0.46F * slam;
        this.head.xRot += -0.20F * hover + 0.14F * slam;

        // Hover: lift halberd skyward cleanly; Slam: plunge halberd shaft straight into the ground
        this.halberdArm.xRot += -1.95F * hover - 0.52F * slam;
        this.halberdArm.zRot += 0.36F * hover - 0.10F * slam;
        this.halberdForearm.xRot += 0.38F * hover + 0.50F * slam;
        this.astralHalberd.xRot += 0.85F * hover + 1.55F * slam;

        this.offHandArm.xRot += -1.75F * hover + 0.58F * slam;
        this.offHandArm.zRot += -0.72F * hover - 0.32F * slam;
        this.rightLeg.xRot += 0.18F * hover - 0.32F * slam;
        this.leftLeg.xRot += 0.18F * hover + 0.30F * slam;
        this.rightShin.xRot += 0.42F * slam;
        this.leftShin.xRot += 0.35F * slam;
        this.capeRoot.xRot += 0.55F * hover + 0.45F * slam;

        // Pure circular Bagua array -> expanding 360-degree shockwave (vfxSlashArc stays strictly FALSE!)
        this.vfxSlashArc.visible = false;
        this.vfxMagicCircle.visible = true;
        this.vfxMagicCircle.yRot += age * (slamPhase > 0.01F ? 0.32F : 0.16F);
        float circleScale = slamPhase > 0.01F
                ? (0.95F + smoothWindow(p, 0.50F, 0.88F) * 1.15F)
                : (0.45F + chargePhase * 0.55F);
        this.vfxMagicCircle.xScale = circleScale;
        this.vfxMagicCircle.zScale = circleScale;
        this.vfxAuraPillars.visible = p < 0.86F;
        this.vfxAuraPillars.yRot -= age * 0.14F;
        float pillarScale = slamPhase > 0.01F ? (1.0F + smoothWindow(p, 0.50F, 0.85F) * 0.75F) : (0.5F + chargePhase * 0.5F);
        this.vfxAuraPillars.xScale = pillarScale;
        this.vfxAuraPillars.zScale = pillarScale;
    }

    /** Phase 1 -> Phase 2 Awakening Transition: Kneel & gather -> Skyward roar & armor unfurl -> Ascended hover settle. */
    private void animatePhaseTransition(float p, float age) {
        float coil = smoothWindow(p, 0.0F, 0.09F) * (1.0F - smoothWindow(p, 0.10F, 0.18F));
        float rise = smoothWindow(p, 0.10F, 0.42F);
        float dive = whipWindow(p, 0.71F, 0.80F);
        float airborne = rise * (1.0F - dive);
        float apexBurst = smoothWindow(p, 0.44F, 0.58F) * (1.0F - smoothWindow(p, 0.68F, 0.76F));
        float hoist = smoothWindow(p, 0.56F, 0.71F) * (1.0F - smoothWindow(p, 0.78F, 0.86F));
        float slamImpact = smoothWindow(p, 0.78F, 0.82F) * (1.0F - smoothWindow(p, 0.88F, 0.99F));

        this.sovereignRoot.y += 4.2F * coil - 54.0F * airborne + 5.8F * slamImpact;
        this.body.xRot += 0.28F * coil - 0.22F * apexBurst + 0.36F * slamImpact;
        this.chest.xRot += 0.32F * coil - 0.28F * apexBurst + 0.38F * slamImpact;
        this.head.xRot += 0.25F * coil - 0.38F * apexBurst + 0.20F * slamImpact;

        this.halberdArm.xRot += -0.35F * coil - 0.95F * apexBurst - 1.95F * hoist + 1.15F * slamImpact;
        this.halberdArm.zRot += -0.15F * coil + 0.88F * apexBurst - 0.12F * slamImpact;
        this.halberdForearm.xRot += 0.35F * coil + 0.25F * apexBurst + 0.42F * slamImpact;
        this.astralHalberd.xRot += 0.65F * coil - 0.35F * apexBurst + 1.18F * slamImpact;

        this.offHandArm.xRot += 0.35F * coil - 0.92F * apexBurst - 0.55F * hoist + 0.85F * slamImpact;
        this.offHandArm.zRot += 0.18F * coil - 0.96F * apexBurst - 0.42F * slamImpact;

        this.rightLeg.xRot += -0.45F * coil + 0.24F * airborne - 0.65F * slamImpact;
        this.rightShin.xRot += 0.82F * coil + 0.36F * airborne + 1.10F * slamImpact;
        this.leftLeg.xRot += 0.34F * coil - 0.16F * airborne + 0.42F * slamImpact;
        this.leftShin.xRot += 0.48F * coil + 0.22F * airborne + 0.68F * slamImpact;

        this.capeRoot.xRot += 0.28F * coil + 0.72F * airborne + 0.58F * slamImpact;
        this.capeMid.xRot += 0.52F * airborne + 0.48F * slamImpact;
        this.capeLower.xRot += 0.64F * airborne + 0.52F * slamImpact;

        this.vfxMagicCircle.visible = false;
        this.vfxAuraPillars.visible = false;
    }

    private static float whipWindow(float value, float start, float end) {
        if (value <= start) return 0.0F;
        if (value >= end) return 1.0F;
        float t = (value - start) / (end - start);
        return t * t * t;
    }

    private static float smoothWindow(float value, float start, float end) {
        if (value <= start) return 0.0F;
        if (value >= end) return 1.0F;
        float t = (value - start) / (end - start);
        return t * t * (3.0F - 2.0F * t);
    }
}
