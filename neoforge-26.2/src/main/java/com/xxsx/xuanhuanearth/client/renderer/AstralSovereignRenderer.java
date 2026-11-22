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
        if (state.actionType == AstralSovereignEntity.ACTION_PHASE_TRANSITION) {
            return state.actionProgress >= 0.48F ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
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
            case AstralSovereignEntity.ACTION_COMBO_SWEEP -> submitSweepCrescent(poseStack, collector, p, state.phase2);
            case AstralSovereignEntity.ACTION_COMBO_OVERHEAD -> submitOverheadLightning(poseStack, collector, p, state.phase2);
            case AstralSovereignEntity.ACTION_COMBO_THRUST -> submitThrustLance(poseStack, collector, p, age, state.phase2);
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
    /** 1) combo_sweep: P1 Single Cold-Silver Arc (6.2m) -> P2 Twin-Moon Golden-Crimson Starfire Arc (8.5m) + 3 Phantom Trailing Blades! */
    private void submitSweepCrescent(PoseStack poseStack, SubmitNodeCollector collector, float p, boolean phase2) {
        if (p < 0.20F || p > 0.84F) return;
        float t = (p - 0.20F) / 0.64F;
        float alpha = Mth.sin(t * Mth.PI);
        int a = (int) (alpha * 242.0F);
        float yawStart = Mth.lerp(t, phase2 ? -2.25F : -1.85F, phase2 ? 0.65F : 0.40F);
        float arcSpan = phase2 ? 2.95F : 2.30F;
        float innerR = 1.85F;
        float outerR = (phase2 ? 7.45F : 5.45F) + t * (phase2 ? 1.15F : 0.85F);
        float yBase = 1.92F;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWEEP), (pose, vc) -> {
            int segs = 32;
            for (int i = 0; i < segs; i++) {
                float u0 = (float) i / segs;
                float u1 = (float) (i + 1) / segs;
                float a0 = yawStart + u0 * arcSpan;
                float a1 = yawStart + u1 * arcSpan;
                float s0 = Mth.sin(a0), c0 = -Mth.cos(a0);
                float s1 = Mth.sin(a1), c1 = -Mth.cos(a1);
                int r1 = phase2 ? 255 : 185, g1 = phase2 ? 225 : 238, b1 = phase2 ? 110 : 255;
                quadBothSides(pose, vc,
                        s0 * innerR, yBase, c0 * innerR, u0, 1.0F,
                        s1 * innerR, yBase, c1 * innerR, u1, 1.0F,
                        s1 * outerR, yBase + 0.22F, c1 * outerR, u1, 0.0F,
                        s0 * outerR, yBase + 0.22F, c0 * outerR, u0, 0.0F,
                        r1, g1, b1, a);
                if (phase2) {
                    float o2 = outerR + 0.88F;
                    float i2 = innerR + 0.35F;
                    quadBothSides(pose, vc,
                            s0 * i2, yBase + 0.42F, c0 * i2, u0, 1.0F,
                            s1 * i2, yBase + 0.42F, c1 * i2, u1, 1.0F,
                            s1 * o2, yBase + 0.64F, c1 * o2, u1, 0.0F,
                            s0 * o2, yBase + 0.64F, c0 * o2, u0, 0.0F,
                            255, 115, 78, (int) (a * 0.82F));
                    float o3 = outerR - 0.55F;
                    float i3 = innerR + 0.65F;
                    quadBothSides(pose, vc,
                            s0 * i3, yBase - 0.36F, c0 * i3, u0, 1.0F,
                            s1 * i3, yBase - 0.36F, c1 * i3, u1, 1.0F,
                            s1 * o3, yBase - 0.24F, c1 * o3, u1, 0.0F,
                            s0 * o3, yBase - 0.24F, c0 * o3, u0, 0.0F,
                            130, 235, 255, (int) (a * 0.65F));
                }
            }
        });

        if (phase2) {
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                for (int k = 0; k < 3; k++) {
                    float ang = yawStart + arcSpan * (0.25F + k * 0.28F);
                    float rad = outerR - 0.85F + (k % 2) * 0.65F;
                    float sx = Mth.sin(ang) * rad, sz = -Mth.cos(ang) * rad;
                    float tx = Mth.cos(ang), tz = Mth.sin(ang);
                    float y = yBase + (k - 1) * 0.42F;
                    float len = 1.85F;
                    quadBothSides(pose, vc,
                            sx - tx * len, y - 0.14F, sz - tz * len, 0.0F, 0.0F,
                            sx + tx * len, y - 0.14F, sz + tz * len, 1.0F, 0.0F,
                            sx + tx * len, y + 0.14F, sz + tz * len, 1.0F, 1.0F,
                            sx - tx * len, y + 0.14F, sz - tz * len, 0.0F, 1.0F,
                            255, 240, 165, (int) (a * 0.92F));
                }
            });
        }
    }

    /** 2) combo_overhead: P1 Single 7m Cyan-Gold Fissure -> P2 9.5m TRIPLE-LANE Fan Fissures + 15 Gilded Obelisks + Shockwave! */
    private void submitOverheadLightning(PoseStack poseStack, SubmitNodeCollector collector, float p, boolean phase2) {
        if (p < 0.28F || p > 0.94F) return;
        if (p <= 0.72F) {
            float vt = Mth.clamp((p - 0.28F) / 0.44F, 0.0F, 1.0F);
            int va = (int) (Mth.sin(vt * Mth.PI) * 245.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_OVERHEAD), (pose, vc) -> {
                int segs = 22;
                float rIn = 1.6F;
                float rOut = phase2 ? 6.6F : 5.0F;
                float startAng = Mth.lerp(vt, 1.45F, -0.35F);
                float span = phase2 ? 2.25F : 1.95F;
                int cr = phase2 ? 255 : 165, cg = phase2 ? 195 : 235, cb = phase2 ? 75 : 255;
                for (int i = 0; i < segs; i++) {
                    float u0 = (float) i / segs;
                    float u1 = (float) (i + 1) / segs;
                    float a0 = startAng - u0 * span;
                    float a1 = startAng - u1 * span;
                    float y0 = 2.5F + Mth.sin(a0) * rIn, z0 = -Mth.cos(a0) * rIn;
                    float y1 = 2.5F + Mth.sin(a1) * rIn, z1 = -Mth.cos(a1) * rIn;
                    float oy0 = 2.5F + Mth.sin(a0) * rOut, oz0 = -Mth.cos(a0) * rOut;
                    float oy1 = 2.5F + Mth.sin(a1) * rOut, oz1 = -Mth.cos(a1) * rOut;
                    quadBothSides(pose, vc,
                            0.0F, y0, z0, u0, 1.0F,
                            0.0F, y1, z1, u1, 1.0F,
                            0.0F, oy1, oz1, u1, 0.0F,
                            0.0F, oy0, oz0, u0, 0.0F,
                            cr, cg, cb, va);
                }
            });
        }
        if (p >= 0.46F) {
            float ct = Mth.clamp((p - 0.46F) / 0.48F, 0.0F, 1.0F);
            float fade = 1.0F - ct * ct;
            int ca = (int) (fade * 242.0F);
            float fissureLen = 2.2F + Math.min(1.0F, ct * 2.5F) * (phase2 ? 7.4F : 5.0F);
            float[] lanes = phase2 ? new float[]{0.0F, 0.48F, -0.48F} : new float[]{0.0F};

            for (float laneYaw : lanes) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotation(laneYaw));
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_CRATER), (pose, vc) -> {
                    float halfW = phase2 ? 1.55F : 1.15F;
                    int fr = phase2 ? 255 : 150, fg = phase2 ? 190 : 225, fb = phase2 ? 75 : 255;
                    quadBothSides(pose, vc,
                            -halfW, 0.08F, -1.6F, 0.0F, 0.0F,
                            halfW, 0.08F, -1.6F, 1.0F, 0.0F,
                            halfW, 0.08F, -1.6F - fissureLen, 1.0F, 1.0F,
                            -halfW, 0.08F, -1.6F - fissureLen, 0.0F, 1.0F,
                            fr, fg, fb, ca);
                });
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                    int obelisks = phase2 ? 5 : 3;
                    for (int k = 0; k < obelisks; k++) {
                        float zPos = -2.2F - k * (fissureLen / obelisks);
                        float delay = Math.max(0.0F, ct - k * 0.08F);
                        float h = Mth.clamp(delay * 5.2F, 0.0F, 1.0F) * (phase2 ? 3.9F : 2.6F) * fade;
                        if (h <= 0.05F) continue;
                        float w = phase2 ? 0.46F : 0.34F;
                        int or = phase2 ? 255 : 140, og = phase2 ? 215 : 230, ob = phase2 ? 100 : 255;
                        quadBothSides(pose, vc,
                                -w, 0.05F, zPos, 0.0F, 1.0F,
                                w, 0.05F, zPos, 1.0F, 1.0F,
                                0.0F, 0.05F + h, zPos, 0.5F, 0.0F,
                                0.0F, 0.05F + h, zPos, 0.5F, 0.0F,
                                or, og, ob, ca);
                        quadBothSides(pose, vc,
                                0.0F, 0.05F, zPos - w, 0.0F, 1.0F,
                                0.0F, 0.05F, zPos + w, 1.0F, 1.0F,
                                0.0F, 0.05F + h, zPos, 0.5F, 0.0F,
                                0.0F, 0.05F + h, zPos, 0.5F, 0.0F,
                                255, 240, 175, (int) (ca * 0.85F));
                    }
                });
                poseStack.popPose();
            }
            if (phase2) {
                submitGroundDecal(poseStack, collector, VFX_CRATER, 2.2F + ct * 5.8F, 0.12F, ct * 2.2F, 255, 175, 75, (int) (fade * 210.0F));
            }
        }
    }

    /** 3) combo_thrust: P1 8.5m Cyan Lance + 2 Rings -> P2 12.5m Golden-Crimson Twin-Helix Dragon Lance + 5 Sonic Rings + 8-Ray Tip Star-Nova! */
    private void submitThrustLance(PoseStack poseStack, SubmitNodeCollector collector, float p, float age, boolean phase2) {
        if (p < 0.24F || p > 0.88F) return;
        float t = (p - 0.24F) / 0.64F;
        float fade = Mth.sin(t * Mth.PI);
        int a = (int) (fade * 242.0F);
        float lanceLen = (phase2 ? 12.5F : 8.5F) * Math.min(1.0F, t * 2.6F);
        float yMid = 1.95F;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            float half = phase2 ? 0.88F : 0.62F;
            int r1 = phase2 ? 255 : 145, g1 = phase2 ? 210 : 235, b1 = phase2 ? 95 : 255;
            quadBothSides(pose, vc,
                    -half, yMid, -1.0F, 0.0F, 0.0F,
                    half, yMid, -1.0F, 1.0F, 0.0F,
                    half * 0.35F, yMid, -1.0F - lanceLen, 1.0F, 1.0F,
                    -half * 0.35F, yMid, -1.0F - lanceLen, 0.0F, 1.0F,
                    r1, g1, b1, a);
            quadBothSides(pose, vc,
                    0.0F, yMid - half, -1.0F, 0.0F, 0.0F,
                    0.0F, yMid + half, -1.0F, 1.0F, 0.0F,
                    0.0F, yMid + half * 0.35F, -1.0F - lanceLen, 1.0F, 1.0F,
                    0.0F, yMid - half * 0.35F, -1.0F - lanceLen, 0.0F, 1.0F,
                    phase2 ? 255 : 200, phase2 ? 125 : 245, phase2 ? 80 : 255, a);
        });

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            int rings = phase2 ? 5 : 2;
            for (int k = 0; k < rings; k++) {
                float zPos = -2.2F - k * (lanceLen / (rings + 0.6F));
                float rIn = (phase2 ? 0.65F : 0.55F) + k * 0.14F;
                float rOut = rIn + (phase2 ? 0.58F : 0.42F);
                int segs = 16;
                for (int s = 0; s < segs; s++) {
                    float a0 = (float) (s * Math.PI * 2.0 / segs) + age * 0.12F;
                    float a1 = (float) ((s + 1) * Math.PI * 2.0 / segs) + age * 0.12F;
                    quadBothSides(pose, vc,
                            Mth.cos(a0) * rIn, yMid + Mth.sin(a0) * rIn, zPos, 0.0F, 0.0F,
                            Mth.cos(a1) * rIn, yMid + Mth.sin(a1) * rIn, zPos, 1.0F, 0.0F,
                            Mth.cos(a1) * rOut, yMid + Mth.sin(a1) * rOut, zPos, 1.0F, 1.0F,
                            Mth.cos(a0) * rOut, yMid + Mth.sin(a0) * rOut, zPos, 0.0F, 1.0F,
                            phase2 ? 255 : 140, phase2 ? 200 : 235, phase2 ? 95 : 255, (int) (a * 0.85F));
                }
            }
            if (phase2 && t > 0.32F) {
                float nt = Mth.clamp((t - 0.32F) / 0.68F, 0.0F, 1.0F);
                float novaR = 0.8F + nt * 3.8F;
                int na = (int) ((1.0F - nt) * 245.0F);
                float tipZ = -1.0F - lanceLen;
                for (int ray = 0; ray < 8; ray++) {
                    float ra = (float) (ray * Math.PI * 2.0 / 8.0) + age * 0.08F;
                    float rx = Mth.cos(ra) * novaR, ry = Mth.sin(ra) * novaR;
                    float px = -Mth.sin(ra) * 0.22F, py = Mth.cos(ra) * 0.22F;
                    quadBothSides(pose, vc,
                            -px, yMid - py, tipZ, 0.0F, 0.0F,
                            px, yMid + py, tipZ, 1.0F, 0.0F,
                            rx + px * 0.15F, yMid + ry + py * 0.15F, tipZ, 1.0F, 1.0F,
                            rx - px * 0.15F, yMid + ry - py * 0.15F, tipZ, 0.0F, 1.0F,
                            255, 215, 105, na);
                }
            }
        });
    }

    /**
     * 4) phase_transition ONLY (BESPOKE — ZERO plain generic circles!):
     *    - Octagonal Celestial Lock Array + 8 Floating Vertical Golden Talisman Monoliths beaming spirit tethers up to soaring Boss!
     *    - Faceted Polyhedral Starlight Cocoon mid-air shattering at p=0.48 into 16 spinning 3D shards!
     *    - Braided Twin Ascending Golden-Violet Ribbons!
     *    - Ground-Slam Impact Shockwave (p >= 0.77): 8-Ray Jagged Molten-Gold Fissure Star + Cylindrical Shockwave Wall + 12 Angled Crater Spires!
     */
    private void submitPhaseTransitionVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float rise = Mth.clamp((p - 0.10F) / 0.32F, 0.0F, 1.0F);
        float dive = Mth.clamp((p - 0.71F) / 0.09F, 0.0F, 1.0F);
        float bossY = 1.9F + rise * (1.0F - dive) * 3.35F;

        float sealAlpha = p < 0.12F ? p / 0.12F : (p > 0.90F ? (1.0F - p) / 0.10F : 1.0F);
        int sa = (int) (sealAlpha * 238.0F);

        // 4a. Ground Octagonal Celestial Lock Array (sharp 8-sided geometric star, NOT a blurry plain circle!)
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            float octR = 4.6F + rise * 0.8F;
            for (int i = 0; i < 8; i++) {
                float a0 = (float) (i * Math.PI * 2.0 / 8.0) + age * 0.03F;
                float a1 = (float) ((i + 1) * Math.PI * 2.0 / 8.0) + age * 0.03F;
                float a2 = (float) ((i + 3) * Math.PI * 2.0 / 8.0) + age * 0.03F;
                float x0 = Mth.cos(a0) * octR, z0 = Mth.sin(a0) * octR;
                float x1 = Mth.cos(a1) * octR, z1 = Mth.sin(a1) * octR;
                float x2 = Mth.cos(a2) * octR, z2 = Mth.sin(a2) * octR;
                quadBothSides(pose, vc,
                        x0 * 0.94F, 0.10F, z0 * 0.94F, 0.0F, 0.0F,
                        x1 * 0.94F, 0.10F, z1 * 0.94F, 1.0F, 0.0F,
                        x1 * 1.04F, 0.10F, z1 * 1.04F, 1.0F, 1.0F,
                        x0 * 1.04F, 0.10F, z0 * 1.04F, 0.0F, 1.0F,
                        255, 215, 115, sa);
                quadBothSides(pose, vc,
                        x0, 0.12F, z0, 0.0F, 0.0F,
                        x2, 0.12F, z2, 1.0F, 0.0F,
                        x2 * 0.97F, 0.12F, z2 * 0.97F, 1.0F, 1.0F,
                        x0 * 0.97F, 0.12F, z0 * 0.97F, 0.0F, 1.0F,
                        190, 120, 255, (int) (sa * 0.78F));
            }
        });

        // 4b. 8 Floating Vertical Golden Talisman Monoliths + 8 Converging Spirit-Tether Ribbons up to soaring Boss!
        if (p < 0.80F) {
            float mFade = p < 0.12F ? p / 0.12F : (p > 0.72F ? (0.80F - p) / 0.08F : 1.0F);
            int ma = (int) (mFade * 242.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                float ringR = 4.6F - rise * 0.55F;
                for (int i = 0; i < 8; i++) {
                    float ang = (float) (i * Math.PI * 2.0 / 8.0) + age * 0.035F;
                    float mx = Mth.cos(ang) * ringR;
                    float mz = Mth.sin(ang) * ringR;
                    float tx = -Mth.sin(ang) * 0.34F;
                    float tz = Mth.cos(ang) * 0.34F;
                    float baseH = 0.55F + Mth.sin(age * 0.18F + i) * 0.25F + rise * 0.85F;
                    float topH = baseH + 3.15F;
                    quadBothSides(pose, vc,
                            mx - tx, baseH, mz - tz, 0.0F, 1.0F,
                            mx + tx, baseH, mz + tz, 1.0F, 1.0F,
                            mx + tx * 0.72F, topH, mz + tz * 0.72F, 1.0F, 0.0F,
                            mx - tx * 0.72F, topH, mz - tz * 0.72F, 0.0F, 0.0F,
                            255, 220, 110, ma);
                    // Converging spirit-tether ribbon from monolith tip up to soaring Boss chest!
                    quadBothSides(pose, vc,
                            mx - tx * 0.25F, topH - 0.15F, mz - tz * 0.25F, 0.0F, 0.0F,
                            mx + tx * 0.25F, topH - 0.15F, mz + tz * 0.25F, 1.0F, 0.0F,
                            tx * 0.20F, bossY, tz * 0.20F, 1.0F, 1.0F,
                            -tx * 0.20F, bossY, -tz * 0.20F, 0.0F, 1.0F,
                            i % 2 == 0 ? 255 : 195, i % 2 == 0 ? 210 : 125, i % 2 == 0 ? 115 : 255, (int) (ma * 0.75F));
                }
            });
        }

        // 4c. Braided Twin Ascending Golden-Violet Ribbons spiraling around soaring Boss
        if (p >= 0.08F && p <= 0.76F) {
            float rFade = Mth.sin(Mth.clamp((p - 0.08F) / 0.68F, 0.0F, 1.0F) * Mth.PI);
            int ra = (int) (rFade * 225.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                for (int strand = 0; strand < 2; strand++) {
                    int segs = 24;
                    float phaseOff = strand * Mth.PI;
                    int cr = strand == 0 ? 255 : 185, cg = strand == 0 ? 215 : 115, cb = strand == 0 ? 105 : 255;
                    for (int s = 0; s < segs; s++) {
                        float t0 = (float) s / segs;
                        float t1 = (float) (s + 1) / segs;
                        float y0 = 0.25F + t0 * (bossY + 1.8F);
                        float y1 = 0.25F + t1 * (bossY + 1.8F);
                        float rad0 = 3.2F * (1.0F - t0 * 0.55F);
                        float rad1 = 3.2F * (1.0F - t1 * 0.55F);
                        float a0 = t0 * 9.5F + age * 0.16F + phaseOff;
                        float a1 = t1 * 9.5F + age * 0.16F + phaseOff;
                        quadBothSides(pose, vc,
                                Mth.cos(a0) * rad0, y0, Mth.sin(a0) * rad0, t0, 0.0F,
                                Mth.cos(a1) * rad1, y1, Mth.sin(a1) * rad1, t1, 0.0F,
                                Mth.cos(a1) * rad1, y1 + 0.34F, Mth.sin(a1) * rad1, t1, 1.0F,
                                Mth.cos(a0) * rad0, y0 + 0.34F, Mth.sin(a0) * rad0, t0, 1.0F,
                                cr, cg, cb, ra);
                    }
                }
            });
        }

        // 4d. Faceted Octahedral Polyhedral Starlight Cocoon (p=0.14..0.48) -> Shatters into 16 spinning 3D crystal shards (p=0.48..0.78)!
        if (p >= 0.14F && p < 0.48F) {
            float ct = (p - 0.14F) / 0.34F;
            float r = 1.65F * (0.45F + 0.55F * Mth.sin(ct * Mth.HALF_PI));
            float h = 2.45F * (0.55F + 0.45F * ct);
            int ca = (int) ((0.45F + 0.55F * ct) * 210.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_CONSTELLATION), (pose, vc) -> {
                for (int i = 0; i < 8; i++) {
                    float a0 = (float) (i * Math.PI * 2.0 / 8.0) + age * 0.14F;
                    float a1 = (float) ((i + 1) * Math.PI * 2.0 / 8.0) + age * 0.14F;
                    float x0 = Mth.cos(a0) * r, z0 = Mth.sin(a0) * r;
                    float x1 = Mth.cos(a1) * r, z1 = Mth.sin(a1) * r;
                    quadBothSides(pose, vc,
                            x0, bossY, z0, 0.0F, 1.0F,
                            x1, bossY, z1, 1.0F, 1.0F,
                            0.0F, bossY + h, 0.0F, 0.5F, 0.0F,
                            0.0F, bossY + h, 0.0F, 0.5F, 0.0F,
                            255, 225, 135, ca);
                    quadBothSides(pose, vc,
                            x0, bossY, z0, 0.0F, 0.0F,
                            x1, bossY, z1, 1.0F, 0.0F,
                            0.0F, bossY - h * 0.85F, 0.0F, 0.5F, 1.0F,
                            0.0F, bossY - h * 0.85F, 0.0F, 0.5F, 1.0F,
                            185, 125, 255, ca);
                }
            });
        } else if (p >= 0.48F && p <= 0.78F) {
            float st = (p - 0.48F) / 0.30F;
            int sha = (int) ((1.0F - st) * 245.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                for (int i = 0; i < 16; i++) {
                    float a = (float) (i * Math.PI * 2.0 / 16.0) + st * 1.4F;
                    float elev = ((i % 4) - 1.5F) * 0.85F;
                    float dist = 1.4F + st * 5.2F;
                    float sx = Mth.cos(a) * dist, sz = Mth.sin(a) * dist;
                    float sy = bossY + elev * (1.0F + st * 1.6F);
                    float szW = 0.28F, szH = 0.52F;
                    float tx = -Mth.sin(a) * szW, tz = Mth.cos(a) * szW;
                    quadBothSides(pose, vc,
                            sx - tx, sy - szH, sz - tz, 0.0F, 1.0F,
                            sx + tx, sy - szH, sz + tz, 1.0F, 1.0F,
                            sx + tx * 0.2F, sy + szH, sz + tz * 0.2F, 0.5F, 0.0F,
                            sx - tx * 0.2F, sy + szH, sz - tz * 0.2F, 0.5F, 0.0F,
                            i % 2 == 0 ? 255 : 200, i % 2 == 0 ? 225 : 135, i % 2 == 0 ? 115 : 255, sha);
                }
            });
        }

        // 4e. Ground-Slam Impact Shockwave (p >= 0.77): 8-Ray Molten-Gold Fissure Star + Cylindrical Wave Wall + 12 Angled Crater Spires!
        if (p >= 0.77F) {
            float wt = Mth.clamp((p - 0.77F) / 0.23F, 0.0F, 1.0F);
            float wFade = 1.0F - wt * wt;
            int wa = (int) (wFade * 248.0F);
            float waveR = 1.8F + wt * 11.4F;

            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                // 8-ray jagged molten-gold ground fracture star
                for (int ray = 0; ray < 8; ray++) {
                    float a = (float) (ray * Math.PI * 2.0 / 8.0);
                    float cx = Mth.cos(a), cz = Mth.sin(a);
                    float px = -cz * 0.42F, pz = cx * 0.42F;
                    float len = 2.0F + wt * 10.5F;
                    quadBothSides(pose, vc,
                            -px, 0.14F, -pz, 0.0F, 0.0F,
                            px, 0.14F, pz, 1.0F, 0.0F,
                            cx * len + px * 0.18F, 0.14F, cz * len + pz * 0.18F, 1.0F, 1.0F,
                            cx * len - px * 0.18F, 0.14F, cz * len - pz * 0.18F, 0.0F, 1.0F,
                            255, 200, 85, wa);
                }
                // Cylindrical expanding shockwave wall
                int segs = 32;
                float wallH = 2.8F * (1.0F - wt * 0.55F);
                for (int i = 0; i < segs; i++) {
                    float a0 = (float) (i * Math.PI * 2.0 / segs);
                    float a1 = (float) ((i + 1) * Math.PI * 2.0 / segs);
                    quadBothSides(pose, vc,
                            Mth.cos(a0) * waveR, 0.08F, Mth.sin(a0) * waveR, 0.0F, 1.0F,
                            Mth.cos(a1) * waveR, 0.08F, Mth.sin(a1) * waveR, 1.0F, 1.0F,
                            Mth.cos(a1) * (waveR + 0.45F), 0.08F + wallH, Mth.sin(a1) * (waveR + 0.45F), 1.0F, 0.0F,
                            Mth.cos(a0) * (waveR + 0.45F), 0.08F + wallH, Mth.sin(a0) * (waveR + 0.45F), 0.0F, 0.0F,
                            255, 195, 95, (int) (wa * 0.82F));
                }
                // 12 angled erupting crater spires around impact perimeter
                for (int k = 0; k < 12; k++) {
                    float a = (float) (k * Math.PI * 2.0 / 12.0) + 0.15F;
                    float rBase = 2.2F + wt * 6.5F;
                    float bx = Mth.cos(a) * rBase, bz = Mth.sin(a) * rBase;
                    float tx = -Mth.sin(a) * 0.36F, tz = Mth.cos(a) * 0.36F;
                    float tipX = Mth.cos(a) * (rBase + 1.45F), tipZ = Mth.sin(a) * (rBase + 1.45F);
                    float sh = 2.9F * wFade;
                    quadBothSides(pose, vc,
                            bx - tx, 0.06F, bz - tz, 0.0F, 1.0F,
                            bx + tx, 0.06F, bz + tz, 1.0F, 1.0F,
                            tipX, 0.06F + sh, tipZ, 0.5F, 0.0F,
                            tipX, 0.06F + sh, tipZ, 0.5F, 0.0F,
                            k % 2 == 0 ? 255 : 170, k % 2 == 0 ? 215 : 235, k % 2 == 0 ? 110 : 255, wa);
                }
            });
            submitGroundDecal(poseStack, collector, VFX_CRATER, waveR, 0.12F, -wt * 2.0F, 255, 185, 80, (int) (wa * 0.78F));
        }
    }

    /** 5) sword_array_buff ONLY: 12-block sword-seal ground array + 12 floating vertical void sword-gate portals! */
    private void submitSwordArrayBuffVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float fade = p < 0.18F ? p / 0.18F : (p > 0.85F ? (1.0F - p) / 0.15F : 1.0F);
        int a = (int) (fade * 235.0F);
        submitGroundDecal(poseStack, collector, VFX_SWORD_SEAL, 5.5F + p * 6.5F, 0.08F, age * 0.09F, 155, 235, 255, a);
        for (int i = 0; i < 12; i++) {
            float ang = (float) (i * Math.PI * 2.0 / 12.0) + age * 0.045F;
            float dist = 3.8F + (i % 3) * 1.4F;
            float px = Mth.cos(ang) * dist;
            float pz = Mth.sin(ang) * dist;
            float py = 2.0F + (i % 3) * 1.1F + Mth.sin(age * 0.16F + i) * 0.3F;
            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.mulPose(Axis.YP.rotation(-ang + Mth.HALF_PI));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_SWORD_SEAL), (pose, vc) -> {
                float r = 1.05F;
                quadBothSides(pose, vc,
                        -r, -r, 0.0F, 0.0F, 0.0F,
                        r, -r, 0.0F, 1.0F, 0.0F,
                        r, r, 0.0F, 1.0F, 1.0F,
                        -r, r, 0.0F, 0.0F, 1.0F,
                        255, 225, 130, a);
            });
            poseStack.popPose();
        }
    }

    /** 6) ultimate_judgment ONLY: 17-block Ground & Sky Bagua + 8 Light Pillars + 8 Plunging 3D Fiery Meteors + 15.5-block 360° Tsunami Shockwave! */
    private void submitUltimateBaguaVfx(PoseStack poseStack, SubmitNodeCollector collector, float p, float age) {
        float baguaScale = p < 0.45F ? (4.8F + (p / 0.45F) * 12.2F) : 17.0F;
        float fade = p < 0.10F ? p / 0.10F : (p > 0.88F ? (1.0F - p) / 0.12F : 1.0F);
        int ba = (int) (fade * 242.0F);

        // Ground & Sky 17-block Bagua Arrays
        submitGroundDecal(poseStack, collector, VFX_BAGUA, baguaScale, 0.10F, age * 0.055F, 255, 215, 105, ba);
        submitGroundDecal(poseStack, collector, VFX_BAGUA, baguaScale * 0.82F, 13.5F, -age * 0.075F, 135, 230, 255, (int) (ba * 0.82F));

        // 8 Alternating Gold/Cyan Light Pillars + 8 Plunging 3D Fiery Meteors
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
            float pillarR = baguaScale * 0.56F;
            for (int i = 0; i < 8; i++) {
                float a = (float) (i * Math.PI * 2.0 / 8.0) + age * 0.025F;
                float px = Mth.cos(a) * pillarR, pz = Mth.sin(a) * pillarR;
                float tx = -Mth.sin(a) * 0.48F, tz = Mth.cos(a) * 0.48F;
                int pr = i % 2 == 0 ? 255 : 120, pg = i % 2 == 0 ? 210 : 230, pb = i % 2 == 0 ? 95 : 255;
                quadBothSides(pose, vc,
                        px - tx, 0.1F, pz - tz, 0.0F, 1.0F,
                        px + tx, 0.1F, pz + tz, 1.0F, 1.0F,
                        px + tx * 0.5F, 13.8F, pz + tz * 0.5F, 1.0F, 0.0F,
                        px - tx * 0.5F, 13.8F, pz - tz * 0.5F, 0.0F, 0.0F,
                        pr, pg, pb, (int) (ba * 0.82F));

                // Plunging fiery meteor streaks during telegraph & detonation
                if (p < 0.72F) {
                    float mt = (p * 2.4F + i * 0.17F) % 1.0F;
                    float my = 15.0F * (1.0F - mt);
                    float mr = 4.2F + (i % 3) * 3.4F + (1.0F - mt) * 3.5F;
                    float mx = Mth.cos(a + 0.35F) * mr;
                    float mz = Mth.sin(a + 0.35F) * mr;
                    quadBothSides(pose, vc,
                            mx - 0.55F, my, mz - 0.55F, 0.0F, 1.0F,
                            mx + 0.55F, my, mz + 0.55F, 1.0F, 1.0F,
                            mx + Mth.cos(a + 0.35F) * 1.4F, my + 4.4F, mz + Mth.sin(a + 0.35F) * 1.4F, 0.5F, 0.0F,
                            mx + Mth.cos(a + 0.35F) * 1.4F, my + 4.4F, mz + Mth.sin(a + 0.35F) * 1.4F, 0.5F, 0.0F,
                            255, 165, 55, ba);
                }
            }

            // 15.5-block Triple 360° Tsunami Shockwave Walls (p >= 0.44)
            if (p >= 0.44F) {
                float wt = Mth.clamp((p - 0.44F) / 0.52F, 0.0F, 1.0F);
                int wa = (int) ((1.0F - wt * wt) * 248.0F);
                int segs = 36;
                for (int layer = 0; layer < 3; layer++) {
                    float r = Math.max(1.2F, (1.8F + wt * 14.2F) - layer * 2.2F);
                    float h = (3.6F - layer * 0.75F) * (1.0F - wt * 0.45F);
                    int wr = layer == 1 ? 255 : 130, wg = layer == 1 ? 195 : 235, wb = layer == 1 ? 85 : 255;
                    for (int s = 0; s < segs; s++) {
                        float a0 = (float) (s * Math.PI * 2.0 / segs);
                        float a1 = (float) ((s + 1) * Math.PI * 2.0 / segs);
                        quadBothSides(pose, vc,
                                Mth.cos(a0) * r, 0.10F, Mth.sin(a0) * r, 0.0F, 1.0F,
                                Mth.cos(a1) * r, 0.10F, Mth.sin(a1) * r, 1.0F, 1.0F,
                                Mth.cos(a1) * (r + 0.55F), 0.10F + h, Mth.sin(a1) * (r + 0.55F), 1.0F, 0.0F,
                                Mth.cos(a0) * (r + 0.55F), 0.10F + h, Mth.sin(a0) * (r + 0.55F), 0.0F, 0.0F,
                                wr, wg, wb, wa);
                    }
                }
            }
        });
    }

    private void submitGroundDecal(PoseStack poseStack, SubmitNodeCollector collector, Identifier tex,
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
