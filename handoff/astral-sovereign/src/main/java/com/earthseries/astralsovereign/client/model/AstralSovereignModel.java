package com.earthseries.astralsovereign.client.model;

import com.earthseries.astralsovereign.AstralSovereignMod;
import com.earthseries.astralsovereign.client.renderer.AstralSovereignRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class AstralSovereignModel extends EntityModel<AstralSovereignRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AstralSovereignMod.MOD_ID, "astral_sovereign"), "main"
    );

    private final ModelPart rootBone;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart haloInner;
    private final ModelPart haloOuter;
    private final ModelPart haloTier3;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart orbitingSwordsRoot;

    public AstralSovereignModel(ModelPart root) {
        super(root);
        this.rootBone = root.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.haloInner = this.chest.getChild("halo_inner");
        this.haloOuter = this.chest.getChild("halo_outer");
        this.haloTier3 = this.chest.getChild("halo_tier3");
        this.rightArm = this.chest.getChild("left_arm"); // +X true right arm holding halberd
        this.leftArm = this.chest.getChild("right_arm"); // -X true left arm
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.leftLeg = this.rootBone.getChild("left_leg");
        this.orbitingSwordsRoot = this.rootBone.getChild("orbiting_swords_root");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00F, 24.00F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(7, 7).addBox(-6.60F, -0.50F, -3.60F, 13.20F, 3.50F, 7.20F)
            .texOffs(42, 1).addBox(-2.40F, 0.80F, -4.40F, 4.80F, 4.00F, 1.40F)
            .texOffs(61, 7).addBox(-5.20F, -10.00F, -3.40F, 10.40F, 10.00F, 6.80F)
            .texOffs(90, 1).addBox(-4.60F, -4.60F, -4.10F, 9.20F, 2.60F, 1.30F)
            .texOffs(111, 1).addBox(-5.00F, -7.80F, -4.30F, 10.00F, 2.80F, 1.40F)
            .texOffs(138, 5).addBox(4.40F, -8.50F, -2.50F, 1.40F, 7.00F, 5.00F)
            .texOffs(151, 5).addBox(-5.80F, -8.50F, -2.50F, 1.40F, 7.00F, 5.00F)
            .texOffs(167, 8).addBox(-6.80F, -1.20F, -4.20F, 13.60F, 2.70F, 8.40F)
            .texOffs(205, 1).addBox(-2.40F, -1.60F, -5.00F, 4.80F, 3.60F, 1.40F)
            .texOffs(218, 1).addBox(-1.40F, -0.80F, -5.40F, 2.80F, 2.00F, 1.10F)
            .texOffs(227, 1).addBox(-2.80F, 1.60F, -4.70F, 5.60F, 12.40F, 1.10F)
            .texOffs(242, 1).addBox(-3.40F, 1.60F, -4.90F, 1.20F, 9.90F, 1.10F)
            .texOffs(247, 1).addBox(2.20F, 1.60F, -4.90F, 1.20F, 9.90F, 1.10F)
            .texOffs(259, 8).addBox(6.60F, 1.00F, -3.80F, 1.40F, 6.50F, 7.60F)
            .texOffs(276, 6).addBox(7.60F, 6.80F, -3.20F, 1.20F, 6.00F, 6.40F)
            .texOffs(293, 8).addBox(-8.00F, 1.00F, -3.80F, 1.40F, 6.50F, 7.60F)
            .texOffs(310, 6).addBox(-8.80F, 6.80F, -3.20F, 1.20F, 6.00F, 6.40F)
            , PartPose.offsetAndRotation(0.00F, -23.00F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
            .texOffs(328, 9).addBox(-8.00F, -12.00F, -4.60F, 16.00F, 11.50F, 9.20F)
            .texOffs(484, 3).addBox(-6.20F, -16.00F, 2.20F, 12.40F, 4.50F, 3.00F)
            .texOffs(49, 25).addBox(-5.50F, -11.00F, 4.20F, 11.00F, 9.00F, 2.80F)
            .texOffs(76, 23).addBox(-2.20F, -8.60F, 6.20F, 4.40F, 4.40F, 1.40F)
            .texOffs(88, 24).addBox(2.80F, -14.50F, 5.80F, 1.80F, 7.00F, 2.00F)
            .texOffs(97, 24).addBox(-4.60F, -14.50F, 5.80F, 1.80F, 7.00F, 2.00F)
            .texOffs(128, 192).addBox(-7.40F, -10.60F, -5.60F, 14.80F, 8.80F, 1.60F)
            .texOffs(416, 0).addBox(-7.60F, -11.30F, -5.80F, 15.20F, 1.10F, 1.90F)
            .texOffs(416, 0).addBox(-6.80F, -2.30F, -5.80F, 13.60F, 1.10F, 1.90F)
            .texOffs(384, 0).addBox(6.80F, -10.40F, -5.40F, 1.50F, 7.90F, 2.90F)
            .texOffs(384, 0).addBox(-8.30F, -10.40F, -5.40F, 1.50F, 7.90F, 2.90F)
            .texOffs(384, 0).addBox(2.20F, -12.30F, -5.10F, 4.00F, 1.50F, 2.70F)
            .texOffs(384, 0).addBox(-6.20F, -12.30F, -5.10F, 4.00F, 1.50F, 2.70F)
            .texOffs(416, 0).addBox(-0.90F, -10.60F, -6.00F, 1.80F, 8.80F, 1.00F)
            .texOffs(416, 0).addBox(-2.60F, -8.40F, -6.20F, 5.20F, 4.00F, 1.10F)
            .texOffs(416, 0).addBox(-2.60F, -8.40F, -6.10F, 5.20F, 4.00F, 0.90F)
            .texOffs(448, 0).addBox(-1.70F, -8.10F, -6.50F, 3.40F, 3.40F, 1.00F)
            .texOffs(384, 0).addBox(3.60F, -8.20F, -5.90F, 2.40F, 3.60F, 0.90F)
            .texOffs(416, 0).addBox(4.20F, -7.40F, -6.10F, 1.20F, 2.00F, 0.90F)
            .texOffs(384, 0).addBox(-6.00F, -8.20F, -5.90F, 2.40F, 3.60F, 0.90F)
            .texOffs(416, 0).addBox(-5.40F, -7.40F, -6.10F, 1.20F, 2.00F, 0.90F)
            , PartPose.offsetAndRotation(0.00F, -9.00F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition cape_root = chest.addOrReplaceChild("cape_root", CubeListBuilder.create()
            .texOffs(105, 23).addBox(-4.00F, -0.00F, 0.00F, 8.00F, 37.00F, 1.20F)
            .texOffs(124, 23).addBox(3.20F, -0.00F, -0.20F, 6.00F, 39.00F, 1.20F)
            .texOffs(139, 23).addBox(-9.20F, -0.00F, -0.20F, 6.00F, 39.00F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, -10.00F, 4.50F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition halo_outer = chest.addOrReplaceChild("halo_outer", CubeListBuilder.create()
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            .texOffs(448, 32).addBox(-1.28F, -29.44F, -0.50F, 2.56F, 5.89F, 1.10F)
            .texOffs(416, 32).addBox(-6.66F, -23.81F, -0.50F, 13.32F, 1.79F, 1.10F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 8.20F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition halo_inner = chest.addOrReplaceChild("halo_inner", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            .texOffs(448, 32).addBox(-6.25F, -14.75F, -0.50F, 12.50F, 1.50F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 6.80F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(384, 0).addBox(-2.50F, -0.40F, -2.30F, 5.00F, 3.20F, 4.60F)
            .texOffs(448, 64).addBox(-4.20F, -8.40F, -4.20F, 8.40F, 8.40F, 8.40F)
            .texOffs(384, 0).addBox(-4.60F, -8.80F, -4.60F, 9.20F, 2.60F, 9.20F)
            .texOffs(416, 0).addBox(-4.80F, -7.40F, -4.80F, 9.60F, 1.60F, 9.60F)
            .texOffs(448, 0).addBox(-1.20F, -8.20F, -5.20F, 2.40F, 2.20F, 1.00F)
            .texOffs(384, 0).addBox(3.80F, -6.20F, -2.20F, 1.20F, 7.50F, 6.60F)
            .texOffs(384, 0).addBox(-5.00F, -6.20F, -2.20F, 1.20F, 7.50F, 6.60F)
            .texOffs(384, 0).addBox(-4.40F, -6.20F, 3.60F, 8.80F, 8.00F, 1.40F)
            .texOffs(416, 0).addBox(-2.80F, -11.40F, -2.80F, 5.60F, 2.80F, 5.60F)
            .texOffs(416, 0).addBox(-1.50F, -15.00F, -2.20F, 3.00F, 3.80F, 4.00F)
            .texOffs(448, 0).addBox(-1.00F, -13.80F, -2.60F, 2.00F, 2.20F, 1.00F)
            .texOffs(416, 0).addBox(4.40F, -12.40F, -1.50F, 1.20F, 7.00F, 3.50F)
            .texOffs(416, 0).addBox(-5.60F, -12.40F, -1.50F, 1.20F, 7.00F, 3.50F)
            .texOffs(384, 192).addBox(1.40F, -27.70F, 0.80F, 20.00F, 18.00F, 0.20F)
            .texOffs(384, 192).addBox(-21.40F, -27.70F, 0.80F, 20.00F, 18.00F, 0.20F)
            .texOffs(480, 0).addBox(-1.50F, -8.70F, 4.60F, 3.00F, 10.00F, 1.60F)
            .texOffs(384, 0).addBox(0.00F, -5.70F, -4.95F, 4.50F, 1.30F, 1.15F)
            .texOffs(384, 0).addBox(-4.50F, -5.70F, -4.95F, 4.50F, 1.30F, 1.15F)
            .texOffs(416, 0).addBox(-0.55F, -6.30F, -5.10F, 1.10F, 2.20F, 1.00F)
            .texOffs(384, 0).addBox(1.80F, -2.70F, -4.80F, 2.60F, 2.40F, 2.20F)
            .texOffs(384, 0).addBox(-4.40F, -2.70F, -4.80F, 2.60F, 2.40F, 2.20F)
            .texOffs(384, 0).addBox(-2.10F, -2.30F, -4.90F, 4.20F, 2.40F, 1.10F)
            .texOffs(416, 0).addBox(-1.50F, -0.50F, -5.05F, 3.00F, 0.90F, 1.05F)
            , PartPose.offsetAndRotation(0.00F, -13.80F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(124, 89).addBox(-6.60F, -3.20F, -5.20F, 9.80F, 4.70F, 10.40F)
            .texOffs(164, 88).addBox(-5.60F, 1.00F, -4.60F, 8.00F, 4.20F, 9.20F)
            .texOffs(198, 87).addBox(-4.60F, 4.80F, -4.00F, 6.40F, 3.60F, 8.00F)
            .texOffs(221, 81).addBox(-6.40F, -7.50F, -1.20F, 2.60F, 5.50F, 2.40F)
            .texOffs(231, 80).addBox(-4.80F, -1.80F, -5.60F, 3.20F, 2.20F, 1.20F)
            .texOffs(244, 84).addBox(-3.60F, 0.50F, -2.60F, 4.80F, 10.30F, 5.20F)
            , PartPose.offsetAndRotation(-11.00F, -10.00F, 0.00F, 0.1745F, 0.0000F, -0.3142F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
            .texOffs(266, 85).addBox(-2.50F, -0.50F, -3.00F, 5.20F, 10.50F, 6.00F)
            .texOffs(286, 82).addBox(-3.50F, -0.00F, -1.20F, 1.40F, 7.50F, 3.00F)
            .texOffs(297, 84).addBox(-1.90F, 8.70F, -2.50F, 4.00F, 2.80F, 5.00F)
            , PartPose.offsetAndRotation(-1.50F, 10.50F, 0.00F, 0.4887F, 0.0000F, 0.1745F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(456, 89).addBox(-3.20F, -3.20F, -5.20F, 9.80F, 4.70F, 10.40F)
            .texOffs(9, 135).addBox(-2.40F, 1.00F, -4.60F, 8.00F, 4.20F, 9.20F)
            .texOffs(43, 134).addBox(-1.80F, 4.80F, -4.00F, 6.40F, 3.60F, 8.00F)
            .texOffs(66, 128).addBox(3.80F, -7.50F, -1.20F, 2.60F, 5.50F, 2.40F)
            .texOffs(76, 127).addBox(1.60F, -1.80F, -5.60F, 3.20F, 2.20F, 1.20F)
            .texOffs(89, 131).addBox(-1.20F, 0.50F, -2.60F, 4.80F, 10.30F, 5.20F)
            , PartPose.offsetAndRotation(11.00F, -10.00F, 0.00F, 0.3142F, 0.0000F, 0.2443F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
            .texOffs(111, 132).addBox(-2.70F, -0.50F, -3.00F, 5.20F, 10.50F, 6.00F)
            .texOffs(133, 131).addBox(-2.10F, 8.70F, -2.50F, 4.00F, 2.80F, 5.00F)
            , PartPose.offsetAndRotation(1.50F, 10.50F, 0.00F, 0.6981F, 0.0000F, -0.1396F));
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
            .texOffs(416, 0).addBox(-0.40F, 22.50F, -2.00F, 0.80F, 3.70F, 1.70F)
            .texOffs(416, 0).addBox(-0.40F, 22.50F, 0.30F, 0.80F, 3.70F, 1.70F)
            .texOffs(0, 0).addBox(0.00F, -45.50F, -15.40F, 0.20F, 28.00F, 26.00F)
            .texOffs(0, 0).addBox(-0.30F, -45.50F, -15.40F, 0.20F, 28.00F, 26.00F)
            .texOffs(0, 0).addBox(0.30F, -45.50F, -15.40F, 0.20F, 28.00F, 26.00F)
            , PartPose.offsetAndRotation(-0.30F, 9.50F, -1.20F, -1.3963F, 0.0000F, 0.0698F));
        PartDefinition vfx_weapon_aura = astral_halberd.addOrReplaceChild("vfx_weapon_aura", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-0.65F, -46.20F, -16.00F, 0.20F, 29.20F, 27.00F)
            .texOffs(0, 0).addBox(0.65F, -46.20F, -16.00F, 0.20F, 29.20F, 27.00F)
            , PartPose.offsetAndRotation(0.00F, -0.00F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition vfx_body_aura = chest.addOrReplaceChild("vfx_body_aura", CubeListBuilder.create()
            .texOffs(384, 32).addBox(-3.60F, -3.60F, -1.80F, 7.20F, 7.20F, 2.80F)
            , PartPose.offsetAndRotation(0.00F, -6.40F, -5.60F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition halo_tier3 = chest.addOrReplaceChild("halo_tier3", CubeListBuilder.create()
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(448, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            .texOffs(416, 32).addBox(-0.94F, -38.00F, -0.50F, 1.88F, 5.55F, 1.00F)
            .texOffs(416, 32).addBox(-6.49F, -32.69F, -0.50F, 12.98F, 1.42F, 1.00F)
            , PartPose.offsetAndRotation(0.00F, -14.00F, 9.60F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(360, 132).addBox(-2.90F, -0.20F, -3.00F, 5.80F, 12.00F, 6.00F)
            .texOffs(384, 131).addBox(-3.50F, 1.20F, -2.40F, 1.20F, 7.30F, 4.80F)
            , PartPose.offsetAndRotation(-5.50F, -23.00F, 0.00F, 0.1396F, 0.1745F, -0.1396F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
            .texOffs(394, 128).addBox(-2.50F, -2.70F, -4.40F, 5.00F, 4.20F, 1.80F)
            .texOffs(408, 127).addBox(-1.10F, -1.70F, -4.90F, 2.20F, 2.20F, 1.10F)
            .texOffs(421, 133).addBox(-3.10F, -0.10F, -3.60F, 6.20F, 9.10F, 7.20F)
            .texOffs(449, 134).addBox(-3.30F, 8.30F, -4.20F, 6.60F, 3.20F, 8.00F)
            .texOffs(473, 127).addBox(-2.50F, 9.30F, -5.00F, 5.00F, 2.20F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, 11.50F, 0.00F, -0.1396F, 0.0000F, 0.1396F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(491, 132).addBox(-2.90F, -0.20F, -3.00F, 5.80F, 12.00F, 6.00F)
            .texOffs(5, 177).addBox(2.30F, 1.20F, -2.40F, 1.20F, 7.30F, 4.80F)
            , PartPose.offsetAndRotation(5.50F, -23.00F, 0.00F, 0.1396F, -0.1745F, 0.1396F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
            .texOffs(15, 174).addBox(-2.50F, -2.70F, -4.40F, 5.00F, 4.20F, 1.80F)
            .texOffs(29, 173).addBox(-1.10F, -1.70F, -4.90F, 2.20F, 2.20F, 1.10F)
            .texOffs(42, 179).addBox(-3.10F, -0.10F, -3.60F, 6.20F, 9.10F, 7.20F)
            .texOffs(70, 180).addBox(-3.30F, 8.30F, -4.20F, 6.60F, 3.20F, 8.00F)
            .texOffs(94, 173).addBox(-2.50F, 9.30F, -5.00F, 5.00F, 2.20F, 1.20F)
            , PartPose.offsetAndRotation(0.00F, 11.50F, 0.00F, -0.1396F, 0.0000F, -0.1396F));
        PartDefinition orbiting_swords_root = root.addOrReplaceChild("orbiting_swords_root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00F, -34.00F, 0.00F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_0 = orbiting_swords_root.addOrReplaceChild("orbit_sword_0", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(10.52F, -1.00F, -25.41F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_1 = orbiting_swords_root.addOrReplaceChild("orbit_sword_1", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(25.41F, -1.00F, -10.52F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_2 = orbiting_swords_root.addOrReplaceChild("orbit_sword_2", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(25.41F, -1.00F, 10.52F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_3 = orbiting_swords_root.addOrReplaceChild("orbit_sword_3", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(10.52F, -1.00F, 25.41F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_4 = orbiting_swords_root.addOrReplaceChild("orbit_sword_4", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-10.52F, -1.00F, 25.41F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_5 = orbiting_swords_root.addOrReplaceChild("orbit_sword_5", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-25.41F, -1.00F, 10.52F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_6 = orbiting_swords_root.addOrReplaceChild("orbit_sword_6", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-25.41F, -1.00F, -10.52F, 0.0000F, 0.0000F, 0.0000F));
        PartDefinition orbit_sword_7 = orbiting_swords_root.addOrReplaceChild("orbit_sword_7", CubeListBuilder.create()
            .texOffs(448, 192).addBox(-3.60F, -10.00F, 0.00F, 7.20F, 20.00F, 0.20F)
            .texOffs(0, 0).addBox(0.00F, -10.00F, -3.60F, 0.20F, 20.00F, 7.20F)
            , PartPose.offsetAndRotation(-10.52F, -1.00F, -25.41F, 0.0000F, 0.0000F, 0.0000F));
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

        // Continuous counter-rotating radiant halos (all 3 rings in Phase 2)
        this.haloInner.zRot = age * 0.06F * speedMul;
        this.haloOuter.zRot = -age * 0.05F * speedMul;
        this.haloTier3.visible = state.phase2;
        this.haloTier3.zRot = age * 0.035F * speedMul;

        // 360-degree uniform orbiting flying-sword ring in Phase 2
        this.orbitingSwordsRoot.visible = state.phase2;
        this.orbitingSwordsRoot.yRot = age * 0.045F;
        this.orbitingSwordsRoot.y = Mth.sin(age * 0.08F) * 1.6F;

        // Heavy armored walk stride
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        this.rightLeg.xRot = Mth.cos(walkPos * 0.6662F) * 0.55F * walkSpeed;
        this.leftLeg.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 0.55F * walkSpeed;

        // Halberd cleave swing animation
        if (state.attackTicks > 0) {
            float progress = 1.0F - (state.attackTicks / 16.0F);
            float swing = Mth.sin(progress * Mth.PI);
            this.rightArm.xRot = 0.31F - swing * 1.45F;
            this.chest.yRot = -0.35F + swing * 0.75F;
        } else {
            this.rightArm.xRot = 0.31F + Mth.sin(age * 0.06F) * 0.04F;
            this.chest.yRot = 0.0F;
        }
        this.leftArm.xRot = 0.17F - Mth.sin(age * 0.06F) * 0.04F;
    }
}
