package com.xxsx.xuanhuanearth.client.renderer;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.model.CultivationSettlerModel;
import com.xxsx.xuanhuanearth.entity.CultivationSettlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

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
        state.breathPhase = net.minecraft.util.Mth.sin((entity.tickCount + partialTick) * 0.09F);
        state.gesturePhase = net.minecraft.util.Mth.sin((entity.tickCount + partialTick) * 0.045F);
        state.walking = entity.walkAnimation.speed(partialTick) > 0.02F;
        state.runAmount = net.minecraft.util.Mth.clamp(entity.walkAnimation.speed(partialTick) * 1.5F,
                0.0F, 1.0F);
        state.attackTime = entity.getAttackAnim(partialTick);
        state.combatPose = entity.getCombatPose();
    }

    @Override
    public Identifier getTextureLocation(CultivationSettlerRenderState state) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, state.role))];
    }
}
