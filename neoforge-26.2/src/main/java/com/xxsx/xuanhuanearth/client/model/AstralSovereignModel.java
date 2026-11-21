package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.AstralSovereignRenderState;
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
 * <p>Generated directly from the canonical Phase 1 and Phase 2 Blockbench deliverables,
 * preserving all per-cube rotations as child PartDefinitions so concentric star halos
 * ({@code halo_inner}, {@code halo_outer}, {@code halo_tier3}), angled armor tassets,
 * and cape folds match the authored geometry. Phase 1 reveals {@code astral_wings};
 * Phase 2 hides {@code astral_wings} and activates {@code halo_tier3} plus the
 * 8 orbiting flying swords centered at chest height ({@code Y = -34.0F}). Built-in
 * VFX sub-groups ({@code vfx_slash_arc}, {@code vfx_magic_circle}, {@code vfx_aura_pillars})
 * are gated to active combat/telegraph ticks.</p>
 */
public final class AstralSovereignModel extends EntityModel<AstralSovereignRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(XuanhuanEarth.id("astral_sovereign"), "main");

    private static final float ORBIT_SWORDS_BASE_Y = -34.0F;

    private final ModelPart sovereignRoot;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart haloInner;
    private final ModelPart haloOuter;
    private final ModelPart haloTier3;
    private final ModelPart astralWings;
    private final ModelPart capeRoot;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart orbitingSwordsRoot;
    private final ModelPart vfxMagicCircle;
    private final ModelPart vfxSlashArc;
    private final ModelPart vfxAuraPillars;

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
        this.capeRoot = this.chest.getChild("cape_root");
        this.rightArm = this.chest.getChild("left_arm");   // +X right-hand halberd arm in Bedrock export
        this.leftArm = this.chest.getChild("right_arm");   // -X off-hand arm
        this.rightLeg = this.sovereignRoot.getChild("right_leg");
        this.leftLeg = this.sovereignRoot.getChild("left_leg");
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
        body.addOrReplaceChild("tabard_front_main", CubeListBuilder.create().texOffs(226, 0).addBox(-2.80F, -0.00F, -0.60F, 5.60F, 12.40F, 1.10F), PartPose.offsetAndRotation(0.00F, 1.60F, -4.10F, -0.1745F, -0.0000F, 0.0000F));
        body.addOrReplaceChild("tabard_front_trim_l", CubeListBuilder.create().texOffs(241, 0).addBox(-3.40F, -0.00F, -0.80F, 1.20F, 9.90F, 1.10F), PartPose.offsetAndRotation(0.00F, 1.60F, -4.10F, -0.1745F, -0.0000F, 0.0000F));
        body.addOrReplaceChild("tabard_front_trim_r", CubeListBuilder.create().texOffs(246, 0).addBox(2.20F, -0.00F, -0.80F, 1.20F, 9.90F, 1.10F), PartPose.offsetAndRotation(0.00F, 1.60F, -4.10F, -0.1745F, -0.0000F, 0.0000F));
        body.addOrReplaceChild("tasset_l_upper", CubeListBuilder.create().texOffs(251, 0).addBox(-0.40F, -0.00F, -3.80F, 1.40F, 6.50F, 7.60F), PartPose.offsetAndRotation(7.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, 0.2793F));
        body.addOrReplaceChild("tasset_l_lower", CubeListBuilder.create().texOffs(270, 0).addBox(0.60F, 5.80F, -3.20F, 1.20F, 6.00F, 6.40F), PartPose.offsetAndRotation(7.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, 0.3840F));
        body.addOrReplaceChild("tasset_r_upper", CubeListBuilder.create().texOffs(285, 0).addBox(-1.00F, -0.00F, -3.80F, 1.40F, 6.50F, 7.60F), PartPose.offsetAndRotation(-7.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, -0.2793F));
        body.addOrReplaceChild("tasset_r_lower", CubeListBuilder.create().texOffs(304, 0).addBox(-1.80F, 5.80F, -3.20F, 1.20F, 6.00F, 6.40F), PartPose.offsetAndRotation(-7.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, -0.3840F));
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
            , PartPose.offsetAndRotation(0.00F, -10.00F, 4.50F, -0.0000F, -0.0000F, 0.0000F));
        cape_root.addOrReplaceChild("cape_center", CubeListBuilder.create().texOffs(104, 22).addBox(-4.00F, -0.00F, -0.30F, 8.00F, 37.00F, 1.20F), PartPose.offsetAndRotation(0.00F, 0.00F, 0.30F, 0.1745F, -0.0000F, 0.0000F));
        cape_root.addOrReplaceChild("cape_left", CubeListBuilder.create().texOffs(123, 22).addBox(-2.30F, -0.00F, -0.30F, 6.00F, 39.00F, 1.20F), PartPose.offsetAndRotation(5.50F, 0.00F, 0.10F, 0.2094F, 0.1047F, 0.1396F));
        cape_root.addOrReplaceChild("cape_right", CubeListBuilder.create().texOffs(138, 22).addBox(-3.70F, -0.00F, -0.30F, 6.00F, 39.00F, 1.20F), PartPose.offsetAndRotation(-5.50F, 0.00F, 0.10F, 0.2094F, -0.1047F, -0.1396F));
        PartDefinition halo_outer = chest.addOrReplaceChild("halo_outer", CubeListBuilder.create()
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 8.20F, -0.0000F, -0.0000F, 0.0000F));
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
            , PartPose.offsetAndRotation(0.00F, -14.00F, 6.80F, -0.0000F, -0.0000F, 0.0000F));
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
        head.addOrReplaceChild("wg_plume_l", CubeListBuilder.create().texOffs(0, 0).addBox(0.00F, -15.00F, 0.00F, 20.00F, 18.00F, 0.00F), PartPose.offsetAndRotation(1.40F, -12.70F, 0.80F, 0.2094F, 0.4538F, 0.0000F));
        head.addOrReplaceChild("wg_plume_r", CubeListBuilder.create().texOffs(0, 0).addBox(-20.00F, -15.00F, 0.00F, 20.00F, 18.00F, 0.00F), PartPose.offsetAndRotation(-1.40F, -12.70F, 0.80F, 0.2094F, -0.4538F, 0.0000F));
        head.addOrReplaceChild("wg_tassel", CubeListBuilder.create().texOffs(480, 0).addBox(-1.50F, -0.50F, -0.20F, 3.00F, 10.00F, 1.60F), PartPose.offsetAndRotation(0.00F, -8.20F, 4.80F, 0.2793F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_brow_l", CubeListBuilder.create().texOffs(384, 0).addBox(0.00F, -1.30F, -0.55F, 4.50F, 1.30F, 1.15F), PartPose.offsetAndRotation(0.00F, -4.40F, -4.40F, -0.0000F, 0.1396F, 0.2094F));
        head.addOrReplaceChild("wg_brow_r", CubeListBuilder.create().texOffs(384, 0).addBox(-4.50F, -1.30F, -0.55F, 4.50F, 1.30F, 1.15F), PartPose.offsetAndRotation(0.00F, -4.40F, -4.40F, -0.0000F, -0.1396F, -0.2094F));
        head.addOrReplaceChild("wg_cheek_l", CubeListBuilder.create().texOffs(384, 0).addBox(-1.20F, -0.70F, -1.00F, 2.60F, 2.40F, 2.20F), PartPose.offsetAndRotation(3.00F, -2.00F, -3.80F, -0.0000F, 0.1745F, 0.0000F));
        head.addOrReplaceChild("wg_cheek_r", CubeListBuilder.create().texOffs(384, 0).addBox(-1.40F, -0.70F, -1.00F, 2.60F, 2.40F, 2.20F), PartPose.offsetAndRotation(-3.00F, -2.00F, -3.80F, -0.0000F, -0.1745F, 0.0000F));
        head.addOrReplaceChild("wg_mask_jaw", CubeListBuilder.create().texOffs(384, 0).addBox(-2.10F, -0.00F, -0.60F, 4.20F, 2.40F, 1.10F), PartPose.offsetAndRotation(0.00F, -2.30F, -4.30F, -0.1396F, -0.0000F, 0.0000F));
        head.addOrReplaceChild("wg_mask_chin_trim", CubeListBuilder.create().texOffs(416, 0).addBox(-1.50F, -0.50F, -0.55F, 3.00F, 0.90F, 1.05F), PartPose.offsetAndRotation(0.00F, -0.00F, -4.50F, -0.1396F, -0.0000F, 0.0000F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(239, 79).addBox(-3.60F, 0.50F, -2.60F, 4.80F, 10.30F, 5.20F)
            , PartPose.offsetAndRotation(-11.00F, -10.00F, 0.00F, -0.1745F, -0.0000F, -0.3142F));
        right_arm.addOrReplaceChild("rpaul_top", CubeListBuilder.create().texOffs(114, 79).addBox(-8.60F, -2.20F, -5.20F, 9.80F, 4.70F, 10.40F), PartPose.offsetAndRotation(2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, 0.2443F));
        right_arm.addOrReplaceChild("rpaul_mid", CubeListBuilder.create().texOffs(155, 79).addBox(-7.60F, -0.00F, -4.60F, 8.00F, 4.20F, 9.20F), PartPose.offsetAndRotation(2.00F, 1.00F, 0.00F, -0.0000F, -0.0000F, 0.1396F));
        right_arm.addOrReplaceChild("rpaul_low", CubeListBuilder.create().texOffs(190, 79).addBox(-6.60F, 0.80F, -4.00F, 6.40F, 3.60F, 8.00F), PartPose.offsetAndRotation(2.00F, 4.00F, 0.00F, -0.0000F, -0.0000F, 0.0698F));
        right_arm.addOrReplaceChild("rpaul_crest", CubeListBuilder.create().texOffs(219, 79).addBox(-1.40F, -5.00F, -1.20F, 2.60F, 5.50F, 2.40F), PartPose.offsetAndRotation(-5.00F, -2.50F, 0.00F, -0.0000F, -0.0000F, 0.3142F));
        right_arm.addOrReplaceChild("rpaul_gem", CubeListBuilder.create().texOffs(230, 79).addBox(-6.80F, -0.80F, -5.60F, 3.20F, 2.20F, 1.20F), PartPose.offsetAndRotation(2.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, 0.2443F));
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
            .texOffs(0, 0).addBox(0.00F, -45.50F, -15.40F, 0.00F, 28.00F, 26.00F)
            .texOffs(0, 0).addBox(-0.30F, -45.50F, -15.40F, 0.00F, 28.00F, 26.00F)
            .texOffs(0, 0).addBox(0.30F, -45.50F, -15.40F, 0.00F, 28.00F, 26.00F)
            , PartPose.offsetAndRotation(-0.30F, 9.50F, -1.20F, 1.3963F, -0.0000F, 0.0698F));
        astral_halberd.addOrReplaceChild("halb_pommel_fin_f", CubeListBuilder.create().texOffs(416, 0).addBox(-0.40F, -1.00F, -2.00F, 0.80F, 3.70F, 1.70F), PartPose.offsetAndRotation(0.00F, 23.50F, 0.00F, -0.3142F, -0.0000F, 0.0000F));
        astral_halberd.addOrReplaceChild("halb_pommel_fin_b", CubeListBuilder.create().texOffs(416, 0).addBox(-0.40F, -1.00F, 0.30F, 0.80F, 3.70F, 1.70F), PartPose.offsetAndRotation(0.00F, 23.50F, 0.00F, 0.3142F, -0.0000F, 0.0000F));
        PartDefinition vfx_weapon_aura = astral_halberd.addOrReplaceChild("vfx_weapon_aura", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-0.65F, -46.20F, -16.00F, 0.00F, 29.20F, 27.00F)
            .texOffs(0, 0).addBox(0.65F, -46.20F, -16.00F, 0.00F, 29.20F, 27.00F)
            , PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition vfx_body_aura = chest.addOrReplaceChild("vfx_body_aura", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -6.40F, -5.60F, -0.0000F, -0.0000F, 0.0000F));
        vfx_body_aura.addOrReplaceChild("vfx_ba_core", CubeListBuilder.create().texOffs(384, 32).addBox(-3.60F, -3.60F, -1.80F, 7.20F, 7.20F, 2.80F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.0000F, 0.7854F));
        PartDefinition halo_tier3 = chest.addOrReplaceChild("halo_tier3", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 9.60F, -0.0000F, -0.0000F, 0.0000F));
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
        PartDefinition vfx_slash_arc = sovereign_root.addOrReplaceChild("vfx_slash_arc", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -28.00F, -4.00F, -0.0000F, -0.0000F, 0.0000F));
        vfx_slash_arc.addOrReplaceChild("vfx_slash_ribbon_0", CubeListBuilder.create().texOffs(0, 0).addBox(-11.50F, -0.00F, -52.00F, 23.00F, 0.00F, 20.00F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, 0.9076F, -0.3491F));
        vfx_slash_arc.addOrReplaceChild("vfx_slash_ribbon_1", CubeListBuilder.create().texOffs(0, 0).addBox(-11.50F, -0.00F, -52.00F, 23.00F, 0.00F, 20.00F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, 0.4538F, -0.3491F));
        vfx_slash_arc.addOrReplaceChild("vfx_slash_ribbon_2", CubeListBuilder.create().texOffs(0, 0).addBox(-11.50F, -0.00F, -52.00F, 23.00F, 0.00F, 20.00F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.0000F, -0.3491F));
        vfx_slash_arc.addOrReplaceChild("vfx_slash_ribbon_3", CubeListBuilder.create().texOffs(0, 0).addBox(-11.50F, -0.00F, -52.00F, 23.00F, 0.00F, 20.00F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.4538F, -0.3491F));
        vfx_slash_arc.addOrReplaceChild("vfx_slash_ribbon_4", CubeListBuilder.create().texOffs(0, 0).addBox(-11.50F, -0.00F, -52.00F, 23.00F, 0.00F, 20.00F), PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, -0.0000F, -0.9076F, -0.3491F));
        PartDefinition vfx_magic_circle = sovereign_root.addOrReplaceChild("vfx_magic_circle", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-48.00F, 0.60F, -48.00F, 96.00F, 0.00F, 96.00F)
            , PartPose.offsetAndRotation(0.00F, -1.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition vfx_aura_pillars = sovereign_root.addOrReplaceChild("vfx_aura_pillars", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_0_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(32.91F, 0.00F, 19.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_0_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(32.91F, 0.00F, 19.00F, -0.0000F, 0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_1_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 38.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_1_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(0.00F, 0.00F, 38.00F, -0.0000F, 0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_2_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-32.91F, 0.00F, 19.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_2_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-32.91F, 0.00F, 19.00F, -0.0000F, 0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_3_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-32.91F, 0.00F, -19.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_3_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-32.91F, 0.00F, -19.00F, -0.0000F, 0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_4_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-0.00F, 0.00F, -38.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_4_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(-0.00F, 0.00F, -38.00F, -0.0000F, 0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_5_0", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(32.91F, 0.00F, -19.00F, -0.0000F, -0.7854F, 0.0000F));
        vfx_aura_pillars.addOrReplaceChild("vfx_pillar_5_1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.50F, -58.00F, 0.00F, 13.00F, 58.00F, 0.00F), PartPose.offsetAndRotation(32.91F, 0.00F, -19.00F, -0.0000F, 0.7854F, 0.0000F));
        PartDefinition orbiting_swords_root = sovereign_root.addOrReplaceChild("orbiting_swords_root", CubeListBuilder.create()
            , PartPose.offsetAndRotation(0.00F, -34.00F, 0.00F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_0 = orbiting_swords_root.addOrReplaceChild("orbit_sword_0", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(10.52F, -1.00F, -25.41F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_1 = orbiting_swords_root.addOrReplaceChild("orbit_sword_1", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(25.41F, -1.00F, -10.52F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_2 = orbiting_swords_root.addOrReplaceChild("orbit_sword_2", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(25.41F, -1.00F, 10.52F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_3 = orbiting_swords_root.addOrReplaceChild("orbit_sword_3", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(10.52F, -1.00F, 25.41F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_4 = orbiting_swords_root.addOrReplaceChild("orbit_sword_4", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-10.52F, -1.00F, 25.41F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_5 = orbiting_swords_root.addOrReplaceChild("orbit_sword_5", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-25.41F, -1.00F, 10.52F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_6 = orbiting_swords_root.addOrReplaceChild("orbit_sword_6", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-25.41F, -1.00F, -10.52F, -0.0000F, -0.0000F, 0.0000F));
        PartDefinition orbit_sword_7 = orbiting_swords_root.addOrReplaceChild("orbit_sword_7", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.00F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.00F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-10.52F, -1.00F, -25.41F, -0.0000F, -0.0000F, 0.0000F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(AstralSovereignRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speedMul = state.phase2 ? 2.2F : 1.0F;

        // Head tracking
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        // Counter-rotating concentric star halos
        this.haloInner.zRot = age * 0.06F * speedMul;
        this.haloOuter.zRot = -age * 0.05F * speedMul;
        this.haloTier3.visible = state.phase2;
        this.haloTier3.zRot = age * 0.035F * speedMul;

        // Phase 1 fanned back swords vs Phase 2 360-degree orbiting sword array
        this.astralWings.visible = !state.phase2;
        this.astralWings.xRot = Mth.sin(age * 0.07F) * 0.05F;

        this.orbitingSwordsRoot.visible = state.phase2;
        this.orbitingSwordsRoot.yRot = age * 0.055F;
        // Preserve base PartPose Y (-34.0F) so the 8 swords float at chest height, not at feet level
        this.orbitingSwordsRoot.y = ORBIT_SWORDS_BASE_Y + Mth.sin(age * 0.08F) * 1.6F;

        // Gate authored VFX meshes to active attack / Bagua telegraph windows
        this.vfxSlashArc.visible = state.attackTicks > 0;
        this.vfxMagicCircle.visible = state.telegraphTicks > 0;
        this.vfxMagicCircle.yRot = age * 0.14F;
        this.vfxAuraPillars.visible = state.phase2 && state.telegraphTicks > 0;
        this.vfxAuraPillars.yRot = -age * 0.08F;

        // Subtle cape billow
        this.capeRoot.xRot = 0.06F + Mth.sin(age * 0.09F) * 0.04F + state.walkAnimationSpeed * 0.25F;

        // Armored stride
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        this.rightLeg.xRot = Mth.cos(walkPos * 0.6662F) * 0.55F * walkSpeed;
        this.leftLeg.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 0.55F * walkSpeed;

        // Combat poses: Bagua telegraph > Flying-sword cast > Melee halberd cleave > Idle
        if (state.telegraphTicks > 0) {
            float pulse = Mth.sin(age * 0.45F) * 0.12F;
            this.rightArm.xRot = -1.25F + pulse;
            this.leftArm.xRot = -1.10F - pulse;
            this.chest.yRot = 0.0F;
        } else if (state.castTicks > 0) {
            float castProgress = 1.0F - (state.castTicks / 12.0F);
            float lift = Mth.sin(castProgress * Mth.PI);
            this.leftArm.xRot = 0.17F - lift * 1.35F;
            this.rightArm.xRot = 0.31F + Mth.sin(age * 0.06F) * 0.04F;
            this.chest.yRot = lift * 0.22F;
        } else if (state.attackTicks > 0) {
            float progress = 1.0F - (state.attackTicks / 16.0F);
            float swing = Mth.sin(progress * Mth.PI);
            this.rightArm.xRot = 0.31F - swing * 1.45F;
            this.leftArm.xRot = 0.17F - Mth.sin(age * 0.06F) * 0.04F;
            this.chest.yRot = -0.35F + swing * 0.75F;
        } else {
            this.rightArm.xRot = 0.31F + Mth.sin(age * 0.06F) * 0.04F;
            this.leftArm.xRot = 0.17F - Mth.sin(age * 0.06F) * 0.04F;
            this.chest.yRot = 0.0F;
        }
    }
}
