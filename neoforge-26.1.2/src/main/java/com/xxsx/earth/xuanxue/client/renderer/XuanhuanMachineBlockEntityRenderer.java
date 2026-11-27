package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.XuanhuanMachineBlock;
import com.xxsx.earth.xuanxue.XuanhuanMachineBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renders live translucent 3D Xianxia VFX above sculpted Xuanhuan machines:
 * - Alchemy Furnace: rotating golden-amber dan-fire aura rings
 * - Talisman Table: levitating translucent Daoist talisman scroll & brush glow
 * - Spirit Array Core: counter-rotating Bagua jade rune halo & soft ascending beam
 */
public class XuanhuanMachineBlockEntityRenderer
        implements BlockEntityRenderer<XuanhuanMachineBlockEntity, XuanhuanMachineBlockEntityRenderer.MachineRenderState> {

    private static final Identifier VFX_BAGUA = XuanhuanEarth.id("textures/entity/astral_vfx_bagua.png");
    private static final Identifier VFX_TALISMAN = XuanhuanEarth.id("textures/entity/astral_vfx_talisman.png");
    private static final Identifier VFX_BEAM = XuanhuanEarth.id("textures/entity/astral_vfx_beam.png");
    private static final Identifier VFX_SWORD_SEAL = XuanhuanEarth.id("textures/entity/astral_vfx_sword_seal.png");
    private static final int FULLBRIGHT = 0x00F000F0;

    public XuanhuanMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(
            XuanhuanMachineBlockEntity be,
            MachineRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);
        BlockState bs = be.getBlockState();
        state.active = bs.hasProperty(XuanhuanMachineBlock.ACTIVE) && bs.getValue(XuanhuanMachineBlock.ACTIVE);
        state.formed = bs.hasProperty(XuanhuanMachineBlock.FORMED) && bs.getValue(XuanhuanMachineBlock.FORMED);
        state.kind = (bs.getBlock() instanceof XuanhuanMachineBlock mb) ? mb.kind() : XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE;
        long gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.animTime = (gameTime % 24000L) + partialTick;
    }

    @Override
    public void submit(
            MachineRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraRenderState
    ) {
        float t = state.animTime;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);

        if (state.kind == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE) {
            float alphaScale = state.active ? 1.0F : 0.42F;
            float bob = Mth.sin(t * 0.09F) * 0.05F;
            submitHorizontalDisc(poseStack, collector, VFX_SWORD_SEAL, 0.62F, 1.08F + bob, t * 0.055F,
                    255, 195, 92, (int) (145 * alphaScale));
            if (state.active) {
                submitHorizontalDisc(poseStack, collector, VFX_BAGUA, 0.44F, 1.24F - bob, -t * 0.085F,
                        255, 130, 62, 135);
            }
        } else if (state.kind == XuanhuanMachineBlock.Kind.TALISMAN_TABLE) {
            float bob = Mth.sin(t * 0.08F) * 0.06F;
            float a = state.active ? 185.0F : 95.0F;
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.25F + bob, 0.0F);
            poseStack.mulPose(Axis.YP.rotation(t * 0.045F));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_TALISMAN), (pose, vc) -> {
                float hw = 0.16F, hh = 0.28F;
                quadBoth(pose, vc,
                        -hw, -hh, 0.0F, 0, 1, 255, 224, 128, (int) a,
                         hw, -hh, 0.0F, 1, 1, 255, 224, 128, (int) a,
                         hw,  hh, 0.0F, 1, 0, 255, 245, 190, (int) a,
                        -hw,  hh, 0.0F, 0, 0, 255, 245, 190, (int) a);
            });
            poseStack.popPose();
            if (state.active) {
                submitHorizontalDisc(poseStack, collector, VFX_SWORD_SEAL, 0.56F, 0.88F, -t * 0.06F, 255, 212, 118, 125);
            }
        } else if (state.kind == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            float scale = state.formed ? 1.18F : (state.active ? 0.82F : 0.52F);
            int alpha = state.formed ? 175 : (state.active ? 130 : 75);
            submitHorizontalDisc(poseStack, collector, VFX_BAGUA, scale, 1.05F + Mth.sin(t * 0.07F) * 0.05F,
                    t * 0.042F, 105, 240, 228, alpha);
            if (state.formed || state.active) {
                submitHorizontalDisc(poseStack, collector, VFX_SWORD_SEAL, scale * 0.68F, 1.28F - Mth.sin(t * 0.09F) * 0.04F,
                        -t * 0.075F, 255, 222, 132, (int) (alpha * 0.85F));
                // Soft vertical translucent crystal spirit beam
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(VFX_BEAM), (pose, vc) -> {
                    float w = state.formed ? 0.28F : 0.18F;
                    float h = state.formed ? 2.2F : 1.3F;
                    int ba = state.formed ? 115 : 70;
                    for (int k = 0; k < 2; k++) {
                        float dx = k == 0 ? w : 0.0F;
                        float dz = k == 0 ? 0.0F : w;
                        quadBoth(pose, vc,
                                -dx, 0.72F, -dz, 0, 1, 120, 242, 236, ba,
                                 dx, 0.72F,  dz, 1, 1, 120, 242, 236, ba,
                                 dx, 0.72F + h, dz, 1, 0, 195, 252, 255, 0,
                                -dx, 0.72F + h, -dz, 0, 0, 195, 252, 255, 0);
                    }
                });
            }
        }
        poseStack.popPose();
    }

    private void submitHorizontalDisc(
            PoseStack poseStack, SubmitNodeCollector collector, Identifier tex,
            float radius, float y, float rotRad, int r, int g, int b, int a
    ) {
        if (a <= 2) return;
        poseStack.pushPose();
        poseStack.translate(0.0F, y, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(rotRad));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(tex), (pose, vc) -> {
            quadBoth(pose, vc,
                    -radius, 0.0F, -radius, 0, 0, r, g, b, a,
                     radius, 0.0F, -radius, 1, 0, r, g, b, a,
                     radius, 0.0F,  radius, 1, 1, r, g, b, a,
                    -radius, 0.0F,  radius, 0, 1, r, g, b, a);
        });
        poseStack.popPose();
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

    public static class MachineRenderState extends BlockEntityRenderState {
        public XuanhuanMachineBlock.Kind kind = XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE;
        public boolean active = false;
        public boolean formed = false;
        public float animTime = 0.0F;
    }
}
