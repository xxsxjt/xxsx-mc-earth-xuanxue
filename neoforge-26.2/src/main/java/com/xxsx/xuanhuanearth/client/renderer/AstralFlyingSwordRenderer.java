package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * Renders the 3D Astral Flying Sword plus a crossed soft-translucent sword-qi ribbon tail.
 */
public class AstralFlyingSwordRenderer extends EntityRenderer<AstralFlyingSwordEntity, AstralFlyingSwordRenderState> {

    private static final Identifier TEXTURE = XuanhuanEarth.id("textures/entity/astral_sovereign_p2.png");
    private static final Identifier VFX_RIBBON = XuanhuanEarth.id("textures/entity/astral_vfx_ribbon.png");
    private static final int FULLBRIGHT = 0x00F000F0;

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
        state.deflected = entity.isDeflected();
    }

    @Override
    public void submit(
            AstralFlyingSwordRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        // 3D sword body
        poseStack.pushPose();
        float s = state.empowered ? 1.18F : 1.0F;
        poseStack.scale(s, s, s);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        collector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(TEXTURE),
                FULLBRIGHT,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                state.outlineColor,
                null
        );
        poseStack.popPose();

        // Crossed translucent flowing sword-qi ribbon tail fading softly to 0 alpha
        float tailLen = state.empowered ? 2.85F : 2.05F;
        float tailW = state.empowered ? 0.24F : 0.17F;
        int rH = state.deflected ? 255 : (state.empowered ? 255 : 145);
        int gH = state.deflected ? 130 : (state.empowered ? 228 : 235);
        int bH = state.deflected ? 90 : (state.empowered ? 140 : 255);
        int rT = state.deflected ? 255 : 95;
        int gT = state.deflected ? 70 : 195;
        int bT = state.deflected ? 70 : 255;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_RIBBON), (pose, vc) -> {
            for (int plane = 0; plane < 2; plane++) {
                float dx = plane == 0 ? tailW : 0.0F;
                float dy = plane == 0 ? 0.0F : tailW;
                // Front half (head -> mid)
                quadBoth(pose, vc,
                        -dx, -dy, -0.10F, 1.0F, 0.0F, rH, gH, bH, 195,
                         dx,  dy, -0.10F, 1.0F, 1.0F, rH, gH, bH, 195,
                         dx * 0.72F, dy * 0.72F, -tailLen * 0.45F, 0.5F, 1.0F, rT, gT, bT, 120,
                        -dx * 0.72F, -dy * 0.72F, -tailLen * 0.45F, 0.5F, 0.0F, rT, gT, bT, 120);
                // Trailing feather (mid -> 0 alpha tip)
                quadBoth(pose, vc,
                        -dx * 0.72F, -dy * 0.72F, -tailLen * 0.45F, 0.5F, 0.0F, rT, gT, bT, 120,
                         dx * 0.72F,  dy * 0.72F, -tailLen * 0.45F, 0.5F, 1.0F, rT, gT, bT, 120,
                         dx * 0.25F,  dy * 0.25F, -tailLen, 0.0F, 1.0F, rT, gT, bT, 0,
                        -dx * 0.25F, -dy * 0.25F, -tailLen, 0.0F, 0.0F, rT, gT, bT, 0);
            }
        });

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraRenderState);
    }

    private static void quadBoth(
            PoseStack.Pose pose, VertexConsumer vc,
            float x0, float y0, float z0, float u0, float v0, int r0, int g0, int b0, int a0,
            float x1, float y1, float z1, float u1, float v1, int r1, int g1, int b1, int a1,
            float x2, float y2, float z2, float u2, float v2, int r2, int g2, int b2, int a2,
            float x3, float y3, float z3, float u3, float v3, int r3, int g3, int b3, int a3
    ) {
        vc.addVertex(pose, x0, y0, z0).setColor(r0, g0, b0, a0).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, x2, y2, z2).setColor(r2, g2, b2, a2).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, x3, y3, z3).setColor(r3, g3, b3, a3).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, 1, 0);

        vc.addVertex(pose, x3, y3, z3).setColor(r3, g3, b3, a3).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, -1, 0);
        vc.addVertex(pose, x2, y2, z2).setColor(r2, g2, b2, a2).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, -1, 0);
        vc.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, -1, 0);
        vc.addVertex(pose, x0, y0, z0).setColor(r0, g0, b0, a0).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0, -1, 0);
    }
}
