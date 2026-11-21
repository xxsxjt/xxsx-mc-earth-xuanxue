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
 * 3D projectile model for {@link com.xxsx.xuanhuanearth.entity.AstralFlyingSwordEntity},
 * UV-mapped to the astral flying-sword sprite region on {@code astral_sovereign_p2.png}.
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
                // Crossed flat starlight sword blade planes matching orbiting sword proportions
                .texOffs(448, 192).addBox(-3.6F, -10.0F, -0.35F, 7.2F, 20.0F, 0.7F)
                .texOffs(448, 192).addBox(-0.35F, -10.0F, -3.6F, 0.7F, 20.0F, 7.2F),
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
