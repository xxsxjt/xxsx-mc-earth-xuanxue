package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.model.AstralFlyingSwordModel;
import com.xxsx.xuanhuanearth.entity.AstralFlyingSwordEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class AstralFlyingSwordRenderer extends EntityRenderer<AstralFlyingSwordEntity, AstralFlyingSwordRenderState> {
    private static final Identifier SWORD_TEXTURE =
            XuanhuanEarth.id("textures/entity/astral_sovereign_p2.png");

    private final AstralFlyingSwordModel model;

    public AstralFlyingSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AstralFlyingSwordModel(context.bakeLayer(AstralFlyingSwordModel.LAYER_LOCATION));
    }

    @Override
    public AstralFlyingSwordRenderState createRenderState() {
        return new AstralFlyingSwordRenderState();
    }

    @Override
    public void extractRenderState(AstralFlyingSwordEntity entity, AstralFlyingSwordRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.empowered = entity.isEmpowered();
        state.returningAfterDeflect = entity.isReturningAfterDeflect();
    }

    @Override
    public void submit(
            AstralFlyingSwordRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        // Align vertical sword tip along forward velocity vector
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - state.xRot));
        float scale = state.empowered ? 1.18F : 0.96F;
        poseStack.scale(scale, scale, scale);

        collector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutout(SWORD_TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        if (state.empowered || state.returningAfterDeflect) {
            collector.submitModel(
                    this.model,
                    state,
                    poseStack,
                    RenderTypes.entityTranslucentEmissive(SWORD_TEXTURE),
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor,
                    null
            );
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraRenderState);
    }
}
