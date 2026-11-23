package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xxsx.xuanhuanearth.AstralHalberdItem;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-side swing blade-tail glow VFX for players wielding Fangtian Astral Halberd.
 * Echoes the Astral Sovereign's crescent tail-glow aesthetic cleanly on both left-click
 * melee swings and right-click combo casts, with ZERO ground-slam crater rings.
 */
public final class HalberdSwingVfx {

    private static final Identifier VFX_TRAIL = XuanhuanEarth.id("textures/entity/astral_vfx_halberd_trail.png");
    private static final int FULLBRIGHT = 0x00F000F0;
    private static final int SWING_DURATION_TICKS = 11;

    private static final class ActiveSwing {
        int ticks;
        int style; // 0 = horizontal crescent sweep, 1 = diagonal aerial cleave, 2 = forward spiral thrust
        float baseYaw;
    }

    private static final Map<UUID, ActiveSwing> ACTIVE_SWINGS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_SWING_TIME = new HashMap<>();
    private static final Map<UUID, Integer> STYLE_COUNTER = new HashMap<>();

    private HalberdSwingVfx() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(HalberdSwingVfx::onClientTick);
        NeoForge.EVENT_BUS.addListener(HalberdSwingVfx::onRenderPlayerPost);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE_SWINGS.clear();
            LAST_SWING_TIME.clear();
            return;
        }
        Iterator<Map.Entry<UUID, ActiveSwing>> it = ACTIVE_SWINGS.entrySet().iterator();
        while (it.hasNext()) {
            ActiveSwing sw = it.next().getValue();
            sw.ticks++;
            if (sw.ticks >= SWING_DURATION_TICKS) {
                it.remove();
            }
        }
        for (Player player : mc.level.players()) {
            UUID id = player.getUUID();
            boolean holding = player.getMainHandItem().getItem() instanceof AstralHalberdItem
                    || player.getOffhandItem().getItem() instanceof AstralHalberdItem;
            int prev = LAST_SWING_TIME.getOrDefault(id, 0);
            int cur = player.swinging ? player.swingTime : 0;
            if (holding && player.swinging && (cur < prev || (prev == 0 && cur >= 0 && !ACTIVE_SWINGS.containsKey(id)))) {
                int nextStyle = STYLE_COUNTER.getOrDefault(id, 0) % 3;
                STYLE_COUNTER.put(id, nextStyle + 1);
                ActiveSwing sw = new ActiveSwing();
                sw.ticks = 0;
                sw.style = nextStyle;
                sw.baseYaw = player.getYRot();
                ACTIVE_SWINGS.put(id, sw);
            }
            LAST_SWING_TIME.put(id, player.swinging ? Math.max(1, cur) : 0);
        }
    }

    private static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        // Match active swing for any visible player (including local player in F5 3rd-person)
        Player matched = null;
        ActiveSwing swing = null;
        var rs = event.getRenderState();
        double bestDist = 1.25D;
        for (Player p : mc.level.players()) {
            ActiveSwing sw = ACTIVE_SWINGS.get(p.getUUID());
            if (sw != null) {
                double dx = p.getX() - rs.x;
                double dy = p.getY() - rs.y;
                double dz = p.getZ() - rs.z;
                double d2 = dx * dx + dy * dy + dz * dz;
                if (d2 < bestDist) {
                    bestDist = d2;
                    matched = p;
                    swing = sw;
                }
            }
        }
        if (matched == null || swing == null) return;

        float progress = Mth.clamp((swing.ticks + event.getPartialTick()) / (float) SWING_DURATION_TICKS, 0.0F, 1.0F);
        float fade = Mth.sin(progress * Mth.PI);
        if (fade <= 0.02F) return;

        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - swing.baseYaw));

        int style = swing.style;
        if (style == 0) {
            // Style 0: Graceful horizontal crescent tail-glow sweep at chest height
            submitCrescentRibbon(poseStack, collector, progress, fade, 1.35F, 1.55F, 3.65F, 0.0F, 0.08F,
                    115, 228, 255, 255, 232, 148);
        } else if (style == 1) {
            // Style 1: Aerial diagonal cleave tail-glow (tilted 38 deg — ZERO ground slam!)
            submitCrescentRibbon(poseStack, collector, progress, fade, 1.48F, 1.45F, 3.55F, 0.66F, -0.18F,
                    155, 212, 255, 255, 216, 128);
        } else {
            // Style 2: Forward twin-ribbon spiral star thrust
            submitThrustTail(poseStack, collector, progress, fade);
        }

        poseStack.popPose();
    }

    private static void submitCrescentRibbon(
            PoseStack poseStack, SubmitNodeCollector collector,
            float p, float fade, float yBase, float innerR, float outerR,
            float rollRad, float pitchTilt,
            int rIn, int gIn, int bIn, int rOut, int gOut, int bOut
    ) {
        float arcSpan = 2.55F;
        float yawStart = Mth.lerp(p, -1.65F, 0.42F);
        int aOut = (int) (fade * 238.0F);
        int aIn = (int) (fade * 95.0F);

        poseStack.pushPose();
        poseStack.translate(0.0F, yBase, 0.0F);
        if (rollRad != 0.0F) poseStack.mulPose(Axis.ZP.rotation(rollRad));
        if (pitchTilt != 0.0F) poseStack.mulPose(Axis.XP.rotation(pitchTilt));

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_TRAIL), (pose, vc) -> {
            int segs = 22;
            for (int i = 0; i < segs; i++) {
                float u0 = (float) i / segs;
                float u1 = (float) (i + 1) / segs;
                float a0 = yawStart + u0 * arcSpan;
                float a1 = yawStart + u1 * arcSpan;
                float s0 = Mth.sin(a0), c0 = -Mth.cos(a0);
                float s1 = Mth.sin(a1), c1 = -Mth.cos(a1);
                // Primary translucent crescent ribbon
                quadBoth(pose, vc,
                        s0 * innerR, -0.05F, c0 * innerR, u0, 1.0F, rIn, gIn, bIn, aIn,
                        s1 * innerR, -0.05F, c1 * innerR, u1, 1.0F, rIn, gIn, bIn, aIn,
                        s1 * outerR,  0.14F, c1 * outerR, u1, 0.0F, rOut, gOut, bOut, aOut,
                        s0 * outerR,  0.14F, c0 * outerR, u0, 0.0F, rOut, gOut, bOut, aOut);
                // Secondary wispy golden outer shimmer offset slightly higher
                float i2 = innerR + 0.35F, o2 = outerR + 0.42F;
                quadBoth(pose, vc,
                        s0 * i2, 0.16F, c0 * i2, u0, 1.0F, rIn, gIn, bIn, (int) (aIn * 0.65F),
                        s1 * i2, 0.16F, c1 * i2, u1, 1.0F, rIn, gIn, bIn, (int) (aIn * 0.65F),
                        s1 * o2, 0.30F, c1 * o2, u1, 0.0F, rOut, gOut, bOut, (int) (aOut * 0.68F),
                        s0 * o2, 0.30F, c0 * o2, u0, 0.0F, rOut, gOut, bOut, (int) (aOut * 0.68F));
            }
        });
        poseStack.popPose();
    }

    private static void submitThrustTail(PoseStack poseStack, SubmitNodeCollector collector, float p, float fade) {
        float reach = Mth.lerp(p, 1.2F, 4.6F);
        int aHead = (int) (fade * 235.0F);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_TRAIL), (pose, vc) -> {
            int segs = 16;
            for (int ribbon = 0; ribbon < 2; ribbon++) {
                float phaseOff = ribbon * Mth.PI;
                for (int i = 0; i < segs; i++) {
                    float u0 = (float) i / segs;
                    float u1 = (float) (i + 1) / segs;
                    float z0 = -0.6F - u0 * reach;
                    float z1 = -0.6F - u1 * reach;
                    float r0 = (1.0F - u0 * 0.55F) * 0.42F;
                    float r1 = (1.0F - u1 * 0.55F) * 0.42F;
                    float ang0 = u0 * 8.5F + phaseOff + p * 6.0F;
                    float ang1 = u1 * 8.5F + phaseOff + p * 6.0F;
                    float x0 = Mth.cos(ang0) * r0, y0 = 1.38F + Mth.sin(ang0) * r0;
                    float x1 = Mth.cos(ang1) * r1, y1 = 1.38F + Mth.sin(ang1) * r1;
                    float w = 0.22F;
                    int a0 = (int) (aHead * u0);
                    int a1 = (int) (aHead * u1);
                    quadBoth(pose, vc,
                            x0 - w, y0 - w * 0.4F, z0, u0, 1.0F, 120, 225, 255, (int) (a0 * 0.55F),
                            x1 - w, y1 - w * 0.4F, z1, u1, 1.0F, 120, 225, 255, (int) (a1 * 0.55F),
                            x1 + w, y1 + w * 0.4F, z1, u1, 0.0F, 255, 238, 165, a1,
                            x0 + w, y0 + w * 0.4F, z0, u0, 0.0F, 255, 238, 165, a0);
                }
            }
        });
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
