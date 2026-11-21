package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.model.AstralSovereignModel;
import com.xxsx.xuanhuanearth.entity.AstralSovereignEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renderer for the Astral Sovereign Boss.
 * Renders the articulated 3D entity model AND submits dedicated, zero-reuse,
 * emissive translucent 3D skill VFX for all 6 distinct combat actions.
 */
public class AstralSovereignRenderer extends MobRenderer<AstralSovereignEntity, AstralSovereignRenderState, AstralSovereignModel> {
    private static final Identifier TEXTURE_PHASE1 =
            XuanhuanEarth.id("textures/entity/astral_sovereign_p1.png");
    private static final Identifier TEXTURE_PHASE2 =
            XuanhuanEarth.id("textures/entity/astral_sovereign_p2.png");

    // 7 dedicated zero-reuse VFX textures (one unique visual identity per skill!)
    private static final Identifier VFX_SWEEP =
            XuanhuanEarth.id("textures/entity/astral_vfx_sweep.png");
    private static final Identifier VFX_OVERHEAD =
            XuanhuanEarth.id("textures/entity/astral_vfx_overhead.png");
    private static final Identifier VFX_CRATER =
            XuanhuanEarth.id("textures/entity/astral_vfx_crater.png");
    private static final Identifier VFX_CONSTELLATION =
            XuanhuanEarth.id("textures/entity/astral_vfx_constellation.png");
    private static final Identifier VFX_SWORD_SEAL =
            XuanhuanEarth.id("textures/entity/astral_vfx_sword_seal.png");
    private static final Identifier VFX_BAGUA =
            XuanhuanEarth.id("textures/entity/astral_vfx_bagua.png");
    private static final Identifier VFX_BEAM =
            XuanhuanEarth.id("textures/entity/astral_vfx_beam.png");

    private static final int FULLBRIGHT = 0xF000F0;

    public AstralSovereignRenderer(EntityRendererProvider.Context context) {
        super(context, new AstralSovereignModel(context.bakeLayer(AstralSovereignModel.LAYER_LOCATION)), 1.05F);
    }

    @Override
    public AstralSovereignRenderState createRenderState() {
        return new AstralSovereignRenderState();
    }

    @Override
    public void extractRenderState(AstralSovereignEntity entity, AstralSovereignRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.phase2 = entity.isPhase2();
        state.actionType = entity.getActionType();
        state.actionProgress = entity.getActionProgress();
        state.swordBuffActive = entity.isSwordBuffActive();
    }

    @Override
    public Identifier getTextureLocation(AstralSovereignRenderState state) {
        if (state.actionType == AstralSovereignEntity.ACTION_PHASE_TRANSITION) {
            return state.actionProgress >= 0.40F ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
        }
        return state.phase2 ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
    }

