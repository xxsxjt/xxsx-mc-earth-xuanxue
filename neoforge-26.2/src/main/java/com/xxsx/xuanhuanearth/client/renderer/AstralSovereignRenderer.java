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
 * Renderer for the Astral Sovereign (镇岳星君) with multi-layer soft-translucent gradient 3D VFX
 * (ethereal spirit ribbons, crossed volumetric light pillars, Daoist talisman monoliths,
 * and 10 original Xianxia Celestial Sword-Vault Portals behind the enlarged rear halos).
 */
public class AstralSovereignRenderer
        extends MobRenderer<AstralSovereignEntity, AstralSovereignRenderState, AstralSovereignModel> {

    private static final Identifier PHASE1_TEXTURE = XuanhuanEarth.id("textures/entity/astral_sovereign_p1.png");
    private static final Identifier PHASE2_TEXTURE = XuanhuanEarth.id("textures/entity/astral_sovereign_p2.png");
    private static final Identifier VFX_SWEEP = XuanhuanEarth.id("textures/entity/astral_vfx_sweep.png");
    private static final Identifier VFX_OVERHEAD = XuanhuanEarth.id("textures/entity/astral_vfx_overhead.png");
    private static final Identifier VFX_CRATER = XuanhuanEarth.id("textures/entity/astral_vfx_crater.png");
    private static final Identifier VFX_CONSTELLATION = XuanhuanEarth.id("textures/entity/astral_vfx_constellation.png");
    private static final Identifier VFX_SWORD_SEAL = XuanhuanEarth.id("textures/entity/astral_vfx_sword_seal.png");
    private static final Identifier VFX_BAGUA = XuanhuanEarth.id("textures/entity/astral_vfx_bagua.png");
    private static final Identifier VFX_BEAM = XuanhuanEarth.id("textures/entity/astral_vfx_beam.png");
    private static final Identifier VFX_RIBBON = XuanhuanEarth.id("textures/entity/astral_vfx_ribbon.png");
    private static final Identifier VFX_TALISMAN = XuanhuanEarth.id("textures/entity/astral_vfx_talisman.png");
    private static final Identifier VFX_SWORD_GATE = XuanhuanEarth.id("textures/entity/astral_vfx_sword_gate.png");
    private static final int FULLBRIGHT = 0x00F000F0;

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
            return state.actionProgress >= 0.48F ? PHASE2_TEXTURE : PHASE1_TEXTURE;
        }
        return state.phase2 ? PHASE2_TEXTURE : PHASE1_TEXTURE;
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

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));

        switch (action) {
            case AstralSovereignEntity.ACTION_COMBO_SWEEP -> submitSweepArc(poseStack, collector, p, state.phase2);
            case AstralSovereignEntity.ACTION_COMBO_OVERHEAD -> submitOverheadLightning(poseStack, collector, p, state.phase2);
            case AstralSovereignEntity.ACTION_COMBO_THRUST -> submitThrustLance(poseStack, collector, p, age, state.phase2);
            case AstralSovereignEntity.ACTION_PHASE_TRANSITION -> submitPhaseTransitionVfx(poseStack, collector, p, age);
            case AstralSovereignEntity.ACTION_SWORD_ARRAY_BUFF -> submitSwordArrayBuffVfx(poseStack, collector, p, age, 1.0F);
            case AstralSovereignEntity.ACTION_ULTIMATE_JUDGMENT -> submitUltimateBaguaVfx(poseStack, collector, p, age);
            default -> {
                if (state.swordBuffActive) {
                    submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 3.1F, 0.06F, age * 0.06F, 175, 232, 255, 95);
                    submitSwordArrayBuffVfx(poseStack, collector, 0.48F, age, 0.48F);
                }
            }
        }
        poseStack.popPose();
    }

    private void submitSweepArc(PoseStack poseStack, SubmitNodeCollector collector, float p, boolean phase2) {
        if (p < 0.22F || p > 0.88F) return;
        float t = (p - 0.22F) / 0.66F;
        float fade = Mth.sin(t * Mth.PI);
        int a = (int) (fade * 195.0F);
        float innerR = phase2 ? 2.4F : 2.1F;
        float outerR = phase2 ? 8.4F : 6.0F;
        float yBase = 1.92F;
        float arcSpan = phase2 ? 3.25F : 2.35F;
        float yawStart = Mth.lerp(t, -1.78F, 0.35F);

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWEEP), (pose, vc) -> {
            int segs = 28;
            for (int i = 0; i < segs; i++) {
                float u0 = (float) i / segs, u1 = (float) (i + 1) / segs;
                float a0 = yawStart + u0 * arcSpan, a1 = yawStart + u1 * arcSpan;
                float s0 = Mth.sin(a0), c0 = -Mth.cos(a0);
                float s1 = Mth.sin(a1), c1 = -Mth.cos(a1);
                int rIn = phase2 ? 255 : 155, gIn = phase2 ? 195 : 225, bIn = phase2 ? 90 : 255;
                int rOut = phase2 ? 255 : 220, gOut = phase2 ? 245 : 248, bOut = phase2 ? 180 : 255;
                quadGradientBothSides(pose, vc,
                        s0 * innerR, yBase, c0 * innerR, u0, 1.0F, rIn, gIn, bIn, (int) (a * 0.35F),
                        s1 * innerR, yBase, c1 * innerR, u1, 1.0F, rIn, gIn, bIn, (int) (a * 0.35F),
                        s1 * outerR, yBase + 0.22F, c1 * outerR, u1, 0.0F, rOut, gOut, bOut, a,
                        s0 * outerR, yBase + 0.22F, c0 * outerR, u0, 0.0F, rOut, gOut, bOut, a);
                if (phase2) {
                    float o2 = outerR + 0.90F, i2 = innerR + 0.45F;
                    quadGradientBothSides(pose, vc,
                            s0 * i2, yBase + 0.40F, c0 * i2, u0, 1.0F, 255, 110, 75, (int) (a * 0.28F),
                            s1 * i2, yBase + 0.40F, c1 * i2, u1, 1.0F, 255, 110, 75, (int) (a * 0.28F),
                            s1 * o2, yBase + 0.62F, c1 * o2, u1, 0.0F, 255, 175, 105, (int) (a * 0.72F),
                            s0 * o2, yBase + 0.62F, c0 * o2, u0, 0.0F, 255, 175, 105, (int) (a * 0.72F));
                }
            }
        });
    }

    private void submitOverheadLightning(PoseStack poseStack, SubmitNodeCollector collector, float p, boolean phase2) {
        if (p < 0.35F) return;
        float t = (p - 0.35F) / 0.65F;
        float fade = Mth.sin(Mth.clamp(t, 0.0F, 1.0F) * Mth.PI);
        float maxDist = phase2 ? 12.2F : 9.2F;
        float reach = Mth.lerp(Mth.clamp(t * 1.45F, 0.0F, 1.0F), 1.4F, maxDist);
        int lanes = phase2 ? 3 : 1;

        for (int lane = 0; lane < lanes; lane++) {
            float laneYaw = lane == 0 ? 0.0F : (lane == 1 ? -0.34F : 0.34F);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotation(laneYaw));
            int pillars = phase2 ? 5 : 4;
            for (int k = 1; k <= pillars; k++) {
                float frac = (float) k / pillars;
                float z = -reach * frac;
                float h = (phase2 ? 5.6F : 4.2F) * (1.15F - frac * 0.22F);
                float w = phase2 ? 0.85F : 0.62F;
                submitVolumetricPillar(poseStack, collector, 0.0F, 0.05F, z, w, h,
                        phase2 ? 255 : 140, phase2 ? 205 : 225, phase2 ? 115 : 255,
                        (int) (fade * 165.0F));
            }
            submitGroundDecal(poseStack, collector, VFX_CRATER, 1.6F + reach * 0.22F, 0.05F, 0.0F,
                    phase2 ? 255 : 145, phase2 ? 195 : 225, phase2 ? 95 : 255, (int) (fade * 155.0F));
            poseStack.popPose();
        }
    }

    private void submitThrustLance(PoseStack poseStack, SubmitNodeCollector collector, float p, float age, boolean phase2) {
        if (p < 0.25F || p > 0.92F) return;
        float t = (p - 0.25F) / 0.67F;
        float fade = Mth.sin(t * Mth.PI);
        float reach = Mth.lerp(Mth.clamp(t * 1.35F, 0.0F, 1.0F), 2.2F, phase2 ? 11.8F : 8.8F);

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_RIBBON), (pose, vc) -> {
            int helices = phase2 ? 2 : 1;
            int segs = 22;
            for (int h = 0; h < helices; h++) {
                float off = h * Mth.PI;
                for (int i = 0; i < segs; i++) {
                    float u0 = (float) i / segs, u1 = (float) (i + 1) / segs;
                    float z0 = -1.2F - u0 * reach, z1 = -1.2F - u1 * reach;
                    float r0 = (1.0F - u0 * 0.6F) * (phase2 ? 0.82F : 0.55F);
                    float r1 = (1.0F - u1 * 0.6F) * (phase2 ? 0.82F : 0.55F);
                    float a0 = u0 * 11.0F + off + age * 0.35F;
                    float a1 = u1 * 11.0F + off + age * 0.35F;
                    float x0 = Mth.cos(a0) * r0, y0 = 1.95F + Mth.sin(a0) * r0;
                    float x1 = Mth.cos(a1) * r1, y1 = 1.95F + Mth.sin(a1) * r1;
                    float w = phase2 ? 0.28F : 0.20F;
                    int alpha0 = (int) (fade * 175.0F * Mth.sin(u0 * Mth.PI));
                    int alpha1 = (int) (fade * 175.0F * Mth.sin(u1 * Mth.PI));
                    quadGradientBothSides(pose, vc,
                            x0 - w, y0 - w * 0.3F, z0, u0, 1.0F, 110, 222, 255, (int) (alpha0 * 0.45F),
                            x1 - w, y1 - w * 0.3F, z1, u1, 1.0F, 110, 222, 255, (int) (alpha1 * 0.45F),
                            x1 + w, y1 + w * 0.3F, z1, u1, 0.0F, phase2 ? 255 : 200, phase2 ? 225 : 245, phase2 ? 130 : 255, alpha1,
                            x0 + w, y0 + w * 0.3F, z0, u0, 0.0F, phase2 ? 255 : 200, phase2 ? 225 : 245, phase2 ? 130 : 255, alpha0);
                }
            }
        });
        if (phase2 && t > 0.42F) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.95F, -1.2F - reach);
            poseStack.mulPose(Axis.ZP.rotation(age * 0.18F));
            float nr = 2.6F * Mth.sin(Mth.clamp((t - 0.42F) / 0.58F, 0.0F, 1.0F) * Mth.PI);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_CONSTELLATION), (pose, vc) ->
                    quadGradientBothSides(pose, vc,
                            -nr, -nr, 0, 0, 1, 255, 222, 128, (int) (fade * 185.0F),
                             nr, -nr, 0, 1, 1, 255, 222, 128, (int) (fade * 185.0F),
                             nr,  nr, 0, 1, 0, 255, 245, 200, (int) (fade * 185.0F),
                            -nr,  nr, 0, 0, 0, 255, 245, 200, (int) (fade * 185.0F)));
            poseStack.popPose();
        }
    }

    /** Phase 1->2 awakening: soft translucent multi-tone silk ribbons + Daoist talisman monoliths + ground-slam shockwave */
    private void submitPhaseTransitionVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float floorAlpha = p < 0.78F ? Mth.sin(Mth.clamp(p / 0.78F, 0.0F, 1.0F) * Mth.PI) : 0.0F;
        if (floorAlpha > 0.02F) {
            submitGroundDecal(poseStack, collector, VFX_BAGUA, 4.8F + p * 1.4F, 0.06F, age * 0.055F,
                    255, 214, 118, (int) (floorAlpha * 165.0F));
            submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 3.2F + p * 0.8F, 0.09F, -age * 0.08F,
                    125, 232, 255, (int) (floorAlpha * 120.0F));
        }

        // 8 Translucent Daoist Golden Rune Talisman Monoliths standing around the circle
        if (p > 0.05F && p < 0.72F) {
            float mFade = Mth.sin(Mth.clamp((p - 0.05F) / 0.67F, 0.0F, 1.0F) * Mth.PI);
            float mRad = 3.85F - p * 0.45F;
            float mH = 2.65F * mFade;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_TALISMAN), (pose, vc) -> {
                for (int i = 0; i < 8; i++) {
                    float ang = (Mth.TWO_PI * i) / 8.0F + age * 0.015F;
                    float cx = Mth.cos(ang) * mRad;
                    float cz = Mth.sin(ang) * mRad;
                    float tx = -Mth.sin(ang) * 0.42F;
                    float tz = Mth.cos(ang) * 0.42F;
                    int aBot = (int) (mFade * 175.0F);
                    int aTop = (int) (mFade * 155.0F);
                    quadGradientBothSides(pose, vc,
                            cx - tx, 0.25F, cz - tz, 0, 1, 255, 206, 98, aBot,
                            cx + tx, 0.25F, cz + tz, 1, 1, 255, 206, 98, aBot,
                            cx + tx, 0.25F + mH, cz + tz, 1, 0, 255, 240, 175, aTop,
                            cx - tx, 0.25F + mH, cz - tz, 0, 0, 255, 240, 175, aTop);
                }
            });
        }

        // Double-layered soft translucent celestial silk helix ribbons (gold -> cyan -> violet, feathered to 0 alpha at ends)
        if (p > 0.08F && p < 0.74F) {
            float rFade = Mth.sin(Mth.clamp((p - 0.08F) / 0.66F, 0.0F, 1.0F) * Mth.PI);
            float bossRise = Mth.sin(Mth.clamp((p - 0.08F) / 0.60F, 0.0F, 1.0F) * Mth.PI) * 3.4F;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_RIBBON), (pose, vc) -> {
                int ribbons = 3;
                int segs = 28;
                for (int rb = 0; rb < ribbons; rb++) {
                    float phaseOff = (Mth.TWO_PI * rb) / ribbons;
                    for (int i = 0; i < segs; i++) {
                        float u0 = (float) i / segs, u1 = (float) (i + 1) / segs;
                        float env0 = Mth.sin(u0 * Mth.PI), env1 = Mth.sin(u1 * Mth.PI);
                        float rad0 = 1.65F + 1.35F * (1.0F - u0 * 0.45F);
                        float rad1 = 1.65F + 1.35F * (1.0F - u1 * 0.45F);
                        float ang0 = u0 * 9.2F + phaseOff + age * 0.16F;
                        float ang1 = u1 * 9.2F + phaseOff + age * 0.16F;
                        float y0 = 0.6F + u0 * (3.6F + bossRise);
                        float y1 = 0.6F + u1 * (3.6F + bossRise);
                        float x0 = Mth.cos(ang0) * rad0, z0 = Mth.sin(ang0) * rad0;
                        float x1 = Mth.cos(ang1) * rad1, z1 = Mth.sin(ang1) * rad1;
                        // Gold -> Cyan -> Violet vertical multi-tone gradient
                        int r0 = (int) Mth.lerp(u0, 255, 165), g0 = (int) Mth.lerp(u0, 215, 195), b0 = (int) Mth.lerp(u0, 115, 255);
                        int r1 = (int) Mth.lerp(u1, 255, 165), g1 = (int) Mth.lerp(u1, 215, 195), b1 = (int) Mth.lerp(u1, 115, 255);
                        float bw = 0.44F;
                        int a0 = (int) (rFade * env0 * 145.0F);
                        int a1 = (int) (rFade * env1 * 145.0F);
                        quadGradientBothSides(pose, vc,
                                x0, y0 - bw, z0, u0, 1.0F, r0, g0, b0, 0,
                                x1, y1 - bw, z1, u1, 1.0F, r1, g1, b1, 0,
                                x1, y1 + bw, z1, u1, 0.0F, r1, g1, b1, a1,
                                x0, y0 + bw, z0, u0, 0.0F, r0, g0, b0, a0);
                    }
                }
            });
        }

        // Meteor-dive ground-slam 360° translucent shockwave ring
        if (p >= 0.72F) {
            float st = (p - 0.72F) / 0.28F;
            float sFade = 1.0F - st;
            float rWave = Mth.lerp(st, 1.8F, 13.2F);
            submitShockwaveWall(poseStack, collector, rWave, 1.9F * sFade, 255, 214, 128, (int) (sFade * 165.0F));
            submitGroundDecal(poseStack, collector, VFX_CRATER, rWave * 0.78F, 0.07F, age * 0.04F,
                    140, 228, 255, (int) (sFade * 145.0F));
        }
    }

    /**
     * Phase 2「万剑天罡阵」:
     * 10 original Chinese Xianxia Celestial Sword-Vault Portals (星渊八卦剑门) opening in a 2-tier fan-arc
     * behind the Boss's enlarged halos, each with a translucent 3D sword tip emerging forward before launch!
     */
    private void submitSwordArrayBuffVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age, float intensity) {
        float openEnv = Mth.sin(Mth.clamp(p, 0.0F, 1.0F) * Mth.PI);
        float alphaMul = Mth.clamp(openEnv * intensity * 1.15F, 0.0F, 1.0F);
        if (alphaMul <= 0.03F) return;

        submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 4.2F, 0.06F, age * 0.075F,
                155, 232, 255, (int) (alphaMul * 145.0F));

        // 2-tier rear fan-arc of 10 Celestial Sword-Vault Portals (6 outer arc + 4 inner upper arc)
        for (int i = 0; i < 10; i++) {
            boolean outer = i < 6;
            int idx = outer ? i : (i - 6);
            int count = outer ? 6 : 4;
            float frac = (count > 1) ? ((float) idx / (count - 1)) * 2.0F - 1.0F : 0.0F;
            float angle = frac * (outer ? 1.24F : 0.88F);
            float radius = outer ? 4.45F : 3.25F;
            float gx = Mth.sin(angle) * radius;
            float gy = (outer ? 2.55F : 3.85F) + Mth.cos(angle) * (outer ? 1.55F : 0.95F);
            float gz = (outer ? 2.35F : 1.95F) + Math.abs(frac) * 0.65F; // strictly behind enlarged halos!

            float gateScale = (outer ? 0.96F : 0.78F) * (0.65F + 0.35F * openEnv);
            int aGate = (int) (alphaMul * (outer ? 225.0F : 195.0F));

            poseStack.pushPose();
            poseStack.translate(gx, gy, gz);
            // Tilt slightly inward toward forward direction (-Z)
            poseStack.mulPose(Axis.YP.rotation(-angle * 0.22F));

            // Swirling cosmic indigo-azure + golden Bagua portal disc
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotation(age * (outer ? 0.085F : -0.105F) + i * 0.7F));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWORD_GATE), (pose, vc) ->
                    quadGradientBothSides(pose, vc,
                            -gateScale, -gateScale, 0.0F, 0, 1, 255, 255, 255, aGate,
                             gateScale, -gateScale, 0.0F, 1, 1, 255, 255, 255, aGate,
                             gateScale,  gateScale, 0.0F, 1, 0, 255, 255, 255, aGate,
                            -gateScale,  gateScale, 0.0F, 0, 0, 255, 255, 255, aGate));
            poseStack.popPose();

            // Translucent celestial sword blade slowly emerging forward (-Z) out of each gate!
            float emerge = 0.55F + 0.75F * Mth.sin(Mth.clamp(p * 1.1F, 0.0F, 1.0F) * Mth.PI);
            float bladeLen = 1.35F * emerge;
            int aBlade = (int) (alphaMul * 215.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_RIBBON), (pose, vc) -> {
                for (int pl = 0; pl < 2; pl++) {
                    float dx = pl == 0 ? 0.14F : 0.0F;
                    float dy = pl == 0 ? 0.0F : 0.14F;
                    quadGradientBothSides(pose, vc,
                            -dx, -dy,  0.12F, 0.0F, 0.0F, 110, 225, 255, 35,
                             dx,  dy,  0.12F, 0.0F, 1.0F, 110, 225, 255, 35,
                             dx * 0.25F,  dy * 0.25F, -bladeLen, 1.0F, 1.0F, 255, 240, 175, aBlade,
                            -dx * 0.25F, -dy * 0.25F, -bladeLen, 1.0F, 0.0F, 255, 240, 175, aBlade);
                }
            });
            poseStack.popPose();
        }
    }

    /** Ultimate「八荒星陨」: Dual Bagua floor array + 6 crossed volumetric translucent light pillars + feathered 360° tsunami shockwave */
    private void submitUltimateBaguaVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float telegraphEnd = 0.54F;
        float arrayAlpha = p < telegraphEnd
                ? Mth.sin((p / telegraphEnd) * Mth.HALF_PI)
                : Math.max(0.0F, 1.0F - (p - telegraphEnd) / (1.0F - telegraphEnd));

        submitGroundDecal(poseStack, collector, VFX_BAGUA, 8.6F, 0.06F, age * 0.035F,
                255, 212, 115, (int) (arrayAlpha * 185.0F));
        submitGroundDecal(poseStack, collector, VFX_CONSTELLATION, 5.4F, 0.09F, -age * 0.06F,
                130, 232, 255, (int) (arrayAlpha * 145.0F));

        // 6 soft crossed volumetric translucent light pillars rising around the Bagua array
        if (arrayAlpha > 0.04F) {
            for (int i = 0; i < 6; i++) {
                float ang = (Mth.TWO_PI * i) / 6.0F + age * 0.025F;
                float px = Mth.cos(ang) * 6.2F;
                float pz = Mth.sin(ang) * 6.2F;
                submitVolumetricPillar(poseStack, collector, px, 0.08F, pz, 0.95F, 10.5F,
                        i % 2 == 0 ? 130 : 255, i % 2 == 0 ? 228 : 210, i % 2 == 0 ? 255 : 120,
                        (int) (arrayAlpha * 155.0F));
            }
        }

        // Feathered translucent 360° tsunami shockwave wall
        if (p >= telegraphEnd) {
            float wt = (p - telegraphEnd) / (1.0F - telegraphEnd);
            float wFade = 1.0F - wt;
            float radius = Mth.lerp(wt, 2.0F, 15.5F);
            submitShockwaveWall(poseStack, collector, radius, 2.8F * (0.4F + 0.6F * wFade),
                    135, 230, 255, (int) (wFade * 175.0F));
            submitShockwaveWall(poseStack, collector, radius * 0.84F, 1.8F * wFade,
                    255, 212, 118, (int) (wFade * 140.0F));
        }
    }

    /** Multi-layer crossed volumetric translucent light pillar (outer soft mist + bright inner core, feathered to 0 alpha at top) */
    private void submitVolumetricPillar(
            PoseStack poseStack, SubmitNodeCollector collector,
            float cx, float yBase, float cz, float width, float height,
            int r, int g, int b, int alpha
    ) {
        if (alpha <= 3) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            // Outer misty crossed planes
            for (int k = 0; k < 2; k++) {
                float dx = k == 0 ? width : 0.0F;
                float dz = k == 0 ? 0.0F : width;
                quadGradientBothSides(pose, vc,
                        cx - dx, yBase, cz - dz, 0, 1, r, g, b, (int) (alpha * 0.72F),
                        cx + dx, yBase, cz + dz, 1, 1, r, g, b, (int) (alpha * 0.72F),
                        cx + dx * 0.85F, yBase + height, cz + dz * 0.85F, 1, 0, r, g, b, 0,
                        cx - dx * 0.85F, yBase + height, cz - dz * 0.85F, 0, 0, r, g, b, 0);
                // Bright narrow inner core
                float cdx = dx * 0.42F, cdz = dz * 0.42F;
                quadGradientBothSides(pose, vc,
                        cx - cdx, yBase, cz - cdz, 0, 1, 240, 250, 255, alpha,
                        cx + cdx, yBase, cz + cdz, 1, 1, 240, 250, 255, alpha,
                        cx + cdx * 0.7F, yBase + height * 0.88F, cz + cdz * 0.7F, 1, 0, 225, 246, 255, 0,
                        cx - cdx * 0.7F, yBase + height * 0.88F, cz - cdz * 0.7F, 0, 0, 225, 246, 255, 0);
            }
        });
    }

    /** Translucent cylindrical shockwave wall whose top crest feathers smoothly to alpha = 0 */
    private void submitShockwaveWall(
            PoseStack poseStack, SubmitNodeCollector collector,
            float radius, float height, int r, int g, int b, int alpha
    ) {
        if (alpha <= 3 || height <= 0.05F) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            int segs = 32;
            for (int i = 0; i < segs; i++) {
                float a0 = (Mth.TWO_PI * i) / segs;
                float a1 = (Mth.TWO_PI * (i + 1)) / segs;
                float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
                float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
                quadGradientBothSides(pose, vc,
                        x0, 0.08F, z0, 0, 1, r, g, b, alpha,
                        x1, 0.08F, z1, 1, 1, r, g, b, alpha,
                        x1 * 1.04F, 0.08F + height, z1 * 1.04F, 1, 0, 235, 248, 255, 0,
                        x0 * 1.04F, 0.08F + height, z0 * 1.04F, 0, 0, 235, 248, 255, 0);
            }
        });
    }

    private void submitGroundDecal(
            PoseStack poseStack, SubmitNodeCollector collector, Identifier tex,
            float radius, float y, float rotRad, int r, int g, int b, int a
    ) {
        if (a <= 3) return;
        poseStack.pushPose();
        poseStack.translate(0.0F, y, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(rotRad));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(tex), (pose, vc) ->
                quadGradientBothSides(pose, vc,
                        -radius, 0.0F, -radius, 0, 0, r, g, b, a,
                         radius, 0.0F, -radius, 1, 0, r, g, b, a,
                         radius, 0.0F,  radius, 1, 1, r, g, b, a,
                        -radius, 0.0F,  radius, 0, 1, r, g, b, a));
        poseStack.popPose();
    }

    private static void quadGradientBothSides(
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
