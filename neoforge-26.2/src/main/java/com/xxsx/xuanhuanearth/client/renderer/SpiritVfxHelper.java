package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Shared Boss-grade 3D translucent emissive VFX helper for Spirit Beasts and Cultivation Settlers.
 */
public final class SpiritVfxHelper {
    public static final Identifier TEX_BAGUA = XuanhuanEarth.id("textures/entity/astral_vfx_bagua.png");
    public static final Identifier TEX_SWORD_SEAL = XuanhuanEarth.id("textures/entity/astral_vfx_sword_seal.png");
    public static final Identifier TEX_CONSTELLATION = XuanhuanEarth.id("textures/entity/astral_vfx_constellation.png");
    public static final Identifier TEX_BEAM = XuanhuanEarth.id("textures/entity/astral_vfx_beam.png");
    public static final Identifier TEX_SWEEP = XuanhuanEarth.id("textures/entity/astral_vfx_sweep.png");
    private static final int FULLBRIGHT = 0xF000F0;

    private SpiritVfxHelper() {}

    public static void submitGroundSeal(PoseStack poseStack, SubmitNodeCollector collector, Identifier tex,
                                        float radius, float y, float rotRad, int r, int g, int b, int a) {
        if (a <= 4) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(rotRad));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(tex), (pose, vc) ->
                quadBothSides(pose, vc,
                        -radius, y, -radius, 0.0F, 0.0F,
                        radius, y, -radius, 1.0F, 0.0F,
                        radius, y, radius, 1.0F, 1.0F,
                        -radius, y, radius, 0.0F, 1.0F,
                        r, g, b, a));
        poseStack.popPose();
    }

    public static void submitBackHalo(PoseStack poseStack, SubmitNodeCollector collector, Identifier tex,
                                      float bodyRotDeg, float y, float backOffset, float radius, float spinRad,
                                      int r, int g, int b, int a) {
        if (a <= 4) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRotDeg));
        poseStack.translate(0.0F, y, backOffset);
        poseStack.mulPose(Axis.ZP.rotation(spinRad));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(tex), (pose, vc) ->
                quadBothSides(pose, vc,
                        -radius, -radius, 0.0F, 0.0F, 0.0F,
                        radius, -radius, 0.0F, 1.0F, 0.0F,
                        radius, radius, 0.0F, 1.0F, 1.0F,
                        -radius, radius, 0.0F, 0.0F, 1.0F,
                        r, g, b, a));
        poseStack.popPose();
    }

    public static void submitOrbitOrbs(PoseStack poseStack, SubmitNodeCollector collector,
                                       int count, float orbitR, float baseY, float orbSize, float age,
                                       int r, int g, int b, int a) {
        if (a <= 4) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(TEX_CONSTELLATION), (pose, vc) -> {
            for (int i = 0; i < count; i++) {
                float ang = (float) (i * Math.PI * 2.0 / count) + age * 0.10F;
                float ox = Mth.cos(ang) * orbitR;
                float oz = Mth.sin(ang) * orbitR;
                float oy = baseY + Mth.sin(age * 0.16F + i * 1.4F) * 0.16F;
                quadBothSides(pose, vc,
                        ox - orbSize, oy - orbSize, oz, 0.0F, 0.0F,
                        ox + orbSize, oy - orbSize, oz, 1.0F, 0.0F,
                        ox + orbSize, oy + orbSize, oz, 1.0F, 1.0F,
                        ox - orbSize, oy + orbSize, oz, 0.0F, 1.0F,
                        r, g, b, a);
                quadBothSides(pose, vc,
                        ox, oy - orbSize, oz - orbSize, 0.0F, 0.0F,
                        ox, oy - orbSize, oz + orbSize, 1.0F, 0.0F,
                        ox, oy + orbSize, oz + orbSize, 1.0F, 1.0F,
                        ox, oy + orbSize, oz - orbSize, 0.0F, 1.0F,
                        r, g, b, a);
            }
        });
    }

    public static void submitAuraRing(PoseStack poseStack, SubmitNodeCollector collector,
                                      float radius, float height, float y, float spinRad,
                                      int r, int g, int b, int a) {
        if (a <= 4) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(TEX_BEAM), (pose, vc) -> {
            int segs = 20;
            for (int i = 0; i < segs; i++) {
                float a0 = (float) (i * Math.PI * 2.0 / segs) + spinRad;
                float a1 = (float) ((i + 1) * Math.PI * 2.0 / segs) + spinRad;
                quadBothSides(pose, vc,
                        Mth.cos(a0) * radius, y, Mth.sin(a0) * radius, 0.0F, 1.0F,
                        Mth.cos(a1) * radius, y, Mth.sin(a1) * radius, 1.0F, 1.0F,
                        Mth.cos(a1) * radius, y + height, Mth.sin(a1) * radius, 1.0F, 0.0F,
                        Mth.cos(a0) * radius, y + height, Mth.sin(a0) * radius, 0.0F, 0.0F,
                        r, g, b, a);
            }
        });
    }

    public static void submitAttackSlash(PoseStack poseStack, SubmitNodeCollector collector,
                                         float bodyRotDeg, float y, float radius, float attackProgress,
                                         int r, int g, int b) {
        if (attackProgress <= 0.02F) return;
        float alpha = Mth.sin(Mth.clamp(attackProgress, 0.0F, 1.0F) * Mth.PI);
        int a = (int) (alpha * 230.0F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRotDeg));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(TEX_SWEEP), (pose, vc) -> {
            int segs = 16;
            float yawStart = Mth.lerp(attackProgress, -1.4F, 0.25F);
            float span = 1.9F;
            float rIn = radius * 0.42F;
            for (int i = 0; i < segs; i++) {
                float u0 = (float) i / segs;
                float u1 = (float) (i + 1) / segs;
                float a0 = yawStart + u0 * span;
                float a1 = yawStart + u1 * span;
                quadBothSides(pose, vc,
                        Mth.sin(a0) * rIn, y, -Mth.cos(a0) * rIn, u0, 1.0F,
                        Mth.sin(a1) * rIn, y, -Mth.cos(a1) * rIn, u1, 1.0F,
                        Mth.sin(a1) * radius, y + 0.14F, -Mth.cos(a1) * radius, u1, 0.0F,
                        Mth.sin(a0) * radius, y + 0.14F, -Mth.cos(a0) * radius, u0, 0.0F,
                        r, g, b, a);
            }
        });
        poseStack.popPose();
    }

    public static void quadBothSides(PoseStack.Pose pose, VertexConsumer vc,
                                     float x0, float y0, float z0, float u0, float v0,
                                     float x1, float y1, float z1, float u1, float v1,
                                     float x2, float y2, float z2, float u2, float v2,
                                     float x3, float y3, float z3, float u3, float v3,
                                     int r, int g, int b, int a) {
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);

        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
    }
}
