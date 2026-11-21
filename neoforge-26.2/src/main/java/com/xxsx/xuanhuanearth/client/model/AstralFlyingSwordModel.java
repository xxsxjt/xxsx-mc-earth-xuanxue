package com.xxsx.xuanhuanearth.client.model;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.renderer.AstralFlyingSwordRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * 3D sculpted celestial flying-sword projectile model for {@link com.xxsx.xuanhuanearth.entity.AstralFlyingSwordEntity},
 * mapped strictly to opaque cyan-crystal, imperial-gold, and crimson swatches on {@code astral_sovereign_p2.png}.
 */
public final class AstralFlyingSwordModel extends EntityModel<AstralFlyingSwordRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(XuanhuanEarth.id("astral_flying_sword"), "main");

    private final ModelPart swordRoot;

    public AstralFlyingSwordModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.swordRoot = root.getChild("sword_root");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("sword_root", CubeListBuilder.create()
                // Razor-sharp tapered luminous cyan blade pointing toward -Y (aligned to velocity in renderer)
                .texOffs(448, 32).addBox(-0.85F, -10.50F, -0.35F, 1.70F, 12.00F, 0.70F)
                .texOffs(448, 0).addBox(-1.35F, -8.50F, -0.22F, 2.70F, 9.50F, 0.44F)
                .texOffs(448, 32).addBox(-0.45F, -13.50F, -0.28F, 0.90F, 3.20F, 0.56F)
                // Bright golden central fuller spine
                .texOffs(416, 32).addBox(-0.32F, -10.20F, -0.52F, 0.64F, 11.40F, 1.04F)
                // Ornate imperial-gold winged crossguard
                .texOffs(416, 0).addBox(-3.20F, 1.20F, -0.75F, 6.40F, 1.20F, 1.50F)
                .texOffs(416, 32).addBox(-1.10F, 0.70F, -0.95F, 2.20F, 1.80F, 1.90F)
                // Crimson-wrapped hilt & gold pommel
                .texOffs(480, 0).addBox(-0.65F, 2.40F, -0.65F, 1.30F, 4.40F, 1.30F)
                .texOffs(416, 0).addBox(-1.00F, 6.60F, -1.00F, 2.00F, 1.40F, 2.00F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(AstralFlyingSwordRenderState state) {
        super.setupAnim(state);
        float spinRate = state.returningAfterDeflect ? 0.45F : (state.empowered ? 0.28F : 0.12F);
        this.swordRoot.yRot = state.ageInTicks * spinRate;
    }
}