    @Override
    public void submit(
            AstralSovereignRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraRenderState
    ) {
        super.submit(state, poseStack, collector, cameraRenderState);

        int action = state.actionType;
        float p = Mth.clamp(state.actionProgress, 0.0F, 1.0F);
        float age = state.ageInTicks;

        if (action == AstralSovereignEntity.ACTION_NONE && !state.swordBuffActive) {
            return;
        }

        poseStack.pushPose();
        // Align VFX with Boss facing yaw
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));

        switch (action) {
            case AstralSovereignEntity.ACTION_COMBO_SWEEP -> submitSweepCrescent(poseStack, collector, p);
            case AstralSovereignEntity.ACTION_COMBO_OVERHEAD -> submitOverheadLightning(poseStack, collector, p);
            case AstralSovereignEntity.ACTION_COMBO_THRUST -> submitThrustLance(poseStack, collector, p, age);
            case AstralSovereignEntity.ACTION_PHASE_TRANSITION -> submitPhaseTransitionVfx(poseStack, collector, p, age);
            case AstralSovereignEntity.ACTION_SWORD_ARRAY_BUFF -> submitSwordArrayBuffVfx(poseStack, collector, p, age);
            case AstralSovereignEntity.ACTION_ULTIMATE_JUDGMENT -> submitUltimateBaguaVfx(poseStack, collector, p, age);
            default -> {
                if (state.swordBuffActive) {
                    submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 2.85F, 0.06F, age * 0.06F, 180, 235, 255, 135);
                }
            }
        }

        poseStack.popPose();
    }

    /** 1) combo_sweep ONLY: Cool Aqua-Silver Horizontal Chest-Height Lunar Crescent Wave */
    private void submitSweepCrescent(PoseStack poseStack, SubmitNodeCollector collector, float p) {
        if (p < 0.24F || p > 0.78F) return;
        float t = (p - 0.24F) / 0.54F;
        float alpha = Mth.sin(t * Mth.PI);
        int a = (int) (alpha * 235.0F);
        float yawStart = Mth.lerp(t, -1.65F, 0.35F);
        float arcSpan = 1.65F;
        float innerR = 1.45F;
        float outerR = 3.55F + t * 0.45F;
        float y = 2.05F; // Chest height horizontal plane!

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWEEP), (pose, vc) -> {
            int segs = 18;
            for (int i = 0; i < segs; i++) {
                float u0 = (float) i / segs;
                float u1 = (float) (i + 1) / segs;
                float a0 = yawStart + u0 * arcSpan;
                float a1 = yawStart + u1 * arcSpan;
                float s0 = Mth.sin(a0), c0 = Mth.cos(a0);
                float s1 = Mth.sin(a1), c1 = Mth.cos(a1);
                quadBothSides(pose, vc,
                        s0 * outerR, y, c0 * outerR, u0, 0.0F,
                        s1 * outerR, y, c1 * outerR, u1, 0.0F,
                        s1 * innerR, y - 0.12F, c1 * innerR, u1, 1.0F,
                        s0 * innerR, y - 0.12F, c0 * innerR, u0, 1.0F,
                        215, 252, 255, a);
            }
        });
    }

    /** 2) combo_overhead ONLY: Royal-Violet & Solar-Gold Lightning Cleave + Molten Crater + 3D Crystal Spires */
    private void submitOverheadLightning(PoseStack poseStack, SubmitNodeCollector collector, float p) {
        if (p >= 0.34F && p <= 0.76F) {
            float t = (p - 0.34F) / 0.42F;
            int a = (int) (Mth.sin(t * Mth.PI) * 245.0F);
            float pitchStart = Mth.lerp(t, -0.25F, 1.45F);
            float arcSpan = 1.55F;
            float innerR = 1.35F;
            float outerR = 3.75F;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_OVERHEAD), (pose, vc) -> {
                int segs = 16;
                for (int i = 0; i < segs; i++) {
                    float u0 = (float) i / segs;
                    float u1 = (float) (i + 1) / segs;
                    float ang0 = pitchStart - (1.0F - u0) * arcSpan;
                    float ang1 = pitchStart - (1.0F - u1) * arcSpan;
                    float y0 = 2.25F + Mth.cos(ang0) * outerR;
                    float z0 = 0.55F + Mth.sin(ang0) * outerR;
                    float y1 = 2.25F + Mth.cos(ang1) * outerR;
                    float z1 = 0.55F + Mth.sin(ang1) * outerR;
                    float iy0 = 2.25F + Mth.cos(ang0) * innerR;
                    float iz0 = 0.55F + Mth.sin(ang0) * innerR;
                    float iy1 = 2.25F + Mth.cos(ang1) * innerR;
                    float iz1 = 0.55F + Mth.sin(ang1) * innerR;
                    quadBothSides(pose, vc,
                            0.0F, y0, z0, u0, 0.0F,
                            0.0F, y1, z1, u1, 0.0F,
                            0.0F, iy1, iz1, u1, 1.0F,
                            0.0F, iy0, iz0, u0, 1.0F,
                            255, 240, 255, a);
                }
            });
        }
        if (p >= 0.48F && p <= 0.94F) {
            float ct = (p - 0.48F) / 0.46F;
            float fade = 1.0F - ct * ct;
            int ca = (int) (fade * 235.0F);
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 2.35F);
            submitGroundDecal(poseStack, collector, VFX_CRATER, 1.75F + ct * 0.75F, 0.06F, 0.0F, 255, 235, 255, ca);
            // 8 erupting tilted amethyst-gold crystal shards around impact crater
            float spireH = (0.45F + Mth.sin(Math.min(1.0F, ct * 2.2F) * Mth.HALF_PI) * 1.45F) * fade;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                for (int i = 0; i < 8; i++) {
                    float a = (i / 8.0F) * Mth.TWO_PI + 0.25F;
                    float r0 = 0.75F;
                    float r1 = 1.35F;
                    float x0 = Mth.cos(a) * r0, z0 = Mth.sin(a) * r0;
                    float x1 = Mth.cos(a) * r1, z1 = Mth.sin(a) * r1;
                    float px = -Mth.sin(a) * 0.22F, pz = Mth.cos(a) * 0.22F;
                    quadBothSides(pose, vc,
                            x0 - px, 0.05F, z0 - pz, 0.0F, 1.0F,
                            x0 + px, 0.05F, z0 + pz, 1.0F, 1.0F,
                            x1 + px * 0.2F, spireH, z1 + pz * 0.2F, 1.0F, 0.0F,
                            x1 - px * 0.2F, spireH, z1 - pz * 0.2F, 0.0F, 0.0F,
                            192, 132, 252, ca);
                }
            });
            poseStack.popPose();
        }
    }

    /** 3) combo_thrust ONLY: Emerald-Jade Forward Mach Shock-Cone & Piercing Lance Streaks */
    private void submitThrustLance(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        if (p < 0.28F || p > 0.84F) return;
        float t = (p - 0.28F) / 0.56F;
        float env = Mth.sin(t * Mth.PI);
        int a = (int) (env * 235.0F);
        float zTip = 4.65F + t * 0.95F;
        float zBase = 1.15F;
        float y = 2.0F;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            int petals = 6;
            for (int i = 0; i < petals; i++) {
                float ang = (i / (float) petals) * Mth.TWO_PI + age * 0.35F;
                float rx = Mth.cos(ang) * 0.68F;
                float ry = Mth.sin(ang) * 0.68F;
                quadBothSides(pose, vc,
                        rx * 0.14F, y + ry * 0.14F, zTip, 0.5F, 0.0F,
                        rx, y + ry, zBase, 0.0F, 1.0F,
                        -rx * 0.25F, y - ry * 0.25F, zBase, 1.0F, 1.0F,
                        -rx * 0.14F, y - ry * 0.14F, zTip, 0.5F, 0.0F,
                        52, 211, 153, a);
            }
        });
    }

    /** 4) phase_transition ONLY: Indigo Inward Suction (P1) -> 28-Mansion Constellation + Twin Golden-Violet Helices */
    private void submitPhaseTransitionVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        // Stage 1 (0..36%): Pure Phase 1 inward collapsing dark-star suction rings
        if (p < 0.38F) {
            float st = p / 0.38F;
            int a = (int) (Mth.sin(st * Mth.PI) * 215.0F);
            for (int i = 0; i < 3; i++) {
                float r = Mth.lerp(st, 3.2F - i * 0.35F, 0.85F);
                float y = 0.65F + i * 0.95F;
                submitHorizontalRing(poseStack, collector, r, r + 0.22F, y, 129, 140, 248, a);
            }
        }
        // Stage 2 (32..96%): 28-Mansion Astronomical Constellation Chart + Twin Ascending Golden-Violet Helices
        if (p >= 0.30F && p <= 0.96F) {
            float bt = (p - 0.30F) / 0.66F;
            float fade = bt < 0.15F ? bt / 0.15F : (bt > 0.82F ? (1.0F - bt) / 0.18F : 1.0F);
            int ca = (int) (fade * 230.0F);
            submitGroundDecal(poseStack, collector, VFX_CONSTELLATION, 3.2F + bt * 0.8F, 0.07F, age * 0.06F, 255, 245, 255, ca);

            if (p <= 0.82F) {
                float ht = (p - 0.30F) / 0.52F;
                float maxH = 1.2F + ht * 5.2F;
                int ha = (int) (Mth.sin(ht * Mth.PI) * 235.0F);
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                    int segs = 28;
                    for (int strand = 0; strand < 2; strand++) {
                        int cr = strand == 0 ? 250 : 168;
                        int cg = strand == 0 ? 184 : 85;
                        int cb = strand == 0 ? 32 : 247;
                        float phase = strand * Mth.PI + age * 0.28F;
                        for (int i = 0; i < segs; i++) {
                            float u0 = (float) i / segs;
                            float u1 = (float) (i + 1) / segs;
                            float a0 = u0 * Mth.TWO_PI * 2.2F + phase;
                            float a1 = u1 * Mth.TWO_PI * 2.2F + phase;
                            float r0 = 1.65F * (1.1F - u0 * 0.28F);
                            float r1 = 1.65F * (1.1F - u1 * 0.28F);
                            float y0 = u0 * maxH;
                            float y1 = u1 * maxH;
                            float x0 = Mth.cos(a0) * r0, z0 = Mth.sin(a0) * r0;
                            float x1 = Mth.cos(a1) * r1, z1 = Mth.sin(a1) * r1;
                            quadBothSides(pose, vc,
                                    x0, y0 - 0.16F, z0, 0.0F, u0,
                                    x1, y1 - 0.16F, z1, 0.0F, u1,
                                    x1, y1 + 0.16F, z1, 1.0F, u1,
                                    x0, y0 + 0.16F, z0, 1.0F, u0,
                                    cr, cg, cb, ha);
                        }
                    }
                });
            }
        }
    }

    /** 5) sword_array_buff ONLY: 16-Sword-Silhouette Seal + 8 Floating Octagonal Void Sword Portals */
    private void submitSwordArrayBuffVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float env = p < 0.15F ? p / 0.15F : (p > 0.85F ? (1.0F - p) / 0.15F : 1.0F);
        int a = (int) (env * 235.0F);
        submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 3.6F, 0.07F, -age * 0.08F, 225, 248, 255, a);

        // 8 floating golden octagonal Void Sword Portals around wide perimeter
        for (int i = 0; i < 8; i++) {
            float ang = (i / 8.0F) * Mth.TWO_PI + age * 0.04F;
            float r = 2.85F + (i % 2) * 0.45F;
            float y = 2.1F + (i % 3) * 0.55F + Mth.sin(age * 0.12F + i) * 0.18F;
            poseStack.pushPose();
            poseStack.translate(Mth.cos(ang) * r, y, Mth.sin(ang) * r);
            poseStack.mulPose(Axis.YP.rotation(-ang + Mth.HALF_PI));
            poseStack.mulPose(Axis.ZP.rotation(age * 0.14F));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWORD_SEAL), (pose, vc) -> {
                float s = 0.55F * env;
                quadBothSides(pose, vc,
                        -s, -s, 0.0F, 0.0F, 1.0F,
                        s, -s, 0.0F, 1.0F, 1.0F,
                        s, s, 0.0F, 1.0F, 0.0F,
                        -s, s, 0.0F, 0.0F, 0.0F,
                        253, 224, 71, a);
            });
            poseStack.popPose();
        }
    }

    /** 6) ultimate_astral_judgment ONLY: Authentic Eight-Trigram Taiji Bagua Array + 360° Fiery-Azure Shockwave */
    private void submitUltimateBaguaVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float fade = p < 0.12F ? p / 0.12F : (p > 0.86F ? (1.0F - p) / 0.14F : 1.0F);
        int ba = (int) (fade * 240.0F);
        float baguaScale = p < 0.50F ? Mth.lerp(p / 0.50F, 2.2F, 4.4F) : 4.4F + (p - 0.50F) * 1.4F;
        submitGroundDecal(poseStack, collector, VFX_BAGUA, baguaScale, 0.08F, age * 0.05F, 255, 248, 235, ba);

        // 360° dual-layer fiery-orange & deep-azure tsunami shockwave after slam (p >= 0.48)
        if (p >= 0.48F && p <= 0.94F) {
            float st = (p - 0.48F) / 0.46F;
            int sa = (int) ((1.0F - st) * 245.0F);
            float r1 = 1.2F + st * 6.8F;
            float r2 = 0.9F + st * 5.4F;
            submitHorizontalRing(poseStack, collector, r1 - 0.55F, r1, 0.16F, 251, 146, 60, sa);
            submitHorizontalRing(poseStack, collector, r2 - 0.42F, r2, 0.30F, 56, 189, 248, (int) (sa * 0.9F));
        }
    }

    private void submitGroundDecal(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Identifier tex,
            float halfSize,
            float y,
            float rotY,
            int r, int g, int b, int a
    ) {
        if (a <= 4) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(rotY));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(tex), (pose, vc) ->
                quadBothSides(pose, vc,
                        -halfSize, y, -halfSize, 0.0F, 0.0F,
                        halfSize, y, -halfSize, 1.0F, 0.0F,
                        halfSize, y, halfSize, 1.0F, 1.0F,
                        -halfSize, y, halfSize, 0.0F, 1.0F,
                        r, g, b, a));
        poseStack.popPose();
    }

    private void submitHorizontalRing(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float innerR,
            float outerR,
            float y,
            int r, int g, int b, int a
    ) {
        if (a <= 4 || outerR <= innerR) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            int segs = 28;
            for (int i = 0; i < segs; i++) {
                float a0 = (i / (float) segs) * Mth.TWO_PI;
                float a1 = ((i + 1) / (float) segs) * Mth.TWO_PI;
                float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
                quadBothSides(pose, vc,
                        c0 * innerR, y, s0 * innerR, 0.0F, 0.5F,
                        c1 * innerR, y, s1 * innerR, 0.0F, 0.5F,
                        c1 * outerR, y, s1 * outerR, 1.0F, 0.5F,
                        c0 * outerR, y, s0 * outerR, 1.0F, 0.5F,
                        r, g, b, a);
            }
        });
    }

    private static void quadBothSides(
            PoseStack.Pose pose,
            VertexConsumer vc,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int r, int g, int b, int a
    ) {
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        // Back face so transparent VFX never cull from any camera angle
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
    }
}
