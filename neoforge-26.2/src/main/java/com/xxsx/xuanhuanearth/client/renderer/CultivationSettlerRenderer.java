package com.xxsx.xuanhuanearth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.model.CultivationSettlerModel;
import com.xxsx.xuanhuanearth.entity.CultivationSettlerEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class CultivationSettlerRenderer extends MobRenderer<CultivationSettlerEntity,
        CultivationSettlerRenderState, CultivationSettlerModel> {
    private static final Identifier[] TEXTURES = {
            XuanhuanEarth.id("textures/entity/cultivation_settler_merchant.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_spring_keeper.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_steward.png")
    };

    public CultivationSettlerRenderer(EntityRendererProvider.Context context) {
        super(context, new CultivationSettlerModel(context.bakeLayer(CultivationSettlerModel.LAYER_LOCATION)), 0.48F);
    }

    @Override
    public CultivationSettlerRenderState createRenderState() {
        return new CultivationSettlerRenderState();
    }

    @Override
    public void extractRenderState(CultivationSettlerEntity entity, CultivationSettlerRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.role = entity.getRole().id();
        state.trading = entity.isTrading();
        state.hurtTime = Math.max(0.0F, entity.hurtTime - partialTick);
        state.breathPhase = Mth.sin((entity.tickCount + partialTick) * 0.09F);
        state.gesturePhase = Mth.sin((entity.tickCount + partialTick) * 0.045F);
        state.walking = entity.walkAnimation.speed(partialTick) > 0.02F;
        state.runAmount = Mth.clamp(entity.walkAnimation.speed(partialTick) * 1.5F, 0.0F, 1.0F);
        state.attackTime = entity.getAttackAnim(partialTick);
        state.combatPose = entity.getCombatPose();
    }

    @Override
    public void submit(CultivationSettlerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
        float age = state.ageInTicks;
        // Role colors: 0 Merchant (warm amber-gold), 1 Spring Keeper (jade-cyan), 2 Sect Steward (celestial violet-gold)
        int r = state.role == 1 ? 115 : (state.role == 2 ? 195 : 255);
        int g = state.role == 1 ? 240 : (state.role == 2 ? 155 : 210);
        int b = state.role == 1 ? 225 : (state.role == 2 ? 255 : 105);
        boolean combat = state.combatPose > 0 || state.attackTime > 0.02F;

        SpiritVfxHelper.submitBackHalo(poseStack, collector, SpiritVfxHelper.TEX_SWORD_SEAL,
                state.bodyRot, 1.38F, 0.56F, combat ? 0.78F : 0.58F, age * 0.06F, r, g, b, combat ? 225 : 155);
        if (state.trading || combat) {
            SpiritVfxHelper.submitGroundSeal(poseStack, collector, SpiritVfxHelper.TEX_BAGUA,
                    combat ? 1.75F : 1.35F, 0.06F, age * (combat ? 0.09F : 0.04F), r, g, b, combat ? 220 : 165);
        }
        if (combat) {
            SpiritVfxHelper.submitOrbitOrbs(poseStack, collector, 4, 1.05F, 1.25F, 0.18F, age * 1.4F, r, g, b, 225);
        }
    }

    @Override
    public Identifier getTextureLocation(CultivationSettlerRenderState state) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, state.role))];
    }
}
